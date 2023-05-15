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

import com.uoi.soongle.service.SoongleService;

@Controller
public class SoongleController {

	@Autowired
	private SoongleService soongleService;
	private Set<String> searchHistory;

	public SoongleController(Set<String> searchHistory) {
		this.searchHistory = new HashSet<String>();
	}
	
	@RequestMapping("/home")
	public String getHome(Model model) throws IOException {
		Path path = Paths.get("luceneindex");
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
	public String retrieveResults(@RequestParam("query") String query, @RequestParam("strategy") String searchType,
	Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		searchHistory.add(query);
		soongleService.setSearchType(searchType);
		List<Map<String, String>> results = null;
		if(searchType.equals("regular")) {
			soongleService.setLastDoc(null);
			results = soongleService.searchIndex("lyrics", query);
		} else if (searchType.equals("group")) {
			soongleService.setLastGroup(0);
			results = soongleService.groupSearchIndex("lyrics", query);
		} else if (searchType.equals("semantic")) {
			//TODO
		}
		model.addAttribute("results", results);
		return "results";
	}
	
	@RequestMapping("/moreResults")
	public String retrieveMoreResults(Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		List<Map<String, String>> results = null;
		if(soongleService.getSearchType().equals("regular")) {
			results = soongleService.searchIndex("lyrics", soongleService.getQuery());
		} else if (soongleService.getSearchType().equals("group")) {
			results = soongleService.groupSearchIndex("lyrics", soongleService.getQuery());
		} else if (soongleService.getSearchType().equals("semantic")) {
			//TODO
		}
		model.addAttribute("results", results);
		return "results";
	}
	 
}
