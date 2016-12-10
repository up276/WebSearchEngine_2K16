package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import com.google.common.collect.HashBiMap;

public class PRF {

	public static Vector<ScoredTerms> Relevance(Vector<ScoredDocument> scoredDocs,int numdocs, int numTerms, HashBiMap<String,Integer> dict,int max_terms){


		HashMap<Integer, Integer> wordProbMap = new HashMap<Integer, Integer>();

		if (numdocs>scoredDocs.size())
		{ // in case numDocs more than the size for the scored documents
			numdocs = scoredDocs.size();
		}

		for (int i=0; i<numdocs; i++){
			ScoredDocument document = scoredDocs.get(i);
			Document d =  document.get_doc();

			//Using provided function in DocumentIndexed
			HashMap<Integer, Integer> wordHash = ((DocumentIndexed) d).getTopWords(max_terms);


			for (int j:wordHash.keySet())
			{
				if (wordProbMap.containsKey(j))
				{

					wordProbMap.put(j, wordHash.get(j)+wordProbMap.get(j));
				}
				else
				{
					wordProbMap.put(j, wordHash.get(j));
				}
			}
		}



		Vector<ScoredTerms> wordProbs = new Vector<ScoredTerms>();



		for (int keys:wordProbMap.keySet())
		{

			double score = ((double) wordProbMap.get(keys));
			ScoredTerms scores = new ScoredTerms(new Terms(dict.inverse().get(keys)), score);
			wordProbs.add(scores);


		}



		//sort in descending order
		Collections.sort(wordProbs, Collections.reverseOrder());

		Double total_score = 0.0;

		//Calculating total score for normalization
		for(int i=0;i<wordProbs.size();i++)
			total_score+= wordProbs.get(i).get_score();


		//Normalizing by total_score
		for(int i=0;i<wordProbs.size();i++)
			wordProbs.get(i).set_score(wordProbs.get(i).get_score()/total_score);

		Vector<ScoredTerms> renormalizedProbs = new Vector<ScoredTerms>();




		//Calculation the new normalization factor

		Double new_total_score=0.0;
		for(int i=0;i<numTerms;i++){
			new_total_score= new_total_score + wordProbs.get(i).get_score();
		}


		//System.out.println("Renormalizing...");

		//renormalizing

		for(int i=0;i<numTerms;i++)
		{
			wordProbs.get(i).set_score(wordProbs.get(i).get_score()/new_total_score);
			renormalizedProbs.add(wordProbs.get(i));
		}
		return renormalizedProbs;

	}


}
