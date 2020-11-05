package com.divroll.datafactory.lucene;

import com.divroll.datafactory.lucene.LuceneIndexer;
import com.divroll.datafactory.lucene.impl.LuceneIndexerImpl;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LuceneIndexerTest {
  @Test
  public void testSearch() throws Exception {
    LuceneIndexer luceneIndexer = LuceneIndexerImpl.getInstance();
    luceneIndexer.index("0-1", 120.8969634, 14.4791297);
    List<String> results = luceneIndexer.searchNeighbor(120.8969634, 14.4791297,
        10.0, null, 10);
    Assert.assertTrue(results.contains("0-1"));
  }
}
