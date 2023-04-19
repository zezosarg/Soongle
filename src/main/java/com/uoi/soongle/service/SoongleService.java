package com.uoi.soongle.service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

@Service
public class SoongleService {
	
	private StandardAnalyzer analyzer;
	private Directory index;
	private ScoreDoc lastDoc;
	private String query;

    @Autowired
    public SoongleService() throws IOException {
		this.index = FSDirectory.open(Paths.get("luceneindex"));
		this.analyzer = new StandardAnalyzer();
	}	

//    public List<Map<String, String>> searchIndex(String inField, String queryString) throws ParseException, IOException, InvalidTokenOffsetsException {
//        query = queryString;
//    	Query query = new QueryParser(inField, analyzer).parse(queryString);
//    	QueryScorer queryScorer = new QueryScorer(query, inField);
//    	Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
//    	Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
//    	highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight
//        IndexReader indexReader = DirectoryReader.open(index);
//        IndexSearcher searcher = new IndexSearcher(indexReader);
//        TopDocs topDocs = searcher.searchAfter(lastDoc, query, 10);
//        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//        List<Map<String, String>> results = new ArrayList<>();
//        for (ScoreDoc scoreDoc : scoreDocs) {
//        	Map<String, String> result = new HashMap<>();
//        	Document document = searcher.doc(scoreDoc.doc);
//            String field = document.get(inField);
//            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, scoreDoc.doc, inField, document, new StandardAnalyzer());
//            String fragment = highlighter.getBestFragment(tokenStream, field);
//            result.put(inField, fragment);
//            for (String s: Arrays.asList("artist", "title", "lyrics"))
//            	if (s != inField)
//            		result.put(s, document.get(s));
//            results.add(result);
//            lastDoc = scoreDoc;
//        }
//        return results;
//    }
    
    public List<Map<String, String>> searchIndex(String inField, String queryString) throws ParseException, IOException, InvalidTokenOffsetsException {
        query = queryString;
    	Query query = new QueryParser(inField, analyzer).parse(queryString);
    	QueryScorer queryScorer = new QueryScorer(query, inField);
    	Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
    	Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
    	highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight
        IndexReader indexReader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        GroupingSearch groupingSearch = new GroupingSearch("artist");
        TopGroups<Object> groups = groupingSearch.search(searcher, query, 0, 10);
//        TopDocs topDocs = searcher.searchAfter(lastDoc, query, 10);
        List<Map<String, String>> results = new ArrayList<>();
	    for (GroupDocs<Object> groupDocs : groups.groups) {
	    	ScoreDoc[] scoreDocs = groupDocs.scoreDocs;
        	for (ScoreDoc scoreDoc : scoreDocs) {
	        	Map<String, String> result = new HashMap<>();
	        	Document document = searcher.doc(scoreDoc.doc);
	            String field = document.get(inField);
	            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, scoreDoc.doc, inField, document, new StandardAnalyzer());
	            String fragment = highlighter.getBestFragment(tokenStream, field);
	            result.put(inField, fragment);
	            for (String s: Arrays.asList("artist", "title", "lyrics"))
	            	if (s != inField)
	            		result.put(s, document.get(s));
	            results.add(result);
	            lastDoc = scoreDoc;
	        }
    	}
        return results;
    }
    
    public void buildIndex() throws IOException {
    	IndexWriterConfig config = new IndexWriterConfig(analyzer);
    	IndexWriter w = new IndexWriter(index, config);
    	List<List<String>> records = loadData();
    	for (List<String> record : records)
    		addDoc(w, record.get(1), record.get(2), record.get(3));	// 0 is id
    	w.close();
    }
	
	public List<List<String>> loadData() throws IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		CSVReader csvReader = new CSVReader(new FileReader("data/corpus.csv"));
		String[] values = null;
		while ((values = csvReader.readNext()) != null)
			records.add(Arrays.asList(values));
		return records;
	}
	
	public void addDoc(IndexWriter w, String artist, String title, String lyrics) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("artist", artist, Field.Store.YES));
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new TextField("lyrics", lyrics, Field.Store.YES));
		w.addDocument(doc);
	}

	public String getQuery() { return query; }

	public void setLastDoc(Object object) {	lastDoc = null;	}
	
}
