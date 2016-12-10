
//We have copied this java File from HW 2 as it ; because we have implemented ranker comprehensive(pagerank + numviews) using Compressed.
//Pls. do not run this
//Thank you !!!


package edu.nyu.cs.cs2580;
import java.io.*;

import com.google.common.collect.HashBiMap;
import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.DocumentFull;
import edu.nyu.cs.cs2580.Indexer;
import edu.nyu.cs.cs2580.Query;
import edu.nyu.cs.cs2580.SearchEngine;
import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.*;

import edu.nyu.cs.cs2580.SearchEngine.Options;
//import edu.nyu.cs.cs2580.HTMLTextExtractor;
import edu.nyu.cs.cs2580.HTMLParser;


/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer implements Serializable {
	private static final long serialVersionUID = 1077111905740085030L;
	//htmlparsenew h = new htmlparsenew();
	HashMap<Integer, String> title_reference = new HashMap<Integer, String>();
	int doc_id = -1;
	//int ct =-1;
	public HashMap<Integer,Vector<Integer>> doctok = new HashMap<Integer, Vector<Integer>>();

	// All unique terms appeared in corpus. Offsets are integer representations.
	private Vector<String> _terms = new Vector<String>();
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();


	// Term document frequency, key is the integer representation of the term and
	// value is the number of documents the term appears in.
	public Map<Integer, Integer> _termDocFrequency =
			new HashMap<Integer, Integer>();
	// Term frequency, key is the integer representation of the term and value is
	// the number of times the term appears in the corpus.
	private Map<Integer, Integer> _termCorpusFrequency =
			new HashMap<Integer, Integer>();

	int idx=0;
	// Stores all Document in memory.
	public Vector<Document> _documents = new Vector<Document>();

	public IndexerInvertedDoconly(){}

	public IndexerInvertedDoconly(Options options) throws IOException, ClassNotFoundException{
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	@Override
	public void constructIndex() throws IOException {
		System.out.println("_options._corpusPrefix" + _options._corpusPrefix);
		System.out.println(_options);
		HTMLParser parser = new HTMLParser();
		DocumentIndexed doc = new DocumentIndexed(_documents.size());



		//final long startTime = System.currentTimeMillis();

		String myDirectoryPath = _options._corpusPrefix;;
		File dir = new File(myDirectoryPath);
		File[] directoryListing = dir.listFiles();

//    StringBuilder contents = new StringBuilder();           // This is a StringBuilder object contents which we will append to

		if (directoryListing != null) {

			for (File path : directoryListing) {
				try {
					String text = parser.createFileInput(path);// Obtains text from HTML file using execute(...)
					// System.out.println(text);
					processDocument(text);
				}
				catch (Exception e)
				{

				}
			}
			//_documents.add(doc);
			++_numDocs;
		}


		//  final long endTime = System.currentTimeMillis();
		// System.out.println("Total execution time: " + (endTime - startTime) );


		System.out.println(
				"Indexed " + _documents.size() + " docs with " +
						_totalTermFrequency + " terms.");

		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Store index to: " + indexFile);

		ObjectOutputStream writer =
				new ObjectOutputStream(new FileOutputStream(indexFile));
		writer.writeObject(this);
		System.out.println("I have written");
		writer.close();
	}

	private void processDocument(String content) throws IOException {
		doc_id=_documents.size();
		DocumentIndexed doc = new DocumentIndexed(doc_id);
		Set<Integer> uniqueTerms = new HashSet<Integer>();
		//doc_id++;
		//Set<Integer> uniqueTerms = new HashSet<Integer>();
		// Vector<Integer> bodytokens = new Vector<>();
		//updateStatistics(bodytokens, uniqueTerms);
    /*for (Integer idx : uniqueTerms) {
      _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
    }*/
		Scanner sc = new Scanner(content).useDelimiter("\t");
		String title = sc.next();
		String body = sc.next();
		String link = sc.next();

		sc=null;
		System.out.println(link);
    /*File file = new File("C:/asshole.txt");
    System.out.println("written");
    FileWriter fw = new FileWriter(file);
    PrintWriter pw = new PrintWriter(fw);
    pw.println(link);
    pw.close();*/
		//tried something new

		readTermVector(title, uniqueTerms, doc);   //reads the title and saves that in title reference
		readTermVector(body, uniqueTerms, doc);
		//Scanner s = new Scanner(body);
		title_reference.put(doc_id,title);
		Set<String> Unique = new HashSet<String>();
		//while(s.hasNext())
		//{
		//  Unique.add(s.next());
		//}
		//s=null;
		doc.setTitle(title);

		Vector<Integer> list;
		for (Integer idx : uniqueTerms) {
			// increase number of docs this term occurs in
			_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
			doc.updateDocTermFreq(idx);

			// add this doc to index
			list = doctok.get(idx);
			// dont subtract 1 here since document is added later
			list.add(_documents.size());
			doctok.put(idx, list);

		}
    /*
    for(String newUniqueTerm:Unique)
    {
      _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
      doc.updateDocTermFreq(idx);
      if(!_dictionary.containsKey(newUniqueTerm))
      {
        _terms.add(newUniqueTerm);  //added new method because of nextdoc
        _dictionary.put(newUniqueTerm,idx);
        _termCorpusFrequency.put(idx, 0);
        _termDocFrequency.put(idx, 0);
        idx++;
        Vector<Integer> indoctok = new Vector<Integer>();
        indoctok.add(doc_id);
        doctok.put(idx,indoctok);


      }
      else
      {
        doctok.get(_dictionary.get(newUniqueTerm)).add(doc_id);
      }
    } */

		_documents.add(doc);
		_numDocs++;
		//make the unnecessary terms to NULL to release the memory
		uniqueTerms=null;
		list=null;
		// s.close();

		//Unique = null;

		//System.out.println(doctok);
	}
/*  public void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques) {
    for (int idx : tokens) {
      uniques.add(idx);
      _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
      ++_totalTermFrequency;
    }
  }*/

	private void readTermVector(String content, Set<Integer> uniques, Document doc) {
		Scanner s = new Scanner(content);
		while (s.hasNext()) {

			String token = s.next();
			int idx = -1;

			// get index from the dictionary or add it
			if (_dictionary.containsKey(token)) {
				idx = _dictionary.get(token);
			} else {

				idx =_dictionary.size();
				_dictionary.put(token, idx);


				_termCorpusFrequency.put(idx, 0);
				_termDocFrequency.put(idx, 0);
				doctok.put(idx,new Vector<Integer>());
			}


			uniques.add(idx);

			((DocumentIndexed) doc).updateTermFreq(idx);

			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
			++_totalTermFrequency;

		}
		return;
	}

	@Override
	public int documentTermFrequency(String term, String url){
		return 0;
	}

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Load index from: " + indexFile);

		ObjectInputStream reader =
				new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();
		this._documents = loaded._documents;
		// Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		//our new methods in new version
		this.doctok = loaded.doctok;
		this.title_reference = loaded.title_reference;

		//our new methods in new version
		this._numDocs = _documents.size();
		for (Integer freq : loaded._termCorpusFrequency.values()) {
			this._totalTermFrequency += freq;
		}
		this._dictionary = loaded._dictionary;
		this._terms = loaded._terms;
		this._termCorpusFrequency = loaded._termCorpusFrequency;
		this._termDocFrequency = loaded._termDocFrequency;
		reader.close();

		System.out.println(this._numDocs + " documents loaded " +
				"with " + _totalTermFrequency + " terms!");




	}

	@Override
	public Document getDoc(int did) {
		return (did >= _documents.size() || did < 0) ? null : _documents.get(did);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		// System.out.println("query   : " + query._tokens);
		// System.out.println("nextDoc title_reference      : " + title_reference);
		// System.out.println("nextDoc doctok      : " + doctok);
		Vector<Integer> PostingList1 = new Vector<Integer>();
		Vector<Integer> idArray = new Vector<Integer>();
		int maxId = -1;
		int sameDocId = -1;
		boolean allQueryTermsInSameDoc = true;
		for (String term : query._tokens) {
			PostingList1 = getPostingListOfTerm(term);
			// System.out.println("Calling next for : " + term);
			idArray.add(next(term, docid));
			// System.out.println("Came out of next for : " + term);
		}
		//System.out.println("ID ARRAY : " + idArray);
		if (idArray.contains(-1)) {
			//System.out.println("----RETURNING  NULL --------------------");
			return null;
		} else if (hasSameValue(idArray)) {
			// System.out.println("hasSameValue(idArray)"+idArray.get(0));
			return _documents.get(idArray.get(0));
		} else {
			// System.out.println("nextDoc(query, getMaxValue(idArray) - 1)");
			return nextDoc(query, getMaxValue(idArray) - 1);
		}

	}
	private Vector<Integer> getPostingListOfTerm(String term) {  // Change this to vector later
		return doctok.get(_dictionary.get(term));
	}

	public int next(String queryTerm, int docid) {
		// System.out.println("INSIDE next -----");
		return binarySearchResultIndex(queryTerm, docid);
	}

	private int binarySearchResultIndex(String term, int current) {
		Vector<Integer> PostingList = getPostingListOfTerm(term);  // // Change this to vector later
		//  System.out.println(PostingList);
		if(PostingList == null)
			return -1;
		// System.out.println("getPostingListOfTerm values : " + PostingList);
		// System.out.println("PostingList.size() : " + PostingList.size());
		int lt = PostingList.size() - 1;  //removed -1 from here
		if (lt == -1 || PostingList.get(lt) <= current) {
			// System.out.println("lt == 0 || PostingList.get(lt) <= current");
			return -1;
		}
		if (PostingList.get(0) > current) {
			// System.out.println("IostingList.get(1)>current");
			// System.out.println("Returning " + PostingList.get(0));
			return PostingList.get(0);
		}
		//System.out.println("binarySearch(PostingList,1,lt,current)");
		return PostingList.get(binarySearch(PostingList, 0, lt, current));

	}


	private int binarySearch(Vector<Integer> PostingList, int low, int high, int current) { // Change this to vector later
		// System.out.println("INSIDE binarySearch -----");
		int mid;
		while (high - low > 1) {
			mid = (low + high) / 2;
			// System.out.println("low " + low + " mid " + mid + " high " + high);
			if (PostingList.get(mid) <= current) {
				low = mid;
			} else {
				high = mid;
			}
		}
		//System.out.println("BINARY Search returned  -----" + high);
		return high;

	}

	private boolean hasSameValue(Vector<Integer> idArray) {
		boolean sameValue = true;
		for (int i = 1; i < idArray.size(); i++) {
			sameValue &= idArray.get(i).equals(idArray.get(0));
		}
		return sameValue;
	}

	private int getMaxValue(Vector<Integer> idArray) {

		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < idArray.size(); i++) {
			if (max < idArray.get(i))
				max = idArray.get(i);

			if (min > idArray.get(i))
				min = idArray.get(i);
		}

		return max;
	}

	//our methods end here and prof method start here.

	public int getTerm(String term){
		return _dictionary.containsKey(term) ? _dictionary.get(term) : -1;
	}


	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _dictionary.containsKey(term) ?
				_termDocFrequency.get(_dictionary.get(term)) : 0;
	}



	@Override
	public int corpusTermFrequency(String term) {
		return _dictionary.containsKey(term) ?
				_termCorpusFrequency.get(_dictionary.get(term)) : 0;
	}


	@Override
	public double NextPhrase(Query query, int docid, int pos){
		return 1.0;
	}


	@Override
	public HashBiMap<String, Integer> getDict(){
		return null;
	}


 /* public int documentTermFrequency(String term, int docid) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }*/

  /*public void readTermVector(String content, Vector<Integer> tokens,Set<String> unique) {
    Scanner s = new Scanner(content);  // Uses white space by default.
    int pastDocId = -1;
    while (s.hasNext()) {
      String token = s.next();
       unique.add(token);
      int idx = -1;
      if(!_dictionary.containsKey(token)) {
        idx = _terms.size();
        _terms.add(token);
        _dictionary.put(token, idx);
        //_termCorpusFrequency.put(idx, 0);
        //_termDocFrequency.put(idx, 0);
      }
    }

    return;
  }
*/
  /*public void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques) {
    for (int idx : tokens) {
      uniques.add(idx);
      _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
      ++_totalTermFrequency;
    }
  }
  /* public void generatetokens(String content, Vector<String> tokens) {
     Scanner s = new Scanner(content);  // Uses white space by default.
     while (s.hasNext()) {
       String token = s.next();
       tokens.add(token);


     }*/
  /*public Vector<String> getTermVector(Vector<Integer> tokens) {
    Vector<String> retval = new Vector<String>();
    for (int idx : tokens) {
      retval.add(_terms.get(idx));
    }
    return retval;
  }*/
}