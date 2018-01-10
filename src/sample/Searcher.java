package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

public class Searcher {

    HashMap<String,String> queryList; //<query number, query string>
    Pattern regex;
    Parser parser;
    Ranker ranker;
    ArrayList<String> queryNumbers;
    String loadPath;

    public Searcher(){
        regex = Pattern.compile("<top>");
        ranker = new Ranker();
    }

    /**
     * sets the parser member
     * @param parse parser object to set
     */
    public void setParser(Parser parse){
        parser=parse;
    }

    /**
     * set the indexer object
     * @param index indexer object to set
     */
    public void setIndexer(Indexer index){
        ranker.setIndexer(index);
    }


    /**
     * gets the queries from a query file and sends to ranker
     * @param path path of query file
     * @return map of query number and list of 50 most relevant doc ids
     */
    public HashMap<String, List<String>> multipleQueries(String path){
        queryNumbers = new ArrayList();
        queryList=new HashMap<>();
        HashMap<String, List<String>> results = new HashMap<>(); //<query number, set of doc ids>

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder builder = new StringBuilder();
            String aux = "";
            try {
                while ((aux = br.readLine()) != null) {
                    builder.append(aux);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] docs = regex.split(builder.toString());
            ArrayList<String> tmpQueries=new ArrayList<>();
            for (int j = 1; j < docs.length; j++) {
                tmpQueries.add(docs[j]);
            }
            for(String s : tmpQueries){
                int startName = s.indexOf(("Number: "));
                int start = s.indexOf("<title>");
                int end = s.indexOf("<desc>");
                int startDis=s.indexOf("Description:");
                int endDis=s.indexOf("<narr>");
                String temp=s;
                if (start != -1 && end != -1 && startName!=-1 &&endDis!=-1 && startDis!=-1) {
                    s = s.substring(start + 7, end)+" "+temp.substring(startDis+12,endDis); //title and description
                  //  s = s.substring(start + 7, end).trim();
                    String number = temp.substring(startName+8,start);
                    queryList.put(number,s);
                    queryNumbers.add(number);
                }
            }
        }
        catch (Exception e){}

        //send each query to get relevant docs
        for(String s : queryList.keySet()){
            results.put(s,getRelevantDocs(queryList.get(s)));
        }
        return results;
    }

    /**
     * gets a single query and returns the relevant documents
     * @param query a single query
     * @return 50 most relevant doc ids
     */
    public List<String> getRelevantDocs(String query){

        parser.termsForQuery.clear();
        parser.split(query);
        HashSet<String> parsedQuery = parser.getTermsForQuery();

        ranker.setLoadPath(loadPath);
        return ranker.rank(parsedQuery);
    }

    /**
     * sets the load path which the posting files are in
     * @param loadPath path of posting files
     */
    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }
}
