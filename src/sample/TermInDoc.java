package sample;

import java.io.Serializable;

/**
 * POSTING ENTRY FOR SINGLE DOCUMENT
 */
public class TermInDoc implements Comparable, Serializable {

    String DocId;
    int tf;
   // boolean isInFirst100Terms;
    int index; //index of first occurence of term

    public TermInDoc(String docId, int tf, int index) {
        DocId = docId;
        this.tf = tf;
        this.index=index;
        //this.isInFirst100Terms = isInFirst100Terms;
    }

    /**
     * document ID getter
     * @return the document ID of this posting record
     */
    public String getDocId() {
        return DocId;
    }

    /**
     * document ID setter
     * @param docId the document ID of this posting record
     */
    public void setDocId(String docId) {
        DocId = docId;
    }


    /**
     * TF getter
     * @return the number of times the term appears in this document
     */
    public int getTf() {
        return tf;
    }

    /**
     * TF setter
     * sets the number of times the term appears in this document by adding 1 to current value
     */
    public void setTf() {
        tf++;
    }


    /**
     * replaces the current TF value with a new TF value
     * @param newtf new TF value to replace current one
     */
    public void updateTf(int newtf){
        tf=newtf;
    }

    /**
     * returns if the term is in the first 100 terms in this document
     * @return true if is in first 100 terms in doc, otherwise false
     */
    public int getIndex() {
        return index;
    }


    /**
     * sets whether the term is in the first 100 terms in this document
     * @param index true if is in first 100 terms in doc, otherwise false
     */
    public void setIndex(int index) {
       this.index = index;
    }

    /**
     * hashcode
     * @return the docID hashcode (string)
     */
    @Override
    public int hashCode() {
        return DocId.hashCode();
    }

    /**
     * determines whether 2 TermInDoc objects are equal
     * @param obj TermInDoc object to compare this to
     * @return whether their Doc IDs are equal
     */
    @Override
    public boolean equals(Object obj) {
        return DocId.equals(((TermInDoc)obj).getDocId());
    }


    /**
     * TermInDoc to string
     * @return (DocID, tf, isInFirst100Terms)
     */
    @Override
    public String toString() {
        return "("+DocId+" "+tf+" "+ index+")";
    }


    /**
     * compares 2 TermInDoc objects
     * @param o TermInDoc object to compare this to
     * @return  who has a smaller tf value
     */
    @Override
    //compared 2 term is doc records according to their TF (smaller tf -> bigger object)
    public int compareTo(Object o) {
        return ((TermInDoc)o).getTf()-tf;
    }
}
