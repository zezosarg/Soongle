package com.uoi.soongle.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uoi.soongle.service.Searcher;
import com.uoi.soongle.service.SearcherFactory;
import com.uoi.soongle.service.SoongleService;

@Controller
public class SoongleController {

	@Autowired
	private SoongleService soongleService;
	private Set<String> searchHistory;
	private Searcher searcher;

	public SoongleController(Set<String> searchHistory) {
		this.searchHistory = new HashSet<String>();
	}
	
	@RequestMapping("/home")
	public String getHome(Model model) throws IOException, ParseException {
		Path path = Paths.get("luceneindex");
		Path path2 = Paths.get("modelindex");
    
		//soongleService.buildModel(); // TODO: enable this for final

		if (!Files.exists(path2))
//TODO			soongleService.buildModelIndex();
		if (!Files.exists(path))
			soongleService.buildIndex();
	    model.addAttribute("history", searchHistory);
		return "home";
	}
	
	@RequestMapping("/rebuildLuceneIndex")
	public String rebuildLuceneIndex(Model model) throws IOException {
		Path path = Paths.get("luceneindex");
		if (Files.exists(path))
			FileUtils.deleteDirectory(new File("luceneindex"));
		soongleService.buildIndex();
	    model.addAttribute("history", searchHistory);
		return "home";
	}

	@RequestMapping("/results")
	public String retrieveResults(@RequestParam("query") String query, @RequestParam("strategy") String searchType, Model model)
	throws ParseException, IOException, InvalidTokenOffsetsException {
		searchHistory.add(query);
		searcher =  new SearcherFactory().createSearcher(searchType);
		searcher.reset();
		List<Map<String, String>> results = searcher.search(searchType, query, soongleService.getModel());
		model.addAttribute("results", results);
		return "results";
	}
	
	@RequestMapping("/moreResults")
	public String retrieveMoreResults(Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		List<Map<String, String>> results = searcher.search("lyrics", searcher.getQuery(), soongleService.getModel());
		model.addAttribute("results", results);
		return "results";
	}
	 
}
