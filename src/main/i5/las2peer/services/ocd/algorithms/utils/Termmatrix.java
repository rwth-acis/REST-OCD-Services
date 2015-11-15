package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
//import org.json.JSONArray;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.preprocessing.StringConverter;


import y.base.Node;
import y.base.NodeCursor;

public class Termmatrix {

	private Array2DRowRealMatrix matrix;
	private LinkedList<String> wordlist;
	private LinkedList<Node> nodelist;
	
	///////////////////
	////Constructor////
	///////////////////
	
	public Termmatrix(){		
	}
	
	public  Termmatrix(CustomGraph graph) throws OcdAlgorithmException{
		NodeCursor nodes = graph.nodes();
		Node node; 
		LinkedList<String> wordlist = new LinkedList<String>();
		StringConverter conv = new StringConverter();
		String threads = "";
		String word = null;
		int column = 0;
		int row = 0;
		Log log = new Log();
		LinkedList<Node> nodeList = new LinkedList<Node>();
		//Termmatrix res = new Termmatrix();
		this.setNodelist(nodeList);
		
		while(nodes.ok()){
			node = nodes.node();
			this.addNode(node);
			threads = graph.getNodeContent(node) + " " + threads;
			nodes.next();
		}
		//for(Iterator<Node> it = nodes.iterator(); it.hasNext();){ 
		//	Node currNode = it.next();
		//	threads = currNode.getContent()+ " " + threads;      // compute all concatenated threads
			
			/*for(Iterator<Node> it2 = tempList.iterator(); it2.hasNext();){ //group by users
				Node currTemp = it2.next();
				if(currTemp.getUserID().equals(currNode.getUserID())){
					currTemp.setContent(currTemp.getContent()+currNode.getContent());
					add = false;
				}
				
			}
			if(add){
				tempList.add(currNode);
			}*/			
		
		
		//int nodesSize = tempList.size();
		int nodesSize = nodes.size();
		
		if(threads == null || threads.isEmpty()){
			throw new OcdAlgorithmException("no content received");
		}
		
		wordlist = conv.StringToList(threads);
		this.setWordlist(wordlist);
		//wordlistdup = listWordsDup(threads);
		//len = stringLength(thread);
		
		Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(nodesSize ,wordlist.size());
		
		//for(Iterator<Node>iter = nodes.iterator(); iter.hasNext();){
			//Node node = iter.next();
		nodes.toFirst();
		while(nodes.ok()){
			node =  nodes.node();
			
			//res.addNode(node.getNodeID());
			//String thr = node.getContent();
			String thr = graph.getNodeContent(node);
			LinkedList<String> temp = conv.StringToListDup(thr);
			RealVector vector = new ArrayRealVector(wordlist.size());
			
			for(Iterator<String> it = wordlist.iterator(); it.hasNext();){
				word = it.next();
				int freq = countWord(word, temp);
				int docFreq = countDoc(word, graph);
				double tfidf = freq * log.value((double)nodesSize/docFreq);
				
				vector.setEntry(column, tfidf);
				column++;
			}
			column = 0;
			matrix.setRowVector(row, vector);
			nodes.next();
			row++;
		}
		
		this.setMatrix(matrix);//json = converter.matrixToJson(matrix, wordlist);
		
		//return json;
	
	}
	
	/////////////////////////
	////Getter and Setter////
	/////////////////////////
	
	public void setMatrix(Array2DRowRealMatrix matrix){
		this.matrix = matrix;
	}
	
	public Array2DRowRealMatrix getMatrix(){
		return matrix;
	}
	
	public void setWordlist(LinkedList<String> wordlist){
		this.wordlist = wordlist;
	}
	
	public LinkedList<String> getWordlist(){
		return wordlist;
	}
	
	public void setNodelist(LinkedList<Node> nodelist){
		this.nodelist = nodelist;
	}
	
	public LinkedList<Node> getNodeIdList(){
		return nodelist;
	}
	
