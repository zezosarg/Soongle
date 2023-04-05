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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

@Service
public class SnoogleService {

	StandardAnalyzer analyzer = new StandardAnalyzer();

    Directory index = FSDirectory.open(Paths.get("/tmp/testindex"));

	IndexWriterConfig config = new IndexWriterConfig(analyzer);

    IndexWriter w = new IndexWriter(index, config);
    
    public SnoogleService(StandardAnalyzer analyzer, Directory index, IndexWriterConfig config, IndexWriter w) {
		super();
		this.analyzer = analyzer;
		this.index = index;
		this.config = config;
		this.w = w;
	}	
	public List<List<String>> loadData() throws IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		CSVReader csvReader = new CSVReader(new FileReader("data/corpus.csv"));
		String[] values = null;
	    while ((values = csvReader.readNext()) != null) {
	    	records.add(Arrays.asList(values));
	    }
	    return records;
	}
	
	private void addDoc(IndexWriter w, String title, String isbn) throws IOException {
		  Document doc = new Document();
		  doc.add(new TextField("title", title, Field.Store.YES));
		  doc.add(new StringField("isbn", isbn, Field.Store.YES));
		  w.addDocument(doc);
	}
	
}
