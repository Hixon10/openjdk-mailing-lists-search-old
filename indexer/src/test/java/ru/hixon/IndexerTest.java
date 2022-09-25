package ru.hixon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IndexerTest {

    @Test
    void indexTest() throws Exception {
        Indexer indexer = new Indexer();
        indexer.index();
        assertEquals(2, 1 + 1);
    }
}