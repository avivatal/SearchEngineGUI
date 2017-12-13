package sample;
import java.io.Serializable;
import java.util.*;

public class Cache implements Serializable {


    PriorityQueue<TermInDictionairy> queue;
    HashMap<String, HashMap<TermInDoc,String>> cache;
    HashMap<String, String> pointersToPosting;

    public Cache() {
        cache=new HashMap<>();
        pointersToPosting=new HashMap<>();
    }

    //get 10000 most popular words in corpus. Sorted by words that appear in the most amount of docs in corpus.
    public void addDictionairy(HashMap<String,TermInDictionairy> dictionairy){
        int maxCacheSize = 10000;

        queue = new PriorityQueue<>(dictionairy.values());
        while(queue.size()>maxCacheSize){
            queue.poll();
        }
    }

    public HashMap<String, String> getPointersToPosting() {
        return pointersToPosting;
    }

    //string to posting records in cache
    public void getLine(String postingLine, int linecounter) {
        String term = postingLine.substring(0, postingLine.indexOf(':'));

        //if the word is one of the 10000 most popular words in corpus
        if (queue.contains(new TermInDictionairy(term))) {
            PriorityQueue<TermInDoc> sortedTermsInDoc = new PriorityQueue<>();
            String line = postingLine.substring(postingLine.indexOf(':') + 3);

            //take all posting lines and tranform to termInDocs
            char[] charsInLine = line.toCharArray();
            TermInDoc tid=null;
            for (int i = 0; i < charsInLine.length; i++) {

                if (charsInLine[i] == '(') {
                    tid = new TermInDoc("null", 0, false);
                    i++;
                    //docID
                    StringBuilder docID = new StringBuilder();
                    while (charsInLine[i] != ' ') {
                        docID.append(charsInLine[i]);
                        i++;
                    }
                    tid.setDocId(docID.toString());
                    StringBuilder tf = new StringBuilder();
                    i++;
                    //tf
                    while (charsInLine[i] != ' ') {
                        tf.append(charsInLine[i]);
                        i++;
                    }
                    tid.updateTf(Integer.parseInt(tf.toString()));
                    i++;
                    //in first 100 words in doc
                    if (charsInLine[i] == 'f') {
                        tid.setInFirst100Terms(false);
                        i += 6;
                    } else if (charsInLine[i] == 't') {
                        tid.setInFirst100Terms(true);
                        i += 5;
                    }
                    if (i > charsInLine.length)
                        break;

                }
                if(tid!=null){
                sortedTermsInDoc.add(tid);
                }
            }

            //take only 25% of the posting records - with the highest tf
            double maxRecordsForTerm = Math.ceil(sortedTermsInDoc.size() / 8.0);
            HashMap<TermInDoc,String> postingsForCache =  new HashMap<>();
            for(int i=0; i<maxRecordsForTerm; i++){
                postingsForCache.put(sortedTermsInDoc.poll(),null);
            }

            //add to cache
            cache.put(term,postingsForCache);
            pointersToPosting.put(term,linecounter+"");
        }
    }






    //cache is map of <term, list of termInDocs> - only relevant termInDocs
    public HashMap<String, HashMap<TermInDoc,String>> getCache() {
        return cache;
    }
}
