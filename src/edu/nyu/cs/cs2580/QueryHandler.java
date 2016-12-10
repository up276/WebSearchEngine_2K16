package edu.nyu.cs.cs2580;

import java.io.IOException;

import java.io.OutputStream;
import java.util.Vector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * Handles each incoming query, students do not need to change this class except
 * to provide more query time CGI arguments and the HTML output.
 *
 * N.B. This class is not thread-safe.
 *
 * @author congyu
 * @author fdiaz
 */
class QueryHandler implements HttpHandler {
    private String format;

    /**
     * CGI arguments provided by the user through the URL. This will determine
     * which Ranker to use and what output format to adopt. For simplicity, all
     * arguments are publicly accessible.
     */
    public static class CgiArguments {
        // The raw user query
        public String _query = "";
        // How many results to return
        private int _numResults = 10;
        private int _numTerms = 10;
        private int _numDocs = 10;

        // The type of the ranker we will be using.
        public enum RankerType {
            NONE,
            FULLSCAN,
            CONJUNCTIVE,
            FAVORITE,
            COSINE,
            PHRASE,
            QL,
            LINEAR,
            COMPREHENSIVE
        }
        public RankerType _rankerType = RankerType.NONE;

        // The output format.
        public enum OutputFormat {
            TEXT,
            HTML,
        }
        public OutputFormat _outputFormat = OutputFormat.TEXT;

