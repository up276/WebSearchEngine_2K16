package edu.nyu.cs.cs2580;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
  private Document _doc;
  private double _score;

  private String query1 = null;

  public ScoredDocument(String query,Document doc, double score) {
    set_doc(doc);
    _score = score;
    query1 = query;
  }

  public String asTextResult() {

    StringBuffer buf = new StringBuffer();
    buf.append(get_doc()._docid).append("\t");
    buf.append(get_doc().getTitle().replaceAll("_"," ")).append("\t");
    buf.append(_score).append("\t");
    buf.append(get_doc().getPageRank()).append("\t");
    buf.append(get_doc().getNumViews()).append("\t");

    return buf.toString();
  }
  public String asHtmlResult() {
    StringBuffer buf = new StringBuffer();
    buf.append("<tr>");
    buf.append("<td>");
    buf.append(query1).append("\t");
    buf.append("</td>");
    buf.append("<td>");
    //buf.append("<br>");
    buf.append(_doc._docid).append("\t");
    buf.append("</td>");
    //buf.append("<br>");
    buf.append("<td>");
    buf.append(_doc.getTitle()).append("\t");
    buf.append("</td>");
    //buf.append("<br>");
    buf.append("<td>");
    buf.append(_score);
    buf.append("</td>");
    buf.append("</tr>");

    return buf.toString();

  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */

  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }

  public Document get_doc() {
	 return _doc;
  }

  public void set_doc(Document _doc) {
	 this._doc = _doc;
  }

  public void updateScore(double additional){
    _score += additional;
  }
}
