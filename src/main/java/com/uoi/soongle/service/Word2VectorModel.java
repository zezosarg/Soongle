package com.uoi.soongle.service;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Word2VectorModel {

    private Word2Vec vec;

    private int documentCount = 0;

    private HashMap<String, INDArray> documentVectors = new HashMap<>();

    public HashMap<String, INDArray> getDocumentVectors() {
        return documentVectors;
    }

    public void setDocumentVectors(HashMap<String, INDArray> documentVectors) {
        this.documentVectors = documentVectors;
    }

    public Word2VectorModel(){

        System.out.println("Loading model....");
        vec = WordVectorSerializer.readWord2VecModel("C:\\IntellijProjects\\World2Vecdl4jtest\\src\\main\\resources\\lexvec.enwiki+newscrawl.300d.W.pos.vectors");

    }
/*
    public INDArray textToVector(String text){
        if(text == null || text.isEmpty()){
            return null;
        }
        text = text.toLowerCase();
        Collection<String> collection = List.of(text.split("\\s+"));
        INDArray indArray = null;

        for(String element: collection){
            if(indArray == null){
                indArray = vec.getWordVectorMatrix(element);
            }else{
                INDArray temp = vec.getWordVectorMatrix(element);
                //System.out.println("The current"+ indArray);
                //System.out.println("The temp: "+ temp);
                if(temp != null) {
                    indArray = indArray.add(temp);
                }
            }
        }
        return indArray;
    }
*/
    public INDArray textToVector(String text){
        if(text == null || text.isEmpty()){
            return null;
        }
        text = text.toLowerCase();
        String[] words = text.split("\\s+");
        //System.out.println("The words: "+ words);
        //System.out.println("The words 0: "+ words[0]);
        int vectorSize = vec.getLayerSize();
        INDArray indArray = Nd4j.zeros(1, vectorSize);

        for(String element: words){
            INDArray temp = vec.getWordVectorMatrix(element);
            if(temp != null) {
                indArray.addi(temp);
            }
        }
        return indArray;
    }
    public double vectorSimilarity(INDArray vector1, INDArray vector2){
        if (vector1 == null || vector2 == null) {
            return -1.0;
        }
        return vector1.equals(vector2) ? 1.0 : Transforms.cosineSim(vector1, vector2);
    }

    public void addDocument(String id, String artist, String title, String lyrics){
        INDArray indArray = textToVector("artist"+" "+artist);
       // System.out.println("The docuemnt stuff "+ id + " "+ artist + " "+ title);
        documentCount++;
        if(documentCount % 100 == 0){
            System.out.println("The document count is: "+ documentCount);
        }
        //in memory for now
        documentVectors.put(id, indArray);
    }

    public List<Integer> getTopDocumentsBasedOnSimilarity(String query, int topN){
        INDArray queryVector = textToVector(query);
        if(queryVector == null){
            return null;
        }
        List<DocScore> docIdAndSimilarity = new ArrayList<>();
        for(Map.Entry<String, INDArray> entry: documentVectors.entrySet()){
            double vecSim = vectorSimilarity(queryVector, entry.getValue());
            docIdAndSimilarity.add(new DocScore(Integer.parseInt(entry.getKey()), vecSim));
        }
        Collections.sort(docIdAndSimilarity, new Comparator<DocScore>() {
            @Override
            public int compare(DocScore a, DocScore b) {
                return Double.compare(b.getScore(), a.getScore());
            }
        });

        List<Integer> topDocuments = new ArrayList<>();
        for(int i = 0; i < topN; i++){
            topDocuments.add(docIdAndSimilarity.get(i).getDocId());
            System.out.println("The doc id: "+ docIdAndSimilarity.get(i).getDocId() + " The score: "+ docIdAndSimilarity.get(i).getScore());
        }

        return topDocuments;
    }

    public void printHashMap() {
    	for (Map.Entry<String, INDArray> entry : documentVectors.entrySet()) {
    	    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    	}
    }

    public List<Integer> getTopDocs(IndexReader indexReader, String query, int topN) throws IOException, ParseException {
        INDArray queryVector = textToVector(query);

        if(queryVector == null){
            return null;
        }

        //IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("modelindex")));
        IndexSearcher searcher = new IndexSearcher(indexReader);

        TopDocs topDocs = searcher.searchAfter(null,
               new MatchAllDocsQuery(),
                40000);

        List<DocScore> docIdAndSimilarity = new ArrayList<>();

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

            Document document = searcher.doc(scoreDoc.doc);
            ///System.out.println(document.get("id"));

            INDArray docVector = null;
            IndexableField[] fields = document.getFields("vector");
            int vectorSize = vec.getLayerSize();
            INDArray vectorArray = Nd4j.zeros(1, vectorSize);

            //create a vector from all the fields
            int i = 0;
            for (IndexableField field : fields) {
                vectorArray.putScalar(i, field.numericValue().doubleValue());
                i++;
                ///System.out.print(", " + field.numericValue() + ", ");
            }

            double vecSim = vectorSimilarity(queryVector, vectorArray);

            ///System.out.println();

            docIdAndSimilarity.add(new DocScore(Integer.parseInt(document.get("id")), vecSim));

        }
        Collections.sort(docIdAndSimilarity, new Comparator<DocScore>() {
            @Override
            public int compare(DocScore a, DocScore b) {
                return Double.compare(b.getScore(), a.getScore());
            }
        });

        List<Integer> topDocuments = new ArrayList<>();
        for(int i = 0; i < topN; i++){
            topDocuments.add(docIdAndSimilarity.get(i).getDocId());
            System.out.println("The doc id: "+ docIdAndSimilarity.get(i).getDocId() + " The score: "+ docIdAndSimilarity.get(i).getScore());
        }
        return topDocuments;

    }

    public void addDoc(IndexWriter w, String id , String artist, String title, String lyrics) throws IOException {

        documentCount++;
        if(documentCount % 100 == 0){
            System.out.println("The document counttt is: "+ documentCount);
        }

        Document document = new Document();

        INDArray indArray = textToVector("artist"+" "+artist);
/*
        if (Integer.parseInt(id) == 5381){
            System.out.println("before load: "+indArray);
        }
*/
        document.add(new TextField("id", id, Field.Store.YES));

        //traverse indArray
        for(int i = 0; i < indArray.length(); i++) {

            //DoubleField doubleField = new DoubleField("double_value", 100.0D, Field.Store.YES);
        	document.add(new StoredField("vector", indArray.getDouble(i)));
        }

        w.addDocument(document);
    }

}

class DocScore {
    private int docId;
    private double score;

    public DocScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    public int getDocId() {
        return docId;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "[" + docId + ", " + score + "]";
    }
}