	////////////////////////
	////Update Functions////
	////////////////////////
	
	public void addNode(Node node){
		this.nodelist.add(node);
	}
	
	public void addWord(String word){
		this.wordlist.add(word);
	}
	
	/////////////////////////////
	////Computation Functions////
	/////////////////////////////
	
	/*public Termmatrix convertTFIDF(LinkedList<Node> nodes) throws Exception{
		LinkedList<String> wordlist = new LinkedList<String>();
		WordConverter conv = new WordConverter();
		String threads = "";
		String word = null;
		int column = 0;
		int row = 0;
		Log log = new Log();
		LinkedList<Node> tempList = new LinkedList<Node>();
		

		for(Iterator<Node> it = nodes.iterator(); it.hasNext();){ 
			boolean add = true;
			Node currNode = it.next();
			threads = currNode.getContent() + threads;      // compute all concatenated threads
			
			/*for(Iterator<Node> it2 = tempList.iterator(); it2.hasNext();){ //group by users
				Node currTemp = it2.next();
				if(currTemp.getUserID().equals(currNode.getUserID())){
					currTemp.setContent(currTemp.getContent()+currNode.getContent());
					add = false;
				}
				
			}
			if(add){
				tempList.add(currNode);
			}*/			
		/*}
		
		//int nodesSize = tempList.size();
		int nodesSize = nodes.size();
		
		if(threads == null || threads.isEmpty()){
			return null;
		}
		
		wordlist = conv.listWords(threads);
		this.setWordlist(wordlist);
		//wordlistdup = listWordsDup(threads);
		//len = stringLength(thread);
		
		Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(nodesSize ,wordlist.size());
		
		for(Iterator<Node>iter = tempList.iterator(); iter.hasNext();){
			Node node = iter.next();
			this.addNode(node.getNodeID());
			String thr = node.getContent();
			LinkedList<String> temp = conv.listWordsDup(thr);
			RealVector vector = new ArrayRealVector(wordlist.size());
			
			for(Iterator<String> it = wordlist.iterator(); it.hasNext();){
				word = it.next();
				int freq = conv.countWord(word, temp);
				int docFreq = conv.countDoc(word, nodes);
				double tfidf = freq * log.value((double)nodesSize/docFreq);
				
				vector.setEntry(column, tfidf);
				column++;
			}
			column = 0;
			matrix.setRowVector(row, vector);
			row++;
		}
		
		this.setMatrix(matrix);//json = converter.matrixToJson(matrix, wordlist);
		
		//return json;
		return this;
	}*/
	
	public int countWord(String word, LinkedList<String> list){
		int res = 0;
		for(Iterator<String> it = list.iterator(); it.hasNext();){
			if(word.equals(it.next())){
				res++;
			}
		}
		
		return res;
	}
	
	public int countDoc(String word, CustomGraph graph){
		int res = 0;
		Node node;
		//Node node = new Node();
		NodeCursor nodes = graph.nodes();
		CharSequence wordSeq = word;
		
		while(nodes.ok()){
			node = nodes.node();
		//for(Iterator<Node> it = nodes.iterator(); it.hasNext();){
		//	node = it.next();
			//if(node.getContent().contains(wordSeq)){
			if(graph.getNodeContent(node).contains(wordSeq)){
				res++;
			}
			nodes.next();
		}
		return res;
	}
	
	public String toString(CustomGraph graph){
		String res = null;
		res = "nodelist: ";
		for(Iterator<Node> it = this.nodelist.iterator(); it.hasNext();){
			res = res + graph.getNodeName(it.next()) + " ";
		}
		res = res + "\n" + "wordlist: " ;
		for(Iterator<String> it1 = this.wordlist.iterator(); it1.hasNext();){
			res = res + it1.next() + " ";
		}
		res = res + "\n" + this.matrix.toString();
		return res;
	}
	
	public void SVD(){
		SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
		this.matrix = (Array2DRowRealMatrix) svd.getU();
	}
}
