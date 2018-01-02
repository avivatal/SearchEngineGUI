package sample;
import java.io.*;
import java.util.*;


public class Summarizer {

    String docname;
    Parser parser;
    String corpusPath;

    public Summarizer(){
        parser=new Parser();
    }
    public void readFile(String docname) {

        try {
            String path = corpusPath + "/" + docname + "/" + docname;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        String document;
       // docSummary(document);
    }

    public void docSummary(String document){

        parser.setDocSummary(true);
        parser.setIsQuery(true);
        HashMap<String, HashSet<String>> sentenceTerms = parser.summarize(document);
        HashMap<String, Integer> sentenceTF = new HashMap<>();
        HashMap<String, Integer> termsTF = parser.getTermsTF();

        for(String sntnce : sentenceTerms.keySet())
        {
            sentenceTF.put(sntnce,0);
            int totalSentenceTF=0;
            for(String term : sentenceTerms.get(sntnce)){
                int tf=termsTF.get(term)+sentenceTF.get(term); //add term tf in doc to current total sentence tf
                sentenceTF.put(sntnce,tf);
            }
        }

        Map sortedMap=sortByValue(sentenceTF);
        List<Integer> list = new ArrayList<>(sortedMap.keySet());
        list=list.subList(0,5);



      /*  for(String sentence : sentences){

            parser.setIsQuery(true);
            parser.split(sentence);
            parser.getTermsForQuery()

        }*/


    }

    private Map<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
