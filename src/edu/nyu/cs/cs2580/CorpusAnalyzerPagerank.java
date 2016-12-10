package edu.nyu.cs.cs2580;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer implements Serializable {

    private static final long serialVersionUID = 2698138733115785548L;
    public CorpusAnalyzerPagerank(Options options) {
        super(options);
    }
    public CorpusAnalyzerPagerank(){}
    int doc_no = -1;//numbers the documents
    HashMap<String,Integer> title_ref = new HashMap<>();//names of all docs
    //this hashmap contains of docids and corresponding links vector
    HashMap<Integer,HashSet<String>> global_page_links = new HashMap<>();
    //HashSet<String> docref = new HashSet<>();
    HashSet<String> all_links_from_all_pages = new HashSet<>();
    HashSet<String> dothtmlpages = new HashSet<>();
    HashMap<Integer,HashSet<Integer>> global_graph = new HashMap<>();
    HashMap<String,Double> pagerank = new HashMap<>();

    // HashMap<Integer,Integer> global_graph = new HashMap<>();
    //Just experimenting with the stuff
    HashMap<String, HashSet<String> > doc_and_all_links = new HashMap<>();



    /**
     * This function processes the corpus as specified inside {@link _options}
     * and extracts the "internal" graph structure from the pages inside the
     * corpus. Internal means we only store links between two pages that are both
     * inside the corpus.
     *
     * Note that you will not be implementing a real crawler. Instead, the corpus
     * you are processing can be simply read from the disk. All you need to do is
     * reading the files one by one, parsing them, extracting the links for them,
     * and computing the graph composed of all and only links that connect two
     * pages that are both in the corpus.
     *
     * Note that you will need to design the data structure for storing the
     * resulting graph, which will be used by the {@link compute} function. Since
     * the graph may be large, it may be necessary to store partial graphs to
     * disk before producing the final graph.
     *
     * @throws IOException
     */
    @Override
    public void prepare() throws IOException {
        System.out.println("Preparing " + this.getClass().getName());
        //String myDirectoryPath = "data/sample";
        String myDirectoryPath = _options._corpusPrefix;
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {

            for (File path : directoryListing) {
                try {

                    processDocument(path);

                } catch (Exception e) {

                }
            }
        }

        //now we have to create the graph of page rank

        System.out.println("Graph Creation");

        //for creating a graph
      /*
    for(String docname:docref)
    {
      //removing the end parts
      //String sub_docname = docname.substring(0,docname.length()-5);
      if(title_ref.containsKey(docname))
      {
        int doc_number = title_ref.get(docname);
        global_graph.put(title_ref.get(docname),doc_number);
      }
    }

//starts
//Stores all doc ids corresponding to a page
*/

//making graph without redirect handling

//handling redirect queries
// we iterate over dothtml pages as these pages contain the redirect urls.
        // HashSet<Integer> redirectpage = new HashSet<>();
     /* for (String page : dothtmlpages) {
          //removing .html ; hence 5 characters
          String redirect_from = page.substring(0, page.length() - 5);
          if( title_ref.containsKey(redirect_from)){
              // redirectpage.put( title_ref.get(redirect_from), title_ref.get(page));
              redirectpage.add(title_ref.get(redirect_from));
              //left part, the -1 one
          }

          //can we prepare a Hashset ???? Maybe yes.
      }
*/

        for(String pagename:doc_and_all_links.keySet()) //iterating through all the pages in the corpus
        {
            HashSet<Integer> integer_representation = new HashSet<>();//this is nothing but the outlinks
            HashSet<String> string_representation = new HashSet<>(); //created just for taking the values

            string_representation = doc_and_all_links.get(pagename);//get the integer representation.

            for(String each_string:string_representation)     //can I make it more efficient ?????? Its O(n^2) now.
            {
                if(title_ref.containsKey(each_string))
                {
                    int i= title_ref.get(each_string);
                    integer_representation.add(i);

      /*  if(redirectpage.contains(i)) {
              // if(title_ref.get(each_string)!= redirectpage.)
               integer_representation.add(i);            //adding the outlinks

           }
           else
               //not handling the redirect
           {
               integer_representation.add(i);
           }*/
                }
            }
            global_graph.put(title_ref.get(pagename),integer_representation);
            integer_representation=null;
            string_representation=null;
        }
//****************Temp done without handling redirect Queries******
        return;
    }





    public void processDocument(File filename) throws IOException
    {
        String file_name;
        doc_no++;
        //This is the object
        HeuristicLinkExtractor link = new HeuristicLinkExtractor(filename);
        //now we need the name of the file
        file_name = link.getLinkSource().toLowerCase();
        //lets convert to lower case for consistency
        //may be no need of link1, let's see that later

        //
        //
        title_ref.put(file_name,doc_no);
        //docref.add(file_name);
        // }

        //now find all the links in that file
        //This vector stores all link in the page
        Vector<String> all_links_page = new Vector<>();

        String a;
        while((a=link.getNextInCorpusLinkTarget())!=null)
        {
            all_links_page.add(a.toLowerCase());
            //just to get all unique links in all the pages
            all_links_from_all_pages.add(a.toLowerCase());
            //maybe ??? lowercase needed or not , I m not sure now
        }
        HashSet<String> new1 = new HashSet<>(all_links_page);
        all_links_page = null;
        doc_and_all_links.put(file_name,new1);
        global_page_links.put(doc_no,new1);
        file_name=null;
        filename=null;
        // new1 = null;
    }

    /**
     * This function computes the PageRank based on the internal graph generated
     * by the {@link prepare} function, and stores the PageRank to be used for
     * ranking.
     *
     * Note that you will have to store the computed PageRank with each document
     * the same way you do the indexing for HW2. I.e., the PageRank information
     * becomes part of the index and can be used for ranking in serve mode. Thus,
     * you should store the whatever is needed inside the same directory as
     * specified by _indexPrefix inside {@link _options}.
     *
     * @throws IOException
     */



    @Override
    public void compute() throws IOException {
        System.out.println("Computing using " + this.getClass().getName());

        //total no of pages in the corpus
        int total_no_of_pages_in_corpus = title_ref.size();
        double lambda = _options._lambda;
        int no_of_iterations = _options._iterations;

        Double right_part_coeff = (1.0-lambda)/total_no_of_pages_in_corpus;

        Vector<Double> rank = new Vector<>();
        Vector<Double> after_computation_rank = new Vector<>();


        //initialised the multiplier of google matrix to all 1's
        for(int j=0; j<total_no_of_pages_in_corpus;j++)
        {
            rank.add(1.0);
        }
        for(int j=0; j<total_no_of_pages_in_corpus;j++)
        {
            after_computation_rank.add(0.0);
        }
        //maybe the above part can be made more efficient later.

        for(int iteration = 0; iteration<no_of_iterations ;iteration++)
        {

            //calculating the left part of the google matrix
            //going through each page in the corpus
            for(int i: global_graph.keySet())
            {
                HashSet<Integer> links = new HashSet<>();
                links = global_graph.get(i);
                Double weight = lambda * (1.0/links.size());
                for(int j=0;j<total_no_of_pages_in_corpus;j++)
                {
                    Double old_value = after_computation_rank.get(j);
                    if(links.contains(j))
                    {
                        after_computation_rank.set(j,old_value + rank.get(i)*(weight + right_part_coeff) );
                    }
                    else //the weight has to be ZERO
                    {
                        after_computation_rank.set(j,old_value+(right_part_coeff)*rank.get(i));

                    }
                }
            }
            //Preparing for the next iteration
            rank = new Vector<>(after_computation_rank);
            for(int index = 0; index<total_no_of_pages_in_corpus;index++)
            {
                after_computation_rank.set(index,0.0);
            }

        }//iteration ends


        //stores the final page rank values
        for(String pagename:title_ref.keySet())
        {
            pagerank.put(pagename,rank.get(title_ref.get(pagename)));
        }
        //setting un needed components to null


        after_computation_rank=null;
        rank=null;
        all_links_from_all_pages=null;
        doc_and_all_links=null;
        global_page_links=null;
        title_ref=null;
        global_graph=null;
        global_page_links=null;




//writing page ranks
        String indexFile = "pageranks.idx";
        System.out.println("Store PageRanks to: " + indexFile);
        ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));

        try {
            writer.writeObject(this);
            writer.close();
        }
        catch(Exception e) {

        }
     /* try{
        writer.writeObject(this.pagerank);
        writer.close();
      }
      catch(Exception e)
      {

      }*/
        return;
    }

    /**
     * During indexing mode, this function loads the PageRank values computed
     * during mining mode to be used by the indexer.
     *
     * @throws IOException
     */
    @Override
    public Object load() throws IOException,ClassNotFoundException {
        System.out.println("Loading using " + this.getClass().getName());
        String indexFile = "pageranks.idx" ;
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));

        CorpusAnalyzerPagerank loaded = (CorpusAnalyzerPagerank)reader.readObject();

        this.pagerank=loaded.pagerank;
        loaded = null;
        System.out.println("Done with loading!!!");
        reader.close();
        return null;
    }

    public Double getPagerank(String doc) {
        if (pagerank.containsKey(doc))
            return  pagerank.get(doc);
        else {
            //System.out.println("DID NOT FIND: " + doc);
            return 0.0;
        }
        //      return 0.0;
        //(_ranked_docs.containsKey(doc) ? _ranked_docs.get(doc) : 0.0);
    }




}
