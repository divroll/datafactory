package com.divroll.datafactory.indexers;

import java.util.List;

public interface LuceneIndexer {

  /**
   * Index a given ID of {@code Entity}
   *
       * @param entityId
   * @param longitude
   * @param latitude
   * @return
       * @throws Exception
   */
  Boolean index(String dir, String entityId, Double longitude, Double latitude) throws Exception;

  Boolean index(String dir, String entityId, String field, String text) throws Exception;

  /**
   *
   * @param longitude
   * @param latitude
   * @param radius
   * @param after the ID of the the tail {@code Entity} from the previous search result
   * @param hits the number of documents to return
   * @return
   * @throws Exception
   */
  List<String> searchNeighbor(String dir, Double longitude, Double latitude, Double radius, String after,
      Integer hits)
      throws Exception;

  List<String> search(String dir, String field, String text, String after,
      Integer hits)
    throws Exception;
}
