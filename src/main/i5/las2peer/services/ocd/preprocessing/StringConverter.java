package i5.las2peer.services.ocd.preprocessing;

import java.util.Iterator;
import java.util.LinkedList;

public class StringConverter {
	
	public LinkedList<String> StringToList(String thread){
		LinkedList<String> res = new LinkedList<String>();
		int begin = 0;
		int end = 0;
		String temp = null;
		boolean add;
		int len = thread.length();
		
		if(thread == null || thread.isEmpty()){
			return null;
		}
		
		for(int i = 0; i < len; i++){
			if(thread.charAt(i) != ' '){
				begin = i;
				end = i;
				
				while(i < len && thread.charAt(i) != ' '){
						end++;
						i++;
				}
				add = true;

				temp = thread.substring(begin, end);
				
				if(res.contains(temp)){
					add = false;
				}
				/*for(Iterator<String> it = res.iterator(); it.hasNext();){
					if(temp.equals(it.next())){
						add = false;
					}
				}*/
				if(add){
					res.add(temp);
				}
			}
		}
		
		return res;
	}
	
	public LinkedList<String> StringToListDup(String thread){
		LinkedList<String> res = new LinkedList<String>();
		int begin = 0;
		int end = 0;
		String temp = null;
		int len = thread.length();
				
		if(thread == null || thread.isEmpty()){
			return null;
		}
		
		for(int i = 0; i < len; i++){
			if(thread.charAt(i) != ' '){
				begin = i;
				end = i;
				while(i < len && thread.charAt(i) != ' '){
					end++;
					i++;
				}
				
				temp = thread.substring(begin, end);
				
				res.add(temp);
				
			}
		}
		
		return res;
	}
}
