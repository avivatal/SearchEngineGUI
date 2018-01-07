package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.geom.Path2D;
import java.io.*;

import java.util.*;

import javafx.scene.control.Alert;

import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
//import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;


public class Controller {

    @FXML
    public javafx.scene.control.Button btn_start;
    public javafx.scene.control.Button btn_reset;
    public javafx.scene.control.Button browse1;
    public javafx.scene.control.Button browse2;
    public javafx.scene.control.Button cacheB;
    public javafx.scene.control.Button dictB;
    public javafx.scene.control.CheckBox stembox;
    public javafx.scene.control.TextField corpBrowse;
    public javafx.scene.control.TextField destBrowse;
    public javafx.scene.control.Button load;
    public javafx.scene.control.Button save;
    public javafx.scene.control.TextField loadtxt;
    public javafx.scene.control.TextField savetxt;
    public javafx.scene.control.TextField browsequery;
    public javafx.scene.control.TextField singlequery;
    public javafx.scene.control.Button browsequerybutton;
    public javafx.scene.control.Button executequeries;
    public javafx.scene.control.Button executequery;
    public javafx.scene.control.Button resetall;
    public javafx.scene.control.CheckBox expandquery;
    public javafx.scene.control.CheckBox docidbox;


    String corpusPath;
    String destinationDirectory;
    private Stage stage;
    private ReadFile rf;
    String savePath;
    String loadPath;
    String queryPath;
    String query;
    String docName;
    private Searcher searcher;

    public Controller() {
        corpusPath=new String() ;
        destinationDirectory=new String() ;
        corpBrowse=new TextField();
        rf=new ReadFile();
        searcher=new Searcher();
        searcher.setParser(rf.ctrl.parser);
        searcher.setIndexer(rf.ctrl.indexer);
        docName="</P><P> Los Angeles just may be the City of Angeles. The parents of the high school"+
                "students are apt to believe it when they learn what the operations people of"+
                "   Northwest Airlines at Los Angeles International Airport did last month. "+
                "</P><P> When Northwest's Flight 190 was being buttoned down for its 12:30 p.m.  departure, nearly half of a group of 40 Japanese high school students were still mired in U.S. Customs following their long flight from Japan. </P> <P>"+
                "Northwest put compassion over schedule and waited more than an hour until the final one of the group, none of whom could speak English, was on board. They"+
                " were, in fact, bound for the Puget Sound area for a three-week intensive"+
                " English language course preparatory to going on to Phoenix and a yearlong"+
                " exchange program. "+ "</P> <P> But one of the flight attendants said,";
    }

    /**
     * calculates how many documents have been indexed overall
     * @return number of indexed documents
     */
    private int calculateNumOfDocs(){
        return rf.ctrl.parser.docNumber;
    }


    /**
     * writes the cache object to the disk in order to calculate the object size. Once calculated the file is deleted.
     * @return the size of the cache object.
     */
    private long calcCacheSize(){
        File file = new File(destinationDirectory+"/"+rf.ctrl.getDirectory()+"/cache.ser");
        long ans = file.length();
        file.delete();
        return ans;
    }

    /**
     * writes the dictionary object to the disk in order to calculate the object size. Once calculated the file is deleted.
     * @return the size of the dictionary object.
     */
    private long calcDictionairySize(){
        File file = new File(destinationDirectory+"/"+rf.ctrl.getDirectory()+"/dictionairy.ser");
        long ans = file.length();
        file.delete();
        return ans;
    }

