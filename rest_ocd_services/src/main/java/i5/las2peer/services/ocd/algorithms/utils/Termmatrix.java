package i5.las2peer.services.ocd.algorithms.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
//import org.apache.commons.math3.linear.RealMatrix;
//import org.json.JSONArray;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class Termmatrix {

	private Array2DRowRealMatrix matrix;
	private LinkedList<String> wordlist;
	private LinkedList<Node> nodelist;
	//private HashMap<Node,ArrayRealVector> tfidfMap;
	///////////////////
	////Constructor////
	///////////////////

	public Termmatrix(){
	}

	public  Termmatrix(CustomGraph graph) throws OcdAlgorithmException{

		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		this.wordlist = new LinkedList<String>();

		//StringConverter conv = new StringConverter();
		//wordlist = conv.StringToList(threads);
		//this.setWordlist(wordlist);

		String name = "";
		String key = "";
		int index = 0;
		int row = 0;
		/*String threads = "";
		String word = null;
		int column = 0;
		int row = 0;
		Log log = new Log();*/
		LinkedList<Node> nodeList = new LinkedList<Node>();
		//Termmatrix res = new Termmatrix();
		this.setNodelist(nodeList);
		HashMap<String,HashMap<String,Double>> indexMap = computeTFIDF(graph);
		HashMap<String,Double> valueMap;
		ArrayRealVector vector = new ArrayRealVector(wordlist.size());
		this.matrix = new Array2DRowRealMatrix(indexMap.size(),wordlist.size() );

		while(nodesIt.hasNext()){
			node = nodesIt.next();
			name = graph.getNodeName(node);
			valueMap = indexMap.get(name);
			if (valueMap != null) {
				this.addNode(node);
				for (Map.Entry<String, Double> entry : valueMap.entrySet()) {
					key = entry.getKey();
					index = wordlist.indexOf(key);
					vector.setEntry(index, entry.getValue());
				}
				this.matrix.setRowVector(row, vector);
				row++;
			}
		}
		/*while(nodes.ok()){
			node = nodes.node();
			this.addNode(node);
			threads = graph.getNodeContent(node) + " " + threads;
			nodes.next();
		}*/
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
		/*int nodesSize = nodes.size();
		
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
		
		this.setMatrix(matrix);*///json = converter.matrixToJson(matrix, wordlist);

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
		nodelist.add(node);
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
	
	/*public int countDoc(String word, CustomGraph graph){
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
	}*/

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

	public RealMatrix SVD(){
		SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
		/*RealMatrix u = svd.getU();
		RealMatrix s = svd.getS();
		RealMatrix v = svd.getV();*/
		return svd.getU();
	}

	private HashMap<String,HashMap<String,Double>> computeTFIDF(CustomGraph graph){
		HashMap<String,HashMap<String,Double>> res = new HashMap<String,HashMap<String,Double>>();
		int noOfDocs = graph.getNodeCount();
		String indexPath = graph.getPath();
		//NodeCursor nodes = graph.nodes();
		TermsEnum termEnum = null;
		TermsEnum idEnum = null;
		PostingsEnum docsEnum = null;
		try {


			Path f = new File(indexPath).toPath();
			IndexReader re = DirectoryReader.open(FSDirectory.open(f)) ;


			for(int k = 0; k < noOfDocs; k++){
				//compute termvector for each document for content and name field
				Terms contentTerms = re.getTermVector(k, "doccontent");
				Terms idTerms = re.getTermVector(k, "docid");
				//if(idTerms != null){
				//compute document/node name
				idEnum = idTerms.iterator();
				BytesRef idBytes = idEnum.next(); //should be only one
				String docName = idBytes.utf8ToString();
				HashMap<String,Double> termMap = new HashMap<String,Double>();
				//check if content termvector is empty
				if(contentTerms == null){
					res.put(docName,termMap);
				}else{
					//iterate through content term vector
					termEnum = contentTerms.iterator();

					long noOfTerms = contentTerms.size();
					ClassicSimilarity sim = new ClassicSimilarity();
					for (int i = 0; i < noOfTerms; i++) {
						//compute string for each term in the termvector and add to the wordlist of the term matrix
						BytesRef termBytes = termEnum.next();
						String termStr = termBytes.utf8ToString();
						if(!wordlist.contains(termStr)){
							wordlist.add(termStr);
						}
						// enumerate through documents, in this case only one
						docsEnum = termEnum.postings(null);
						int docIdEnum;
						while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
							//get the term frequency in the document
							int tf = docsEnum.freq();
							//compute inverse document frequency
							float idf = sim.idf(termEnum.docFreq(), re.numDocs());
							termMap.put(termStr, (double) (tf * idf));

						}
						res.put(docName, termMap);
					}
				}
			}
			//}

			return res;
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
