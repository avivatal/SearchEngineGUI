package sample;

import java.io.*;
import java.util.*;


public class Summarizer {

    String docname;
    Parser parser;
    String corpusPath;
    String directory;

    public Summarizer(){
        parser=new Parser();
    }
    public void readFile(String docname) {

        try {
            String docpath = corpusPath + "/" + directory + "/documents.txt";
            BufferedReader br = new BufferedReader(new FileReader(docpath));
            String aux = "";
            while ((aux = br.readLine()) != null) {
                if(aux.startsWith(docname)){
                    break;
                }
            }
            br.close();

            String[] docProperties = aux.split("\\, ");

            String path = corpusPath + "/"  + docProperties[3]+"/"+docProperties[3];
            BufferedReader docReader = new BufferedReader(new FileReader(path));
            StringBuilder builder = new StringBuilder();
            String filetxt = "";
            try {
                while ((filetxt = docReader.readLine()) != null) {
                    builder.append(filetxt);
                }
                br.close();

                //extract document
                String[] docs = builder.toString().split("<DOC>");
                String text="";
                for(int i=0; i<docs.length; i++){
                    String s = docs[i].substring(docs[i].indexOf("<DOCNO>")+7, docs[i].indexOf("</DOCNO>")).trim();
                    if(s.equals(docname)){
                        if(s.length()>6) {
                            int start = s.indexOf("<TEXT>");
                            int end = s.indexOf("</TEXT>");
                            if (start != -1 && end != -1) {
                                text = docs[i].substring(start + 6, end);
                                docSummary(text);
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void docSummary(String document){

        parser.setDocSummary(true);
        parser.setIsQuery(true);
        HashMap<String, HashSet<String>> sentenceTerms = parser.summarize(document);
        HashMap<String, Double> sentenceTF = new HashMap<>();
        HashMap<String, Integer> termsTF = parser.getTermsTF();

        for(String sntnce : sentenceTerms.keySet())
        {
            sentenceTF.put(sntnce,0.0);
            int totalSentenceTF=0;
            for(String term : sentenceTerms.get(sntnce)){
                double tf;
                if(sentenceTF.containsKey(sntnce)) {
                    tf = termsTF.get(term) + sentenceTF.get(sntnce); //add term tf in doc to current total sentence tf
                }else{  tf = termsTF.get(term);}

                sentenceTF.put(sntnce,tf);
            }

            //  double termcount=sentenceTF.get(sntnce)/sentenceTerms.get(sntnce).size();
            //normalize tf sum by word count in sentence
            String temp = sntnce;
            double wordcount = temp.length()-temp.replace(" ","").length();
            wordcount=sentenceTF.get(sntnce)/wordcount;
            if(Double.isNaN(wordcount) || Double.isInfinite(wordcount)){
                sentenceTF.remove(sntnce);
            }
            else{
                sentenceTF.put(sntnce,wordcount);}

        }



        Map sortedMap=sortByValue(sentenceTF);
        List<String> list = new ArrayList<>(sortedMap.keySet());
        list=list.subList(0,5);



      /*  for(String sentence : sentences){

            parser.setIsQuery(true);
            parser.split(sentence);
            parser.getTermsForQuery()

        }*/


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

    public void setCorpusPath(String path){
        corpusPath=path;
    }

    public void setWithStemming(boolean b){
        if(b){
            directory="withStem";
        }
        else{
            directory="noStem";
        }
    }
}
