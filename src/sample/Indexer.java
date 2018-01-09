package sample;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {


    HashMap<String,TermInDictionairy> dictionairy; //terms and their dictionary entries
    int numberOfTempPostingFiles=0; //number of temporary posting files created so far in current run
    Cache cache;
    String destinationDirectory; //directory of the stem/noStem directory
    String directory;   //stem/noStem directory


    HashMap<String, Double> docWeights;

    HashMap<String, Integer> docLengths;
    int numOfDocsInCorpus;

    public Indexer() {
        dictionairy = new HashMap<>();
        cache = new Cache();
        docWeights = new HashMap<>();

    }

    public void setDocWeights(HashMap<String, Double> docWeights) {
        this.docWeights = docWeights;
    }

    public void setDocLengths(HashMap<String, Integer> docLengths) {
        this.docLengths = docLengths;
        numOfDocsInCorpus=docLengths.size();
    }
    public HashMap<String, Integer> getDocLengths() {
        return docLengths;
    }
    public HashMap<String, Double> getDocWeights() {
        return docWeights;
    }

    /**
     * sets the directory of the stem/noStem directory
     * @param directory name of directory to save posting files in
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }


    /**
     * sets the directory which the stem/noStem directories should be in
     * @param destinationDirectory
     */
    public void setPath(String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
    }


    /**
     * dictionary getter
     * @return the current dictionary
     */
    public HashMap<String, TermInDictionairy> getDictionairy() {
        return dictionairy;
    }

    /**
     * The indexing function.
     * For each indexed term, check if needs to be added to dictionary and updates the appropriate fields in dictionary entry.
     * Creates a temporary posting file for all terms in current group of files.
     * @param stemmedTerms the terms to be indexed. not necessarily stemmed - according to the stem checkbox.
     */
    public void index(HashMap<String, HashMap<String,TermInDoc>> stemmedTerms){

        for (Map.Entry<String,HashMap<String,TermInDoc>>  entry : stemmedTerms.entrySet()) {

            //if word doesnt appear in dictionairy - add it
            if(!dictionairy.containsKey(entry.getKey())){
                dictionairy.put(entry.getKey(),new TermInDictionairy(entry.getKey()));
            }
            dictionairy.get(entry.getKey()).setNumberOfDocumentsOccuresIn(entry.getValue().size());
            int sumTF=0;
            for(TermInDoc tid : entry.getValue().values()){
                sumTF += tid.getTf();
            }
            dictionairy.get(entry.getKey()).setTotalOccurencesInCorpus(sumTF);
        }

    //create temp posting file

    //sort stemmedTerms map
    SortedSet<String> sortedKeys = new TreeSet<String>(stemmedTerms.keySet());

        try {
        //create file for temp posting file
        numberOfTempPostingFiles++;
        PrintWriter writer = new PrintWriter(destinationDirectory+"/"+directory+"/"+numberOfTempPostingFiles+".txt" , "UTF-8");


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

    /**
     * merges 2 posting records of same terms into a single posting record
     * @param first posting record of term X
     * @param second posting record of term Y
     * @return merged posting record of term X from the 2 records given
     */
    private String mergeSameTerm(String first, String second){
        first+= second.substring(second.indexOf(": ")+2);
        return first;
    }

    /**
     * Once the dictionary is finalized, send it to the cache to determine which words should be included in cache
     */
    public void sendDictionairyToCache(){
        cache.addDictionary(dictionairy);
    }

    /**
     * iterates over the temporary posting files and merges them into final posting files using merge sort.
     */
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

    /**
     * Merges posting files of a level from the merge sort into the next level of the merge sort. (e.g. level with 4 files to a level with 2 files)
     * @param startIndex the file number of the first posting file in current level
     * @param endIndex the file number of the last posting file in current level
     * @param nextcounter the file number of the first new posting file to be created
     * @return the file number of the last posting file in the next level
     */
    //merges one level of the merge sort tree. start index is the lowest file number and end index is the highest file number to be merged in current iteration. nextCounter is the number of the new file to be created
    public int merge(int startIndex, int endIndex, int nextcounter) {

        int currentIndex = startIndex; //index of sorted files
        int counter = nextcounter; //index of new file
        int ans=0;

        try {
            //check if uneven - only rename the last file to the last counter of this iteration
            if((endIndex-startIndex)%2==0){
                int lastFileIndex = nextcounter+((endIndex-startIndex)/2);
                Path source = Paths.get(destinationDirectory+"/"+directory+"/"+endIndex+".txt");
                Files.move(source, source.resolveSibling(destinationDirectory+"/"+directory+"/"+lastFileIndex+".txt"));
                endIndex--;
                ans++;

            }

            //haven't finished this iteration
            while (currentIndex < endIndex) {

                //create new file
                PrintWriter writer = new PrintWriter(destinationDirectory+"/"+directory+"/"+(counter++)+".txt", "UTF-8");

                //open files to merge
                File file1=new File(destinationDirectory+"/"+directory+"/"+currentIndex+".txt");
                File file2=new File(destinationDirectory+"/"+directory+"/"+(currentIndex+1)+".txt");
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
                    line2=reader2.readLine();
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

    /**
     * When the merge sort has merged all files except the last 2, merges them into final posting files, 1 file per each letter, and 1 file for non-letters
     * @param start file name of the first posting file to be merged
     * @param end file name of the seconf posting file to be merged.
     */
    public void mergeAlphabetic(int start, int end){

        try {
            char current = 'a';
            //CREATE NEW FILE for non-letters (symbols and numbers)
            PrintWriter writer = new PrintWriter(destinationDirectory+"/"+directory+"/"+"nonLetters.txt", "UTF-8");

            //OPEN FILES TO MERGE
            File file1=new File(destinationDirectory+"/"+directory+"/"+start+".txt");
            File file2=new File(destinationDirectory+"/"+directory+"/"+end+".txt");
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
                        cache.getLine(line1,lineCounter);
                        updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                        calcDocWeight(line1);
                        line1 = reader1.readLine();
                    } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) > 0) {
                        writer.println(line2);
                        cache.getLine(line2,lineCounter);
                        calcDocWeight(line2);
                        updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                        line2 = reader2.readLine();
                    } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) == 0) {
                        String merged=mergeSameTerm(line1, line2);
                        writer.println(merged);
                        cache.getLine(merged,lineCounter);
                        calcDocWeight(merged);
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
                cache.getLine(line1,lineCounter);
                calcDocWeight(line1);
                updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                line1=reader1.readLine();
            }

            while(line2!=null && line2.charAt(0)<97|| line2.charAt(0)>122){
                writer.println(line2);
                cache.getLine(line2,lineCounter);
                calcDocWeight(line2);
                updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                line2=reader2.readLine();
            }
            writer.flush();

            //MERGE THE FILES - EACH LETTER IN DIFFERENT FILE
            char currentChar = 'a';
            while(currentChar<123){
                lineCounter=1;
                PrintWriter letterWriter = new PrintWriter(destinationDirectory+"/"+directory+"/"+currentChar+".txt", "UTF-8");
                while(line1!=null && line2!=null && line1.charAt(0)==currentChar && line2.charAt(0)==currentChar) {
                    if(line1.length()>0 && line2.length()>0 && line1.contains(":") && line2.contains(":")) {
                        if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) < 0) {
                            letterWriter.println(line1);
                            cache.getLine(line1,lineCounter);
                            calcDocWeight(line1);
                            updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                            line1 = reader1.readLine();
                        } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) > 0) {
                            letterWriter.println(line2);
                            cache.getLine(line2,lineCounter);
                            calcDocWeight(line2);
                            updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                            line2 = reader2.readLine();
                        } else if (line1.substring(0, line1.indexOf(":")).compareTo(line2.substring(0, line2.indexOf(":"))) == 0) {
                            String merged = mergeSameTerm(line1, line2);
                            letterWriter.println(merged);
                            cache.getLine(merged,lineCounter);
                            calcDocWeight(merged);
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
                    cache.getLine(line1,lineCounter);
                    calcDocWeight(line1);
                    updatePointerToPosting(line1.substring(0, line1.indexOf(":")), lineCounter++);
                    line1=reader1.readLine();
                }


                while(line2!=null && line2.charAt(0)==currentChar){
                    letterWriter.println(line2);
                    cache.getLine(line2,lineCounter);
                    calcDocWeight(line2);
                    updatePointerToPosting(line2.substring(0, line2.indexOf(":")), lineCounter++);
                    line2=reader2.readLine();
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

            pointerDictoCache();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * updates the pointers from dictionary to posting
     * @param term the term that its pointer needs to be updated
     * @param lineNumber the line number of the terms entry in the posting file - that will be the pointer.
     */
    private void updatePointerToPosting(String term, int lineNumber){
        dictionairy.get(term).setPointerToPosting(lineNumber);
    }

    /**
     * updated pointers from dictionary to cache, where a pointer is a cache entry value (a partial posting line)
     */
    public void pointerDictoCache(){
        for(String termInCache : cache.getCache().keySet()) {
            dictionairy.get(termInCache).setPointerToTermInCache(cache.getCache().get(termInCache));
        }
    }

    public void calcDocWeight(String postingLine)
    {
        String term = postingLine.substring(0,postingLine.indexOf(':'));
        postingLine=postingLine.substring(postingLine.indexOf(':')+1);
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
                while (charsInLine[i] != ')') {
                    i++;
                }
                i++;

                int docLength = docLengths.get(docID.toString());

                //calculate weight
                double tfnormal = (Double.parseDouble(tf.toString())/docLength);
                int numOfDocsOccuresInCorpus = dictionairy.get(term).getNumberOfDocumentsOccuresIn();
                Double idf = Math.log10(numOfDocsInCorpus/numOfDocsOccuresInCorpus)/Math.log10(2);

                if(docWeights.containsKey(docID.toString())){
                    double currentWeight = docWeights.get(docID.toString());
                    currentWeight+= Math.pow(tfnormal*idf,2);
                    docWeights.put(docID.toString(),currentWeight);
                }
                else{
                    docWeights.put(docID.toString(),Math.pow(tfnormal*idf,2));
                }
            }

        }
    }

    public void calcAvgLength(){
        double sum=0;
        for(int length : docLengths.values()){
            sum += length;
        }
        sum= sum/docLengths.size();

        try {
            PrintWriter avglenWriter = new PrintWriter(destinationDirectory + "/" + directory + "/avgDocLengthNoStem.txt", "UTF-8");
            avglenWriter.println(sum);
            avglenWriter.flush();
            avglenWriter.close();
        }catch(Exception e){}

    }

}
