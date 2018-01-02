package sample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by aviva on 27/11/2017.
 */
public class ReadFile
{
    Control ctrl;
    Pattern regex; //splits files to documents
    String stopwordsPath;
    String destinationDirectory;
    int counter; //counter of read files

    public ReadFile() {

        regex = Pattern.compile("<DOC>");
        ctrl=new Control();
    }

    /**
     * counter getter
     * @return number of files that have been read so far
     */
    public int getCounter() {
        return counter;
    }

    /**
     * sets the path to the file containing the stop words
     * @param stopwordsPath path to file
     */
    public void setStopwordsPath(String stopwordsPath){
        this.stopwordsPath=stopwordsPath;
    }

    /**
     * ets the path to the directory to save the with/without stem directoried (which will include the posting files)
     * @param destinationDirectory
     */
    public void setDestinationDirectory(String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
    }


    /**
     * passes the paths of the stopwords and the destination directory to the Control that is in charge of initializing parse and index
     */
    public void setCtrl(){
        ctrl.setPaths(stopwordsPath, destinationDirectory);
    }


    /**
     * reads files from corpus and sends to control who initializes parse and index
     * @param corpusPath path of directory of corpus - includes a directory for each file (directory and file have same name)
     * @throws FileNotFoundException if there is a file that isn't in the correct format in the corpus
     */
    public void read(String corpusPath) throws FileNotFoundException{

        counter=0;
        ArrayList<String> documents=new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] listOfFiles = corpusFolder.listFiles();
        int corpusSize = listOfFiles.length;

        while(counter<corpusSize){
            documents.clear();
            //reads in groups of 70 files to parse+index each group separately
            for(int i=0; i<70 && counter<corpusSize; i++){
                try {
                    String path = listOfFiles[counter].getPath() + "/" + listOfFiles[counter].getName();
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
                    for (int j = 1; j < docs.length; j++) {
                        documents.add(docs[j]);
                    }
                    counter++;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            //sends to control to parse and index
            ctrl.control(documents);
            System.out.println("done "+counter);
        }
        //all files have been indexed, initializes cache calculation
        ctrl.calcCache();
        //mergers the temporary posting files
        ctrl.merge();

    }

}





