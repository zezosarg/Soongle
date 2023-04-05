package com.uoi.soongle.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.opencsv.CSVReader;
import com.uoi.soongle.service.SoongleService;

@Controller
public class SoongleController {

	@Autowired
	SoongleService soongleService;
	
	@RequestMapping("/home")
	public String getHome() {	 
		return "home";
	}

	@RequestMapping("/results")
	public String retrieveResults(@RequestParam String sourceText, Model model) throws IOException, ParseException {
		soongleService.fillIndex();	// this has to be executed once TODO
		List<Document> results = soongleService.searchIndex("artist", sourceText);
		for (Document result : results)
			System.out.println(result.get("title"));
		System.out.println(results.size());
		return "home";
	}
	 
}
