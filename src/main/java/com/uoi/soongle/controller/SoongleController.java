package com.uoi.soongle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SoongleController {
	
	 @RequestMapping("/home")
	 public String getHome() {
		 System.out.println("home sweet home");
		 return "home";
	 }

	 @RequestMapping("/results")
	 public String retrieveResults(@RequestParam String sourceText, Model model) {
		 System.out.println(sourceText);
		 return "home";
	 }
	 
}
