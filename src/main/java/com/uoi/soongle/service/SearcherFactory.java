package com.uoi.soongle.service;

public class SearcherFactory {

	public SoongleSearcher createSearcher(String searchType) {
		
		if(searchType.equals("regular")) {
			return new RegularSearcher();
		} 
		else if (searchType.equals("group")) {
			return new GroupSearcher();
		} 
		else if (searchType.equals("semantic")) {
			return new SemanticSearcher();
		} 
		else {
			return null;
		}
	}
	
}
