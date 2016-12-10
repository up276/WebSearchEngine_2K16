package edu.nyu.cs.cs2580;

class ScoredTerms implements Comparable<ScoredTerms>{
	private Terms _term;
	private double _score;
	
	public ScoredTerms(Terms term, double score) {
	    _term = term;
	    _score = score;
	  }

	public String asTextResult() {
	    StringBuffer buf = new StringBuffer();
	    buf.append(_term.getName()).append("\t");
	    buf.append(_score);
	    return buf.toString();
	  }

	@Override
	public int compareTo(ScoredTerms t) {


		
		if (this._score == t._score)
			return 0;
		else
			return (this._score > t._score) ? 1 : -1;
	}
	public String asHtmlResult() {
	    return "";
	  }

public Terms get_term() {
	return _term;
}

public void set_term(Terms term) {
	_term = term;
}
	
public void set_score(double score)
{
	_score = score;
}

public double get_score(){
	return _score;
}
}