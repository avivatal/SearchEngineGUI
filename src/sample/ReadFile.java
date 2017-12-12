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

    public ReadFile(String stopwordsPath, String destinationDirectory) {
        ctrl = new Control(stopwordsPath, destinationDirectory);
        regex = Pattern.compile("<DOC>");
    }

    public void read(String corpusPath) throws FileNotFoundException{

        int counter=0;
        ArrayList<String> documents=new ArrayList<>();
        File corpusFolder = new File(corpusPath);
        File[] listOfFiles = corpusFolder.listFiles();
        int corpusSize = listOfFiles.length;

        while(counter<1){
            documents.clear();
            for(int i=0; i<100 && counter<corpusSize; i++){
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
            ctrl.control(documents);
            System.out.println("done "+counter);
        }
        ctrl.merge();

    }


}





