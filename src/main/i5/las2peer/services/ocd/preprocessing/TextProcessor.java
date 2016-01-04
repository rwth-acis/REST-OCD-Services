package i5.las2peer.services.ocd.preprocessing;

import java.util.Locale;

import org.apache.lucene.analysis.en.PorterStemFilter;
import org.tartarus.snowball.ext.PorterStemmer;

public class TextProcessor {
	
	public String preprocText(String thread){
		thread  = deleteNonWords(thread);
		//thread = stemming(thread);
		return thread;
		
	}
	
	private String deleteNonWords(String thread){
		String result = null;
		
		thread = thread.replaceAll("<[^>]*>", ""); 	// remove html tags
		result = thread.replaceAll("\\p{Punct}"," ");		// remove Punctuation
		result = result.toLowerCase(Locale.ROOT);
		return result;
	}
	
	private String stemming(String thread){
		PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(thread);
        stemmer.stem();
        return stemmer.getCurrent();
	}

}
