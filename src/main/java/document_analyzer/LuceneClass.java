package document_analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRefBuilder;

public class LuceneClass {

	private IndexWriter indexWriter;
	private IndexReader indexReader;
	private Directory directory;
	private FileOperator fileOperator = new FileOperator();
	private Map vector;
	private ArrayList<Map> documentVectors;
	
	

	public LuceneClass(String indexDirectoryPath) throws IOException {

		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		Path path = FileSystems.getDefault().getPath(indexDirectoryPath, "index");
		directory = new SimpleFSDirectory(path);
		indexWriter = new IndexWriter(directory, indexWriterConfig);

	}

	
	
	public ArrayList<Map> getTermVectors() {

		try {
			indexReader = DirectoryReader.open(directory);

			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			documentVectors = new ArrayList<Map>();
			for (int i = 0; i < indexReader.maxDoc(); ++i) {
				vector = new HashMap<String ,Long>();
				final Terms terms = indexReader.getTermVector(i, "Contents");
				
				if (terms != null) {

					int numTerms = 0;
					// record term occurrences for corpus terms above threshold
					TermsEnum term = terms.iterator();
					
					while (term.next() != null) {
						
						vector.put(term.term().utf8ToString(), term.totalTermFreq());
//						System.out.println(term.term().utf8ToString() + " | " + term.totalTermFreq() + " | "
//								+ indexSearcher.count(new TermQuery(new Term("Contents", term.term().utf8ToString()))));
						++numTerms;
						
					}
					documentVectors.add(vector);
					//System.out.println("Document " + i + " had " + numTerms + " terms");
				} else {
					System.err.println("Document " + i + " had a null terms vector for body");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return documentVectors;
	}

	
	
	
	
	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

	
	
	
	public void indexDirectory(String directory) throws IOException {

		for (File file : new File(directory).listFiles()) {
			
			System.out.println("Indexing " + file.getCanonicalPath());
			Document document = getDocument(file);
			indexWriter.addDocument(document);
		}
		
		indexWriter.flush();
	}

	private Document getDocument(File file) throws IOException {

		Document document = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		type.setTokenized(true);
		type.setStored(true);
		type.setStoreTermVectors(true);

		// index file contents
		// fileOperator.readFiles(file.getAbsolutePath());
		// System.out.println(fileOperator.readFiles(file.getAbsolutePath()));
		Field contentField = new Field("Contents", fileOperator.readFiles(file.getAbsolutePath()), type);
		// System.out.println(contentField.tokenStreamValue().toString());
		Field fileNameField = new TextField("FileName", file.getName(), Store.YES);
		// index file name
		// Field fileNameField = new StoredField("FileName", file.getName());
		// 
		// // index file path
		Field filePathField = new StringField("Path", file.getAbsolutePath(), Field.Store.YES);
		// file.getAbsolutePath());

		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);

		return document;
	}
	
	
	
	public ArrayList<ArrayList<Double>> getSimilarityMatrix(ArrayList<Map> vectors) {

		CosineSimilarity cosineSimilarity = new CosineSimilarity();
		
		ArrayList<ArrayList<Double>> similarityMatrix = new ArrayList<ArrayList<Double>>();
		
		for (Map vector1 : vectors) {
			
			ArrayList<Double> rowSimilarity = new ArrayList<Double>();
			
			for (Map vector2 : vectors) {
				rowSimilarity.add(cosineSimilarity.calculateCosineSimilarity(vector1, vector2));
			}
			similarityMatrix.add(rowSimilarity);
		}
		return similarityMatrix;

	}

}
