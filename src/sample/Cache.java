package sample;
import java.util.*;

public class Cache {

    //TreeMap<TermInDoc, String> sortedTermsInDoc;
    PriorityQueue<CacheRecord> sortedTermsInDoc;
    HashMap<String, HashSet<TermInDoc>> cache;

    public Cache() {
        sortedTermsInDoc = new PriorityQueue<>();
        cache=new HashMap<>();
    }

    public void addToCache(Map.Entry<String, HashMap<String,TermInDoc>> entry){

        int maxCacheSize = 10000;

        for(TermInDoc termInDoc : (entry.getValue()).values()){
            //sorts by termInDocs tf value
            sortedTermsInDoc.add(new CacheRecord(entry.getKey(),termInDoc));

            //saves for each term the most relevant posting record
            if(cache.get(entry.getKey())!=null){
                cache.get(entry.getKey()).add(termInDoc);
            }
            else{
                cache.put(entry.getKey(),new HashSet<TermInDoc>());
                cache.get(entry.getKey()).add(termInDoc);
            }

            //if max cache size exceeded, remove posting record with lowest tf
           while(cache.size()>maxCacheSize){
                CacheRecord lowestTf = sortedTermsInDoc.poll(); //removes from sortedTermsInDoc

               //remove from cache
               //if removing the last record for the term, delete the term from the list
               if(cache.get(lowestTf.getTerm()).size()==1){
                   cache.remove(lowestTf.getTerm());
               }
               //not removing the last record, remove only the record
               else if(lowestTf!=null) {
                   cache.get(lowestTf.getTerm()).remove(lowestTf.getTermInDoc()); //removes from cache PROBLEMMMMMMMMMMMMMMMMMMM
               }
           }
        }
    }

    //cache is map of <term, list of termInDocs> - only relevant termInDocs
    public HashMap<String, HashSet<TermInDoc>> getCache() {
        return cache;
    }

    public PriorityQueue<CacheRecord> getSortedTermsInDoc() {
        return sortedTermsInDoc;
    }
}
