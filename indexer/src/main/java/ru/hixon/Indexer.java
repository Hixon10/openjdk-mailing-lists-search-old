package ru.hixon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.temporal.ChronoUnit.SECONDS;


public class Indexer {

    private static final Duration HTTP_TIMEOUT = Duration.of(5, SECONDS);
    private static final String THREAD_SEARCH_SUBSTRING = "[ Thread ]";

    private final Database database;
    private final HttpClient httpClient;
    private final List<String> mailingListArchives;

    public Indexer(Database database, HttpClient httpClient, List<String> mailingListArchives) {
        this.database = database;
        this.httpClient = httpClient;
        this.mailingListArchives = mailingListArchives;
    }

    public void index() throws Exception {
        Set<ThreadUrlInMailingList> threadUrlsForIndex = getThreadUrlsForIndex();

        /// TODO...
    }

    /**
     * For each mailing list define, which months should be indexed.
     * We need always reindex the current month, because it could change.
     * Apart from that, we need to index months, which we have never indexed before.
     * @return thread urls, which need to index
     */
    private Set<ThreadUrlInMailingList> getThreadUrlsForIndex() throws Exception {
        List<CompletableFuture<HttpResponse<String>>> resultsCf = new ArrayList<>();
        for (String mailingListArchive : mailingListArchives) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(mailingListArchive))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();
            resultsCf.add(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
        }
        CompletableFuture.allOf(resultsCf.toArray(new CompletableFuture[0])).join();

        Set<ThreadUrlInMailingList> threadUrlsForIndex = new HashSet<>();
        Set<String> indexedMonthUrls = database.getIndexedMonthUrls();

        for (int i = 0; i < resultsCf.size(); i++) {
            HttpResponse<String> archivesPageResponse = resultsCf.get(i).get();
            String currentMailingListUrl = mailingListArchives.get(i);
            if (archivesPageResponse.statusCode() != 200) {
                throw new RuntimeException("Wrong HTTP code, while getting archivesPage: code=%d, link=%s".formatted(archivesPageResponse.statusCode(), currentMailingListUrl));
            }
            Set<ThreadUrlInMailingList> threadLinks = parseThreadLinks(archivesPageResponse.body(), indexedMonthUrls, currentMailingListUrl);
            threadUrlsForIndex.addAll(threadLinks);
        }

        return threadUrlsForIndex;
    }

    static Set<ThreadUrlInMailingList> parseThreadLinks(String archivePageContent, Set<String> indexedMonthUrls, String currentMailingListUrl) {
        int index = archivePageContent.indexOf(THREAD_SEARCH_SUBSTRING);
        boolean firstAdded = false;
        Set<ThreadUrlInMailingList> result = new HashSet<>();
        while (index >= 0) {
            StringBuilder sb = new StringBuilder();
            for (int j = index - 3; j >= 0; j--) {
                char ch = archivePageContent.charAt(j);
                if (ch == '"') {
                    break;
                }
                sb.append(ch);
            }
            String threadUrl = currentMailingListUrl + sb.reverse();
            // we want to index either the current month, or month, which we haven't seen before
            if (!firstAdded || !indexedMonthUrls.contains(threadUrl)) {
                result.add(new ThreadUrlInMailingList(threadUrl, currentMailingListUrl));
            }
            firstAdded = true;
            index = archivePageContent.indexOf(THREAD_SEARCH_SUBSTRING, index + 1);
        }
        return result;
    }
}
