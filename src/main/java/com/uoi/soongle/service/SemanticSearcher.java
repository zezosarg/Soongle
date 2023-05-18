package com.uoi.soongle.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;

import com.uoi.soongle.model.DocScore;
import com.uoi.soongle.model.Word2VectorModel;

public class SemanticSearcher extends SoongleSearcher{

	private int lastWord2Vec;
	
	@Override
	public List<Map<String, String>> search(String inField, String queryString, Word2VectorModel model) throws ParseException, IOException, InvalidTokenOffsetsException {
		int maxResultsPerPage = 10;
		List<Map<String, String>> results = new ArrayList<>();
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("modelindex")));
		List<DocScore> docOrder = model.getTopDocs(indexReader, queryString, lastWord2Vec ,maxResultsPerPage);

		for (DocScore docScore : docOrder) {
			IndexReader indexReaderLucene = DirectoryReader.open(FSDirectory.open(Paths.get("luceneindex")));
			IndexSearcher searcher = new IndexSearcher(indexReaderLucene);
			Query queryObj = new QueryParser("id", new StandardAnalyzer()).parse(docScore.getDocId()+"");
			TopDocs topDocs = searcher.searchAfter(null, queryObj, 1);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				Map<String, String> result = new HashMap<>();
				Document document = searcher.doc(scoreDoc.doc);
				for (String s: Arrays.asList("artist", "title", "lyrics"))
					result.put(s, document.get(s));
				results.add(result);
			}
		}
		lastWord2Vec += 10;
		return results;
	}
}
