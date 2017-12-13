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
    Stemmer stemmer;
    Indexer indexer;
    HashMap<String,String> documentProperties;
    String destinationDirectory;
    HashMap<String, String> beforeAfterStem;
    boolean withStemming;
    String directory;


    Control() {
        parser = new Parser();
        stemmer = new Stemmer();
        indexer = new Indexer();
        beforeAfterStem = new HashMap<>();
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
        indexer.setDirectory(directory);
    }

    public void setPaths(String stopwordsPath, String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
        indexer.setPath(destinationDirectory);
        documentProperties=new HashMap<>();
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
        HashMap<String, ArrayList<String>> parseddocs = parser.getParsedDocs();

        //stem
        //stemmedTerms - for each term we save a map of docs and properties of term in that doc
        HashMap<String, HashMap<String,TermInDoc>> stemmedTerms = new HashMap<>();

        try{
            //create file to save document properties
            File dir = new File(destinationDirectory+"/"+directory);
            dir.mkdir();
            PrintWriter writer = new PrintWriter(destinationDirectory+"/"+directory+"/"+"documents.txt","UTF-8");

            //iterate over all docs in file
            int docsCounter=0;
            for (ArrayList doc:parseddocs.values()) {
                docsCounter++;
                //stem each word in doc
                int numberOfTermsInDoc = 0;
                TermInDoc maxTF = new TermInDoc("null", 0, false);
                String docName=(String) doc.get(0);
                String mostFrequentTerm="";
                for (int j = 1; j < doc.size(); j++) {
                    String word = (String) (doc.get(j));
                    numberOfTermsInDoc++;
                    String term="";
                    if(withStemming) {
                        if (!beforeAfterStem.containsKey(word)) {
                            stemmer.add(word.toCharArray(), word.length());
                            stemmer.stem();
                            term = stemmer.toString();
                            beforeAfterStem.put(word, term);
                        } else {
                            term = beforeAfterStem.get(word);
                        }
                    }
                    else{
                        term=word;
                    }

                    //if term is new in hashmap
                    if (!stemmedTerms.containsKey(term)) {
                        TermInDoc tid = new TermInDoc(docName, 1, false);
                        if (j < 100) {
                            tid.setInFirst100Terms(true);
                        }
                        HashMap<String, TermInDoc> map = new HashMap<>();
                        map.put(docName, tid);
                        stemmedTerms.put(term, map);
                        if (tid.getTf() > maxTF.getTf()) {
                            maxTF = tid;
                            mostFrequentTerm=term;
                        }
                    }
                    //term appears in hashmap
                    else {
                        //if doc appears in stemmed term - update TF
                        if (stemmedTerms.get(term).containsKey((docName))) {
                            (stemmedTerms.get(term)).get(docName).setTf();
                        }
                        //if doc doesnt appear in stemmed term, create new TermInDoc entry
                        else {
                            stemmedTerms.get(term).put(docName, new TermInDoc(docName, 1, false));
                            if (j < 100) {
                                stemmedTerms.get(term).get(docName).setInFirst100Terms(true);
                            }
                        }
                        if (stemmedTerms.get(term).get(docName).getTf() > maxTF.getTf()) {
                            maxTF = stemmedTerms.get(term).get(docName);
                            mostFrequentTerm=term;
                        }
                    }

                }
                //enter the documents properties into the file
                documentProperties.put(docName, docsCounter + "");
                //save in file at line #docsCounter: "docID: length, most frequent term"
                writer.println(docName+": "+numberOfTermsInDoc+", "+mostFrequentTerm);
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //finish sterm
        //indexer
        indexer.index(stemmedTerms);


        System.out.println("done");

    }

    public void merge(){
        indexer.mergeTempPostings();
    }

    public void calcCache() { indexer.sendDictionairyToCache(); }
}
