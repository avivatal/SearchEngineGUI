package sample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Control {

    HashMap<String,String> stopwords = new HashMap();
    Parser parser;
    Indexer indexer;
    String destinationDirectory;
    boolean withStemming;
    String directory;


    Control() {
        parser = new Parser();
        indexer = new Indexer();
    }


    public String getDirectory() {
        return directory;
    }

    public void setWithStemming(boolean withStemming) {
        this.withStemming = withStemming;
        if(withStemming){
            directory="withStem";
        }
        else{
            directory="noStem";
        }
        parser.setDirectory(directory);
        parser.setWithStemming(withStemming);
        indexer.setDirectory(directory);
    }

    public void setPaths(String stopwordsPath, String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
        indexer.setPath(destinationDirectory);
        parser.setDestinationDirectory(destinationDirectory);
        try {
            BufferedReader br = new BufferedReader(new FileReader(stopwordsPath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                stopwords.put(line,null);
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            //     e.printStackTrace();
        }
    }

    public void control(ArrayList<String> documents) {


        //parse docs in current file
        parser.parse(documents, stopwords);
        System.out.println("done parse");

        //indexer
        indexer.index(parser.getStemmedTerms());

        System.out.println("done");

    }

    public void merge(){
        parser.writer.close();
        indexer.mergeTempPostings();
    }

    public void calcCache() { indexer.sendDictionairyToCache(); }
}
