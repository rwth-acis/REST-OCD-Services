package i5.las2peer.services.ocd.preprocessing;

import java.util.LinkedList;

public class TextProcessor {
	
	public String preprocText(String thread){
		thread  = deleteNonWords(thread);
		//thread = stemming(thread);
		return thread;
	}
	
	private String deleteNonWords(String thread){
		String result = null;
		
		thread = thread.replaceAll("<[^>]*>", ""); 	// remove html tags
		result = thread.replaceAll("\\p{Punct}","");		// remove Punctuation
		
		return result;
	}
	
	private String stemming(String thread){
		LinkedList<String> list = new LinkedList<String>();
		String result = thread;
		
		return result;
	}

}
