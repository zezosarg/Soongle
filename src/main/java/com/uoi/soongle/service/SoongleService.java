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
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.uoi.soongle.model.Word2VectorModel;

@Service
public class SoongleService {

	Word2VectorModel model;

    public void buildIndex() throws IOException {
    	IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    	IndexWriter w = new IndexWriter(FSDirectory.open(Paths.get("luceneindex")), config);
    	List<List<String>> records = loadData();
    	for (List<String> record : records)
    		addDoc(w, record.get(0), record.get(1), record.get(2), record.get(3));	// 0 is id
    	w.close();
    }

	public void buildModel(){
		if(model == null)
			model = new Word2VectorModel();
	}

	public void buildModelIndex() throws IOException, ParseException {

		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter w = new IndexWriter(FSDirectory.open(Paths.get("modelindex")), config);

		List<List<String>> records = loadData();
		for (List<String> record : records)
			model.addDoc(w, record.get(0), record.get(1), record.get(2), record.get(3));

		w.close();
/*
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("modelindex")));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TopDocs topDocs = searcher.searchAfter(null,
				new QueryParser("id", new StandardAnalyzer()).parse("5381"),
				10);

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document document = searcher.doc(scoreDoc.doc);
			System.out.println(document.get("id"));

			IndexableField[] fields = document.getFields("vector");
			for (IndexableField field : fields) {
				System.out.print(", " + field.numericValue() + ", ");
			}
			System.out.println();

		}
*/
	}
	
	public List<List<String>> loadData() throws IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		CSVReader csvReader = new CSVReader(new FileReader("data/corpus.csv"));
		String[] values = null;
		while ((values = csvReader.readNext()) != null)
			records.add(Arrays.asList(values));
		return records;
	}
	
	public void addDoc(IndexWriter w, String id ,String artist, String title, String lyrics) throws IOException {
		Document document = new Document();
		
//		FieldType fieldType = new FieldType();
//		fieldType.setStored(true);
//		fieldType.setDocValuesType(DocValuesType.SORTED);
//		Field artistField = new Field("artist", artist, fieldType);
//		document.add(artistField);
				
//		document.add(new SortedDocValuesField("artist", new BytesRef(artist)));
//		document.add(new StoredField("artist", artist));
		
		document.add(new SortedDocValuesField("artist", new BytesRef(artist)));
		document.add(new TextField("id", id, Field.Store.YES));
		document.add(new TextField("artist", artist, Field.Store.YES));
		document.add(new TextField("title", title, Field.Store.YES));
		document.add(new TextField("lyrics", lyrics, Field.Store.YES));
		w.addDocument(document);
	}
	
	public Word2VectorModel getModel() {
		return model;
	}
	
}
