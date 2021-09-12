package i5.las2peer.services.ocd.adapters;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides general functionalities for adapters.
 * @author Sebastian
 *
 */
public class Adapters {

	/**
	 * Checks whether a character is a line break.
	 * @param character The character.
	 * @return True if the character is a line break, otherwise false.
	 */
	public static boolean isLineBreak(int character) {
		switch (character) {
			case '\r':
				return true;
			case '\n':
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Reads in the next line from a reader.
	 * @param reader The reader from which the next line is read.
	 * @return The line as a list of strings which were separated by white space.
	 * @throws java.io.IOException in case the reading fails
	 */
	public static List<String> readLine(Reader reader) throws IOException {
		List<String> line = new ArrayList<String>();
		int nextChar = reader.read();
		/*
		 * Skips potential additional line break characters.
		 */
		while (Adapters.isLineBreak(nextChar)) {
			nextChar = reader.read();
		}
		/*
		 * Extracts all strings from the line, separated by whitespace
		 */
		while (!Adapters.isLineBreak(nextChar)  && nextChar != -1 && reader.ready()) {
			String str = "";
			/*
			 * Skips white space other than line separators
			 */
			while (Character.isWhitespace(nextChar)
					&& !Adapters.isLineBreak(nextChar)) {
				nextChar = reader.read();
			}
			/*
			 * Reads string until next whitespace or EOF
			 */
			while (nextChar != -1 && !Character.isWhitespace(nextChar)
					&& !Adapters.isLineBreak(nextChar)) {
				str += (char) nextChar;
				nextChar = reader.read();
			}
			if (!str.equals("")) {
				line.add(str);
			}
		}
		return line;
	}
	
	/**
	 * Reads in the next line from a reader.
	 * @param reader The reader from which the next line is read.
	 * @return The line as a list of strings which were separated by tab stop.
	 * @throws java.io.IOException in case the reading fails
	 */
	public static List<String> readLineTab(Reader reader) throws IOException {
		List<String> line = new ArrayList<String>();
		int nextChar = reader.read();
		/*
		 * Skips potential additional line break characters.
		 */
		while (Adapters.isLineBreak(nextChar)) {
			nextChar = reader.read();
		}
		/*
		 * Extracts all strings from the line, separated by tabs
		 */
		while (!Adapters.isLineBreak(nextChar)  && nextChar != -1 && reader.ready()) {
			String str = "";
			/*
			 * Skips white space other than line separators
			 */
			while (nextChar == '\t'
					&& !Adapters.isLineBreak(nextChar)) {
				nextChar = reader.read();
			}
			/*
			 * Reads string until next tab
			 */
			while (nextChar != -1 && !(nextChar == '\t')
					&& !Adapters.isLineBreak(nextChar)) {
				str += (char) nextChar;
				nextChar = reader.read();
			}
			if (!str.equals("")) {
				line.add(str);
			}
		}
		return line;
	}
	
	/**
	 * Reads in the next line from a reader.
	 * @param reader The reader from which the next line is read.
	 * @param lineLength the expected length of the line
	 * @return The line as a list of strings which were separated by tab stop and ignores some linebreaks.
	 * @throws java.io.IOException in case the reading fails
	 */
	public static List<String> readLineTabIgnoreLineBreak(Reader reader, int lineLength) throws IOException {
		List<String> line = new ArrayList<String>();
		int nextChar = reader.read();
		/*
		 * Skips potential additional line break characters.
		 */
		while (Adapters.isLineBreak(nextChar)) {
			nextChar = reader.read();
		}
		/*
		 * Extracts all strings from the line, separated by tabs
		 */
		/*while ((!Adapters.isLineBreak(nextChar) || line.size() < lineLength)  && nextChar != -1 && reader.ready()) {
			String str = "";
			
			if(Adapters.isLineBreak(nextChar) && line.size() < lineLength){
				nextChar = reader.read();
			}*/
			/*
			 * Skips white space other than line separators
			 */
			/*while (nextChar == '\t'
					&& !Adapters.isLineBreak(nextChar)) {
				nextChar = reader.read();
			}*/
			
			/*
			 * Reads string until next tab
			 */
			/*while (nextChar != -1 && !(nextChar == '\t')) {
				if(Adapters.isLineBreak(nextChar)){
					if(line.size() < lineLength - 1){
						nextChar = reader.read();
					}else{
						break;
					}
				}else{
				str += (char) nextChar;
				nextChar = reader.read();
				}
			}
			if (!str.equals("")) {
				line.add(str);
			}
		}*/
		
		//Extract all strings of a line, separated by tab but ignore line breaks until specified length
		while(line.size() < lineLength && nextChar != -1){
			String str = "";
			
			//skip line breaks in between the lines
			if(Adapters.isLineBreak(nextChar) && line.size() < lineLength){
				nextChar = reader.read();
			}
			
			if(nextChar == '\t'){
				nextChar = reader.read();
				if(nextChar == '\t'){
					str = " ";			//prevent empty strings after two tabs
					line.add(str);
					str = "";
				}
			}
			while(nextChar != -1 && !(nextChar == '\t')){
				 if(Adapters.isLineBreak(nextChar)){
					 if(line.size() == lineLength - 1){
						 break;
					 }
					 nextChar = ' ';
				 }
				str += (char) nextChar;
				nextChar = reader.read();
			}
			
			if (!str.equals("")) {
				line.add(str);
			}
		}
		return line;
	}
	
}
