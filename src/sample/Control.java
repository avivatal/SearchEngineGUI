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

    HashMap<String,String> stopwords;
    Parser parser;
    Indexer indexer;
    String destinationDirectory;
    boolean withStemming;
    String directory;


    Control() {
        parser = new Parser();
        indexer = new Indexer();
        stopwords = new HashMap();
    }

    /**
     * getter of the directory of the posting docs and the document properties doc
     * @return name of inner folder in destination folder - according to selection to stem/not stem.
     */
    public String getDirectory() {
        return directory;
    }


    /**
     * notifies the parser and the indexer whether to stem terms.
     * @param withStemming boolean parameter according to the checkbox (stem/no stem)
     */
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

    /**
     * notifies the indexer and the parser of the destination directory where the stem/no stem directories should be created.
     * extracts all stop words from the stop words document into the memory.
     * @param stopwordsPath
     * @param destinationDirectory
     */
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
        } catch (Exception e) { }
    }

    /**
     * activates the parsing and indexing the current group of documents.
     * @param documents the current group of documents passed on from the readFile
     */
    public void control(ArrayList<String> documents) {

        //parse docs in current file
        parser.parse(documents, stopwords);

        //indexer
        indexer.index(parser.getStemmedTerms());

    }

    /**
     * after all the groups of files have been parsed and indexed, activates the indexer to merge all the temporary posting files
     */
    public void merge(){
        indexer.setDocLengths(parser.getDocLenghts());
        parser.writer.close();
        indexer.mergeTempPostings();
    }

    /**
     * after all terms are in the dictionary, activates the indexer to send it to the cache to determine which words will be in the cache
     */
    public void calcCache() { indexer.sendDictionairyToCache(); }
}
