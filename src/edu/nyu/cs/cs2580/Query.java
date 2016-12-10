package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public Vector<String> _tokens = new Vector<String>();
  public HTMLParser Cleaner = new HTMLParser();
  public String _raw;
  //public StringBuilder raw1;

  public Query(String query) {
    _query = query;
    _raw=query;
    String charsToDel = "`~!@#$%^&*()_-+=[]\\{}|;':\",./<>?";
    _raw = _raw.replaceAll("[" + Pattern.quote(charsToDel)  + "]", " ").trim();
    //_raw = _raw.replaceAll()

  }
  
  public String getQuery()
    {
	return _query;
    }
    
  public void processQuery() {
    if (_query == null) {
      return;
    }
    _query=_query.replace('+', ' ');
    // **** clean and stem query ****
    try {
	_query = Cleaner.apply_porter(_query);
    }
    catch (IOException e) {
	System.err.println("Could not clean query: " + e.getMessage());
    }
    // **************
    Scanner s = new Scanner(_query);
    System.out.println(_query);
    while (s.hasNext()) {
     // _tokens.add(s.next());
      String query_part = s.next();
      query_part = query_part.toLowerCase();
      String charsToDel = "`~!@#$%^&*()_-+=[]\\{}|;':\",./<>?";
      query_part = query_part.replaceAll("[" + Pattern.quote(charsToDel)  + "]", " ").trim();
      _tokens.add(query_part);
    }
    s.close();
  }
}
