package sample;
import java.io.Serializable;
import java.util.HashMap;


/**
 * DICTIONARY ENTRY
 */
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
        this.totalOccurencesInCorpus = 0;
        this.pointerToPosting = 0;
        pointerToTermInCache =null;
        numberOfDocumentsOccuresIn=0;
        this.term=term;

    }

    /**
     * hash code is calculated by term hashcode (string)
     * @return hash code
     */
    @Override
    public int hashCode() {
        return term.hashCode();
    }


    /**
     * dictionary entry as string
     * @return term: number of occurrences in corpus
     */
    @Override
    public String toString() {
        return term + ": "+getTotalOccurencesInCorpus()+" Occurrences in Corpus";
    }


    /**
     * pointer to cache getter
     * @return the partial posting records in the cache for this term
     */
    public String getPointerToTermInCache() {
        return pointerToTermInCache;
    }


    /**
     * pointer to cache setter
     * @param pointerToTermInCache the partial posting record in the cache for this term
     */
    public void setPointerToTermInCache(String pointerToTermInCache) {
        this.pointerToTermInCache = pointerToTermInCache;
    }

    /**
     * how many times does this term appear in the entire corpus
     * @return total occurences in the corpus for this term
     */
    public int getTotalOccurencesInCorpus() {
        return totalOccurencesInCorpus;
    }

    /**
     * updates the total number of this term in the corpus - adds 1 to the current value
     */
    public void setTotalOccurencesInCorpus(int occurencesInCorpus) {
        this.totalOccurencesInCorpus+=occurencesInCorpus;
    }

    /**
     * poitner to posting getter
     * @return line number of the posting record of this term in the posting file
     */
    public int getPointerToPosting() {
        return pointerToPosting;
    }

    /**
     * pointer to posting setter sets the pointer to posting
     * @param pointerToPosting line number of this term in posting files
     */
    public void setPointerToPosting(int pointerToPosting) {
        this.pointerToPosting = pointerToPosting;
    }


    /**
     * indicates in how many documents in the corpus does this term appear in
     * @return number of docs the term appears in
     */
    public int getNumberOfDocumentsOccuresIn() {
        return numberOfDocumentsOccuresIn;
    }

    public void setNumberOfDocumentsOccuresIn(int numberOfDocumentsOccuresIn) {
        this.numberOfDocumentsOccuresIn += numberOfDocumentsOccuresIn;
    }


    /**
     * compares 2 termInDictionary objects
     * @param o termInDictionary object to compare to
     * @return who has a larger numberOfDocumentsOccuresIn value
     */
    @Override
    public int compareTo(Object o) {
        return numberOfDocumentsOccuresIn - ((TermInDictionairy)o).getNumberOfDocumentsOccuresIn();
    }


    /**
     * term getter
     * @return the term of this dictionary entry record
     */
    public String getTerm() {
        return term;
    }


    /**
     * term setter
     * @param term the term of this dictionary entry record
     */
    public void setTerm(String term) {
        this.term = term;
    }


    /**
     * compares 2 termInDictionary objects and determines if they're equal
     * @param obj termInDictionary object to compare to
     * @return if they have the same term property
     */
    @Override
    public boolean equals(Object obj) {
        return term.equals(((TermInDictionairy)obj).getTerm());
    }
}


