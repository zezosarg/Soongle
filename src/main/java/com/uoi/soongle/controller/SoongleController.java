package com.uoi.soongle.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.opencsv.CSVReader;

@Controller
public class SoongleController {
	
	 @RequestMapping("/home")
	 public String getHome() {
		 
		 return "home";
	 }

	 @RequestMapping("/results")
	 public String retrieveResults(@RequestParam String sourceText, Model model) {
		 System.out.println(sourceText);
		 return "home";
	 }
	 
}
