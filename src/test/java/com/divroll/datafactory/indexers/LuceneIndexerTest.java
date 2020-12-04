/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.divroll.datafactory.indexers;

import com.divroll.datafactory.TestEnvironment;
import com.divroll.datafactory.indexers.impl.LuceneIndexerImpl;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LuceneIndexerTest {
  @Test
  public void testSimpleIndex() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    LuceneIndexer luceneIndexer = LuceneIndexerImpl.getInstance();
    boolean isIndexed = luceneIndexer.index(environment, "0-0", "message", "hello world");
    Assert.assertTrue(isIndexed);
    List<String> results = luceneIndexer.search(environment, "message", "hello", null, 10);
    Assert.assertTrue(results.contains("0-0"));
  }

  //@Test
  //public void testSearchGeoLocation() throws Exception {
  //  String environment = TestEnvironment.getEnvironment();
  //  LuceneIndexer luceneIndexer = LuceneIndexerImpl.getInstance();
  //  boolean isIndexed = luceneIndexer.index(environment, "0-1", 120.8969634, 14.4791297);
  //  Assert.assertTrue(isIndexed);
  //  List<String> results = luceneIndexer.searchNeighbor(environment,120.8969634, 14.4791297,
  //      10.0, null, 10);
  //  Assert.assertTrue(results.contains("0-1"));
  //}
}
