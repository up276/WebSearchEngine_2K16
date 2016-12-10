package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.StringBuilder;
import java.util.Scanner;
import java.io.File;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.ByteArrayInputStream;


public class HTMLParser{

    //defuault constructor
    public HTMLParser(){}

    public String createFileInput(File file) throws IOException,NullPointerException
    {
        //combination of body and everything
        StringBuilder output_string = new StringBuilder();
        StringBuilder url = new StringBuilder();


        String HTMLString = readFile(file);
        Document doc = Jsoup.parse(HTMLString);

        //add the file title here
        output_string.append(file.getName());
        output_string.append('\t');

        //add the body
        //start with the h1

        output_string.append(getTextfromtag(doc,"h1"));
        //output_string.append(' ');
        output_string.append(getTextfromtag(doc,"h2"));
        //output_string.append(' ');
        output_string.append(getTextfromtagbody(doc,"p"));
        //output_string.append(' ');
        output_string.append('\t');
        output_string.append('0');  //numviews
        url.append("en.wikipedia.org/wiki/");
        url.append(file.getName());
        output_string.append('\t');
        output_string.append(url);

        return output_string.toString();




    }


    private String readFile(File filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        StringBuilder outputstring = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            outputstring.append(line);
        }
        reader.close();
        return outputstring.toString();
    }

    private String getTextfromtag(Document html_doc, String tag) {
        StringBuilder sectionText = new StringBuilder();
        Elements tags = html_doc.getElementsByTag(tag);
        for (Element elem : tags) {
            sectionText.append(elem.text());
            sectionText.append(' ');
        }
        return sectionText.toString();
    }


    private String getTextfromtagbody(Document html_doc, String tag) throws IOException{
        String chars_to_be_deleted = "`~!@#$%^&*()_-+=[]\\{}|;':\",./<>?";

        StringBuilder sectionText = new StringBuilder();
        Elements tags = html_doc.getElementsByTag(tag);
        for (Element elem : tags) {
            sectionText.append(elem.text());
            sectionText.append(' ');
        }
        String body = sectionText.toString();
        body = body.replaceAll("[" + Pattern.quote(chars_to_be_deleted)  + "]", " ");
        body = apply_porter(body);
        return  body;

    }

    public String apply_porter (String body) throws IOException{
        StringBuilder out = new StringBuilder();
        char[] w = new char[501];
        Stemmer porter = new Stemmer();
        InputStream in = new ByteArrayInputStream( body.getBytes() );

        while(true) {
            int ch = in.read();
            if (Character.isLetter((char) ch)) {
                int j = 0;
                while(true) {
                    ch = Character.toLowerCase((char) ch);
                    w[j] = (char) ch;
                    if (j < 500) j++;
                    ch = in.read();
                    if (!Character.isLetter((char) ch)) {

                        for (int c = 0; c < j; c++) porter.add(w[c]);

                        porter.stem();
                        out.append(porter.toString());

                        break;
                    }
                }
            }
            if (ch < 0) break;
            out.append((char)ch);
        }

        return out.toString();
    }



}