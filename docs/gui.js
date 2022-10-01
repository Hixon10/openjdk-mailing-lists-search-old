var execBtn = document.getElementById("execute");
var outputElm = document.getElementById('output');
var errorElm = document.getElementById('error');
var commandsElm = document.getElementById('commands');

// Start the worker in which sql.js will run
var worker = new Worker("worker.sql-wasm-1.8.0.js");
worker.onerror = error;

// Open a database
const databaseUrlPrefix = "https://hixon10.github.io/openjdk-mailing-lists-search/";
const databasePartNames = ["db-part-00", "db-part-01", "db-part-02", "db-part-03", "db-part-04", "db-part-05", "db-part-06", "db-part-07", "db-part-08", "db-part-09"];

const databasePartPromises = [];

for (const dbPartName of databasePartNames) {
	// disable cache https://stackoverflow.com/a/59493583/1756750
	const ms = Date.now();
	const currentPartUrl = databaseUrlPrefix+dbPartName+"?dummy="+ms;
	const currentDbPartPromise = fetch(currentPartUrl, {
		  headers: {
			'Cache-Control': 'no-cache',
			'pragma': 'no-cache'
		  }
		}).then(res => res.arrayBuffer())
		  .then(buf => new Uint8Array(buf));
	databasePartPromises.push(currentDbPartPromise);
}

  
Promise.all(databasePartPromises)
  .then(databaseParts => {
	  
	  // https://stackoverflow.com/a/49129872/1756750
	  let length = 0;
	  databaseParts.forEach(item => {
		  length += item.length;
	  });
	  
	  let mergedArray = new Uint8Array(length);
	  let offset = 0;
	  databaseParts.forEach(item => {
		  mergedArray.set(item, offset);
		  offset += item.length;
	  });
	
	  worker.postMessage({
		id: 1,
		action: "open",
		buffer: databaseParts, /*Optional. An ArrayBuffer representing an SQLite Database file*/
	  });
});



// Connect to the HTML element we 'print' to
function print(text) {
	outputElm.innerHTML = text.replace(/\n/g, '<br>');
}
function error(e) {
	console.log(e);
	errorElm.style.height = '2em';
	errorElm.textContent = e.message;
}

function noerror() {
	errorElm.style.height = '0';
}

// Run a command in the database
function execute(commands) {
	tic();
	worker.onmessage = function (event) {
		var results = event.data.results;
		toc("Executing SQL");
		if (!results) {
			error({message: event.data.error});
			return;
		}

		tic();
		outputElm.innerHTML = "";
		for (var i = 0; i < results.length; i++) {
			outputElm.appendChild(tableCreate(results[i].columns, results[i].values));
		}
		toc("Displaying results");
	}
	worker.postMessage({ action: 'exec', sql: commands });
	outputElm.textContent = "Fetching results...";
}

// Create an HTML table
var tableCreate = function () {
	function valconcat(vals, tagName) {
		if (vals.length === 0) return '';
		var open = '<' + tagName + '>', close = '</' + tagName + '>';
		return open + vals.join(close + open) + close;
	}
	return function (columns, values) {
		var tbl = document.createElement('table');
		var html = '<thead>' + valconcat(columns, 'th') + '</thead>';
		var rows = values.map(function (v) { return valconcat(v, 'td'); });
		html += '<tbody>' + valconcat(rows, 'tr') + '</tbody>';
		tbl.innerHTML = html;
		return tbl;
	}
}();

// Execute the commands when the button is clicked
function execEditorContents() {
	noerror()
	execute(editor.getValue() + ';');
}
execBtn.addEventListener("click", execEditorContents, true);

// Performance measurement functions
var tictime;
if (!window.performance || !performance.now) { window.performance = { now: Date.now } }
function tic() { tictime = performance.now() }
function toc(msg) {
	var dt = performance.now() - tictime;
	console.log((msg || 'toc') + ": " + dt + "ms");
}

// Add syntax highlihjting to the textarea
var editor = CodeMirror.fromTextArea(commandsElm, {
	mode: 'text/x-mysql',
	viewportMargin: Infinity,
	indentWithTabs: true,
	smartIndent: true,
	lineNumbers: true,
	matchBrackets: true,
	autofocus: true,
	extraKeys: {
		"Ctrl-Enter": execEditorContents,
	}
});


