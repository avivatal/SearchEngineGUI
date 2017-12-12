package sample;
public class CacheRecord implements Comparable{

    String term;
    TermInDoc termInDoc;

    public CacheRecord(String term, TermInDoc termInDoc) {

        this.term = term;
        this.termInDoc = termInDoc;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public TermInDoc getTermInDoc() {
        return termInDoc;
    }

    public void setTermInDoc(TermInDoc termInDoc) {
        this.termInDoc = termInDoc;
    }

    @Override
    public int compareTo(Object o) {
        return termInDoc.compareTo(((CacheRecord)o).getTermInDoc());
    }
}
