package i5.las2peer.services.ocd.utils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;


public class DocIndexer {

	private String indexPath;
	
	
	public DocIndexer(String pathIndex){
		this.indexPath = pathIndex;
		
	}
	
	
	
	public void indexDoc(String docid, String docContent) throws IOException{
		Path f = new File(indexPath).toPath();
		try{
			SimpleFSDirectory dir = new SimpleFSDirectory(f);
			//only stopword removal
			//IndexWriter iW = new IndexWriter(dir, new IndexWriterConfig(new StopAnalyzer()));
			//stopword removal and stemming using Porter Stemmer
			IndexWriter iW = new IndexWriter(dir, new IndexWriterConfig(new EnglishAnalyzer()));     
            Document doc = new Document();
            
            final FieldType fieldType = new FieldType();
            //fieldType.setIndexed(true);
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            fieldType.setStored(true);
            fieldType.setStoreTermVectors(true);
            fieldType.setTokenized(true);
            
            doc.add(new Field("doccontent", docContent, fieldType));
            doc.add(new Field("docid", docid, fieldType));
            iW.addDocument(doc);
            
            iW.close();
		}catch (CorruptIndexException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	
	public void indexDocPerField(String docid, String docContent) throws IOException{
		Path f = new File(indexPath).toPath();
		try{
			SimpleFSDirectory dir = new SimpleFSDirectory(f);
			//only stopword removal
			//IndexWriter iW = new IndexWriter(dir, new IndexWriterConfig(new StopAnalyzer()));
			//stopword removal and stemming using Porter Stemmer
			Map<String, Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
			analyzerPerField.put("docid", new WhitespaceAnalyzer());
			analyzerPerField.put("doccontent", new EnglishAnalyzer());
			PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(),analyzerPerField);
			IndexWriter iW = new IndexWriter(dir, new IndexWriterConfig(analyzer));     
            Document doc = new Document();
            
            final FieldType fieldType = new FieldType();
            //fieldType.setIndexed(true);
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            fieldType.setStored(true);
            fieldType.setStoreTermVectors(true);
            fieldType.setTokenized(true);
            
            doc.add(new Field("doccontent", docContent, fieldType));
            doc.add(new Field("docid", docid, fieldType));
            iW.addDocument(doc);
            
            iW.close();
		}catch (CorruptIndexException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
}
