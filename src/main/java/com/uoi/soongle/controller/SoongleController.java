package com.uoi.soongle.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

import com.uoi.soongle.service.SoongleSearcher;
import com.uoi.soongle.service.SearcherFactory;
import com.uoi.soongle.service.SoongleService;

@Controller
public class SoongleController {

	@Autowired
	private SoongleService soongleService;
	private Set<String> searchHistory;
	private SoongleSearcher soongleSearcher;

	public SoongleController(Set<String> searchHistory) throws IOException, ParseException {
		this.searchHistory = new HashSet<String>();
	}
	
	@RequestMapping("/home")
	public String getHome(Model model) throws IOException, ParseException {
		soongleService.buildModel();
		if (!Files.exists(Paths.get("luceneindex")))
			soongleService.buildIndex();
		if (!Files.exists(Paths.get("modelindex")))
			soongleService.buildModelIndex();
	    model.addAttribute("history", searchHistory);
		return "home";
	}
	
	@RequestMapping("/rebuildLuceneIndex")
	public String rebuildLuceneIndex(Model model) throws IOException {
		if (Files.exists(Paths.get("luceneindex")))
			FileUtils.deleteDirectory(new File("luceneindex"));
		soongleService.buildIndex();
		return "home";
	}
	
	@RequestMapping("/rebuildModelIndex")
	public String rebuildModelIndex(Model model) throws IOException, ParseException {
		if (Files.exists(Paths.get("modelindex")))
			FileUtils.deleteDirectory(new File("modelindex"));
		soongleService.buildModelIndex();
		return "home";
	}

	@RequestMapping("/results")
	public String retrieveResults(@RequestParam("query") String query, @RequestParam("strategy") String searchType, Model model)
	throws ParseException, IOException, InvalidTokenOffsetsException {
		searchHistory.add(query);
		soongleService.setQuery(query);
		soongleSearcher =  new SearcherFactory().createSearcher(searchType);
		List<Map<String, String>> results = soongleSearcher.search("lyrics", query, soongleService.getModel());
		model.addAttribute("results", results);
		return "results";
	}
	
	@RequestMapping("/moreResults")
	public String retrieveMoreResults(Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		List<Map<String, String>> results = soongleSearcher.search("lyrics", soongleService.getQuery(), soongleService.getModel());
		model.addAttribute("results", results);
		return "results";
	}
}
