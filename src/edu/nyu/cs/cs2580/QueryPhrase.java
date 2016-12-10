package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
	
	public Vector<Query> phrase = new Vector<Query>();

  public QueryPhrase(String query) {
    super(query);
  }

  @Override
  public void processQuery() {
	  
	  if (_query == null) {
	      return;
	    }
  	
  	
  String q=_query;
  q=q.replace('+', ' ');
  String ph;
    System.out.println("query string :"+q);
  
  Pattern p = Pattern.compile("\"([^\"]*)\"");
  Matcher m = p.matcher(q);
  
  q=q.replace("\"", " ");
  while (m.find()) {
  	
  	
    ph=m.group(1);
    Query q_query=new Query(ph);
    q_query.processQuery();
    System.out.println("query processed:"+q_query._query);
    
    phrase.add(q_query);
  }
  
    // **** clean and stem query ****
    try {
	q = Cleaner.apply_porter(q);
    }
    catch (IOException e) {
	System.err.println("Could not clean query: " + e.getMessage());
    }
  
  
  Scanner s = new Scanner(q);
  while (s.hasNext()) {
    String query_part = s.next();
    //System.out.println("old "+query_part);
    query_part = query_part.toLowerCase();
    String charsToDel = "`~!@#$%^&*()_-+=[]\\{}|;':\",./<>?";
    query_part = query_part.replaceAll("[" + Pattern.quote(charsToDel)  + "]", " ").trim();
    //System.out.println("new "+ query_part);
    _tokens.add(query_part);
    //_tokens.add(s.next());
  }
  s.close();
	  
  }
}
