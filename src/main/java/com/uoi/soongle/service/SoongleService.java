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
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
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

	Word2VectorModel model = null;

	private ScoreDoc lastDoc;

	private int lastGroup;
	private String query;

	private boolean modelBuilt = false;

    public List<Map<String, String>> searchIndex(String inField, String queryString) throws ParseException, IOException, InvalidTokenOffsetsException {
        query = queryString;
    	Query query = new QueryParser(inField, new StandardAnalyzer()).parse(queryString);
    	QueryScorer queryScorer = new QueryScorer(query, inField);
    	Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
    	Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
    	highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("luceneindex")));
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.searchAfter(lastDoc, query, 10);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<Map<String, String>> results = new ArrayList<>();
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
        return results;
    }
    
    public List<Map<String, String>> groupSearchIndex(String inField, String queryString) throws ParseException, IOException, InvalidTokenOffsetsException {
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("luceneindex")));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		int maxGroupsPerPage = 5;
		int groupDocumentLimit = 100;

		query = queryString;
		Query query = new QueryParser(inField, new StandardAnalyzer()).parse(queryString);

    	QueryScorer queryScorer = new QueryScorer(query, inField);
    	Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
    	Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
    	highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight

		GroupingSearch groupingSearch = new GroupingSearch("artist");
		groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));
		groupingSearch.setCachingInMB(4.0, true);
		groupingSearch.setAllGroups(true);
		groupingSearch.setGroupDocsLimit(groupDocumentLimit);

		TopGroups<BytesRef> topGroups = groupingSearch.search(indexSearcher, query, lastGroup, maxGroupsPerPage);
        System.out.println("topGroups.groups.length "+topGroups.groups.length);
        List<Map<String, String>> documentsList = new ArrayList<>();
	    for (GroupDocs<BytesRef> groupDocs : topGroups.groups) {
	    	ScoreDoc[] scoreDocs = groupDocs.scoreDocs;
	    	System.out.println("scoreDocs.length "+scoreDocs.length);
        	for (ScoreDoc scoreDoc : scoreDocs) {
	        	Map<String, String> fieldMap = new HashMap<>();

	        	Document document = indexSearcher.doc(scoreDoc.doc);
	            String field = document.get(inField);
	            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, scoreDoc.doc, inField, document, new StandardAnalyzer());
	            String fragment = highlighter.getBestFragment(tokenStream, field);

	            fieldMap.put(inField, fragment);
	            for (String s: Arrays.asList("artist", "title", "lyrics"))
	            	if (s != inField)
	            		fieldMap.put(s, document.get(s));
	            documentsList.add(fieldMap);
	        }
    	}

		lastGroup += maxGroupsPerPage;
        return documentsList;
    }

	public List<Map<String, String>> searchWord2Vec(String queryString) throws IOException {
		query = queryString;
		List<Map<String, String>> results = new ArrayList<>();

		List<Integer> docOrder = model.getTopDocumentsBasedOnSimilarity(queryString, 10);
		//for (Integer docId : docOrder) {
		//	System.out.println("Highscore: " + docId);
		//}
		return results;
	}

    public void buildIndex() throws IOException {

    	IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    	IndexWriter w = new IndexWriter(FSDirectory.open(Paths.get("luceneindex")), config);
    	List<List<String>> records = loadData();
    	for (List<String> record : records)
    		addDoc(w, record.get(0), record.get(1), record.get(2), record.get(3));	// 0 is id
    	w.close();
    }

	public void buildModel() throws IOException, ParseException {
		if(modelBuilt)
			return;

		model = new Word2VectorModel();

		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter w = new IndexWriter(FSDirectory.open(Paths.get("modelindex")), config);

		List<List<String>> records = loadData();
		for (List<String> record : records)
			model.addDoc(w, record.get(0), record.get(1), record.get(2), record.get(3));

		w.close();

		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("modelindex")));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TopDocs topDocs = searcher.searchAfter(null,
				new QueryParser("id", new StandardAnalyzer()).parse("5381"),
				10);

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document document = searcher.doc(scoreDoc.doc);
			System.out.println(document.get("id"));

			IndexableField[] fields = document.getFields("vector");
			for (IndexableField field : fields) {
				System.out.print(", " + field.numericValue() + ", ");
			}
			System.out.println();

		}

		modelBuilt = true;
	}
	
	public List<List<String>> loadData() throws IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		CSVReader csvReader = new CSVReader(new FileReader("data/corpus.csv"));
		String[] values = null;
		while ((values = csvReader.readNext()) != null)
			records.add(Arrays.asList(values));
		return records;
	}
	
	public void addDoc(IndexWriter w, String id ,String artist, String title, String lyrics) throws IOException {
		Document document = new Document();
		
//		FieldType fieldType = new FieldType();
//		fieldType.setStored(true);
//		fieldType.setDocValuesType(DocValuesType.SORTED);
//		Field artistField = new Field("artist", artist, fieldType);
//		document.add(artistField);
				
//		document.add(new SortedDocValuesField("artist", new BytesRef(artist)));
//		document.add(new StoredField("artist", artist));
		
		document.add(new SortedDocValuesField("artist", new BytesRef(artist)));
		document.add(new TextField("id", id, Field.Store.YES));
		document.add(new TextField("artist", artist, Field.Store.YES));
		document.add(new TextField("title", title, Field.Store.YES));
		document.add(new TextField("lyrics", lyrics, Field.Store.YES));
		w.addDocument(document);
	}

	public String getQuery() { return query; }

	public void setLastDoc(Object object) {	lastDoc = null;	}

	public void setLastGroup(int i) {
		lastGroup = i;
	}
}