    /**
     * calculates the size of all posting documents
     * @return total posting size
     */
    private long calcPostingSize(){
        File dir = new File(destinationDirectory+"/"+rf.ctrl.getDirectory());
        long length = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                length += file.length();
        }
        return length;
    }

    /**
     * updates the stage to the stage created in the main
     * @param primaryStage the main stage of the GUI
     */
    public void setStage(Stage primaryStage){
        stage=primaryStage;
    }


    /**
     * opens a directory chooser to select the directory which the corpus is in
     */
    public void browseCorpus(){

        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Browse");
            File selectedDirectory = chooser.showDialog(stage);
            corpusPath = (selectedDirectory.toString());
            corpBrowse.setText(selectedDirectory.toString());
        }catch (Exception e){}

    }

    /**
     * opens a directory chooser to select the directory which the queries file is in
     */
    public void browseQueries(){

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Browse");
            File selectedDirectory = chooser.showOpenDialog(stage);//showDialog(stage);
            queryPath = (selectedDirectory.toString());
            browsequery.setText(selectedDirectory.toString());
        }catch (Exception e){}

    }

    /**
     * get query input from user
     */
    public void getQuery(){
        corpusPath="C:/Users/avevanes/Downloads/corpus";
        destinationDirectory="C:/Users/avevanes/Downloads";
        rf.ctrl.setWithStemming(stembox.isSelected());
        rf.ctrl.setPaths(corpusPath+"/stop_words.txt",destinationDirectory);
        searcher.parser.setStopwords(rf.ctrl.getStopwords());
        searcher.parser.setIsQuery(true);

        try {
            ObjectInputStream docWeight = new ObjectInputStream(new FileInputStream(destinationDirectory+"/"+rf.ctrl.directory+"/docWeightsWithStem.ser"));
            rf.ctrl.indexer.setDocWeights((HashMap<String,Double>)docWeight.readObject());
            docWeight.close();

            ObjectInputStream docLength = new ObjectInputStream(new FileInputStream(destinationDirectory+"/"+rf.ctrl.directory+"/docLengthWithStem.ser"));
            rf.ctrl.indexer.setDocLengths((HashMap<String,Integer>)docLength.readObject());
            docWeight.close();
        }
        catch (Exception e){}



        List<String> list=null;
        long startTime=System.currentTimeMillis();
        if(singlequery.getText() != ""){
            if(docidbox.isSelected())
            {
                Summarizer summary=new Summarizer();
                summary.parser.setStopwords(rf.ctrl.getStopwords());
                summary.setCorpusPath(corpusPath);
                summary.setWithStemming(stembox.isSelected());
               // docName=singlequery.getText().trim();
                summary.docSummary(docName);
              //  list = summary.getList();
            }
            else {
                long totalTime=System.currentTimeMillis()-startTime;
                query = singlequery.getText().trim();
                list=searcher.getRelevantDocs(query);

                ObservableList<String> items= FXCollections.observableArrayList();
                items.add("The number of documents that have been retrieved is: "+list.size());
                items.add("The total runtime is: "+totalTime);
                items.add("The document IDs retrieved are: ");
                for(String s:list){
                    items.add(s);
                }

                Stage newstage = new Stage();
                newstage.setTitle("Results");
                BorderPane pane = new BorderPane();
                Scene scene = new Scene(pane);
                newstage.setScene(scene);
                ListView<String> listView = new ListView<>();
                listView.setItems(items);
                pane.setCenter(listView);
                newstage.setAlwaysOnTop(true);
                newstage.setOnCloseRequest(
                        e -> {
                            e.consume();
                            newstage.close();
                        });
                newstage.showAndWait();
            }
        }

        StringBuilder results = new StringBuilder();
        for(String s:list){
            results.append(s+"\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("The query has been successfuly executed.\n" +
                "The number of documents that have been retrieved is: "+list.size()+
        "\nThe total runtime is: "+totalTime+"" +
                "\nThe document IDs retrieved are: \n"+results.toString());
        alert.show();





    }

    public void getQueryPath()
    {
        ///////
        rf.ctrl.setWithStemming(stembox.isSelected());
        rf.ctrl.setPaths("C:/Users/avevanes/Downloads/corpus/stop_words.txt","C:/Users/avevanes/Downloads");
        searcher.parser.setStopwords(rf.ctrl.getStopwords());
        searcher.parser.setIsQuery(true);
        destinationDirectory="C:/Users/avevanes/Downloads";
        try {
            ObjectInputStream docWeight = new ObjectInputStream(new FileInputStream(destinationDirectory+"/"+rf.ctrl.directory+"/docWeightsWithStem.ser"));
            rf.ctrl.indexer.setDocWeights((HashMap<String,Double>)docWeight.readObject());
            docWeight.close();

            ObjectInputStream docLength = new ObjectInputStream(new FileInputStream(destinationDirectory+"/"+rf.ctrl.directory+"/docLengthWithStem.ser"));
            rf.ctrl.indexer.setDocLengths((HashMap<String,Integer>)docLength.readObject());
            docWeight.close();
        }
        catch (Exception e){}
        /////



        long startTime=System.currentTimeMillis();
        if(queryPath!=""){
            queryPath=browsequery.getText();
        }

        StringBuilder result = new StringBuilder();
        HashMap<String, List<String>> results = searcher.multipleQueries(queryPath);
        int totalSize=0;
 /*       for(Map.Entry<String, List<String>>  entry : results.entrySet()){
            result.append("Query Number: "+entry.getKey()+"\nResults:\n");
            for(String s:entry.getValue()){
                result.append(s+"\n");
                totalSize++;
            }
        }

        long totalTime=System.currentTimeMillis()-startTime;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("The queries have been successfuly executed.\n" +
                "The number of documents that have been retrieved is: "+totalSize+
                "\nThe total runtime is: "+totalTime+"" +
                "\nThe document IDs retrieved are: \n"+result.toString());
        alert.show();*/
////

        long totalTime=System.currentTimeMillis()-startTime;

        ObservableList<String> items= FXCollections.observableArrayList();
        for(Map.Entry<String, List<String>>  entry : results.entrySet()){
            items.add("The queries have been successfuly executed.");
            items.add("The number of documents that have been retrieved is: "+totalSize);
            items.add("The total runtime is: "+totalTime);
            items.add("The document IDs retrieved are: "+result.toString());
            items.add("Query Number: "+entry.getKey());
            items.add("Results: ");
            for(String s:entry.getValue()){
                items.add(s);
                totalSize++;
            }
        }

        Stage newstage = new Stage();
        newstage.setTitle("Results");
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane);
        newstage.setScene(scene);
        ListView<String> listView = new ListView<>();
        listView.setItems(items);
        pane.setCenter(listView);
        newstage.setAlwaysOnTop(true);
        newstage.setOnCloseRequest(
                e -> {
                    e.consume();
                    newstage.close();
                });
        newstage.showAndWait();

    }
    /**
     * opens a directory chooser to select the directory to save the posting files in
     */
    public void browseDest(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Browse");
            File selectedDirectory = chooser.showDialog(stage);
            destinationDirectory = (selectedDirectory.toString());
            destBrowse.setText(selectedDirectory.toString());
        }catch (Exception e){}

    }

    /**
     * opens a directory chooser to select the directory to save the cache and dictionary in
     */
    public void browsesave(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Save");
            File selectedDirectory = chooser.showDialog(stage);
            savePath = selectedDirectory.toString();
            savetxt.setText(savePath);
        }catch (Exception e){}
    }

    /**
     * opens a directory chooser to select the directory to load the cache and dictionary from
     */
    public void browseload(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Load");
            File selectedDirectory = chooser.showDialog(stage);
            loadPath = selectedDirectory.toString();
            loadtxt.setText(loadPath);
        }catch (Exception e){}
    }

    /**
     * saves the cache and the dictionary in the selected path by writing the objects into a .ser file.
     * According to the stem checkbox selection, will save with appropriate file name to avoid overriding.
     */
    public void save(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                try {
                    if(savePath==null){
                        savePath=destinationDirectory;
                    }
                    String cache;
                    String dict;
                    if(stembox.isSelected()){
                        cache = "/cacheStemmed.ser";
                        dict = "/dictionairyStemmed.ser";
                    }else{
                        cache = "/cacheNotStemmed.ser";
                        dict = "/dictionairyNotStemmed.ser";
                    }

                    ObjectOutputStream outCache = new ObjectOutputStream(new FileOutputStream(savePath+cache));
                    outCache.writeObject(rf.ctrl.indexer.cache);
                    outCache.flush();
                    outCache.close();

                    ObjectOutputStream outDict = new ObjectOutputStream(new FileOutputStream(savePath+dict));
                    outDict.writeObject(rf.ctrl.indexer.dictionairy);
                    outDict.flush();
                    outDict.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Cache and Dictionairy have been saved");
                    alert.show();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Please Enter a Path");
                    alert.show();;
                }
            }});
    }

    /**
     * loads the cache and the dictionary from the selected path - reads from a .ser object into RAM.
     * the files names that are read from are according to the stem checkbox selection
     */
    public void load(){
        //  Platform.runLater(new Runnable(){
        //    @Override
        //    public void run() {
        try {
            String cachepath;
            String dict;
            if(stembox.isSelected()){
                cachepath = "/cacheStemmed.ser";
                dict = "/dictionairyStemmed.ser";
                rf.ctrl.withStemming=true;
                rf.ctrl.parser.setWithStemming(true);
            }else{
                cachepath = "/cacheNotStemmed.ser";
                dict = "/dictionairyNotStemmed.ser";
                rf.ctrl.withStemming=false;
                rf.ctrl.parser.setWithStemming(false);
            }
            ObjectInputStream inCache = new ObjectInputStream(new FileInputStream((loadPath+cachepath)));
            Cache cache = (Cache) inCache.readObject();
            inCache.close();
            rf.ctrl.indexer.cache=cache;

            ObjectInputStream inDict = new ObjectInputStream(new FileInputStream(loadPath+dict));
            rf.ctrl.indexer.dictionairy = (HashMap<String,TermInDictionairy>)inDict.readObject();
            inDict.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Cache and Dictionairy have been loaded");
            alert.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please Enter a Valid Path");
            alert.show();;
        }
        //    }});
    }


    /**
     * activates the indexing process - starting with reading the files, parsing them (with or without stemming) and finally indexing them.
     */
    public void run(){
        Platform.runLater(new Runnable(){
            @Override
            public void run(){
                try {
                    //disable buttons
                    btn_start.setDisable(true);
                    btn_reset.setDisable(true);
                    browse1.setDisable(true);
                    browse2.setDisable(true);

                    //if paths not browsed, checks if path was manually entered
                    if(corpusPath.toString().length()==0){
                        corpusPath=corpBrowse.getText();
                    }
                    if(destinationDirectory.toString().length()==0){
                        destinationDirectory=destBrowse.getText();
                    }

                    //initialization of parameters that need to be set for each run
                    rf.ctrl.indexer.numberOfTempPostingFiles=0;
                    rf.ctrl.setWithStemming(stembox.isSelected());
                    rf.setDestinationDirectory(destinationDirectory);
                    rf.setStopwordsPath(corpusPath+"/stop_words.txt");
                    rf.setCtrl();
                    rf.ctrl.parser.setWriter();

                    //read, parse, stem, index
                    rf.read(corpusPath+"/corpus");

                    //save cache and dictionary to calculate their sizes
                    saveCacheToSer();
                    saveDictToSer();

                    try {
                        File dict = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/docWeightsWithStem.ser");
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dict));
                        out.writeObject(rf.ctrl.indexer.getDocWeights());
                        out.flush();
                        out.close();

                        File docLength = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/docLengthWithStem.ser");
                        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(docLength));
                        output.writeObject(rf.ctrl.indexer.getDocLengths());
                        output.flush();
                        output.close();
                    }catch (Exception e){}

                  /*  Searcher searcher = new Searcher();
                    searcher.parser=rf.ctrl.parser;
                    searcher.indexer=rf.ctrl.indexer;
                    searcher.parser.setIsQuery(true);
                    searcher.getRelevantDocs("Newspapers in the Former Yugoslav Republic of Macedonia");*/
                    //saveDictTxt();

                    //enable buttons
                    btn_start.setDisable(false);
                    btn_reset.setDisable(false);
                    browse1.setDisable(false);
                    browse2.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Indexing Process has Completed\n"
                            +"Dictionairy Size is: "+calcDictionairySize()+" bytes"
                            +"\nPosting Size is: "+calcPostingSize()+" bytes"+
                            "\nCache Size is: "+calcCacheSize()+" bytes"+
                            "\nNumber of documents indexed: "+calculateNumOfDocs());
                    alert.show();

                }catch (NullPointerException e){
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("File Path Invalid");
                    alert.show();
                    btn_start.setDisable(false);
                    btn_reset.setDisable(false);
                    browse1.setDisable(false);
                    browse2.setDisable(false);
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("File Path Invalid");
                    alert.show();
                    btn_start.setDisable(false);
                    btn_reset.setDisable(false);
                    browse1.setDisable(false);
                    browse2.setDisable(false);
                }


            }});
        //thread.start();
       /* try {
            thread.join();
        }*/
       /* catch (InterruptedException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("File Path Invalid");
            alert.show();
        }*/
     /*   Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Indexing Process has Completed");
        alert.show();*/
    }

    public void saveDictForZipf(){
        try {

            PrintWriter writerzipf = new PrintWriter(destinationDirectory + "/zipf.txt", "UTF-8");
            PrintWriter writertxt = new PrintWriter(destinationDirectory + "/dictext.txt", "UTF-8");
            HashMap<String,TermInDictionairy> dict = rf.ctrl.indexer.dictionairy;
            for(TermInDictionairy tid : dict.values()){
                writerzipf.println(tid.term+","+tid.totalOccurencesInCorpus);
                writertxt.println(tid.toString());
            }
            SortedSet<String> sortedKeys = new TreeSet<String>(dict.keySet());

            ObservableList<String> items= FXCollections.observableArrayList();
            int counter=1;
            for (String term : sortedKeys) {
                StringBuilder TermDetails = new StringBuilder();
                items.add(term+","+dict.get(term).getTotalOccurencesInCorpus());
                counter++;
            }

        }catch (Exception e){}
    }
    /**
     * saves the cache from the last run into a serializable file - later used to calculate its size
     */
    public void saveCacheToSer(){
        try {
            File cache = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/cache.ser");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache));
            out.writeObject(rf.ctrl.indexer.cache);
            out.flush();
            out.close();
        }catch (Exception e){}
    }

    /**
     * saves the dictionary from the last run into a serializable file - later used to calculate its size
     */
    public void saveDictToSer(){
        try {
            File dict = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/dictionairy.ser");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dict));
            out.writeObject(rf.ctrl.indexer.getDictionairy());
            out.flush();
            out.close();
        }catch (Exception e){}
    }


    /**
     * deletes from disk all posting files, both with or without stemming.
     * if there is a save or load path, deletes cache and dictionary files from there
     * initializes the ReadFile property to a new one
     */
    public void reset(){

        if(destinationDirectory!=null) {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            //DELETE THE DIRECTORIES
            File withStem = new File(destinationDirectory + "/withStem");
            File noStem = new File(destinationDirectory + "/noStem");
            if(withStem.exists()){
                File[] files = withStem.listFiles();
                for (File aFile : files) {
                    aFile.delete();
                }
                withStem.delete();
            }
            if(noStem.exists()){
                File[] files = noStem.listFiles();
                for (File aFile : files) {
                    aFile.delete();
                }
                noStem.delete();
            }

            //delete cache and dictionary from load/save paths
            if(savePath!=null){
                File cachestemmed = new File(savePath+"/cacheStemmed.ser");
                File dictStemmed = new File(savePath+"/dictionairyStemmed.ser");
                File cacheNotstemmed = new File(savePath+"/cacheNotStemmed.ser");
                File dictNotStemmed = new File(savePath+"/dictionairyNotStemmed.ser");
                cachestemmed.delete();
                dictStemmed.delete();
                cacheNotstemmed.delete();
                dictNotStemmed.delete();
            }
            if(loadPath!=null){
                File cachestemmed = new File(loadPath+"/cacheStemmed.ser");
                File dictStemmed = new File(loadPath+"/dictionairyStemmed.ser");
                File cacheNotstemmed = new File(loadPath+"/cacheNotStemmed.ser");
                File dictNotStemmed = new File(loadPath+"/dictionairyNotStemmed.ser");
                cachestemmed.delete();
                dictStemmed.delete();
                cacheNotstemmed.delete();
                dictNotStemmed.delete();
            }

            //delete from RAM
            rf=new ReadFile();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Reset has Completed");
            alert.show();
            btn_start.setDisable(false);
            btn_reset.setDisable(false);
            browse1.setDisable(false);
            browse2.setDisable(false);
            cacheB.setDisable(false);
            dictB.setDisable(false);
        }
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Unable to Reset");
            alert.show();
        }


    }

    /**
     * displays the current cache from the recent run as a list of all the terms and the posting entries saved in cache
     */
    public void displayCache() {

        try {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            HashMap<String, String> cache = rf.ctrl.indexer.cache.getCache();

            if(cache.size()==0){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Must Index or Load Cache First In Order to Display Cache!");
                alert.show();
                btn_start.setDisable(false);
                btn_reset.setDisable(false);
                browse1.setDisable(false);
                browse2.setDisable(false);
                cacheB.setDisable(false);
                dictB.setDisable(false);
            }
            else{
                //sort the terms in the cache lexicographically
                SortedSet<String> sortedKeys = new TreeSet<String>(cache.keySet());

                ObservableList<String> items= FXCollections.observableArrayList();
                int counter=1;
                for (String term : sortedKeys) {
                    items.add(term+ ": "+cache.get(term));
                    counter++;
                }

                Stage newstage = new Stage();
                newstage.setTitle("Cache");
                BorderPane pane = new BorderPane();
                Scene scene = new Scene(pane);
                newstage.setScene(scene);
                ListView<String> list = new ListView<>();
                list.setItems(items);
                pane.setCenter(list);
                newstage.setAlwaysOnTop(true);
                newstage.setOnCloseRequest(
                        e -> {
                            e.consume();
                            newstage.close();
                        });
                newstage.showAndWait();


                btn_start.setDisable(false);
                btn_reset.setDisable(false);
                browse1.setDisable(false);
                browse2.setDisable(false);
                cacheB.setDisable(false);
                dictB.setDisable(false);
            }}
        catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Must Index First In Order to Display Cache!");
            alert.show();
        }
    }

    /**
     * displays the dictionary as a list of terms and their total number of occurrences in all the corpus
     */
    public void displayDictionairy(){

        try {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            HashMap<String, TermInDictionairy> dictionairy = rf.ctrl.indexer.getDictionairy();
            if(dictionairy.size()==0){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Must Index or Load Dictionary First In Order to Display Dictionary!");
                alert.show();
                btn_start.setDisable(false);
                btn_reset.setDisable(false);
                browse1.setDisable(false);
                browse2.setDisable(false);
                cacheB.setDisable(false);
                dictB.setDisable(false);
            }
            else{
                //sort the terms in the cache lexicographically
                SortedSet<String> sortedKeys = new TreeSet<String>(dictionairy.keySet());

                ObservableList<String> items= FXCollections.observableArrayList();
                int counter=1;
                for (String term : sortedKeys) {
                    StringBuilder TermDetails = new StringBuilder();
                    TermDetails.append(counter+") "+term + ": "+dictionairy.get(term).getTotalOccurencesInCorpus()+" Occurrences in Corpus");

                    items.add(TermDetails.toString());
                    counter++;
                }

                Stage newstage = new Stage();
                newstage.setTitle("Dictionairy");
                BorderPane pane = new BorderPane();
                Scene scene = new Scene(pane);
                newstage.setScene(scene);
                ListView<String> list = new ListView<>();
                list.setItems(items);
                pane.setCenter(list);
                newstage.setAlwaysOnTop(true);
                newstage.setOnCloseRequest(
                        e -> {
                            e.consume();
                            newstage.close();
                        });
                newstage.showAndWait();


                btn_start.setDisable(false);
                btn_reset.setDisable(false);
                browse1.setDisable(false);
                browse2.setDisable(false);
                cacheB.setDisable(false);
                dictB.setDisable(false);
            }}
        catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Must Index First In Order to Display Dictionary!");
            alert.show();
        }
    }




}
