package sample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {



    HashMap<String,TermInDictionairy> dictionairy;
    int numberOfTempPostingFiles=0;
    Cache cache;
    String destinationDirectory;

    public Indexer(String destinationDirectory) {
        dictionairy = new HashMap<>();
        cache = new Cache();
        this.destinationDirectory=destinationDirectory;
    }

    public HashMap<String, TermInDictionairy> getDictionairy() {
        return dictionairy;
    }

    public void index(HashMap<String, HashMap<String,TermInDoc>> stemmedTerms){

        for (Map.Entry<String,HashMap<String,TermInDoc>>  entry : stemmedTerms.entrySet()) {

            //if word doesnt appear in dictionairy - add it
            if(!dictionairy.containsKey(entry.getKey())){
                dictionairy.put(entry.getKey(),new TermInDictionairy());
            }
            //word is in dictionairy - update numberOfAppearancesInCorpus
            else{
                dictionairy.get(entry.getKey()).setNumberOfAppearancesInCorpus();
            }

            //add to cache
            cache.addToCache(entry);
        }
        //add pointers from dictionairy to cache
        for(String termInCache : cache.getCache().keySet()){
            dictionairy.get(termInCache).setPointerToTermInCache(cache.getCache().get(termInCache));
        }


        //temp posting file

        //sort stemmedTerms map
        SortedSet<String> sortedKeys = new TreeSet<String>(stemmedTerms.keySet());

        try {
            //create file for temp posting file
            numberOfTempPostingFiles++;
            PrintWriter writer = new PrintWriter(destinationDirectory+"/"+numberOfTempPostingFiles+".txt" , "UTF-8");


            //write each posting entry to file
            for (String key : sortedKeys) {
                //key = term, value = <docID, TermInDoc>
                HashMap<String, TermInDoc> value = stemmedTerms.get(key);
                //string to write to file
                StringBuilder line = new StringBuilder();
                line.append(key+": ");
                for (TermInDoc termInDoc : value.values()) {
                    line.append(" "+termInDoc.toString());
                }
                writer.println(line.toString());
                writer.flush();

            }
            writer.close();
        }
        catch (Exception e){ e.printStackTrace();}
    }

    private String mergeSameTerm(String first, String second){
        first+= second.substring(second.indexOf(": ")+1);
        return first;
    }

    //iterates over the temporary posting files and merges them into final posting files using merge sort.
    public void mergeTempPostings(){
        int start=1;
        int end=numberOfTempPostingFiles;
        int iterations = (int)(Math.ceil(Math.log10(end)/Math.log10(2))+1); //height of merging tree is ceil(log2(#files))+1

        //merge levels iterations
        for(int i=1; i<iterations; i++){
            System.out.println("end: "+end+" iteratons: "+iterations+" start: "+start);
            //last merge - merge into alphabetical files
            if(end-start==1){
                mergeAlphabetic(start, end);
            }
            //not last merge
            else {
                int tmp = merge(start, end, end + 1);
                start = end + 1;
                end = tmp;
            }
        }
    }

    //merges one level of the merge sort tree. start index is the lowest file number and end index is the highest file number to be merged in current iteration. nextCounter is the number of the new file to be created
    public int merge(int startIndex, int endIndex, int nextcounter) {

        int currentIndex = startIndex; //index of sorted files
        int counter = nextcounter; //index of new file
        int ans=0;

        try {
        //check if uneven - only rename the last file to the last counter of this iteration
        if((endIndex-startIndex)%2==0){
            int lastFileIndex = nextcounter+((endIndex-startIndex)/2);
            Path source = Paths.get(destinationDirectory+"/"+endIndex+".txt");
            Files.move(source, source.resolveSibling(destinationDirectory+"/"+lastFileIndex+".txt"));
            endIndex--;
            ans++;

        }

        //haven't finished this iteration
            while (currentIndex < endIndex) {

                //create new file
                PrintWriter writer = new PrintWriter(destinationDirectory+"/"+(counter++)+".txt", "UTF-8");

                //open files to merge
                File file1=new File(destinationDirectory+"/"+currentIndex+".txt");
                File file2=new File(destinationDirectory+"/"+(currentIndex+1)+".txt");
                FileReader fileReader1 = new FileReader(file1);
                FileReader fileReader2 = new FileReader(file2);
                BufferedReader reader1=new BufferedReader(fileReader1);
                BufferedReader reader2=new BufferedReader(fileReader2);

                //read line from each file and compare
                String line1 = reader1.readLine();
                String line2 = reader2.readLine();
                try {
                    while (line1 != null && line2 != null) {
                        if (line1.length() > 0 && line2.length() > 0 && line1.contains(":") && line2.contains(":")) {
                            if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) < 0) {
                                writer.println(line1);
                                line1 = reader1.readLine();
                            } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) > 0) {
                                writer.println(line2);
                                line2 = reader2.readLine();
                            } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) == 0) {
                                writer.println(mergeSameTerm(line1, line2));
                                line1 = reader1.readLine();
                                line2 = reader2.readLine();
                            }
                        }
                        writer.flush();
                    }
                }catch (StringIndexOutOfBoundsException e) {
                    System.out.println(line1);
                    System.out.printf(line2);
                }

                //if reached the end of only one of the files - write the rest of the other file
                while(line1!=null){
                    writer.println(line1);
                    line1=reader1.readLine();
                }

                while(line2!=null){
                    writer.println(line2);
                    line2=reader1.readLine();
                }

                //delete old files and close readers and writers
                fileReader1.close();
                fileReader2.close();
                file1.delete();
                file2.delete();
                writer.close();
                reader1.close();
                reader2.close();

                currentIndex+=2;
            }
        }catch(Exception e){
            e.printStackTrace();
            return counter;
        }

        //return the new end index for the next iteration
        return counter-1+ans;
    }

    //last merge - merges 2 posting files into final posting files - 1 file per each letter, and 1 file for non-letters
    public void mergeAlphabetic(int start, int end){

        try {
            char current = 'a';
            //CREATE NEW FILE for non-letters (symbols and numbers)
            PrintWriter writer = new PrintWriter(destinationDirectory+"/"+"nonLetters.txt", "UTF-8");

            //OPEN FILES TO MERGE
            File file1=new File(destinationDirectory+"/"+start+".txt");
            File file2=new File(destinationDirectory+"/"+end+".txt");
            FileReader fileReader1 = new FileReader(file1);
            FileReader fileReader2 = new FileReader(file2);
            BufferedReader reader1=new BufferedReader(fileReader1);
            BufferedReader reader2=new BufferedReader(fileReader2);

            //READ LINE FROM EACH FILE
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();

            //iterate only over non letters
            int lineCounter=1;
            while(line1!=null && line2!=null && ((line1.charAt(0)<97)|| line1.charAt(0)>122) && ((line2.charAt(0)<97)|| line2.charAt(0)>122)) {
                if(line1.length()>0 && line2.length()>0 && line1.contains(":") && line2.contains(":")) {
                    if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) < 0) {
                        writer.println(line1);
                        updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                        line1 = reader1.readLine();
                    } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) > 0) {
                        writer.println(line2);
                        updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                        line2 = reader2.readLine();
                    } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) == 0) {
                        writer.println(mergeSameTerm(line1, line2));
                        updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                        line1 = reader1.readLine();
                        line2 = reader2.readLine();
                    }
                }
                writer.flush();
            }

            //IF REACHED THE END OF THE NON LETTERS IN ONLY ONE OF THE FILES - WRITE THE REST OF THE NEXT FILE UNTIL LETTER
            while(line1!=null && line1.charAt(0)<97|| line1.charAt(0)>122){ //first file still contains non-letters
                writer.println(line1);
                updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                line1=reader1.readLine();
            }

            while(line2!=null && line2.charAt(0)<97|| line2.charAt(0)>122){
                writer.println(line2);
                updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                line2=reader1.readLine();
            }
            writer.flush();

            //MERGE THE FILES - EACH LETTER IN DIFFERENT FILE
            char currentChar = 'a';
            while(currentChar<123){
                lineCounter=1;
                PrintWriter letterWriter = new PrintWriter(destinationDirectory+"/"+currentChar+".txt", "UTF-8");
                while(line1!=null && line2!=null && line1.charAt(0)==currentChar && line2.charAt(0)==currentChar) {
                    if(line1.length()>0 && line2.length()>0 && line1.contains(":") && line2.contains(":")) {
                        if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) < 0) {
                            letterWriter.println(line1);
                            updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                            line1 = reader1.readLine();
                        } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) > 0) {
                            letterWriter.println(line2);
                            updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                            line2 = reader2.readLine();
                        } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) == 0) {
                            letterWriter.println(mergeSameTerm(line1, line2));
                            updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                            line1 = reader1.readLine();
                            line2 = reader2.readLine();
                        }
                        letterWriter.flush();
                    }

                }

                //IF REACHED THE END OF THE NON LETTERS IN ONLY ONE OF THE FILES - WRITE THE REST OF THE NEXT FILE UNTIL LETTER
                while(line1!=null && line1.charAt(0)==currentChar){ //first file still contains old letter
                    letterWriter.println(line1);
                    updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                    line1=reader1.readLine();
                }


                while(line2!=null && line2.charAt(0)==currentChar){
                    letterWriter.println(line2);
                    updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                    line2=reader1.readLine();
                }
                letterWriter.flush();
                currentChar++;
                letterWriter.close();
            }

            //DELETE OLD FILES AND CLOSE READERS
            fileReader1.close();
            fileReader2.close();
            file1.delete();
            file2.delete();
            writer.close();

            reader1.close();
            reader2.close();



        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void updatePointerToPosting(String term, int lineNumber){
        dictionairy.get(term).setPointerToPosting(lineNumber);
    }
}
