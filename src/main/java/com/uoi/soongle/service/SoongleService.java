package com.uoi.soongle.service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

@Service
public class SoongleService {
	
	private StandardAnalyzer analyzer;
	private Directory index;

    @Autowired
    public SoongleService() throws IOException {
		this.index = FSDirectory.open(Paths.get("/tmp/testindex"));
		this.analyzer = new StandardAnalyzer();
	}	

    public List<Document> searchIndex(String inField, String queryString) throws ParseException, IOException {
        Query query = new QueryParser(inField, analyzer).parse(queryString);
        IndexReader indexReader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 10);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs)
            documents.add(searcher.doc(scoreDoc.doc));
        return documents;	//TODO DON'T allow duplicates
    }
    
    public void buildIndex() throws IOException {
    	IndexWriterConfig config = new IndexWriterConfig(analyzer);
    	IndexWriter w = new IndexWriter(index, config);
    	List<List<String>> records = loadData();
    	for (List<String> record : records)
    		addDoc(w, record.get(1), record.get(2), record.get(3));	// 0 is id
    	w.close();
    }
	
	public List<List<String>> loadData() throws IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		CSVReader csvReader = new CSVReader(new FileReader("data/corpus.csv"));
		String[] values = null;
		while ((values = csvReader.readNext()) != null)
			records.add(Arrays.asList(values));
		return records;
	}
	
	public void addDoc(IndexWriter w, String artist, String title, String lyrics) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("artist", artist, Field.Store.YES));
		doc.add(new StringField("title", title, Field.Store.YES));
		doc.add(new StringField("lyrics", lyrics, Field.Store.YES));
		w.addDocument(doc);
	}
	
}
