package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Ranker {

    Indexer indexer;
    double avgDocLen;
    String loadPath;

    public Ranker(){
        avgDocLen=-1.0;
    }


    /**
     * ranks documents according to their relevance to a query using cosine similarity, BM25 and the terms index in doc.
     * @param parsedQuery terms in a query
     * @return 50 most relevant docs
     */
    public List<String> rank(HashSet<String> parsedQuery){

        List<String> result=null;
        HashMap<String, Double> bm25InDocs = new HashMap<>(); //<docID, totalBM25>
        HashMap<String, Double> tfIndexInDocs = new HashMap<>(); //<docID, totalIndex>
        HashMap<String, Double> weightsInDocs = new HashMap<>(); //<docID, totalQueryTermsWeight>

        for(String term : parsedQuery){

        //get the posting record for each term in query
            if(indexer.getDictionairy().containsKey(term)){
                TermInDictionairy tid = indexer.getDictionairy().get(term);
                double idf = Math.log10(indexer.numOfDocsInCorpus/tid.getNumberOfDocumentsOccuresIn())/Math.log10(2);

                String path= loadPath+"/";
                char firstLetter = term.substring(0,1).toCharArray()[0];
                if(firstLetter<97|| firstLetter>122){
                    path+= "nonLetters.txt";
                }
                else{
                    path+=firstLetter+".txt";
                }

                try {
                    BufferedReader posting = new BufferedReader(new FileReader(path));
                    int lineNumber = tid.getPointerToPosting();
                    String postingLine = "";

                    for (int i = 0; i < lineNumber; i++) {
                        postingLine = posting.readLine();
                    }


                    //get tfs from posting line
                    postingLine = postingLine.substring(postingLine.indexOf(':') + 1);
                    char[] charsInLine = postingLine.toCharArray();
                    for (int i = 0; i < charsInLine.length; i++) {
                        if (charsInLine[i] == '(') {
                            i++;

                            //docID
                            StringBuilder docID = new StringBuilder();
                            while (charsInLine[i] != ' ') {
                                docID.append(charsInLine[i]);
                                i++;
                            }
                            StringBuilder tf = new StringBuilder();
                            i++;
                            //tf
                            while (charsInLine[i] != ' ') {
                                tf.append(charsInLine[i]);
                                i++;
                            }
                            StringBuilder index=new StringBuilder();
                            while (charsInLine[i] != ')') {
                                index.append(charsInLine[i]);
                                i++;
                            }
                            i++;

                            double docLength = indexer.docLengths.get(docID.toString());

                            //calculate weight
                            double tfnormal = Double.parseDouble(tf.toString()) / docLength;

                            double k=1.4;
                            double b = 0.75;
                            double bm25;
                            double bmidf = indexer.docLengths.size() - indexer.getDictionairy().get(term).getNumberOfDocumentsOccuresIn() + 0.5;
                            bmidf = bmidf/(indexer.getDictionairy().get(term).getNumberOfDocumentsOccuresIn()+0.5);
                            bmidf = Math.log10(bmidf)/Math.log10(2);

                            //get avg doc length
                            if(avgDocLen==-1.0){
                                String avgLenPath = loadPath+"/avgDocLength.txt";
                                BufferedReader br = new BufferedReader(new FileReader(avgLenPath));
                                String avgLen = br.readLine();
                                avgDocLen = Double.parseDouble(avgLen);
                            }

                            //calculate BM25 for current terms and document
                            double lenNormal = docLength / avgDocLen;
                            double bm_mone = Double.parseDouble(tf.toString()) * (k+1);
                            double bm_mechane = Double.parseDouble(tf.toString()) + k*(1-b+b*lenNormal);
                            bm25= bmidf * (bm_mone / bm_mechane);

                            if (bm25InDocs.containsKey(docID.toString())) {
                                double currentWeight = bm25InDocs.get(docID.toString());
                                currentWeight += bm25;
                                bm25InDocs.put(docID.toString(), currentWeight);
                            } else {
                                bm25InDocs.put(docID.toString(), bm25);
                            }

                            //calculate the ((doc length-index of term in doc)*tf) / doc length
                            double tfAndIndex = docLength - Double.parseDouble(index.toString());
                            tfAndIndex = tfAndIndex / docLength;
                            tfAndIndex *= Double.parseDouble(tf.toString());

                            if (tfIndexInDocs.containsKey(docID.toString())) {
                                double currentWeight = tfIndexInDocs.get(docID.toString());
                                currentWeight += tfAndIndex;
                                tfIndexInDocs.put(docID.toString(), currentWeight);
                            } else {
                                tfIndexInDocs.put(docID.toString(), tfAndIndex);
                            }

                            //calculate cosine similarity (partial, only numerator)
                            if (weightsInDocs.containsKey(docID.toString())) {
                                double currentWeight = weightsInDocs.get(docID.toString());
                                currentWeight += (tfnormal * idf);
                                weightsInDocs.put(docID.toString(), currentWeight);
                            } else {
                                double currentWeight = (tfnormal * idf);
                                weightsInDocs.put(docID.toString(), currentWeight);
                            }
                        }
                    }} catch (Exception e){}
            }
        }

        //calculate CosSim for each document that has one of the query terms.
        for(String s : weightsInDocs.keySet()){

            double docConst = indexer.docWeights.get(s);
            docConst=docConst*parsedQuery.size();
            docConst=Math.sqrt(docConst);
            double cossim = weightsInDocs.get(s)/docConst;
            double bm25 = bm25InDocs.get(s);
            double tfIndex = tfIndexInDocs.get(s);
            weightsInDocs.put(s,(cossim*0.05 + bm25*0.9 + tfIndex*0.05));
        }
        //get 50 most relevant docs
        Map sortedMap=sortByValue(weightsInDocs);
        result = new ArrayList<>(sortedMap.keySet());
        result=result.subList(0,Math.min(50,result.size()));

        return result;
    }

    /**
     * sets the indexer member
     * @param indexer indexer object
     */
    public void setIndexer(Indexer indexer){this.indexer=indexer;}

    private Map<String, Double> sortByValue(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * path of the posting files
     * @param loadPath path of posting files
     */
    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }
}
