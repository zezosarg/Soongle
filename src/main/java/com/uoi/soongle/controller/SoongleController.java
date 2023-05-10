package com.uoi.soongle.controller;

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

		soongleService.buildModel();

		if (!Files.exists(path))
			soongleService.buildIndex();
//		Path path = Paths.get("luceneindex");
//		if (Files.exists(path))
//			FileUtils.deleteDirectory(new File("luceneindex"));
//		soongleService.buildIndex();
	    model.addAttribute("history", searchHistory);
		return "home";
	}

	@RequestMapping("/results")
	public String retrieveResults(@RequestParam("query") String query, Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		searchHistory.add(query);
		soongleService.setLastDoc(null);
		soongleService.setLastGroup(0);
		soongleService.searchWord2Vec(query);
		List<Map<String, String>> results = soongleService.groupSearchIndex("lyrics", query);//soongleService.searchIndex("lyrics", query);
		model.addAttribute("results", results);
		return "results";
	}
	
	@RequestMapping("/moreResults")
	public String retrieveMoreResults(Model model) throws ParseException, IOException, InvalidTokenOffsetsException {
		List<Map<String, String>> results = soongleService.groupSearchIndex("lyrics", soongleService.getQuery());
		model.addAttribute("results", results);
		return "results";
	}
	 
}
