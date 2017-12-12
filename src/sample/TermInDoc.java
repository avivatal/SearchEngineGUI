package sample;
public class TermInDoc implements Comparable {

    String DocId;
    int tf;
    boolean isInFirst100Terms;

    public TermInDoc(String docId, int tf, boolean isInFirst100Terms) {
        DocId = docId;
        this.tf = tf;
        this.isInFirst100Terms = isInFirst100Terms;
    }

    public String getDocId() {
        return DocId;
    }

    public void setDocId(String docId) {
        DocId = docId;
    }

    public int getTf() {
        return tf;
    }

    public void setTf() {
        tf++;
    }

    public boolean isInFirst100Terms() {
        return isInFirst100Terms;
    }

    public void setInFirst100Terms(boolean inFirst100Terms) {
        isInFirst100Terms = inFirst100Terms;
    }

    @Override
    public int hashCode() {
        return DocId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return DocId.equals(((TermInDoc)obj).getDocId());
    }

    @Override
    public String toString() {
        return "("+DocId+" "+tf+" "+String.valueOf(isInFirst100Terms)+")";
    }


    @Override
    //compared 2 term is doc records according to their TF (smaller tf -> bigger object)
    public int compareTo(Object o) {
        return tf-((TermInDoc)o).getTf();
    }
}
