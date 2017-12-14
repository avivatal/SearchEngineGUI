package sample;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Parser {
    HashMap<String, String > months;
    HashMap<String,String> whitespaces;
    HashMap<String,String> stopwords;
    Stemmer stemmer;
    String destinationDirectory;
    HashMap<String, String> beforeAfterStem;
    String directory; //with/without stem directory
    HashMap<String, HashMap<String,TermInDoc>> stemmedTerms;
    int numberOfTermsInDoc;
    String docName; //current document
    TermInDoc maxTF; //max tf in current document
    String mostFrequentTerm; //most frequent term in current document
    HashMap<String,String> documentProperties; //pointer for each doc to the line number of the doc properties saved in disk.
    int docNumber; //counter for documents
    boolean withStemming;
    PrintWriter writer;
    // Pattern regex;

    public Parser() {
        stemmedTerms=new HashMap<>();
        stemmer=new Stemmer();
        beforeAfterStem=new HashMap<>();
        documentProperties=new HashMap<>();
        docNumber=0;
        numberOfTermsInDoc=0;
        //  this.parsedDocs = new HashMap<>();
        whitespaces=new HashMap<>();
        whitespaces.put(".",null); whitespaces.put(",",null);whitespaces.put("'",null); whitespaces.put("/",null);
        months=new HashMap<String, String>();
        months.put("Jan", "01"); months.put("Feb","02");months.put("Mar","03");months.put("Apr","04");months.put("May","05");months.put("Jun","06");months.put("Jul","07");months.put("Aug","08");months.put("Sep","09");months.put("Oct","10");months.put("Nov","11");months.put("Dec","12");
        months.put("January", "01");months.put("February","02");months.put("March","03");months.put("April","04");months.put("June","06");months.put("July","07");months.put("August","08");months.put("September","09");months.put("October","10");months.put("November","11");months.put("December","12");
        months.put("JAN", "01");months.put("FEB","02");months.put("MAR","03");months.put("APR","04");months.put("MAY","05");months.put("JUN","06");months.put("JUL","07");months.put("AUG","08");months.put("SEP","09");months.put("OCT","10");months.put("NOV","11");months.put("DEC","12");
        months.put("JANUARY", "01");months.put("FEBRUARY","02");months.put("MARCH","03");months.put("APRIL","04");months.put("JUNE","06");months.put("JULY","07");months.put("AUGUST","08");months.put("SEPTEMBER","09");months.put("OCTOBER","10");months.put("NOVEMBER","11");months.put("DECEMBER","12");
        //   regex=Pattern.compile("\\-+|\\s+|\\\n+|\\(+|\\)+|\\;+|\\:+|\\?+|\\!+|\\<+|\\>+|\\}+|\\{+|\\]+|\\[+|\\*+|\\++|\\|+|\\\"+|\\=+|\\#+|\\`+|\\\\+");
    }

    public void setWriter(){
        try {
            File dir = new File(destinationDirectory + "/" + directory);
            dir.mkdir();
            writer = new PrintWriter(destinationDirectory + "/" + directory + "/" + "documents.txt", "UTF-8");
        }catch(Exception e){}
    }

    public HashMap<String, HashMap<String,TermInDoc>> getStemmedTerms() {
        return stemmedTerms;
    }

    public void parse(ArrayList<String> rfDocs, HashMap<String, String> stopwords){
        stemmedTerms.clear();
        this.stopwords=stopwords;
        try {
            //PrintWriter writer = new PrintWriter(destinationDirectory + "/" + directory + "/" + "documents.txt", "UTF-8");
            //iterate over all docs, for each word in doc - parse and stem (if checked)
            for(int i=0; i<rfDocs.size(); i++){
                docNumber++;
                numberOfTermsInDoc=0;
                mostFrequentTerm="";
                maxTF = new TermInDoc("null", 0, false);
                docName=extractName(rfDocs.get(i));
                //parsedDocs.put(docName, new ArrayList<String>());
                //parsedDocs.get(docName).add(docName);
                documentProperties.put(docName,docNumber+"");
                split(extractText(rfDocs.get(i)),docName);

                //write document properties
                writer.println(docName+": "+numberOfTermsInDoc+", "+mostFrequentTerm+", "+maxTF.getTf()); //for each document save properties on disk
                writer.flush();
            }

        }catch(Exception e){e.printStackTrace();};
    }

    public String extractText(String s){
        if(s.length()>6) {
            int start = s.indexOf("<TEXT>");
            int end = s.indexOf("</TEXT>");
            if (start != -1 && end != -1) {
                s = s.substring(start + 6, end);
            }
        }
        return s;
    }

    public String extractName(String s){
        s = s.substring(s.indexOf("<DOCNO>")+7, s.indexOf("</DOCNO>")).trim();
        return s;
    }

    public void split(String text, String i) {

        String[] splited = text.split("\\-+|\\s+|\\\n+|\\(+|\\)+|\\;+|\\:+|\\?+|\\!+|\\<+|\\>+|\\}+|\\{+|\\]+|\\[+|\\*+|\\++|\\|+|\\\"+|\\=+|\\#+|\\`+|\\\\+");
        // String[] splited = regex.split(text);

        int splitedlen = splited.length;

        for (int j = 0; j < splitedlen; j++) {

            //avoid parsing empty strings
            splited[j].trim();
            splited[j] = cleanFromStart(splited[j]);
            if (!(splited[j].equals("") || splited[j].equals(" ")) && splited[j].length() > 0) {


                int splitedj = splited[j].length();
                String splitedStringj = splited[j];
                int splitedj1 = 0;
                if (splitedlen > j + 1) {
                    splitedj1 = splited[j + 1].length();
                }

                //**************CAPITAL LETTER EXPRESSIONS********************
                //if the first letter is in uppercase, or the first char is a whitespace and the second char is uppercase
                if (((splitedj > 1 && Character.isUpperCase(splitedStringj.charAt(0))))) {

                    if (!months.containsKey(splitedStringj)) {

                        //check next word
                        boolean bool = true;
                        if (whitespaces.containsKey(splitedStringj.substring(splitedj - 1))) {
                            bool = false; //there is a whitespace after current word- end of expression
                        }
                        splited[j] = cleanFromEnd(splitedStringj);
                        splitedStringj = splited[j];
                        splitedj = splitedStringj.length();
                        StringBuilder builder = new StringBuilder();
                        builder.append(splitedStringj.toLowerCase());
                        int index = j;

                        while (bool) {
                            //if next word starts with uppercase and isnt a month
                            if (splitedlen > index + 1 && splited[index + 1].length() > 0 && Character.isUpperCase(splited[index + 1].charAt(0)) && !months.containsKey(splited[index + 1])) {

                                if ( whitespaces.containsKey(splited[index + 1].substring(splited[index + 1].length() - 1))) {
                                    bool = false;
                                    splited[index + 1] = cleanFromStart(cleanFromEnd(splited[index + 1]));
                                    if (splitedlen > j + 1) {
                                        splitedj1 = splited[j + 1].length();
                                    }
                                    builder.append(" " + splited[index + 1].toLowerCase().substring(0, splited[index + 1].length() - 1));
                                    index++;
                                } else {
                                    splited[index + 1] = cleanFromStart(cleanFromEnd(splited[index + 1]));
                                    if (splitedlen > j + 1) {
                                        splitedj1 = splited[j + 1].length();
                                    }
                                    builder.append(" " + splited[index + 1].toLowerCase());
                                    index++;
                                }
                            }
                            //if next word is "of" check next word
                            else if (splitedlen > index + 2 && splited[index + 1].equals("of")) {
                                int counter = index;
                                while (splited[counter + 2].equals(" ") || splited[counter + 2].equals("")) {
                                    counter++;
                                }
                                if (Character.isUpperCase(splited[counter + 2].charAt(0))) {
                                    splited[index + 1] = cleanFromStart(cleanFromEnd(splited[index + 1]));
                                    if (splitedlen > j + 1) {
                                        splitedj1 = splited[j + 1].length();
                                    }
                                    builder.append(" " + splited[index + 1].toLowerCase());
                                    index = counter + 1;
                                } else {
                                    bool = false;
                                } //next word after 'of' isnt upper case
                            }
                            //next word is space or not uppercase
                            else {
                                if (splitedlen > index + 1 && splited[index + 1].equals(" ")) {
                                    index++;
                                } else {
                                    bool = false;
                                }
                            }

                        }
                        String expression = builder.toString();
                        //save expression
                        if (!stopwords.containsKey(expression)) {
                            stemWord(expression);
                        }
                        //save each word in expression
                        if (index != j) {
                            for (String s : expression.split(" ")) {
                                if (!stopwords.containsKey(s)) {
                                    stemWord(s);
                                }
                            }
                            j = index;
                        }
                    } else {
                        //current string is a month
                        String tmp = "";
                        if (splitedlen > j + 1) {
                            tmp = cleanFromStart(cleanFromEnd(splited[j + 1]));

                            //check if next string is day or year
                            if (isNumeric(tmp)) {
                                splited[j + 1] = tmp;
                                splitedj1 = splited[j + 1].length();

                                //MONTH DD -> DD/MM *OR* MONTH DD YYYY -> DD/MM/YYYY
                                if (Integer.parseInt(splited[j + 1]) > 0 && Integer.parseInt(splited[j + 1]) < 32) {
                                    if (splitedj1 == 1) {
                                        splited[j + 1] = "0" + splited[j + 1];
                                    }

                                    //is next next string a year
                                    String temp = "";
                                    if (splitedlen > j + 2) {
                                        temp = cleanFromStart(cleanFromEnd(splited[j + 2]));
                                        ;
                                    }
                                    if (splitedlen > j + 2 && isNumeric(temp) && temp.length() == 4) {
                                        //save as date
                                        splited[j + 2] = temp;
                                        stemWord(splited[j + 1] + "/" + months.get(splitedStringj) + "/" + splited[j + 2]);
                                        j += 2;
                                    }
                                    //no year, only month and day
                                    else {
                                        stemWord(splited[j + 1] + "/" + months.get(splitedStringj));
                                        j++;
                                    }
                                }

                                //MONTH YYYY -> MM/YYYY
                                else if (((isNumeric(splited[j + 1]) && (splitedj1 == 4)))) {
                                    stemWord(months.get(splitedStringj) + "/" + splited[j + 1]);
                                    j++;
                                }
                            }

                        }

                    }
                }

                //********END CAPITAL LETTERS CHECK***********

                else {
                    //remove dots, commas and apostrophes
                    splited[j] = cleanFromEnd(splitedStringj);
                    splited[j].trim();
                    splitedStringj = splited[j];
                    splitedj = splitedStringj.length();

                    //**********CHECK IF DATE*****************


                    //if NEXT string is month
                    String tmp = "";
                    if (splitedlen > j + 1) {
                        tmp = cleanFromStart(cleanFromEnd(splited[j + 1]));
                    }

                    if (months.containsKey(tmp)) {

                        splited[j + 1] = tmp; //cleaned

                        //DDth MONTH YYYY -> DD/MM/YYYY
                        if (splitedj > 2 && splitedStringj.substring(splitedj - 2).equals("th")) { //if current string ends with 'th'
                            if (isNumeric(splitedStringj.substring(0, splitedj - 2))) { //check if its numeric without the 'th'
                                splited[j] = splitedStringj.substring(0, splitedj - 2); //remove the 'th'
                                splitedStringj = splited[j];
                                splitedj -= 2;
                            }
                        }
                        if (isNumeric(splitedStringj)) {
                            if (Integer.parseInt(splitedStringj) > 0 && Integer.parseInt(splitedStringj) < 32) { //check if day
                                //add zero if D and not DD
                                if (splitedj == 1) {
                                    splited[j] = "0" + splitedStringj;
                                    splitedStringj = splited[j];
                                }
                                //check for year
                                String temp = "";
                                if (splitedlen > j + 2) {
                                    temp = cleanFromStart(cleanFromEnd(splited[j + 2]));
                                    ;
                                }
                                if (splitedlen > j + 2 && isNumeric(temp) && temp.length() == 4) {
                                    splited[j + 2] = temp;
                                    //save as date - DD MONTH YYYY -> DD/MM/YYYY
                                    stemWord(splitedStringj + "/" + months.get(splited[j + 1]) + "/" + splited[j + 2]);
                                    j += 2;
                                }
                                //if year is written in short (YY)
                                else if (splitedlen > j + 2 && isNumeric(temp) && temp.length() == 2) {
                                    //save as date - DD MONTH YY -> DD/MM/YYYY
                                    splited[j + 2] = temp;
                                    stemWord(splitedStringj + "/" + months.get(splited[j + 1]) + "/" + "19" + splited[j + 2]);
                                    j += 2;
                                } else {
                                    //save as date - DD MONTH -> DD/MM
                                    stemWord(splitedStringj + "/" + months.get(splited[j + 1]));
                                    j++;
                                }
                            }
                        } else { //current word isnt connected to a date, save it
                            if (!stopwords.containsKey(splitedStringj)) {
                                stemWord(splitedStringj.toLowerCase());
                            }
                        }
                    }

                    //***********END DATES CHECK*****************

                    //***********NUMBERS***************
                    else if (isDecimal(splitedStringj)) {

                        //is decimal number
                        if (splitedStringj.contains(".")) {
                        /*    try {
                                BigDecimal bd = new BigDecimal(splitedStringj);
                                bd = bd.setScale(2, RoundingMode.HALF_UP);
                                splited[j] = bd.toString();
                                splitedStringj = splited[j];
                                splitedj = splitedStringj.length();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                System.out.println("PROBLEM: " + splitedStringj); //////////delete
                            }*/
                            splited[j] = round(splitedStringj);
                            splitedStringj = splited[j];
                            splitedj = splitedStringj.length();

                            //if second decimal digit is zero, remove it
                            try {
                                if (splitedStringj.endsWith("0") && splitedj > 1) {
                                    splited[j] = splitedStringj.substring(0, splitedj - 1);
                                    splitedStringj = splited[j];
                                    splitedj--;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println(splitedStringj + " " + splitedj);
                            }
                        }

                        //is percent (word)
                        if (splitedlen > j + 1 && (splited[j + 1].equals("percent") || splited[j + 1].equals("percentage"))) {
                            stemWord(splitedStringj + " percent");
                            j++;
                        }
                        //number without percent
                        else{
                            stemWord(splitedStringj);
                        }
                    }
                    //is percent
                    else if ((splitedj > 1 && splitedStringj.endsWith("%"))) {
                        splited[j] = splitedStringj.substring(0, splitedj - 1); //remove '%'
                        splitedStringj = splited[j];
                        splitedj--;
                        //is decimal
                        if (splitedStringj.contains(".") && isNumeric(splitedStringj)) {
                          /*  try {
                                BigDecimal bd = new BigDecimal(splitedStringj);
                                bd = bd.setScale(2, RoundingMode.HALF_UP);
                                splited[j] = bd.toString();
                                splitedStringj = splited[j];
                                splitedj = splitedStringj.length();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                System.out.println("PROBLEM: " + splitedStringj); //delete!
                            }*/
                            splited[j]=round(splitedStringj);
                            splitedStringj = splited[j];
                            splitedj = splitedStringj.length();
                            //ends with zero - remove it
                            if (splitedStringj.endsWith("0")) {
                                splited[j] = splitedStringj.substring(0, splitedj - 1);
                                splitedStringj = splited[j];
                                splitedj--;
                            }
                            //save as percent
                            stemWord(splitedStringj + " percent");
                        } else { //not a decimal number
                            stemWord(splitedStringj + " percent");
                        }
                    }

                    //number with commas
                    else if (splitedStringj.contains(",")) {
                        String tempNoCommas = splitedStringj;
                        tempNoCommas = tempNoCommas.replaceAll(",", "");
                        if (isNumeric(tempNoCommas)) {
                            //decimal
                            if (tempNoCommas.contains(".") && isDecimal(tempNoCommas)) {
                                /*try {
                                    BigDecimal bd = new BigDecimal(tempNoCommas);
                                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                                    tempNoCommas = bd.toString();
                                } catch (Exception e) {
                                    System.out.println(tempNoCommas);
                                    e.printStackTrace();
                                }*/
                                tempNoCommas=round(tempNoCommas);
                            }
                            stemWord(tempNoCommas);
                        }
                    } else { //regular word - save it

                        if (!stopwords.containsKey(splitedStringj)) {
                            stemWord(splitedStringj.toLowerCase());
                        }
                    }

                }
            }
        }
    }








    public boolean isNumeric(String str)
    {
        try
        {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private String cleanFromStart(String s){
        if(s.length()>0) {
            //clean from start
            char current = s.charAt(0);
            while (s.length() > 1 && whitespaces.containsKey(current + "")) {
                s = s.substring(1);
                current = s.charAt(0);
            }
        }
        if(whitespaces.containsKey(s)){
            s="";
        }
        return s;
    }

    private String cleanFromEnd(String s) {
        if(s.length()>0) {
            char current = s.charAt(0);
            //clean from end
            if (s.length() > 0) {
                current = s.charAt(s.length() - 1);
            }
            while (s.length() > 1 && whitespaces.containsKey(current + "")) {
                s = s.substring(0, s.length() - 1);
                current = s.charAt(s.length() - 1);
            }

            //remove apostrophe
            if (s.endsWith("'s") || s.endsWith("'S")) {
                s = s.substring(0, s.length() - 2);
            }

        }
        if(whitespaces.containsKey(s)){
            s="";
        }
        return s;
    }

    private String round(String s){
        int dot = s.indexOf('.');
        String first = s.substring(0,dot);
        String sec=s.substring(dot+1);
        int finScnd=0;
        if(sec.length()>2){

            if(sec.charAt(2)>5){
                finScnd = (Integer.parseInt(sec.charAt(1)+"")+1);
            }
            else{
                finScnd=(Integer.parseInt(sec.charAt(1)+""));
            }
            return first+"."+sec.charAt(0)+finScnd+"";
        }
        return s;
    }

    private boolean isDecimal(String str){
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    //destination directory for all files to be saved in
    public void setDestinationDirectory(String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
    }
    //directory for with stemming files or without stemming files
    public void setDirectory(String directory){
        this.directory=directory;
    }

    public void setWithStemming(boolean withStemming){
        this.withStemming=withStemming;
    }
    public void stemWord(String word){

        numberOfTermsInDoc++; //number of terms in current document BEFORE stem
        String term="";
        if(withStemming) {
            if (!beforeAfterStem.containsKey(word)) {
                stemmer.add(word.toCharArray(), word.length());
                stemmer.stem();
                term = stemmer.toString();
                beforeAfterStem.put(word, term);
            } else {
                term = beforeAfterStem.get(word);
            }
        }
        else{
            term=word;
        }

        //if term is new in hashmap
        if (!stemmedTerms.containsKey(term)) {
            TermInDoc tid = new TermInDoc(docName, 1, false);
            if (numberOfTermsInDoc < 100) {
                tid.setInFirst100Terms(true);
            }
            HashMap<String, TermInDoc> map = new HashMap<>();
            map.put(docName, tid);
            stemmedTerms.put(term, map);
            if (tid.getTf() > maxTF.getTf()) {
                maxTF = tid;
                mostFrequentTerm=term;
            }
        }
        //term appears in hashmap
        else {
            //if doc appears in stemmed term - update TF
            if (stemmedTerms.get(term).containsKey((docName))) {
                (stemmedTerms.get(term)).get(docName).setTf();
            }
            //if doc doesnt appear in stemmed term, create new TermInDoc entry
            else {
                stemmedTerms.get(term).put(docName, new TermInDoc(docName, 1, false));
                if (numberOfTermsInDoc < 100) {
                    stemmedTerms.get(term).get(docName).setInFirst100Terms(true);
                }
            }
            if (stemmedTerms.get(term).get(docName).getTf() > maxTF.getTf()) {
                maxTF = stemmedTerms.get(term).get(docName);
                mostFrequentTerm=term;
            }
        }

    }


}


