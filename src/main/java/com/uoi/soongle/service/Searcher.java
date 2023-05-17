package com.uoi.soongle.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import com.uoi.soongle.model.Word2VectorModel;

public abstract class Searcher {
	
	public abstract List<Map<String, String>> search(String inField, String queryString, Word2VectorModel model) throws ParseException, IOException, InvalidTokenOffsetsException;

}
