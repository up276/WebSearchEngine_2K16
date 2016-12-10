package edu.nyu.cs.cs2580;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.util.Comparator;
import java.util.Collections;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import com.google.common.collect.HashBiMap;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable {

	private static final long serialVersionUID = 1626440145434710491L;

	//public HashMap<Integer, BitSet > doctokhash=new HashMap<Integer,BitSet>();
	//public Vector<Document> _documentsvector=new Vector<Document>();
	private HashMap<Integer, Integer> coded_map = new HashMap<Integer,Integer>();
	private HashMap<Integer, BitSet > doctok=new HashMap<Integer,BitSet>();
	private HashBiMap<String, Integer> _dictionary = HashBiMap.create();
	private Map<String, Vector<Integer>> _decoded = new HashMap<String, Vector<Integer>>();


	// Stores all Document in memory.
	private Vector<Document> _documents=new Vector<Document>();

	// Term frequency, key is the integer representation of the term and value is
	// the number of times the term appears in the corpus.
	private Map<Integer, Integer> _termCorpusFrequency =
			new HashMap<Integer, Integer>();

	// Term document frequency, key is the integer representation of the term and
	// value is the number of documents the term appears in.
	private Map<Integer, Integer> _termDocFrequency =
			new HashMap<Integer, Integer>();
	private Map<String, Integer> cache = new HashMap<String, Integer>();
	private Integer c_t = -1;
	private HashMap<Integer,Vector<Integer>> token_pos = new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> _skip_pointer=new HashMap<Integer,Vector<Integer>>();
	private HashMap<Integer,Vector<Integer>> _term_list=new HashMap<Integer,Vector<Integer>>();

	private StopWords _StopWords = null;


	public IndexerInvertedCompressed(Options options) throws IOException, ClassNotFoundException {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
		_StopWords = new StopWords(options);
	}

	@Override
	public void constructIndex() throws IOException {


		// System.out.println("inside construct index srsrdgd");
		// System.out.println("inside construct index efbwebvkbevb");
		// System.out.println("options prefix"+_options._corpusPrefix);
		HTMLParser HTMLObj = new HTMLParser();

		File pathToCorpus = new File(_options._corpusPrefix);
		System.out.println("Constructing index documents in: " + pathToCorpus.toString());



		for (File file : pathToCorpus.listFiles()) {
			try{
				if ( !file.isDirectory() && !file.isHidden()) {

					String fileContent = HTMLObj.createFileInput(file);
					//CorpusAnalyzer.HeuristicLinkExtractor f = new CorpusAnalyzer.HeuristicLinkExtractor(file);
					// CorpusAnalyzer.HeuristicLinkExtractor link = new CorpusAnalyzer.HeuristicLinkExtractor(file);
					//now we need the name of the file
					//CorpusAnalyzer.HeuristicLinkExtractor link = new CorpusAnalyzer.HeuristicLinkExtractor(file);
					//String file_name = link.getLinkSource().toLowerCase();

					//System.out.println(file.getName());
					processDocument(fileContent);
					token_pos.clear();

					//    token_pos.clear();


				}


			}
			catch(Exception e){ System.out.println("There was a problem. "+e.toString()); }
		}

		HTMLObj = null;
		pathToCorpus = null;



		System.out.println(
				"Indexed " + Integer.toString(_numDocs) + " documents with " +
						Long.toString(_totalTermFrequency) + " terms.");

		String indexFile = _options._indexPrefix +"/corpus.idx";
		System.out.println("Storing index to: " + indexFile);

		//System.out.println("Hello I am now here");



		// temporary vectors
		Vector<Integer> list = new Vector<Integer>();
		Vector<Integer> skip = new Vector<Integer>();
		Vector<Integer> posting = new Vector<Integer>();

		BitSet bits=new BitSet();

		System.out.println(_dictionary.size());

		for(int i : _dictionary.values()) {
			// get the term position list and skip pointer
			list =_term_list.get(i);
			skip = _skip_pointer.get(i);

			// create final posting for term
			posting = update_skip(skip,skip.size());
			posting.addAll(list);

			bits=coded_map_encode(posting,i);

			doctok.put(i, bits);


			bits=new BitSet();

		}



		_term_list=null;
		_skip_pointer=null;
		token_pos=null;
		_StopWords = null;

		try{

			ObjectOutputStream writer =
					new ObjectOutputStream(new FileOutputStream(indexFile));
			writer.writeObject(this);
			writer.close();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}


	}

	private BitSet coded_map_encode(Vector<Integer> posting, int i2) {

		BitSet b_bitset=new BitSet();
		int d_val;
		int r_val;

		int symbol;

		int idx_val=0;

		for(int i=0;i<posting.size();i++)
		{

			symbol=posting.get(i);
			symbol++;

			d_val= (int) (Math.log(symbol)/Math.log(2));

			r_val= (int) (symbol - Math.pow(2, d_val));

			b_bitset.set(idx_val, idx_val+d_val);

			idx_val=idx_val+d_val;



			b_bitset.set(idx_val, false);

			idx_val=idx_val+1;
			for(int j=d_val-1;j>=0;j--)
			{

				if(r_val-Math.pow(2, j)>=0)
				{
					b_bitset.set(idx_val);
					idx_val=idx_val+1;
					r_val=(int) (r_val-Math.pow(2, j));
				}
				else
				{
					b_bitset.set(idx_val,false);

					idx_val=idx_val+1;

				}
			}
		}

		coded_map.put(i2, idx_val);

		return b_bitset;
	}


	private Tuple<BitSet, Integer> coded_map_encode(Vector<Integer> posting) {
		// TODO Auto-generated method stub

		BitSet b_bitset=new BitSet();
		int symbol;
		int d_val;
		int r_val;
		int idx_val=0;

		for(int i=0;i<posting.size();i++)
		{

			symbol=posting.get(i);
			symbol++;

			d_val= (int) (Math.log(symbol)/Math.log(2));
			r_val= (int) (symbol - Math.pow(2, d_val));

			b_bitset.set(idx_val, idx_val+d_val);

			idx_val=idx_val+d_val;
			b_bitset.set(idx_val, false);

			idx_val=idx_val+1;

			for(int j=d_val-1;j>=0;j--)
			{

				if(r_val-Math.pow(2, j)>=0)
				{
					b_bitset.set(idx_val);
					idx_val=idx_val+1;
					r_val=(int) (r_val-Math.pow(2, j));
				}
				else
				{
					b_bitset.set(idx_val,false);

					idx_val=idx_val+1;

				}
			}
		}

		return new Tuple<BitSet, Integer>(b_bitset, idx_val);
	}


	private Vector<Integer> update_skip(Vector<Integer> skip, int size) {


		// add skip list size to each index per document
		for(int i = 0; i < skip.size(); i++) {
			if(i % 2 != 0) {
				skip.set(i, skip.get(i) + size);
			}
		}
		return skip;
	}


	private void processDocument(String content) {

		//IndexerFullScan ifs = new IndexerFullScan();
		Scanner s = new Scanner(content).useDelimiter("\t");
		Set<Integer> uniqueTerms = new HashSet<Integer>();
		HashMap<Integer, Integer> document_tf = new HashMap<Integer, Integer>();

		// pass the title
		String title = s.next();

		// pass the body
		document_tf = readTermVector(s.next(), uniqueTerms);
		Tuple<BitSet, Integer> doctf_bits = convertToBitSet(document_tf, _options._keepTerms);

		Integer.parseInt(s.next());
		// System.out.println("body" + body);
		//String url = s.next();

		s.close();


		// create the document
		DocumentIndexed doc = new DocumentIndexed(_documents.size());
		doc.setTitle(title);
		doc.setNumViews( ( (LogMinerNumviews)_logMiner).getNumviews(title.toLowerCase()));
		doc.setPageRank( ( (CorpusAnalyzerPagerank )_corpusAnalyzer).getPagerank(title.toLowerCase()));

		doc.saveTopWords(doctf_bits.getFirst(), doctf_bits.getSecond());
		((DocumentIndexed) doc).removeAll();
		// add the document
		_documents.add(doc);
		_numDocs++;
		//while(s.hasNext())
		//{
		//System.out.println("Next Element" + body);
		//}
		// create postings lists and skip pointers
		Vector<Integer> positions=new Vector<Integer>();
		Vector<Integer> list=new Vector<Integer>();
		Vector<Integer> skip=new Vector<Integer>();
		for (Integer idx : uniqueTerms) {
			// goes over each and every body token
			// increase number of docs this term appears in
			_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);

			// get the vectors
			skip = _skip_pointer.get(idx);
			list = _term_list.get(idx);
			positions = token_pos.get(idx);

			// System.out.println(_dictionary.get(idx));
			//  positions=delta_encode(positions,idx);

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
			_skip_pointer.put(idx, skip);
			_term_list.put(idx, list);

		}

	}

	private HashMap<Integer, Integer> readTermVector(String content, Set <Integer> uniques) {
		Scanner s = new Scanner(content);  // Uses white space by default.
		int pos = -1;
		Vector<Integer> positions = new Vector<Integer>();
		HashMap<Integer, Integer> document_tf = new HashMap<Integer, Integer>();

		while (s.hasNext()) {

			String token = s.next();
			pos++;
			int idx = -1;


         /*

         while (s.hasNext()) {
      String token = s.next();
      int idx = -1;
      if (_dictionary.containsKey(token)) {
        idx = _dictionary.get(token);
      } else {
        idx = _terms.size();
        _terms.add(token);
        _dictionary.put(token, idx);
        _termCorpusFrequency.put(idx, 0);
        _termDocFrequency.put(idx, 0);
      }
      tokens.add(idx);
    }
          */


			// get index from the dictionary or add it
			if (!_dictionary.containsKey(token)) {
				idx = _dictionary.size();
				_dictionary.put(token, idx);

				_termCorpusFrequency.put(idx, 0);
				_termDocFrequency.put(idx, 0);

				// create these things for new word
				_skip_pointer.put(idx, new Vector<Integer>());
				_term_list.put(idx, new Vector<Integer>());
				token_pos.put(idx, new Vector<Integer>());
				doctok.put(idx,new BitSet());

				// doc_prev_val.replace(bodyTokens.elementAt(i),index);

				// new1.get(doc_id).add(index-sum);
				// ArrayList<Integer> arr = new ArrayList<Integer>();
				// arr.add(index-index1);
				//System.out.print(index1 + "/t");

				// new1.replace(doc_id,arr);

			} else {
				idx = _dictionary.get(token);
			}

			// make sure term is in term_position
			if (!token_pos.containsKey(idx)) {
				token_pos.put(idx, new Vector<Integer>() );
			}


			// add position of the term
			positions = token_pos.get(idx);
			positions.add(pos);
			token_pos.put(idx, positions);

			// add term to the unique set
			uniques.add(idx);
			//new1.put(doc_id,new2);
			// docid1=doc_id;

			// update stats
			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
			if(!_StopWords.contains(token)) {
				if (!document_tf.containsKey(idx))
					document_tf.put(idx, 0);
				document_tf.put(idx, document_tf.get(idx) + 1);
			}
			++_totalTermFrequency;

		}
		s.close();
		return document_tf;
	}


	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {

		String indexFile = _options._indexPrefix+"/corpus.idx";
		System.out.println("Load index from: " + indexFile);

		// read in the index file
		ObjectInputStream reader =
				new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedCompressed loaded =
				(IndexerInvertedCompressed) reader.readObject();

		this._documents = loaded._documents;
		this._numDocs = _documents.size();

		// Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		for (Integer freq : loaded._termCorpusFrequency.values())
			this._totalTermFrequency += freq;




		this.doctok = loaded.doctok;
		this._dictionary = loaded._dictionary;
		this._termCorpusFrequency = loaded._termCorpusFrequency;
		this._termDocFrequency = loaded._termDocFrequency;
		this.coded_map=loaded.coded_map;
		//System.out.println("pppp");
		this._corpusAnalyzer=loaded._corpusAnalyzer;
		//_corpusAnalyzer.load();
		this._logMiner=loaded._logMiner;
		//_logMiner.load();


		reader.close();
		loaded=null;
		//System.out.println(Integer.toString(_numDocs) + " documents loaded " + "with " + Long.toString(_totalTermFrequency) + " terms!");

	}


	private Tuple<BitSet, Integer> convertToBitSet(HashMap<Integer, Integer> doc_tf, int nterms) {

		Vector<Integer> word_counts = new Vector<Integer>();

		ArrayList<Tuple<Integer, Integer>> tuples = new ArrayList<Tuple<Integer, Integer>>();
		for (Integer word : doc_tf.keySet())
			tuples.add(new Tuple<Integer, Integer>( word, doc_tf.get(word)));

		Comparator< Tuple<Integer, Integer>> comparator = new Comparator<Tuple<Integer, Integer>>() {
			public int compare(Tuple<Integer, Integer> tupleA, Tuple<Integer, Integer> tupleB) {
				// tupleB then tuple A to do descending order
				return tupleB.getSecond().compareTo(tupleA.getSecond());
			}
		};
		Collections.sort(tuples, comparator);

		int count = 0;
		for(Tuple<Integer, Integer> tuple : tuples) {
			word_counts.add(tuple.getFirst());
			word_counts.add(tuple.getSecond());
			count++;
			if(count == nterms) break;
		}

		return coded_map_encode(word_counts);

	}

	public HashBiMap<String, Integer> getDict(){
		return _dictionary;
	}


	@Override
	public Document getDoc(int docid) {
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */

	private Double next(String t, Integer current) {

		// get the postings list



		Vector<Integer> Pt=_decoded.get(t);




		// get index of last doc
		int lt = get_lt(Pt);
		// done if already returned the last one
		if(lt == -1 || Pt.get(lt) <= current)
			return Double.POSITIVE_INFINITY;

		// first time return the first doc
		if(Pt.get(0) > current) {
			c_t = 0;
			return 1.0 * Pt.get(c_t);
		}

		// go back
		if(c_t > 0 && Pt.get(c_t-2) <= current)
			c_t = 0;

		// go find next doc
		while (Pt.get(c_t) <= current && c_t < lt)
			c_t = c_t+2;

		// return the docid
		return 1.0*Pt.get(c_t);
	}

	@Override
	public Document nextDoc(Query query, int docid) {


		Vector<Double> docids = new Vector<Double>(query._tokens.size());


		for(int i=0;i<query._tokens.size();i++)
		{

			if(!_decoded.containsKey(query._tokens.get(i)))
			{


				_decoded.put(query._tokens.get(i), coded_map_decode(doctok.get(_dictionary.get(query._tokens.get(i))), query._tokens.get(i)));

			}
		}


		// get next doc for each term in query
		for(int i = 0; i < query._tokens.size(); i++) {

			String token = query._tokens.get(i);
			c_t = cache.containsKey(token) ? cache.get(token) : -1;

			docids.add(i, next(query._tokens.get(i), docid) );

			cache.put(token, c_t);

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
	private int get_lt(Vector<Integer> pt) {
		// TODO Auto-generated method stub
		int sz = pt.size();
		int i = 0;

		// return -1 if no doc present
		if(pt.size()==0)
			return -1;

		while(true) {
			// check if the skip pointer goes to end of posting list
			if(pt.get(i+1) == sz-1)
				return i;
			// go to next dox
			i=i+2;
		}
	}


	private int get_doc_start(Vector<Integer> pt, int docid) {

		int lt = get_lt(pt);
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

	private int get_doc_end(Vector<Integer> pt, int docid) {

		int lt = get_lt(pt);
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
				// found, return end position of the occurrences list for that doc
				return pt.get(i+1);
			}
		}

		// you shouldnt ever get here
		return -1;
	}



	private Double next_pos(String token, int docid, int pos) {
		// TODO Auto-generated method stub

		Vector<Integer> Pt=_decoded.get(token);

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
		for(; Pt.get(i) <= pos; i++);

		// return that next position
		return 1.0 * Pt.get(i);

	}

	@Override
	public double NextPhrase(Query query, int docid, int pos) {

		// System.out.println(query._tokens);
		// doing what the psuedo code says to do
		Document doc = nextDoc(query, docid-1);
		int doc_verify = doc._docid;
		if(doc_verify!=docid)
			return Double.POSITIVE_INFINITY;

		// get the position of each query term in doc
		Vector<Double> pos_vec = new Vector<Double>(query._tokens.size());
		for(int i = 0; i<query._tokens.size(); i++) {

			//
			Double it = next_pos(query._tokens.get(i), docid, pos);
			pos_vec.add(i, it);
			// System.out.println(query._tokens.get(i) + " "+ pos_vec.get(i));
			if(pos_vec.get(i) == Double.POSITIVE_INFINITY)
				return Double.POSITIVE_INFINITY;
		}

		int incr = 1;
		for(int j = 0; j < pos_vec.size() - 1; j++) {
			if(pos_vec.get(j)+1 == pos_vec.get(j+1))
				incr++;
		}

		if(incr == pos_vec.size())
		{
			System.out.println("Query:" + query._tokens);
			System.out.println("Positions: " + pos_vec);
			return pos_vec.get(0);
		}

		int next_p=Collections.max(pos_vec).intValue()-1;

		return NextPhrase(query, docid,next_p );

	}


	private Vector<Integer> coded_map_decode(BitSet b, String t) {
		// TODO Auto-generated method stub

		int start=0;
		int end=0;
		int first_zero;
		int d;
		int rem = 0;
		int k;

		Vector<Integer> pt=new Vector<Integer>();
		BitSet r;



		while(end<coded_map.get(_dictionary.get(t))-1)
		{
			rem=0;
			first_zero=b.nextClearBit(start);

			end=first_zero+first_zero-start;

			d=(int) Math.pow(2, first_zero-start);

			if(d==1)
				rem=0;

			else
			{

				r=new BitSet();
				r=b.get(first_zero+1, end+1);

				int r_size=end-first_zero;


				for(int n=0;n<r_size;n++)
				{
					int myInt = (r.get(n)) ? 1 : 0;

					rem=(int) (rem+ Math.pow(2, (r_size-1)-n) * myInt);
				}
			}
			k=rem+d;
			k--;
			pt.add(k);
			start=end+1;

		}


		return pt;
	}




	@Override
	public int corpusTermFrequency(String term) {
		return _dictionary.containsKey(term) ?
				_termCorpusFrequency.get(_dictionary.get(term)) : 0;
	}


	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _dictionary.containsKey(term) ?
				_termDocFrequency.get(_dictionary.get(term)) : 0;
	}

	@Override
	public int documentTermFrequency(String term, String did) {
		int docid = Integer.parseInt(did);
		if (_dictionary.containsKey(term)) {
			Vector<Integer> Pt = _decoded.get(term);
			// index for positions of term in the doc
			int positions_indx = get_doc_start(Pt, docid);
			return positions_indx != -1 ? Pt.get(positions_indx - 1) : 0;
		}
		return 0;
	}


	public static class Tuple<T, R> {
		private T first;
		private R second;
		public Tuple(T t_val, R r_val) {
			this.first = t_val;
			this.second = r_val;
		}

		public T getFirst() {
			return first;
		}
		public R getSecond() {
			return second;
		}

	}


}