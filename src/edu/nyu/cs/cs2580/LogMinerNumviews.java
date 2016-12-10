package edu.nyu.cs.cs2580;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.security.NoSuchAlgorithmException;

import edu.nyu.cs.cs2580.CorpusAnalyzer.HeuristicLinkExtractor;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner implements Serializable {
  private static final long serialVersionUID = 2698138733115785548L;
  public LogMinerNumviews(){}

  public HashMap<String, Integer> numViewsMap = new HashMap<String, Integer>();

  public LogMinerNumviews(Options options) {
    super(options);
  }

  /**
   * This function processes the logs within the log directory as specified by
   * the {@link _options}. The logs are obtained from Wikipedia dumps and have
   * the following format per line: [language]<space>[article]<space>[#views].
   * Those view information are to be extracted for documents in our corpus and
   * stored somewhere to be used during indexing.
   *
   * Note that the log contains view information for all articles in Wikipedia
   * and it is necessary to locate the information about articles within our
   * corpus.
   *
   * @throws IOException
   */
  @Override
  public void compute() throws IOException {
    System.out.println("Computing using " + this.getClass().getName());


    Document.HeuristicDocumentChecker Checker= null;
    try {
      Checker = new Document.HeuristicDocumentChecker();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }


    String corpusDir = _options._corpusPrefix;
    final File DirCorpus = new File(corpusDir);


    BufferedReader reader = null;
    String line = null;
    String[] splitline = null;
    for (final File fileEntry : DirCorpus.listFiles()) {

      if ( !fileEntry.isDirectory() ) {

        if(fileEntry.isHidden())
          continue;

        HeuristicLinkExtractor file = new HeuristicLinkExtractor(fileEntry);

        // Get Main source page link
        Checker.addDoc(file.getLinkSource().toLowerCase());

        String fileName = file.getLinkSource().toLowerCase();



        if(! numViewsMap.containsKey(fileName))
        {

          numViewsMap.put(fileName,0);
        }
        fileName=null;
      }
    }






    String log_path = _options._logPrefix+"/20160601-160000.log";

    reader = new BufferedReader(new FileReader(log_path));


    while ((line=reader.readLine()) != null) {
      try {

        splitline = line.split(" ");

        int numViews = 0;

        try {
          numViews = Integer.parseInt(splitline[2]);
        } catch (Exception e) {
          continue;
        }
        //System.out.println(numViews);
        //System.out.println(splitline[1]);

        String k = splitline[1].toLowerCase();

        if (numViewsMap.get(k) == null) {

          //numViewsMap.put(k, numViews);
          continue;
        } else {
          if(Checker.checkDoc(k) && splitline.length ==3)
          {
            numViewsMap.put(k, numViewsMap.get(k) + numViews);
          }


        }

        k = null;
      }

      catch (Exception e)
      {
        System.out.println(e.toString());
      }

    }

    System.out.println("Finished while loop");

    String indexPath = "numviews.idx";
    System.out.println("Store Numviews to: " + indexPath);
    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexPath));

    try {
      System.out.println("Staring to write numViews");
      writer.writeObject(this);
      System.out.println("Done writing, closing file");
      writer.close();
      System.out.println("Successfully wrote numViews");
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    reader.close();



  }

  /**
   * During indexing mode, this function loads the NumViews values computed
   * during mining mode to be used by the indexer.
   *
   * @throws IOException
   */
  @Override
  public Object load() throws IOException,ClassNotFoundException {
    System.out.println("Loading using " + this.getClass().getName());

    String indexFile = "numviews.idx";
    System.out.println("Load Numviews from: " + indexFile);

    // read in the index file
    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
    LogMinerNumviews loaded = (LogMinerNumviews) reader.readObject();

    System.out.println("Loaded Num views");

    this.numViewsMap=loaded.numViewsMap;
    loaded = null;
    reader.close();


    return null;
  }
  public Integer getNumviews(String doc) {

    //System.out.println(doc);
    return  (numViewsMap.get(doc));

  }

  public HashMap<String, Integer> get_numViews() {
    return numViewsMap;
  }

  /*public void set_numViews(HashMap<String, Integer> _numViews) {
    this.numViewsMap = _numViews;
  }*/

}

