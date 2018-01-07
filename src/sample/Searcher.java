package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

public class Searcher {

    HashMap<String,String> queryList; //<query number, query string>
    Pattern regex;
    Parser parser;
    Indexer indexer;
    Ranker ranker;
    ArrayList<String> queryNumbers;

    public Searcher(){
        regex = Pattern.compile("<top>");
        ranker = new Ranker();
    }
    public void setParser(Parser parse){
        parser=parse;
    }
    public void setIndexer(Indexer index){
        indexer=index;
    }


    public HashMap<String, List<String>> multipleQueries(String path){
        queryNumbers = new ArrayList();
        queryList=new HashMap<>();
        HashMap<String, List<String>> results = new HashMap<>(); //<query number, set of doc ids>

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder builder = new StringBuilder();
            String aux = "";
            try {
                while ((aux = br.readLine()) != null) {
                    builder.append(aux);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] docs = regex.split(builder.toString());
            ArrayList<String> tmpQueries=new ArrayList<>();
            for (int j = 1; j < docs.length; j++) {
                tmpQueries.add(docs[j]);
            }
            for(String s : tmpQueries){
                int startName = s.indexOf(("Number: "));
                int start = s.indexOf("<title>");
                int end = s.indexOf("<desc>");
                int startDis=s.indexOf("Description:");
                int endDis=s.indexOf("<narr>");
                String temp=s;
                if (start != -1 && end != -1 && startName!=-1 &&endDis!=-1 && startDis!=-1) {
                    s = s.substring(start + 7, end)+" "+temp.substring(startDis+12,endDis);
                    String number = temp.substring(startName+8,start);
                    queryList.put(number,s);
                    queryNumbers.add(number);
                }
            }
        }
        catch (Exception e){}

        //send each query to get relevant docs
        for(String s : queryList.keySet()){
            results.put(s,getRelevantDocs(queryList.get(s)));
        }
        return results;
    }

    //gets a single query and returns the relevant documents
    public List<String> getRelevantDocs(String query){

        parser.split(query);
        HashSet<String> parsedQuery = parser.getTermsForQuery();

        double currentQueryWeight;
        List<String> list=null;
        HashMap<String, Double> weightsInDocs = new HashMap<>(); //<docID, totalQueryTermsWeight>

        for(String term : parsedQuery){

            if(indexer.getDictionairy().containsKey(term)){
                TermInDictionairy tid = indexer.getDictionairy().get(term);
                double idf = Math.log10(indexer.numOfDocsInCorpus/tid.getNumberOfDocumentsOccuresIn())/Math.log10(2);

                String path= indexer.destinationDirectory+"/"+indexer.directory+"/";
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

                            double k=1.2;
                            double b = 0.75;
                            double bm25;
                            double bmidf = indexer.docLengths.size() - indexer.getDictionairy().get(term).getNumberOfDocumentsOccuresIn() + 0.5;
                            bmidf = bmidf/(indexer.getDictionairy().get(term).getNumberOfDocumentsOccuresIn()+0.5);
                            bmidf = Math.log10(bmidf)/Math.log10(2);
                            double lenNormal = docLength / 244.906;
                            double bm_mone = Double.parseDouble(tf.toString()) * (k+1);
                            double bm_mechane = Double.parseDouble(tf.toString()) + k*(1-b+b*lenNormal);
                            bm25= bmidf * (bm_mone / bm_mechane);


                            double tfAndIndex = docLength - Double.parseDouble(index.toString());
                            tfAndIndex = tfAndIndex / docLength;
                            tfAndIndex *= Double.parseDouble(tf.toString());



                            if (weightsInDocs.containsKey(docID.toString())) {
                                double currentWeight = weightsInDocs.get(docID.toString());
                                currentWeight += (tfnormal * idf)*0.3 + (bm25*0.6) + (tfAndIndex * 0.1);
                                //currentWeight+= docLength/(Double.parseDouble(index.toString()))*0.05;
                                weightsInDocs.put(docID.toString(), currentWeight);
                            } else {
                                double currentWeight = (tfnormal * idf)*0.3 + (bm25*0.6) + (tfAndIndex * 0.1);
                               // currentWeight += docLength/(Double.parseDouble(index.toString()))*0.05;
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
            weightsInDocs.put(s,weightsInDocs.get(s)/docConst);
        }

        //get 50 most relevant docs
        Map sortedMap=sortByValue(weightsInDocs);
        list = new ArrayList<>(sortedMap.keySet());
        list=list.subList(0,Math.min(50,list.size()));
        return list;
    }






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
}
