package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;
import java.util.Comparator;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import edu.nyu.cs.cs2580.IndexerInvertedCompressed.Tuple;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews. 
 */
public class RankerComprehensive extends Ranker {

    /*
     public RankerConjunctive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }
     */

    public RankerComprehensive(Options options,
                               CgiArguments arguments, Indexer indexer) {
        super(options, arguments, indexer);
        System.out.println("Using Ranker: " + this.getClass().getSimpleName());
    }
/*
 @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
    Document doc = null;
    int docid = -1;
    while ((doc = _indexer.nextDoc(query, docid)) != null) {
      System.out.println("RETURNED ---------------------->" + doc._docid);
      rankQueue.add(new ScoredDocument(doc, 1.0));
      if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
      System.out.println("Replacing" + docid + " with " + doc._docid);
      docid = doc._docid;
    }

    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    return results;
  }
 */



    private void rerank(Vector<ScoredDocument> orig_ranks) {

        //System.out.println("point 1 ");
        ArrayList<Tuple<ScoredDocument, Double>> numviewsTuples = new ArrayList<Tuple<ScoredDocument, Double>>();

        //System.out.println("point 2 ");
        ArrayList<Tuple<ScoredDocument, Double>> pagerankTuples = new ArrayList<Tuple<ScoredDocument, Double>>();


        //System.out.println("point 3");
        // rerank the top 50 documents

        for (int i = 0; i < orig_ranks.size() && i < 55; i++) {
            //System.out.println("point 4");
            ScoredDocument scoredoc = orig_ranks.get(i);
            pagerankTuples.add(new Tuple<ScoredDocument, Double>(scoredoc, scoredoc.get_doc().getPageRank()));
            numviewsTuples.add(new Tuple<ScoredDocument, Double>(scoredoc, (double) scoredoc.get_doc().getNumViews()));
        }
        //System.out.println("point 6");

        Comparator< Tuple<ScoredDocument, Double>> comparator = new Comparator<Tuple<ScoredDocument, Double>>() {
            public int compare(Tuple<ScoredDocument, Double>tupleA, Tuple<ScoredDocument, Double> tupleB) {
                // if tupleB then tuple A to do descending order
                //System.out.println("point 5 ");
                return tupleB.getSecond().compareTo(tupleA.getSecond());
            }
        };
        //System.out.println("point 7 ");

        Collections.sort(pagerankTuples, comparator);

        Collections.sort(numviewsTuples, comparator);
        //System.out.println("point 8 ");


        for (int i = 0; i < pagerankTuples.size(); i++) {
            //
            ScoredDocument scoredoc_pagerank = pagerankTuples.get(i).getFirst();

            //System.out.println("point 9 ");
            ScoredDocument scoredoc_numviews = numviewsTuples.get(i).getFirst();

            double score_val;
            if (isBetween(i, 0, 9)){
                score_val = 1.0;
            }
            else if (isBetween(i, 20, 29)) {
                score_val = 0.6;
            }
            else if (isBetween(i, 10, 19)) {
                score_val = 0.8;
            }
            else if (isBetween(i, 40, 49)) {
                score_val = 0.2;
            }
            else if (isBetween(i, 30, 39)) {
                score_val = 0.4;
            }

            else {
                score_val = 0.1;
            }
            //System.out.println("point 10 ");
            scoredoc_pagerank.updateScore(score_val);
            scoredoc_numviews.updateScore(score_val);
            //System.out.println("point 11");
        }

    }


    @Override
    public Vector<ScoredDocument> runQuery(Query query, int numResults) {

        Vector<ScoredDocument> all = new Vector<ScoredDocument>();
        QueryPhrase qp=new QueryPhrase(query._raw);
        qp.processQuery();

        //System.out.println("point 12");
        Document i = _indexer.nextDoc(query, -1);
        Double pos;
        int j;
        //System.out.println("point 13 ");
        while(i != null) {

            if(qp.phrase.size()>0) {
                //System.out.println("point 14 ");
                for(j=0;j<qp.phrase.size();j++) {
                    //System.out.println("RETURNED ---------------------->" + doc._docid);
                    pos=_indexer.NextPhrase(qp.phrase.get(j), i._docid, -1);
                    if (pos != Double.POSITIVE_INFINITY)
                        // System.out.println( "Position: " + pos+ " Docid: " + i._docid + " Docname: " + i.getTitle() );
                        if(pos==Double.POSITIVE_INFINITY)
                            break;
                }
                //System.out.println("point 15 ");
                if(j==qp.phrase.size())
                    all.add(scoreDocument(query, i));
                //System.out.println("Replang"d);
            }
            else {
                // System.out.println(  " Docid: " + hi );
                all.add(scoreDocument(query, i));
            }
            //System.out.println("point 16");
            i = _indexer.nextDoc(query,i._docid);
        }

        Collections.sort(all, Collections.reverseOrder());
        //System.out.println("point 17 ");
        rerank(all);
        //System.out.println("point 18");
        Collections.sort(all, Collections.reverseOrder());

        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        //System.out.println("point 19 ");
        for (int j1 = 0; j1 < all.size() && j1 < numResults; ++j1)
            results.add(all.get(j1));
        //System.out.println("point 20 ");
        return results;
    }



    private boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }



    private double bodyRunquery(Query query, Document doc) {
        double score_val = 0.0;
        //System.out.println("point 21 ");

        if (_options._indexerType.equals("inverted-doconly")) {
            for (String queryToken : query._tokens) {
                int idx_val = ((IndexerInvertedDoconly) _indexer).getTerm(queryToken);
                if (idx_val >= 0 )
                    score_val += ((DocumentIndexed) doc).getTFIDF(idx_val);
                //System.out.println("point 22 ");
            }
        }

        else {
            // totaldocs
            int num_docs = _indexer.numDocs();
            for (String queryToken : query._tokens){

                // total number of occurrences of term
                int tf_val = _indexer.documentTermFrequency(queryToken, Integer.toString(doc._docid) );

                //total number of docs word is present
                int df_val = _indexer.corpusDocFrequencyByTerm(queryToken);

                double idf_val = ( 1 + Math.log( (double) num_docs/df_val ) / Math.log(2) );
                score_val += tf_val * idf_val;

                // System.out.println(queryToken + score_val);
            }
            score_val = Math.log(score_val);
        }
        return score_val;
        //System.out.println("score_val"+score_val) ");
    }



    private double titleRunquery(Query query, Document doc) {
        String title = ((DocumentIndexed) doc).getTitle();
        Vector<String> titleTokens = new Vector<String>( Arrays.asList(title.split(" ")) );

        double size = (double) query._tokens.size();
        titleTokens.retainAll(query._tokens);
        double score_val = titleTokens.size() / size;
//System.out.println("score_val"+score_val) ");
        return score_val;
    }



    private ScoredDocument scoreDocument(Query query, Document document) {
        //System.out.println("total_score"+total_score) ");
        double titleScore = titleRunquery(query, document);
        //System.out.println("total_score"+total_score)");
        double bodyScore = bodyRunquery(query, document);
        double total_score = titleScore + bodyScore;
        //System.out.println("total_score"+total_score) ");
        return new ScoredDocument(query._query, document, total_score);
        //System.out.println("total_score"+total_score);
    }




}
