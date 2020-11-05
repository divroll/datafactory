package com.divroll.datafactory.lucene.impl;

import com.divroll.datafactory.lucene.LuceneIndexer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import jetbrains.exodus.env.ContextualEnvironment;
import jetbrains.exodus.lucene.ExodusDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial3d.Geo3DPoint;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneIndexerImpl implements LuceneIndexer {

  private IndexSearcher searcher;
  private IndexWriter writer;

  private static LuceneIndexerImpl instance;

  private LuceneIndexerImpl() {
    if (instance != null) {
      throw new RuntimeException("Only one instance of LuceneIndexer is allowed");
    }
}

  public static LuceneIndexerImpl getInstance() {
    if (instance == null) {
      instance = new LuceneIndexerImpl();
    }
    return instance;
  }

  @Override public Boolean index(String entityId, Double longitude, Double latitude)
      throws Exception {
    if (writer == null) {
      Directory dir = FSDirectory.open(Paths.get("C:/temp/Lucene"));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
      writer = new IndexWriter(dir, iwc);
    }
    Document doc = new Document();
    doc.add(new StoredField("entityId", entityId));
    doc.add(new LatLonPoint("latlon", latitude, longitude));
    Geo3DPoint point = new Geo3DPoint("geo3d", latitude, longitude);
    doc.add(point);
    writer.addDocument(doc);
    return null;
  }

  @Override
  public List<String> searchNeighbor(Double longitude, Double latitude, Double radius, String after,
      Integer hits) throws Exception {
    if (writer == null) {
      Directory dir = FSDirectory.open(Paths.get("C:/temp/Lucene"));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
      writer = new IndexWriter(dir, iwc);
    }
    List<String> neighbors = new ArrayList<>();
    searcher = new IndexSearcher(DirectoryReader.open(writer));
    TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery("latlon",
        latitude, longitude, radius), hits);
    for (ScoreDoc scoreDoc : docs.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      neighbors.add(doc.get("entityId"));
    }
    return neighbors;
  }
}
