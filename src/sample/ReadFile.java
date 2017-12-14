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
    Pattern regex;
    String stopwordsPath;
    String destinationDirectory;
    int counter;

    public ReadFile() {

        regex = Pattern.compile("<DOC>");
        ctrl=new Control();
    }

    public int getCounter() {
        return counter;
    }

    public void setStopwordsPath(String stopwordsPath){
        this.stopwordsPath=stopwordsPath;
    }

    public void setDestinationDirectory(String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
    }

    public void setCtrl(){
        ctrl.setPaths(stopwordsPath, destinationDirectory);
    }

    public void read(String corpusPath) throws FileNotFoundException{

        counter=0;
        ArrayList<String> documents=new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] listOfFiles = corpusFolder.listFiles();
        int corpusSize = listOfFiles.length;

        while(counter<10){
            documents.clear();
            for(int i=0; i<5 && counter<corpusSize; i++){
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

                    //String[] docs = (builder.toString()).split("<DOC>");
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
            System.out.printf("done read");
            ctrl.control(documents);
            System.out.println("done "+counter);
        }
        ctrl.calcCache();
        ctrl.merge();

    }

}





