package edu.nyu.cs.cs2580;
import java.io.*;
import java.util.*;

import static java.lang.Float.NaN;

/**
 * Created by Pranav Chaphekar on 11/30/2016.
 */
public class QueryExpansion {

public static HashMap<String,Integer> wordcount = new HashMap<>();
public static HashMap<String,Integer> wordcountdouble = new HashMap<>();

public static void CorpusData() throws FileNotFoundException {
    Scanner filereader = new Scanner(new File("C:\\web\\assignmentweb\\data\\questions_test.txt")).useDelimiter("\n");
    while(filereader.hasNext())
    {
        String filecontent = filereader.next();
        BuildTable(filecontent);
        BuildTabledouble(filecontent);
    }
    System.out.println("I made the hashtable and now I am writing it");
    String corpus_oneword  ="corpus_oneword.idx";
    String corpus_twoword = "corpus_twoword.idx";
    //System.out.println("Storing to: " + corpus_oneword);
    try{

        ObjectOutputStream writer =
                new ObjectOutputStream(new FileOutputStream(corpus_oneword));
        ObjectOutputStream writer1 = new ObjectOutputStream(new FileOutputStream(corpus_twoword));
        writer.writeObject(wordcount);
        writer1.writeObject(wordcountdouble);
        writer.close();
        writer1.close();
    }
    catch(Exception e)
    {
        System.out.println(e.toString());
    }
    System.out.println("I have written.BYE");

}

//builds the table for unigram
public static void BuildTable(String filecontent){
    Scanner bodyscanner = new Scanner(filecontent);
    while(bodyscanner.hasNext())
    {
    String word = bodyscanner.next();
    if (wordcount.containsKey(word))
    {
        int count = wordcount.get(word);
        wordcount.replace(word,++count);
    }
    else
    {
        wordcount.put(word,1);
    }
    }
}

//builds the table for bigram
public static void BuildTabledouble(String filecontent) {

    Scanner sc = new Scanner(filecontent);
    while (sc.hasNext()) {
        String word1 = sc.next().concat("$");
        String word2 = null;
        if (sc.hasNext()) {
            word2 = sc.next();
            String combinedword = word1.concat(word2).toLowerCase().trim();
            if (wordcountdouble.containsKey(combinedword)) {
                int count = wordcountdouble.get(combinedword);
                count = count + 1;
                wordcountdouble.replace(combinedword, count);
            } else {
                wordcountdouble.put(combinedword, 1);
            }

        }
    }
}

//all 4 measures for probabilistic models start here
//dice
public static double dice(String a, String b) {
    String ab = a.concat(b).trim();
    double num =0.0, denoi1 = 0.0, denoi2 = 0.0;
    if(wordcountdouble.containsKey(ab))
        num = (double)wordcountdouble.get(ab);
    if(wordcount.containsKey(a))
            denoi1 = wordcount.get(a);

    if(wordcount.containsKey(b))
        denoi2 = wordcount.get(b);

    double answer = 0.0;
    try {
        answer = num / (denoi1 + denoi2);
        return answer;
    }
    catch (Exception e)
    {
        return 0.0;
    }



}
//mim
public static double mim(String a, String b) {
        String ab = a.concat(b).toLowerCase().trim();
    double num =0.0, denoi1 = 0.0, denoi2 = 0.0;
    if(wordcountdouble.containsKey(ab))
        num = (double)wordcountdouble.get(ab);
    if(wordcount.containsKey(a))
        denoi1 = (double)wordcount.get(a);
    if(wordcount.containsKey(b))
        denoi2 = (double)wordcount.get(b);
    double answer = 0.0;
    try {
        answer = num / (denoi1 * denoi2);
        return answer;
    }
    catch (Exception e)
    {
        return 0.0;
    }

}
//emim
public static double emim(String a, String b) {
    String ab = a.concat(b).toLowerCase().trim();
    double nab =0.0, na = 0.0, nb = 0.0;
    if(wordcountdouble.containsKey(ab))
        nab = (double)wordcountdouble.get(ab);
    if(wordcount.containsKey(a))
        na = (double)wordcount.get(a);
    if(wordcount.containsKey(b))
        nb = (double)wordcount.get(b);
    double answer = 0.0;
    try {
      //  answer = nab * Math.log();
        return answer;
    }
    catch (Exception e)
    {
        return 0.0;
    }

}
public static void nextword(String queryword){
    //double co_eff = -999.00;
    TreeMap<Double,String> next_word_map = new TreeMap<>();
    for(String s:wordcount.keySet())
    {
       double value = dice(queryword.toLowerCase(),s.toLowerCase());
       if(value!=0.0 && !Double.isNaN(value))
        next_word_map.put(value,s);

    }
    System.out.println("The next word can be:");
    //reverse ordering
  //  System.out.println(next_word_map.descendingMap());

    //printing
    for(Double d: next_word_map.descendingKeySet())
        System.out.println(next_word_map.get(d));
}



public static void main(String args[]) throws FileNotFoundException
{
    CorpusData();
    String queryword = "started";
    nextword(queryword.concat("$")); //$ is like space


}



}