package com.divroll.datafactory.indexers.impl;

import com.divroll.datafactory.database.impl.DatabaseManagerImpl;
import com.divroll.datafactory.indexers.LuceneIndexer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import jetbrains.exodus.lucene.ExodusDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial3d.Geo3DPoint;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

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

  @Override public Boolean index(String dir, String entityId, Double longitude, Double latitude)
      throws Exception {
    ExodusDirectory exodusDirectory = DatabaseManagerImpl.getInstance().getExodusDirectory(dir);
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    return exodusDirectory.getEnvironment().computeInTransaction(txn -> {
      try {
        IndexWriter writer =  new IndexWriter(exodusDirectory, iwc);
        Document doc = new Document();
        doc.add(new StoredField("entityId", entityId));
        doc.add(new LatLonPoint("latlon", latitude, longitude));
        Geo3DPoint point = new Geo3DPoint("geo3d", latitude, longitude);
        doc.add(point);
        writer.addDocument(doc);
        writer.close();
        return true;
      } catch (IOException e) {
        return false;
      }
    });
  }

  @Override public Boolean index(String dir, String entityId, String field, String text) throws Exception {
    ExodusDirectory exodusDirectory = DatabaseManagerImpl.getInstance().getExodusDirectory(dir);
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    return exodusDirectory.getEnvironment().computeInTransaction(txn -> {
      try {
        IndexWriter writer =  new IndexWriter(exodusDirectory, iwc);
        Document doc = new Document();
        doc.add(new StoredField("entityId", entityId));
        doc.add(new TextField(field, text, Field.Store.YES));
        writer.addDocument(doc);
        writer.close();
        return true;
      } catch (IOException e) {
        return false;
      }
    });
  }

  @Override
  public List<String> searchNeighbor(String dir, Double longitude, Double latitude, Double radius, String after,
      Integer hits) throws Exception {
    ExodusDirectory exodusDirectory = DatabaseManagerImpl.getInstance().getExodusDirectory(dir);
    Analyzer analyzer = new StandardAnalyzer();
    List<String> neighbors = new ArrayList<>();
    return exodusDirectory.getEnvironment().computeInTransaction(txn -> {
      try {
        IndexReader reader = DirectoryReader.open(exodusDirectory);
        searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery("latlon",
            latitude, longitude, radius), hits);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
          Document doc = searcher.doc(scoreDoc.doc);
          neighbors.add(doc.get("entityId"));
        }
        reader.close();
      } catch (IOException e) {
        txn.abort();
        throw new UncheckedIOException(e);
      }
      return neighbors;
    });
  }

  @Override public List<String> search(String dir, String field, String queryString, String after, Integer hits)
      throws Exception {
    ExodusDirectory exodusDirectory = DatabaseManagerImpl.getInstance().getExodusDirectory(dir);
    Analyzer analyzer = new StandardAnalyzer();
    List<String> entityIds = new ArrayList<>();
    return exodusDirectory.getEnvironment().computeInTransaction(txn -> {
      try {
        IndexReader reader = DirectoryReader.open(exodusDirectory);
        searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryString);
        TopDocs docs = searcher.search(query, hits);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
          Document doc = searcher.doc(scoreDoc.doc);
          System.out.println("score: " + scoreDoc.score +
              " -- " + field + ": " + doc.get(field));
          entityIds.add(doc.get("entityId"));
        }
        reader.close();
      } catch (IOException e) {
        txn.abort();
        throw new UncheckedIOException(e);
      } catch (ParseException e) {
        txn.abort();
      }
      return entityIds;
    });
  }
}
