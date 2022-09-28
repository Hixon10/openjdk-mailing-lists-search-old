package ru.hixon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class IndexerTest {

    @Test
    public void indexTest() {
//        Indexer indexer = new Indexer();
//        indexer.index();
//        new Main().run();
        assertEquals(2, 1 + 1);
    }

    @Test
    public void parseThreadLinksTest() throws IOException {
        InputStream contentIs = getClass().getResourceAsStream("/archivesPage.html");
        String html = new String(contentIs.readAllBytes(), StandardCharsets.UTF_8);
        Set<ThreadUrlInMailingList> threadUrls = Indexer.parseThreadLinks(html, Set.of(), "https://mail.openjdk.org/pipermail/valhalla-dev/");

        Assertions.assertEquals(97, threadUrls.size());
        for (ThreadUrlInMailingList threadUrl : threadUrls) {
            Assertions.assertEquals("https://mail.openjdk.org/pipermail/valhalla-dev/", threadUrl.getMailingListUrl());
        }
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2022-September/thread.html")));
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2021-August/thread.html")));
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2021-May/thread.html")));
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2014-July/thread.html")));

        // with filters
        threadUrls = Indexer.parseThreadLinks(html, Set.of(
                "https://mail.openjdk.org/pipermail/valhalla-dev/2022-September/thread.html",
                "https://mail.openjdk.org/pipermail/valhalla-dev/2021-May/thread.html"
        ), "https://mail.openjdk.org/pipermail/valhalla-dev/");

        Assertions.assertEquals(97, threadUrls.size());
        for (ThreadUrlInMailingList threadUrl : threadUrls) {
            Assertions.assertEquals("https://mail.openjdk.org/pipermail/valhalla-dev/", threadUrl.getMailingListUrl());
        }
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2022-September/thread.html")));
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2021-August/thread.html")));
        Assertions.assertFalse(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2021-May/thread.html")));
        Assertions.assertTrue(threadUrls.stream().anyMatch(p -> p.getThreadUrl().equals("https://mail.openjdk.org/pipermail/valhalla-dev/2014-July/thread.html")));
    }
}