package edu.nyu.cs.cs2580;
import java.io.*;
import java.util.*;

public class Spearman {
	//this will store the pagerank & numviews input
	static String pagerank = null;
	static String numviews = null;
	static HashMap<String,Double> oldpagerank = new HashMap<>();
	static HashMap<String,Integer> oldnumviews = new HashMap<>();
	static HashMap<String,Integer> sortedpagerank1= new HashMap<>();

	//This will store the numviews & pagerank in sorted order
	static HashMap<String,Double> sortedpagerank = null;
	static HashMap<String,Integer> sortednumviews = new HashMap<>();

	public static void main(String args[]) throws ClassNotFoundException, IOException
	{
		parseCommandLine(args);
		//load the old numviews
		loadnumviews();
		//compute new numviews
		newnumviews();
		//load old pagerank
		loadpageranks();
		//compute new pagerank
		newpageranks();
		System.out.println("The Spearman Co-efficient is:" + Spearman());
	}

	//takes the arguments
	public static void parseCommandLine(String[] args)
			throws IOException, NumberFormatException {
		 pagerank = args[0] ;//+ "pageranks.idx";
		 numviews = args[1] ;//+ "numviews.idx";
		//pagerank = "pageranks.idx";
		//numviews = "numviews.idx";
	}

	public  static void loadnumviews() throws FileNotFoundException,IOException,ClassNotFoundException
	{
		System.out.println("Loading Numviews from:" + numviews);
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream("numviews.idx"));
		LogMinerNumviews loaded = (LogMinerNumviews) reader.readObject();
		oldnumviews = loaded.numViewsMap;
		loaded = null;
		reader.close();
	}


	public static void newnumviews()
	{
		Set set = oldnumviews.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry me = (Map.Entry)iterator.next();
		}
        /*sortednumviews = sortByValues(oldnumviews);
        sortednumviews =  give_ranks(sortednumviews);
        sortednumviews = sortByValuesnormalorder(sortednumviews);*/
		sortednumviews = sortByValuesnormalorder(give_ranks(sortByValues(oldnumviews)));
	}


	public static void loadpageranks() throws FileNotFoundException,IOException,ClassNotFoundException
	{
		System.out.println("Loading Pageranks from:" + pagerank);
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream("pageranks.idx"));
		CorpusAnalyzerPagerank loaded = (CorpusAnalyzerPagerank)reader.readObject();
		oldpagerank = loaded.pagerank;
		loaded=null;
		reader.close();
	}


	public static void newpageranks() {
		Set set = oldpagerank.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry me = (Map.Entry) iterator.next();
		}

		sortedpagerank = sortByValues(oldpagerank);
		sortedpagerank1 = give_ranks_pagerank(sortedpagerank);
	}

	// sortedpagerank = sortByValues(oldpagerank);
      /*  sortedpagerank = sortByValues(oldpagerank);
        HashMap<String,Integer> sortedpagerank1= new HashMap<>();
        sortedpagerank1 =  give_ranks_pagerank(sortedpagerank);
        sortedpagerank1 = sortByValuesnormalorder(sortedpagerank1);
       // sortednumviews = sortByValuesnormalorder(give_ranks_pagerank(sortByValues(oldpagerank)));

    }

    //**********Sorts by value***************************************/

	private static HashMap sortByValues(HashMap map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o2,Object o1) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

//***********************Ends Sort by Value*****************************//
	//************normal order********************************************//

	private static HashMap sortByValuesnormalorder(HashMap map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1,Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}
//*****************normal order ends******************//


	public static double Spearman()
	{
		int no_of_docs = oldnumviews.size();
		// int sum_numviews = 0;
		//int sum_pageranks = 0;
		double numerator = 0;
		double denoi1 = 0;
		double denoi2 = 0;
		double z = ((no_of_docs * (no_of_docs + 1))/2)/no_of_docs;
       /* for(String pagename : sortednumviews.keySet())
        {
            sum_numviews = sum_numviews + sortednumviews.get(pagename);
        }
        double ybar = (double)sum_numviews/(double)no_of_docs;

        for(String pagename: sortedpagerank1.keySet())
        {
            sum_pageranks = sum_pageranks + sortedpagerank1.get(pagename);
        }
        double xbar = (double) sum_pageranks / (double)no_of_docs;*/

		for(String pagename : sortednumviews.keySet())
		{
			numerator = numerator + (sortedpagerank1.get(pagename) - z)*(sortednumviews.get(pagename) - z);
			denoi1 = denoi1 + Math.pow((sortedpagerank1.get(pagename) - z),2);
			denoi2 = denoi2 + Math.pow((sortednumviews.get(pagename) - z),2);
		}

		return  numerator/Math.sqrt(denoi1 * denoi2);

       /*int denominator = no_of_docs * (no_of_docs * no_of_docs -1);
       double right_hand_part = 0.0;


       for(String pagename : sortednumviews.keySet())
       {
       int yk = sortednumviews.get(pagename);
       int xk = sortedpagerank1.get(pagename);
       right_hand_part = right_hand_part + Math.pow(yk - xk,2);

   }
   double numerator = 6 * right_hand_part;
       double spearman = 1 - (numerator)/denominator;*/




		//return 1.0;
	}

	//**************************for numviews**************************************************//
	public static HashMap<String, Integer> give_ranks(HashMap<String,Integer> oldranks_with_values)
	{
		int rank =0;
		HashMap<String,Integer> newranks = new HashMap<String,Integer>();
		Vector<String> sorted_acc_to_pagenames = new Vector<>();
		Map.Entry<String,Integer> entry=oldranks_with_values.entrySet().iterator().next();
		String key= entry.getKey();
		int  prev=entry.getValue();
		int current =0;
		int flag =1;
		for(String pagename:oldranks_with_values.keySet())
		{
			current = oldranks_with_values.get(pagename);
			if(current == prev)
			{
				sorted_acc_to_pagenames.add(pagename);
				flag=0;
			}
			else if (flag==0)
			{
				for (String pagename1 : sorted_acc_to_pagenames) {
					rank++;
					newranks.put(pagename1, rank);
				}


				rank++;
				newranks.put(pagename,rank);
				flag = 1;

			}
			else if (flag == 1)
			{
				rank++;
				newranks.put(pagename,rank);
			}

		}
		return newranks;
	}
	//***********************************for pagerank******************************************//
	public static HashMap<String, Integer> give_ranks_pagerank(HashMap<String,Double> oldranks_with_values)
	{
		int rank =0;
		HashMap<String,Integer> newranks = new HashMap<String,Integer>();
		Vector<String> sorted_acc_to_pagenames = new Vector<>();

		Map.Entry<String,Double> entry=oldranks_with_values.entrySet().iterator().next();
		String key= entry.getKey();
		double  prev=entry.getValue();
		double current =0.0;
		int flag =1; //flags are used for lexical sorting of documents
		for(String pagename:oldranks_with_values.keySet())
		{
			current = oldranks_with_values.get(pagename);
			if(current == prev)
			{
				sorted_acc_to_pagenames.add(pagename);
				flag=0;
			}
			else if (flag==0)
			{
				for (String pagename1 : sorted_acc_to_pagenames) {
					rank++;
					newranks.put(pagename1, rank);
				}
				rank++;
				newranks.put(pagename,rank);
				flag = 1;
			}
			else if (flag == 1)
			{
				rank++;
				newranks.put(pagename,rank);
			}

		}
		return newranks;
	}
}




