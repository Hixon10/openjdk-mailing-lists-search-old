# Search for openjdk mailing lists

This project allows you to search across several [OpenJDK mailing lists](https://mail.openjdk.org/mailman/listinfo). 

The unique thing of this project is local search. When you open [a search page](https://hixon10.github.io/openjdk-mailing-lists-search/), `js-script` downloads `SQLite database` with indexed mailing lists.

The bad thing is you need to download the whole search index. The good thing is you can execute arbitrary sql queries on top of this db. 

## How it works
1. Periodically, [github action](https://github.com/Hixon10/openjdk-mailing-lists-search/blob/main/.github/workflows/ci.yaml) runs an [indexer](https://github.com/Hixon10/openjdk-mailing-lists-search/tree/main/indexer). The indexer downloads new emails from an archive (e.g., [The amber-dev Archives](https://mail.openjdk.org/pipermail/amber-dev/)), and inserts them to a [mydatabase.db](https://github.com/Hixon10/openjdk-mailing-lists-search/tree/main/docs).
2. When you open a [search frontend](https://hixon10.github.io/openjdk-mailing-lists-search/), [sql.js](https://github.com/sql-js/sql.js/) lib downloads `mydatabase.db`, and prepares the db, using `WebAssembly`.
3. Finally, you can execute any SQL-queries locally, without any server interaction.
