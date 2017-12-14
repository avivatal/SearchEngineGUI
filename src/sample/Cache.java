package sample;
import java.io.Serializable;
import java.util.*;

public class Cache implements Serializable {

    HashSet<TermInDictionairy> queue;
    //PriorityQueue<TermInDictionairy> queue;
    HashMap<String, String> cache; //term+posting lines
    HashMap<String, String> pointersToPosting;

    public Cache() {
        queue=new HashSet<>();
        cache=new HashMap<>();
        pointersToPosting=new HashMap<>();
       // queue=new HashMap<>();
    }

    //get 10000 most popular words in corpus. Sorted by words that appear in the most amount of docs in corpus.
    public void addDictionairy(HashMap<String,TermInDictionairy> dictionairy){
        int maxCacheSize = 10000;

        List<TermInDictionairy> list = new ArrayList<>();
        list.addAll(dictionairy.values());
        Collections.sort(list);
        int listlen=list.size();
        list = list.subList(listlen-maxCacheSize,listlen);
        queue.addAll(list);

    }

    public HashMap<String, String> getPointersToPosting() {
        return pointersToPosting;
    }

    //string to posting records in cache
    public void getLine(String postingLine, int linecounter) {
        String term = postingLine.substring(0, postingLine.indexOf(':'));
        //if the word is one of the 10000 most popular words in corpus
        if (queue.contains(new TermInDictionairy(term))) {
          //  PriorityQueue<TermInDoc> sortedTermsInDoc = new PriorityQueue<>();
            String line = postingLine.substring(postingLine.indexOf(':') + 3);
            int index=0;
            int bracket=0;
            int indexOfBracket=0;
            int length = line.length();
            char[] lineChars = line.toCharArray();
            while(index<length && bracket<5) {
                if (lineChars[index] == ')') {
                    bracket++;
                    indexOfBracket=index;
                }
                index++;
            }
            line=line.substring(0,indexOfBracket+1);
            //add to cache
            cache.put(term,line);
            pointersToPosting.put(term,linecounter+"");

            //take all posting lines and tranform to termInDocs
          /*  char[] charsInLine = line.toCharArray();
            TermInDoc tid=null;
            int counter=0;
           /* for (int i = 0; i < charsInLine.length || counter<5; i++) {
                counter++;
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
                    // sortedTermsInDoc.add(tid);
                    postingsForCache.put(tid,null);
                }*/
            }

            //take only 25% of the posting records - with the highest tf
           /* double maxRecordsForTerm = Math.ceil(sortedTermsInDoc.size() / 8.0);
            HashMap<TermInDoc,String> postingsForCache =  new HashMap<>();
            for(int i=0; i<maxRecordsForTerm; i++){
                postingsForCache.put(sortedTermsInDoc.poll(),null);
            }*/



    }






    //cache is map of <term, list of termInDocs> - only relevant termInDocs
    public HashMap<String, String> getCache() {
        return cache;
    }
}
