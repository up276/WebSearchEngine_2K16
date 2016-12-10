
package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class Bhattacharyya {
	
	static String prf_path=null;
	static String output_path=null;
	static Vector<String> queries=new Vector<String>();
	static Vector<String> paths=new Vector<String>();
	
	
	@SuppressWarnings("resource")
	private static void query_similarity() throws IOException
	{

        String line;
		BufferedReader b1;
        b1 = new BufferedReader(new FileReader(prf_path));


		
		while((line=b1.readLine()) != null)
		{

			String[] parts = line.toString().split(":");
			queries.add(parts[0]);
			paths.add(parts[1]);
		}

		b1.close();
		
		Double final_beta;
		
		PrintWriter writer = new PrintWriter("./"+output_path,"UTF-8");
		
		for(int i=0; i<paths.size();i++)
		{
			for(int j=i+1;j<paths.size(); j++)
			{
				final_beta=calculateBeta(paths.get(i),paths.get(j));
				//System.out.println("");
				writer.printf("%s\t%s\t%f\n",queries.get(i),queries.get(j),final_beta);
				System.out.printf("%s\t%s\t%f\n",queries.get(i),queries.get(j),final_beta);
			}
		}
		writer.close();
		
	}




	@SuppressWarnings("resource")
	private static Double calculateBeta(String inp1,String inp2) throws IOException
	{
		Double beta = 0.0;
        Double beta_i_j = 0.0;

		BufferedReader b1= new BufferedReader(new FileReader(inp1));
		BufferedReader b2= new BufferedReader(new FileReader(inp2));

		String line1;
		String line2;
		
		String[] s1;
		String[] s2;

		Vector<String>words1=new Vector<String>();
		Vector<String>words2=new Vector<String>();
		Vector<Double>probs1=new Vector<Double>();
		Vector<Double>probs2=new Vector<Double>();
		
		while(((line1=b1.readLine())!=null)&&(line2=b2.readLine())!=null)
		{

            s1= line1.toString().split("\t");
            s2= line2.toString().split("\t");

            words1.add(s1[0]);
            probs1.add(Double.parseDouble(s1[1]));

            words2.add(s2[0]);
            probs2.add(Double.parseDouble(s2[1]));


        }

		for(int i=0;i<words1.size();i++)
		{
			for(int j=0;j<words2.size();j++)
			{
				if(words1.get(i).equals(words2.get(j)))
				{
					beta_i_j = probs1.get(i)*probs2.get(j);
                    beta += Math.sqrt(beta_i_j);
				}
			}
		}

		b1.close();
		b2.close();


		return beta;	
	
		
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		if (args.length==2)
		{
			prf_path=args[0];
			output_path=args[1];
		}
		else
		{
			System.out.println("Invalid arguments");
		}


		System.out.println("Calculating the Bhattacharyya coefficient...");
		query_similarity();
		
	}

}

