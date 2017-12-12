package sample;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Parser {
    HashMap<String, ArrayList<String>> parsedDocs;
    HashMap<String, String > months;
    HashSet<String> whitespaces;
    HashSet<String> stopwords;
    Pattern regex;

    public Parser() {
        this.parsedDocs = new HashMap<>();
        whitespaces=new HashSet<>();
        whitespaces.addAll(Arrays.asList(".",",","'","/"));
        months=new HashMap<String, String>();
        months.put("Jan", "01"); months.put("Feb","02");months.put("Mar","03");months.put("Apr","04");months.put("May","05");months.put("Jun","06");months.put("Jul","07");months.put("Aug","08");months.put("Sep","09");months.put("Oct","10");months.put("Nov","11");months.put("Dec","12");
        months.put("January", "01");months.put("February","02");months.put("March","03");months.put("April","04");months.put("June","06");months.put("July","07");months.put("August","08");months.put("September","09");months.put("October","10");months.put("November","11");months.put("December","12");
        months.put("JAN", "01");months.put("FEB","02");months.put("MAR","03");months.put("APR","04");months.put("MAY","05");months.put("JUN","06");months.put("JUL","07");months.put("AUG","08");months.put("SEP","09");months.put("OCT","10");months.put("NOV","11");months.put("DEC","12");
        months.put("JANUARY", "01");months.put("FEBRUARY","02");months.put("MARCH","03");months.put("APRIL","04");months.put("JUNE","06");months.put("JULY","07");months.put("AUGUST","08");months.put("SEPTEMBER","09");months.put("OCTOBER","10");months.put("NOVEMBER","11");months.put("DECEMBER","12");
        regex=Pattern.compile("\\-+|\\s+|\\\n+|\\(+|\\)+|\\;+|\\:+|\\?+|\\!+|\\<+|\\>+|\\}+|\\{+|\\]+|\\[+|\\*+|\\++|\\|+|\\\"+|\\=+|\\#+|\\`+|\\\\+");
    }

    public HashMap<String, ArrayList<String>> getParsedDocs() {
        return parsedDocs;
    }

    public void parse(ArrayList<String> rfDocs, HashSet<String> stopwords){
        parsedDocs.clear();
        this.stopwords=stopwords;
        for(int i=0; i<rfDocs.size(); i++){
            String docName=extractName(rfDocs.get(i));
            parsedDocs.put(docName, new ArrayList<String>());
            parsedDocs.get(docName).add(docName);
            split(extractText(rfDocs.get(i)),docName);
        }
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

        //String[] splited = text.split("\\-+|\\s+|\\\n+|\\(+|\\)+|\\;+|\\:+|\\?+|\\!+|\\<+|\\>+|\\}+|\\{+|\\]+|\\[+|\\*+|\\++|\\|+|\\\"+|\\=+|\\\\+");
        String[] splited = regex.split(text);

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
                        if (whitespaces.contains(splitedStringj.substring(splitedj - 1))) {
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

                                if (splitedlen > index + 1 && whitespaces.contains(splited[index + 1].substring(splited[index + 1].length() - 1))) {
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
                        if (!stopwords.contains(expression)) {
                            parsedDocs.get(i).add(expression);
                        }
                        //save each word in expression
                        if (index != j) {
                            for (String s : expression.split(" ")) {
                                if (!stopwords.contains(s)) {
                                    parsedDocs.get(i).add(s);
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
                            if (splitedlen > j + 1 && isNumeric(tmp)) {
                                splited[j + 1] = tmp;
                                splitedj1 = splited[j + 1].length();

                                //MONTH DD -> DD/MM *OR* MONTH DD YYYY -> DD/MM/YYYY
                                if (Double.parseDouble(splited[j + 1]) > 0 && Double.parseDouble(splited[j + 1]) < 32) {
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
                                        parsedDocs.get(i).add(splited[j + 1] + "/" + months.get(splitedStringj) + "/" + splited[j + 2]);
                                        j += 2;
                                    }
                                    //no year, only month and day
                                    else {
                                        parsedDocs.get(i).add(splited[j + 1] + "/" + months.get(splitedStringj));
                                        j++;
                                    }
                                }

                                //MONTH YYYY -> MM/YYYY
                                else if (splitedlen > j + 1 && ((isNumeric(splited[j + 1]) && (splitedj1 == 4)))) {
                                    parsedDocs.get(i).add(months.get(splitedStringj) + "/" + splited[j + 1]);
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
                            if (!splitedStringj.contains(".") && Integer.parseInt(splitedStringj) > 0 && Integer.parseInt(splitedStringj) < 32) { //check if day
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
                                    parsedDocs.get(i).add(splitedStringj + "/" + months.get(splited[j + 1]) + "/" + splited[j + 2]);
                                    j += 2;
                                }
                                //if year is written in short (YY)
                                else if (splitedlen > j + 2 && isNumeric(temp) && temp.length() == 2) {
                                    //save as date - DD MONTH YY -> DD/MM/YYYY
                                    splited[j + 2] = temp;
                                    parsedDocs.get(i).add(splitedStringj + "/" + months.get(splited[j + 1]) + "/" + "19" + splited[j + 2]);
                                    j += 2;
                                } else {
                                    //save as date - DD MONTH -> DD/MM
                                    parsedDocs.get(i).add(splitedStringj + "/" + months.get(splited[j + 1]));
                                    j++;
                                }
                            }
                        } else { //current word isnt connected to a date, save it
                            if (!stopwords.contains(splitedStringj)) {
                                parsedDocs.get(i).add(splitedStringj.toLowerCase());
                            }
                        }
                    }

                    //***********END DATES CHECK*****************

                    //***********NUMBERS***************
                    else if (isNumeric(splitedStringj) || isDecimal(splitedStringj)) {

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
                            parsedDocs.get(i).add(splitedStringj + " percent");
                            j++;
                        }
                        //number without percent
                        else{
                            parsedDocs.get(i).add(splitedStringj);
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
                            parsedDocs.get(i).add(splitedStringj + " percent");
                        } else { //not a decimal number
                            parsedDocs.get(i).add(splitedStringj + " percent");
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
                            parsedDocs.get(i).add(tempNoCommas);
                        }
                    } else { //regular word - save it

                        if (!stopwords.contains(splitedStringj)) {
                            parsedDocs.get(i).add(splitedStringj.toLowerCase());
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
            while (s.length() > 1 && whitespaces.contains(current + "")) {
                s = s.substring(1);
                current = s.charAt(0);
            }
        }
        if(whitespaces.contains(s)){
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
            while (s.length() > 1 && whitespaces.contains(current + "")) {
                s = s.substring(0, s.length() - 1);
                current = s.charAt(s.length() - 1);
            }

            //remove apostrophe
            if (s.endsWith("'s") || s.endsWith("'S")) {
                s = s.substring(0, s.length() - 2);
            }
      /*      if (isNumeric(s)) {
                if (s.endsWith("f") || s.endsWith("d") || s.endsWith("D") || s.endsWith("F")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (s.endsWith(".d") || s.endsWith(".D")) {
                    s = s.substring(0, s.length() - 2);
                }*/

         //   }
        }
        if(whitespaces.contains(s)){
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


}


