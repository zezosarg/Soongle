package com.uoi.soongle.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;

import com.uoi.soongle.model.Word2VectorModel;

public class RegularSearcher extends Searcher {

	private ScoreDoc lastDoc;

	@Override
	public List<Map<String, String>> search(String inField, String queryString, Word2VectorModel model) throws ParseException, IOException, InvalidTokenOffsetsException {
		String[] fields = {"title", "artist", "lyrics"};
    	Query query = new MultiFieldQueryParser(fields, new StandardAnalyzer()).parse(queryString);
    	QueryScorer queryScorer = new QueryScorer(query);
    	//Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);

		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");

    	Highlighter highlighter = new Highlighter(formatter,queryScorer); // Set the best scorer fragments
    	//highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("luceneindex")));
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.searchAfter(lastDoc, query, 10);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<Map<String, String>> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
        	Map<String, String> result = new HashMap<>();
        	Document document = searcher.doc(scoreDoc.doc);
            String field = document.get("lyrics");
			String fieldA = document.get("artist");
			String fieldT = document.get("title");

            TokenStream tokenStream = TokenSources.getTokenStream("lyrics",field, new StandardAnalyzer());
			TokenStream tokenStreamA = TokenSources.getTokenStream("artist",fieldA, new StandardAnalyzer());
			TokenStream tokenStreamT = TokenSources.getTokenStream("title",fieldT, new StandardAnalyzer());

            String fragment = highlighter.getBestFragment(tokenStream, field);
			String fragmentA = highlighter.getBestFragment(tokenStreamA, fieldA);
			String fragmentT = highlighter.getBestFragment(tokenStreamT, fieldT);

			result.put("lyrics", Objects.requireNonNullElse(fragment, field));
			result.put("artist", Objects.requireNonNullElse(fragmentA, fieldA));
			result.put("title", Objects.requireNonNullElse(fragmentT, fieldT));


            results.add(result);
            lastDoc = scoreDoc;
        }
        return results;
	}
}
