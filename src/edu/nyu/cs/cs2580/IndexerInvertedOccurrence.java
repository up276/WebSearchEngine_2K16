package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;


import com.google.common.collect.HashBiMap;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends Indexer implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1077111905740085030L;
	private Map<String, Integer> cache = new HashMap<String, Integer>();
	private Integer cached_index = -1;
	private HashMap<Integer,Vector<Integer>> token_position = new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> skip_pointer=new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> token_list=new HashMap<Integer,Vector<Integer>>();

	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	private HashMap<Integer, Vector<Integer> > doctok=new HashMap<Integer,Vector<Integer>>();
	private Vector<Document> _documents=new Vector<Document>();

	private Map<Integer, Integer> _termCorpusFrequency =
			new HashMap<Integer, Integer>();
	private Map<Integer, Integer> _termDocFrequency =
			new HashMap<Integer, Integer>();

	public IndexerInvertedOccurrence(Options options) throws IOException, ClassNotFoundException{
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	public void constructIndex() throws IOException {
		//System.out.println("_options._corpusPrefix" + _options._corpusPrefix);
		//System.out.println(_options);
		HTMLParser parse = new HTMLParser();

		String corpusDir = _options._corpusPrefix;
		System.out.println("Constructing index documents in: " + corpusDir);
//    StringBuilder contents = new StringBuilder();           // This is a StringBuilder object contents which we will append to

		final File Dir = new File(corpusDir);
		int n_doc = 0;
		System.out.println("STARTING INDEXING .... ");
		for (final File fileEntry : Dir.listFiles()) {
			if ( !fileEntry.isDirectory() ) {


				try {
					// String text = parser.getConvertedDocument(path);// Obtains text from HTML file using execute(...)
					// System.out.println(text);
					// System.out.println(" DOCUMENT PROCESSED --> " + fileEntry.getName());
					//  n_doc++;

					String nextDoc = parse.createFileInput(fileEntry);
					processDocument(nextDoc);


					token_position.clear();
				} catch(Exception E){}

				//}
			}
		}
		parse = null;


		// creating new vectors
		Vector<Integer> list = new Vector<Integer>();
		Vector<Integer> skip = new Vector<Integer>();
		Vector<Integer> posting = new Vector<Integer>();

		System.out.println(
				"Indexed " + Integer.toString(_numDocs) + " docs with " +
						Long.toString(_totalTermFrequency) + " terms.");

		String indexFile = _options._indexPrefix + "/occurences_corpus.idx";
		System.out.println("Store index to: " + indexFile);
		ObjectOutputStream writer =
				new ObjectOutputStream(new FileOutputStream(indexFile));


		for(int i : _dictionary.values()) {
			// get the term position list and skip pointer
			list =token_list.get(i);
			skip = skip_pointer.get(i);

			// create final posting for term
			posting = next_skip_pointer(skip,skip.size());
			posting.addAll(list);
			doctok.put(i, posting);

		}


		// Empty used space
		indexFile=null;
		list=null;
		skip=null;
		_options=null;
		token_list=null;
		skip_pointer=null;
		token_position=null;
		corpusDir=null;
		parse=null;



		writer.writeObject(this);
		writer.close();

	}



	private void processDocument(String content) {
		// TODO Auto-generated method stub

		Scanner s = new Scanner(content).useDelimiter("\t");
		Set<Integer> uniqueTerms = new HashSet<Integer>();

		// pass the title
		String title = s.next();
		readTermVector(title, uniqueTerms);

		// pass the body
		readTermVector(s.next(), uniqueTerms);

		// get number of views
		int numViews = Integer.parseInt(s.next());

		String url = s.next();

		s=null;

		// create the document
		DocumentIndexed doc = new DocumentIndexed(_documents.size());
		doc.setTitle(title);
		doc.setNumViews(numViews);
		doc.setUrl(url);
		((DocumentIndexed) doc).removeAll();

		// add the document
		_documents.add(doc);
		_numDocs++;

		// create postings lists and skip pointers
		Vector<Integer> positions=new Vector<Integer>();
		Vector<Integer> list=new Vector<Integer>();
		Vector<Integer> skip=new Vector<Integer>();
		for (Integer idx : uniqueTerms) {
			// increase number of docs this term appears in
			_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);

			// get the vectors
			skip = skip_pointer.get(idx);
			list = token_list.get(idx);
			positions = token_position.get(idx);

			// add document ID
			list.add(_documents.size()-1);
			// add number of occurrences
			list.add(positions.size());
			// add all the positions in the document
			list.addAll(positions);

			// add document ID
			skip.add(_documents.size()-1);
			// add how far to skip to the last element of this documents list
			skip.add(list.size()-1);

			// set it
			skip_pointer.put(idx, skip);
			token_list.put(idx, list);

		}

	}

	private void readTermVector(String content, Set <Integer> uniques) {
		Scanner s = new Scanner(content);  // Uses white space by default.
		int pos = 0;
		Vector<Integer> positions = new Vector<Integer>();
		while (s.hasNext()) {

			String token = s.next();
			int idx = -1;

			// get index from the dictionary or add it
			if (!_dictionary.containsKey(token)) {
				idx = _dictionary.size();
				_dictionary.put(token, idx);
 /*for(Integer i:title_reference.keySet())
    {
      System.out.println("Key "+ i + "value  "+ title_refere

      nce.get(i));
    }*/
   /* for(Integer i:doctok.keySet())
    {
      ArrayList<Integer> a=new ArrayList<Integer>();
      a= doctok.get(i);
      System.out.println("key "+ i );
      for(int j = 0; j < a.size(); j++) {
        System.out.print(a.get(j)+" ");
      }
    }*/
				_termCorpusFrequency.put(idx, 0);

				//increase term doc freq
				_termDocFrequency.put(idx, 0);

				// create these things for new word
				skip_pointer.put(idx, new Vector<Integer>());
				token_list.put(idx, new Vector<Integer>());
				token_position.put(idx, new Vector<Integer>());

				// add the whole list into doctoc main hash map
				doctok.put(idx,new Vector<Integer>());

			} else {
				idx = _dictionary.get(token);
			}

			// make sure term is in term_position
			if (!token_position.containsKey(idx)) {
				token_position.put(idx, new Vector<Integer>() );
			}


			// adding positions
			positions = token_position.get(idx);
			positions.add(pos);
			token_position.put(idx, positions);
			uniques.add(idx);
			// updating stats
			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
			++_totalTermFrequency;

			pos++;
		}
		return;
	}


	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {

		// Load index file from stored index
		String indexFile = _options._indexPrefix + "/occurences_corpus.idx";
		System.out.println("Load index from: " + indexFile);

		// read in the index file
		ObjectInputStream reader =
				new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedOccurrence loaded =
				(IndexerInvertedOccurrence) reader.readObject();

		this._documents = loaded._documents;
		this._numDocs = _documents.size();

		//	Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		for (Integer freq : loaded._termCorpusFrequency.values())
			this._totalTermFrequency += freq;

		this.doctok = loaded.doctok;
		this._dictionary = loaded._dictionary;
		this._termCorpusFrequency = loaded._termCorpusFrequency;
		this._termDocFrequency = loaded._termDocFrequency;
		reader.close();
		loaded=null;
		System.out.println(Integer.toString(_numDocs) + " documents loaded " +
				"with " + Long.toString(_totalTermFrequency) + " terms!");

	}

	@Override
	public Document getDoc(int docid) {
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	private Vector<Integer> next_skip_pointer(Vector<Integer> skip, int size) {
		// TODO Auto-generated method stub

		// add skip list size to each index per document
		for(int i = 0; i < skip.size(); i++) {
			if(i % 2 != 0) {
				skip.set(i, skip.get(i) + size);
			}
		}
		return skip;
	}
	private Double next(String t, Integer current) {

		// get the postings list
		Vector<Integer> Pt = new Vector<Integer>();
		Pt = doctok.get(_dictionary.get(t));

		// get index of last doc
		int lt = get_last(Pt);
		// done if already returned the last one
		if(lt == -1 || Pt.get(lt) <= current)
			return Double.POSITIVE_INFINITY;

		// first time return the first doc
		if(Pt.get(0) > current) {
			cached_index = 0;
			return 1.0 * Pt.get(cached_index);
		}

		// go back
		if(cached_index > 0 && Pt.get(cached_index-2) <= current)
			cached_index = 0;

		// go find next doc
		while (Pt.get(cached_index) <= current && cached_index < lt)
			cached_index = cached_index+2;

		// return the docid
		return 1.0*Pt.get(cached_index);
	}

	@Override
	public Document nextDoc(Query query, int docid) {

		Vector<Double> docids = new Vector<Double>(query._tokens.size());

		// get next doc for each term in query
		for(int i = 0; i < query._tokens.size(); i++) {

			String token = query._tokens.get(i);
			cached_index = cache.containsKey(token) ? cache.get(token) : -1;

			docids.add(i, next(query._tokens.get(i), docid) );

			cache.put(token, cached_index);

			if(docids.get(i) == Double.POSITIVE_INFINITY)
				return null;
		}

		// found the next one
		if(Collections.max(docids) == Collections.min(docids))
			return _documents.get(docids.get(0).intValue());

		// not a match, run again
		return nextDoc(query, Collections.max(docids).intValue()-1);

	}

	// return index that of last doc in skip pointer list
	private int get_last(Vector<Integer> pt) {
		// TODO Auto-generated method stub
		int size1 = pt.size();
		int i = 0;

		// return -1 if no doc present
		if(pt.size()==0)
			return -1;

		while(true) {
			// check if the skip pointer goes to end of posting list
			if(pt.get(i+1) == size1-1)
				return i;
			// go to next dox
			i=i+2;
		}
	}


	private int get_doc_start(Vector<Integer> pt, int docid) {

		int lt = get_last(pt);
		if (lt == -1)
			return -1;

		int cur_doc = -1;
		int i = 0;
		// find the doc id in skip pointer list
		while (i <= lt) {
			cur_doc = pt.get(i);
			// did not find, continue
			if (cur_doc != docid) {
				i += 2;
			} else {
				// if it was the first doc
				// skip over ptr_indx, docid, num_occ
				if (i == 0)
					return lt + 4;
				else
					// go to prev doc ptr, jump, then skip docid, num_occ
					return pt.get(i - 1) + 3;
			}
		}
		return -1;
	}
	//finds the end of the document
	private int get_doc_end(Vector<Integer> pt, int docid) {

		int lt = get_last(pt);
		if (lt == -1) {
			return -1;
		}

		int cur_doc = -1;
		int i = 0;
		// find the doc id in skip pointer list
		while (i <= lt) {
			cur_doc = pt.get(i);
			// did not find, continue
			if (cur_doc != docid) {
				i += 2;
			} else {

				return pt.get(i+1);
			}
		}


		return -1;
	}



	/*

     private Double next_pos(String token, int docid, int pos) {
       Vector<Integer> Pt = doctok.get(_dictionary.get(token));

       // end of occurrence list for doc
       int indx_end = get_doc_end(Pt, docid);
       // if cur position is at or past the last occurence, no more possible phrases
       if( indx_end == -1 || Pt.get(indx_end) < pos)
         return Double.POSITIVE_INFINITY;

       // get the index of the first position
       int indx_start = get_doc_start(Pt, docid);
       // first time called return the first occurrence
       if (Pt.get(indx_start) > pos)
         return 1.0 * Pt.get(indx_start);

       // iterate through position list until you pass current position
       int i = indx_start;
       for(; Pt.get(i) < pos; i++);

       // return that next position
       return 1.0 * Pt.get(i);

     }*/
 /*

  public Double first_pos (String token, int docid) {

    Vector<Integer> Pt=doctok.get(_dictionary.get(token));

    int lt=get_last(Pt);


    if(lt==-1 || Pt.get(lt)<docid)
      return Double.POSITIVE_INFINITY;

    int i;
    boolean found=false;

    for(i=0;i<=lt;i=i+2)
    {
      if(Pt.get(i)==docid)
      {
        found=true;
        break;
      }
    }

    if(found)
    {
      System.out.println(i);
      i--;
      int start_idx= Pt.get(i)+1;
      if(Pt.get(start_idx)!=docid)
      {
        System.out.println("WRONG LOGIC");
      }
      start_idx=start_idx+2;
      return (double) Pt.get(start_idx);

    }
    else
      return null;

  }

*/
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
	public int documentTermFrequency(String term, String did) {
		int docid = Integer.parseInt(did);
		if (_dictionary.containsKey(term)) {
			Vector<Integer> Pt = doctok.get( _dictionary.get(term) );
			int positions_indx = get_doc_start(Pt, docid);
			return positions_indx != -1 ? Pt.get(positions_indx - 1) : 0;
		}
		return 0;
	}

	@Override
	public  HashBiMap<String, Integer> getDict()
	{
		return null;
	}

@Override
public double NextPhrase(Query query, int docid, int pos)
{
	return 1.0;
}


}