        public CgiArguments(String uriQuery) {
            String[] params = uriQuery.split("&");
            for (String param : params) {
                String[] keyval = param.split("=", 2);
                if (keyval.length < 2) {
                    continue;
                }
                String key = keyval[0].toLowerCase();
                String val = keyval[1];
                if (key.equals("query")) {
                    _query = val.replace("+"," ");
                } else if (key.equals("num")) {
                    try {
                        _numResults = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                } else if (key.equals("ranker")) {
                    try {
                        _rankerType = RankerType.valueOf(val.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                } else if (key.equals("format")) {
                    try {
                        _outputFormat = OutputFormat.valueOf(val.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                }
                else if (key.equals("numdocs")) {
                    try {
                        _numDocs = Integer.parseInt(val);
                    }
                    catch (NumberFormatException e) {
                    }
                }
                else if (key.equals("numterms")){
                    try {
                        _numTerms = Integer.parseInt(val);
                    }
                    catch (NumberFormatException e){
                    }
                }
            }  // End of iterating over params
        }
    }

    // For accessing the underlying documents to be used by the Ranker. Since
    // we are not worried about thread-safety here, the Indexer class must take
    // care of thread-safety.
    private Indexer _indexer;
    private String query;

    public QueryHandler(Options options, Indexer indexer) {
        _indexer = indexer;
    }

    private void respondWithMsg(HttpExchange exchange, final String message)
            throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
       if(format.equals("html"))
            responseHeaders.set("Content-type","text/html");
       else
            responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(message.getBytes());
        responseBody.close();
    }

    private void constructTextOutput(final Vector<ScoredDocument> docs, StringBuffer response) {
        response.append("DocID \t Title \t Final_Score\tPage Rank Score\tNum Views score");
        response.append("\n______________________________________________________________________________\n");
        for (ScoredDocument doc : docs) {

            response.append(response.length() > 0 ? "\n" : "");
            response.append(doc.asTextResult());

        }
        response.append(response.length() > 0 ? "\n" : "");
    }

   private void constructHTMLOutput(final Vector<ScoredDocument> docs, StringBuffer response) {
        response.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table {\n" +
                "    font-family: arial, sans-serif;\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;\n" +
                "}\n" +
                "\n" +
                "td, th {\n" +
                "    border: 1px solid #dddddd;\n" +
                "    text-align: left;\n" +
                "    padding: 8px;\n" +
                "}\n" +
                "\n" +
                "tr:nth-child(even) {\n" +
                "    background-color: #dddddd;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<table>");

        for (ScoredDocument doc : docs) {
            response.append(response.length() > 0 ? "\n" : "");
            response.append(doc.asHtmlResult());
        }
        response.append("</table>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n");

        response.append(response.length() > 0 ? "\n" : "No result returned!");

    }

    private void constructTermOutput(Vector<ScoredTerms> terms, StringBuffer response) {

        if(terms.size()==0)
            response.append("ERROR:404 No Document Found");

        for (ScoredTerms term : terms) {
            response.append(response.length() > 0 ? "\n" : "");
            response.append(term.asTextResult());

        }
        response.append(response.length() > 0 ? "\n" : "");
    }


    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
            return;
        }

        // Print the user request header.
        Headers requestHeaders = exchange.getRequestHeaders();
        System.out.print("Incoming request: ");
        for (String key : requestHeaders.keySet()) {
            System.out.print(key + ":" + requestHeaders.get(key) + "; ");
        }
        System.out.println();

        // Validate the incoming request.
        String uriQuery = exchange.getRequestURI().getQuery();
        String uriPath = exchange.getRequestURI().getPath();
        if (uriPath == null || uriQuery == null) {
            respondWithMsg(exchange, "Something wrong with the URI!");
        }
        if (!uriPath.equals("/search") && !uriPath.equals("/prf")) {
            respondWithMsg(exchange, "Only /search and /prf is handled!");
        }
        if (uriPath.equals("/search"))
        {
            System.out.println("Query: " + uriQuery);

            // Process the CGI arguments.
            CgiArguments cgiArgs = new CgiArguments(uriQuery);
            if (cgiArgs._query.isEmpty()) {
                respondWithMsg(exchange, "No query is given!");
            }

            // Create the ranker.
            Ranker ranker = Ranker.Factory.getRankerByArguments(
                    cgiArgs, SearchEngine.OPTIONS, _indexer);
            if (ranker == null)
            {
                respondWithMsg(exchange,
                        "Ranker " + cgiArgs._rankerType.toString() + " is not valid!");
            }

            // Processing the query.
            Query processedQuery = new Query(cgiArgs._query);
            processedQuery.processQuery();


            // Ranking.
            Vector<ScoredDocument> scoredDocs =
                    ranker.runQuery(processedQuery, cgiArgs._numResults);
            StringBuffer response = new StringBuffer();
            switch (cgiArgs._outputFormat) {
                case TEXT:
                    constructTextOutput(scoredDocs, response);
                    break;
                case HTML:
                    // @CS2580: Plug in your HTML output
                    format = "html";
                    query = cgiArgs._query;
                    constructHTMLOutput(scoredDocs, response);
                    break;
                default:
                    // nothing
            }

            respondWithMsg(exchange, response.toString());
            System.out.println("Finished query: " + cgiArgs._query);
        }
        else if (uriPath.equals("/prf"))
        {  System.out.println("Query: " + uriQuery);

            // Process the CGI arguments.
            CgiArguments cgiArgs = new CgiArguments(uriQuery);
            if (cgiArgs._query.isEmpty()) {
                respondWithMsg(exchange, "No query is given!");
            }

            // Create the ranker.
            Ranker ranker = Ranker.Factory.getRankerByArguments(
                    cgiArgs, SearchEngine.OPTIONS, _indexer);
            if (ranker == null)
            {
                respondWithMsg(exchange,
                        "Ranker " + cgiArgs._rankerType.toString() + " is not valid!");
            }

            // Processing the query.
            Query processedQuery = new Query(cgiArgs._query);
            processedQuery.processQuery();


            // Ranking.
            Vector<ScoredDocument> scoredDocs =
                    ranker.runQuery(processedQuery, cgiArgs._numDocs);

            // Need a method that retrieves terms (scoreddocs, numterms)

            Vector<ScoredTerms> scored = PRF.Relevance(scoredDocs,cgiArgs._numDocs, cgiArgs._numTerms, _indexer.getDict(),SearchEngine.OPTIONS._keepTerms);
            StringBuffer response = new StringBuffer();

            switch (cgiArgs._outputFormat) {
                case TEXT:
                    constructTermOutput(scored, response);
                    break;
                case HTML:
                    format = "html";
                    query = cgiArgs._query;
                    constructHTMLOutput(scoredDocs, response);
                    break;
                default:
                    // nothing
            }

            respondWithMsg(exchange, response.toString());
            System.out.println("Finished query: " + cgiArgs._query);


        }
    }
}