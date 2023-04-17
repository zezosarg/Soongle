package com.uoi.soongle.controller;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uoi.soongle.service.SoongleService;

@Controller
public class SoongleController {

	@Autowired
	SoongleService soongleService;
	
	@RequestMapping("/home")
	public String getHome() {	 
		return "home";
	}

	@RequestMapping("/buildIndex")
	public String buildIndex() throws IOException {
		soongleService.buildIndex();
		return "home";
	}
	
	@RequestMapping("/results")
	public String retrieveResults(@RequestParam("query") String query, @RequestParam("field") String field, Model model) throws ParseException, IOException {
		List<Document> results = soongleService.searchIndex(field, query);
		model.addAttribute("results", results);
		return "results";
	}
	 
}
