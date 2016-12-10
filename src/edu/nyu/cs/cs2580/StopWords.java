package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.io.Serializable;

import edu.nyu.cs.cs2580.SearchEngine.Options;

public class StopWords implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6420210487534560806L;
	private HashSet<String> _stopwords = new HashSet<String>();

	public StopWords(Options options) throws IOException {

		HTMLParser PorterStemmer = new HTMLParser();
		BufferedReader br = new BufferedReader(new FileReader(options._stopWordsList));
		String line;
		while ((line = br.readLine()) != null) 
   			_stopwords.add( PorterStemmer.apply_porter(line.trim()) );
		br.close();
	}

	public Boolean contains(String word) {
		return _stopwords.contains(word);
	}

}