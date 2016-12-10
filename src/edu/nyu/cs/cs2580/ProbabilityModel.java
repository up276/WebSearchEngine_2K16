package edu.nyu.cs.cs2580;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.TreeMap;

//import QueryExpansion.java;
/**
 * Created by Pranav Chaphekar on 12/2/2016.
 */
public class ProbabilityModel {
    HashMap<String,Integer> wordcount = new HashMap<>();
    HashMap<String,Integer> wordcountdouble = new HashMap<>();
    //constructors
    public ProbabilityModel() throws FileNotFoundException,IOException,ClassNotFoundException{
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream("data/unigram_map.idx"));
        wordcount = (HashMap<String,Integer>) reader.readObject();

       // QueryExpansion loaded = (QueryExpansion)reader.readObject();
      // this.wordcount = loaded.unigram_map;
        ObjectInputStream reader1 = new ObjectInputStream(new FileInputStream("data/bigram_map.idx"));
        //QueryExpansion loaded1 = (QueryExpansion)reader.readObject();
      this.wordcountdouble =(HashMap<String,Integer>) reader1.readObject();
        //this.wordcountdouble= loaded.wordcountdouble;
    }
    //Dice co-eff
    public double dice(String a, String b) {
        String ab = a.concat(b).trim();
        double num =0.0, denoi1 = 0.0, denoi2 = 0.0;
        if(wordcountdouble.containsKey(ab))
            num = (double)wordcountdouble.get(ab);
        if(wordcount.containsKey(a))
            denoi1 = (double)wordcount.get(a);
        if(wordcount.containsKey(b))
            denoi2 = (double)wordcount.get(b);
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

    public void nextword(String queryword){
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
        next_word_map.descendingMap();
        //printing
        for(Double d: next_word_map.descendingKeySet())
            System.out.println(next_word_map.get(d));
    }



}

class MainProbabiltyModel{
    public static void main(String args[]) throws FileNotFoundException,IOException,ClassNotFoundException
    {
        ProbabilityModel pb = new ProbabilityModel();
        String queryword = "a";
        pb.nextword(queryword.concat("$"));
    }
}
