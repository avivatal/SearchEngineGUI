package sample;

import java.io.*;
import java.util.*;


public class Summarizer {

   // String docname;
    Parser parser;
    String loadPath;
    String directory;

    public Summarizer(){
        parser=new Parser();
    }
    public ArrayList<String> readFile(String docname) {

        ArrayList<String> newArrayList=null;
        try {
            String docpath = loadPath + "/" + directory + "/documents.txt";
            BufferedReader br = new BufferedReader(new FileReader(docpath));
            String aux = "";
            while ((aux = br.readLine()) != null) {
                if(aux.startsWith(docname+":")){
                    break;
                }
            }
            br.close();

            String[] docProperties = aux.split("\\, ");

            String path = loadPath + "/corpus/"  + docProperties[3]+"/"+docProperties[3];
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
                for(int i=0; i<docs.length; i++) {
                    if (!docs[i].equals("")) {
                        String s = docs[i].substring(docs[i].indexOf("<DOCNO>") + 7, docs[i].indexOf("</DOCNO>")).trim();
                        if (s.equals(docname)) {
                            if (s.length() > 6) {
                                int start = docs[i].indexOf("<TEXT>");
                                int end = docs[i].indexOf("</TEXT>");
                                if (start != -1 && end != -1) {
                                    text = docs[i].substring(start + 6, end);
                                    newArrayList = docSummary(text);
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newArrayList;
    }

    public ArrayList<String> docSummary(String document){

        parser.setDocSummary(true);
        parser.setIsQuery(true);
        String[] sentences = document.split("\\. ");
        //check for sentences that shouldn't be splited


        for(int i=0; i<sentences.length; i++){
            String two = sentences[i].substring(sentences[i].length()-2);
            String three = sentences[i].substring(sentences[i].length()-3);
            if(two.equals("Mr") || two.equals("Ms") || two.equals("Lt") || two.equals("St") || three.equals("a.m") || three.equals("p.m") || three.equals("U.S") || three.equals("Mrs")){
                sentences[i+1] = sentences[i]+sentences[i+1];
            }
        }


        HashMap<String, HashSet<String>> sentenceTerms = parser.summarize(sentences);
        HashMap<String, Double> sentenceTF = new HashMap<>();
        HashMap<String, Integer> termsTF = parser.getTermsTF();



        for(String sntnce : sentenceTerms.keySet())
        {
            sentenceTF.put(sntnce,0.0);
            for(String term : sentenceTerms.get(sntnce)){
                double tf;
                boolean isNumber;
                try {
                    double d=Double.parseDouble(term);
                    isNumber=true;
                }
                catch (Exception e){
                    isNumber=false;
                }
                if(!isNumber){
                    if(sentenceTF.containsKey(sntnce)) {
                        tf = termsTF.get(term) + sentenceTF.get(sntnce); //add term tf in doc to current total sentence tf
                    }else{  tf = termsTF.get(term);}

                    sentenceTF.put(sntnce,tf);
                }
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

        ArrayList<String> result = new ArrayList<>();
        for(int i=0; i<sentences.length; i++){
            if(list.contains(sentences[i])){
                result.add("Score: "+(list.indexOf(sentences[i])+1)+"\n "+sentences[i]);
            }
        }
        return result;
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

    public void setLoadPath(String path){
        loadPath=path;
    }

    public void setWithStemming(boolean b){
        if(b){
            directory="withStem";
        }
        else{
            directory="noStem";
        }
        parser.setWithStemming(b);
    }
}
