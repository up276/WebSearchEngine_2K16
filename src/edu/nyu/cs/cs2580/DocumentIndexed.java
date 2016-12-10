package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.HashMap;
import java.io.Serializable;
import java.util.BitSet;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document implements Serializable {
    private static final long serialVersionUID = 9184892508124423115L;
    
    private static HashMap <Integer, Integer> _df = new HashMap<Integer,Integer>();
    private HashMap <Integer, Integer> _doc_tf = new HashMap<Integer,Integer>();
    private HashMap <Integer, Integer> _doc_tfidf = new HashMap<Integer,Integer>();
    private BitSet _top_words = new BitSet();
    private Integer _bit_index = null;
    
    public DocumentIndexed(int docid) {
	super(docid);
    }

    public void updateDocTermFreq(int idx) {
	if (_df.containsKey(idx)) {
	    int old = _df.get(idx);
	    _df.put(idx, old + 1);
	} else {
	    _df.put(idx, 1);
	}

	return;
    }

    public void updateTermFreq(int idx) {
	if (_doc_tf.containsKey(idx)) {
	    int old = _doc_tf.get(idx);
	    _doc_tf.put(idx, old + 1);
	} else {
	    _doc_tf.put(idx, 1);
	}

	return;
    }

    public void saveTopWords(BitSet word_counts, Integer bit_position){
    	_top_words = word_counts;
    	_bit_index = bit_position;
    }


    public void createTFIDF(int num_docs) {
	double total = 0.0;
      
	// Calculate tf*idf 
	for( int key : _doc_tf.keySet() )
	    {
		int tf = _doc_tf.get( key );
		int df = _df.get( key );
		      
		double idf = ( 1 + Math.log( (double) num_docs/df ) / Math.log(2) );
		Double tfidf = tf * idf * 1000;
		_doc_tfidf.put( key, tfidf.intValue() );
		total += tf * tf * idf * idf;

	    }
      
	//Normalize
	for( Integer key : _doc_tf.keySet() )
	    {
	    Double temp_tfidf = _doc_tfidf.get( key ) * 1.0;
	    Integer final_tfidf = ( (Double) (temp_tfidf / Math.sqrt(total)) ).intValue();
		_doc_tfidf.put( key, final_tfidf );
	    }
	_doc_tf = null;
	
	return;
    }

    public void removeDF(){
	_df = null;
	return;
    }

    public void removeAll(){
	_df = null;
	_doc_tf = null;
	_doc_tfidf = null;
	return;
    }

    public Double getTFIDF(Integer idx) {
	return _doc_tfidf.containsKey(idx) ? _doc_tfidf.get(idx) / 10.0 : 0.0;
    }


    public HashMap<Integer, Integer> getTopWords(int m){
    	HashMap<Integer, Integer> top_words = new HashMap<Integer, Integer>();
    	Vector<Integer> word_counts = Elias_decode(_top_words);
    	for(int i = 0; i < m && i < word_counts.size(); i = i + 2)
    		top_words.put(word_counts.get(i), word_counts.get(i + 1));
    	return top_words;
    }



	private Vector<Integer> Elias_decode(BitSet b) {
		 int start = 0;
		 int end = 0;
		 int first_zero;
		 int d;
		 int rem = 0;
		 int k;
		 
		 Vector<Integer> pt = new Vector<Integer>();
		 BitSet r;
		
		while( end < _bit_index - 1) {
			rem = 0;
			first_zero = b.nextClearBit(start);
	   		
			end = first_zero + first_zero - start;
			
	   		d = (int) Math.pow(2, first_zero - start);
	   		
	   		if( d == 1 )
	   			rem = 0;
	   		else {
		   		r = new BitSet();
			   	r = b.get(first_zero + 1, end + 1);
			   	
			   	int r_size = end - first_zero;
			   	
		   		for(int n = 0; n < r_size; n++)
		   		{
		   			int myInt = (r.get(n)) ? 1 : 0;
		   			rem = (int) (rem + Math.pow(2, (r_size-1) - n) * myInt);
		   		}
	   		}

	   		k = rem + d;
	   		k--;
	   		pt.add(k);
	   		start = end+1;
		}
		return pt;
	}

}


