package sample;
import java.io.Serializable;
import java.util.HashMap;

public class TermInDictionairy implements Comparable, Serializable{

    int totalOccurencesInCorpus;
    int pointerToPosting;
    String pointerToTermInCache;
    int numberOfDocumentsOccuresIn;
    String term;

//if there is a cache record for the term, the pointer to posting will remain zero, and the pointer to cache will hold a reference to the cache record.
// from the cache record pointed at, the posting record on disc is reachable.
//if there is no cache record for the term, the cache record will be null and the pointer to posting will be >0

    public TermInDictionairy(String term) {
        this.totalOccurencesInCorpus = 1;
        this.pointerToPosting = 0;
        pointerToTermInCache =null;
        numberOfDocumentsOccuresIn=0;
        this.term=term;

    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public String toString() {
        return term + ": "+getTotalOccurencesInCorpus()+" Occurrences in Corpus";
    }

    public String getPointerToTermInCache() {
        return pointerToTermInCache;
    }

    public void setPointerToTermInCache(String pointerToTermInCache) {
        this.pointerToTermInCache = pointerToTermInCache;
    }
    public int getTotalOccurencesInCorpus() {
        return totalOccurencesInCorpus;
    }

    public void setTotalOccurencesInCorpus() {
        this.totalOccurencesInCorpus++;
    }

    public int getPointerToPosting() {
        return pointerToPosting;
    }

    public void setPointerToPosting(int pointerToPosting) {
        this.pointerToPosting = pointerToPosting;
    }

    public int getNumberOfDocumentsOccuresIn() {
        return numberOfDocumentsOccuresIn;
    }

    public void setNumberOfDocumentsOccuresIn(int numberOfDocumentsOccuresIn) {
        this.numberOfDocumentsOccuresIn += numberOfDocumentsOccuresIn;
    }

    @Override
    public int compareTo(Object o) {
        return numberOfDocumentsOccuresIn - ((TermInDictionairy)o).getNumberOfDocumentsOccuresIn();
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public boolean equals(Object obj) {
        return term.equals(((TermInDictionairy)obj).getTerm());
    }
}


