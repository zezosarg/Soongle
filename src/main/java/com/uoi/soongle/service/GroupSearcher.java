package com.uoi.soongle.service;

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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.uoi.soongle.model.Word2VectorModel;

public class GroupSearcher extends SoongleSearcher{

	private int lastGroup;

	@Override
	public List<Map<String, String>> search(String inField, String queryString, Word2VectorModel model) throws ParseException, IOException, InvalidTokenOffsetsException {
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("luceneindex")));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		int maxGroupsPerPage = 5;
		int groupDocumentLimit = 100;
		Query query = new QueryParser(inField, new StandardAnalyzer()).parse(queryString);

    	QueryScorer queryScorer = new QueryScorer(query, inField);
    	Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
    	highlighter.setTextFragmenter(new NullFragmenter()); // Set fragment to highlight

		GroupingSearch groupingSearch = new GroupingSearch("artist");
		groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));
		groupingSearch.setCachingInMB(4.0, true);
		groupingSearch.setAllGroups(true);
		groupingSearch.setGroupDocsLimit(groupDocumentLimit);

		TopGroups<BytesRef> topGroups = groupingSearch.search(indexSearcher, query, lastGroup, maxGroupsPerPage);
        List<Map<String, String>> documentsList = new ArrayList<>();
	    for (GroupDocs<BytesRef> groupDocs : topGroups.groups) {
	    	ScoreDoc[] scoreDocs = groupDocs.scoreDocs;
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
}
