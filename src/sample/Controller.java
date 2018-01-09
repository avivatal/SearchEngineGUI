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
import javax.swing.filechooser.FileNameExtensionFilter;
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
    public javafx.scene.control.TextField saveResultstxt;
    public javafx.scene.control.Button browseResults;
    public javafx.scene.control.Button saveResults;

    String corpusPath;
    String destinationDirectory;
    private Stage stage;
    private ReadFile rf;
    String savePath;
    String loadPath; //cache, dictionary, corpus, stopwords, posting location
    String queryPath;
    String query;
    String saveResultsPath;
    private Searcher searcher;
    HashMap<String, List<String>> results;

    public Controller() {
        corpusPath=new String() ;
        destinationDirectory=new String() ;
        corpBrowse=new TextField();
        rf=new ReadFile();
        searcher=new Searcher();
        searcher.setParser(rf.ctrl.parser);
        searcher.setIndexer(rf.ctrl.indexer);
        results = new HashMap<>();
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


        results.clear();
       // destinationDirectory="C:/Users/avevanes/Downloads";
        long startTime=System.currentTimeMillis();
        rf.ctrl.setWithStemming(stembox.isSelected());
        rf.ctrl.setPaths(loadPath+"/stop_words.txt",null);  //
        searcher.setLoadPath(loadPath);
        searcher.parser.setStopwords(rf.ctrl.getStopwords());
        searcher.parser.setIsQuery(true);

        try {
            ObjectInputStream docWeight;
            ObjectInputStream docLength;
            if(stembox.isSelected()) {
                docWeight = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docWeightsWithStem.ser"));
                docLength = new ObjectInputStream(new FileInputStream(loadPath+"/"+rf.ctrl.directory+"/docLengthWithStem.ser"));
            }
            else{
                docWeight = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docWeightsNoStem.ser"));
                docLength = new ObjectInputStream(new FileInputStream(loadPath+"/"+rf.ctrl.directory+"/docLengthNoStem.ser"));
            }
            rf.ctrl.indexer.setDocWeights((HashMap<String,Double>)docWeight.readObject());
            docWeight.close();
            rf.ctrl.indexer.setDocLengths((HashMap<String,Integer>)docLength.readObject());
            docLength.close();

            rf.ctrl.indexer.calcAvgLength();
        }
        catch (Exception e){}



        List<String> list=null;
        if(!singlequery.getText().equals("")){
            //if docID to summarize
            if(docidbox.isSelected())
            {
                Summarizer summary=new Summarizer();
                summary.parser.setStopwords(rf.ctrl.getStopwords());
                summary.setLoadPath(loadPath);
                summary.setWithStemming(stembox.isSelected());
                ArrayList<String> result = summary.readFile(singlequery.getText().trim());

                ObservableList<String> items= FXCollections.observableArrayList();
                for(int i=0;i<result.size();i++){
                    items.add(result.get(i));
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
            else
                //if query
                {
                query = singlequery.getText().trim();
                list=searcher.getRelevantDocs(query);
                long totalTime=System.currentTimeMillis()-startTime;
                ObservableList<String> items= FXCollections.observableArrayList();
                items.add("The number of documents that have been retrieved is: "+list.size());
                items.add("The total runtime is: "+totalTime);
                items.add("The document IDs retrieved are: ");
                for(String s:list){
                    items.add(s);
                }
                results.put("0",new ArrayList<>(list));

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
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Enter a query or a document ID");
            alert.show();
        }
    }

    public void getQueryPath()
    {
        if(loadPath==null || loadPath.equals("")){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Enter a path to load query file from!");
            alert.show();
        }
        else {
            try {
                results.clear();
                long startTime = System.currentTimeMillis();
                rf.ctrl.setWithStemming(stembox.isSelected());
                rf.ctrl.setPaths(loadPath + "/stop_words.txt", null);
                searcher.setLoadPath(loadPath);
                searcher.parser.setStopwords(rf.ctrl.getStopwords());
                searcher.parser.setIsQuery(true);
                //destinationDirectory="C:/Users/avevanes/Downloads";
                try {
                    ObjectInputStream docWeight;
                    ObjectInputStream docLength;
                    if (stembox.isSelected()) {
                        docWeight = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docWeightsWithStem.ser"));
                        docLength = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docLengthWithStem.ser"));
                    } else {
                        docWeight = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docWeightsNoStem.ser"));
                        docLength = new ObjectInputStream(new FileInputStream(loadPath + "/" + rf.ctrl.directory + "/docLengthNoStem.ser"));
                    }
                    rf.ctrl.indexer.setDocWeights((HashMap<String, Double>) docWeight.readObject());
                    docWeight.close();
                    rf.ctrl.indexer.setDocLengths((HashMap<String, Integer>) docLength.readObject());
                    docLength.close();
                    rf.ctrl.indexer.calcAvgLength();
                } catch (Exception e) {
                }

                //not manually entered path, used browse button
                if (queryPath != "") {
                    queryPath = browsequery.getText();
                }

                results = searcher.multipleQueries(queryPath);

                long totalTime = System.currentTimeMillis() - startTime;

                ObservableList<String> items = FXCollections.observableArrayList();
                items.add("The queries have been successfuly executed.");
                items.add("The total runtime is: " + totalTime);
                for (String s : results.keySet()) {
                    items.add("Query Number: " + s);
                    items.add("The number of documents that have been retrieved is: " + results.get(s).size());
                    items.add("The document IDs retrieved are:");

                    for (String doc : results.get(s)) {
                        items.add(doc);
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
            catch(Exception e){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Please Enter a Valid Path!");
                alert.show();}
        }

    }

    public void saveResults() {
        if (saveResultsPath != null && !saveResultsPath.equals("")) {
            try {
                PrintWriter writer = new PrintWriter(saveResultsPath + ".txt", "UTF-8");
                if (searcher.queryNumbers == null) {
                    for (String docid : results.get("0")) {
                        writer.println("0" + " 0 " + docid + " 1 42.38 mt");
                        writer.flush();
                    }
                } else {
                    for (String s : searcher.queryNumbers) {
                        for (String docid : results.get(s)) {
                            writer.println(s + " 0 " + docid + " 1 42.38 mt");
                            writer.flush();
                        }
                    }
                }
                writer.close();
            } catch (Exception e) {
            }


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Results has been save!");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Enter a path to save results");
            alert.show();
        }
    }

    /**
     * opens a directory chooser to select the directory to save the posting files in
     */
    public void browseSaveResults(){
        try {
            JFileChooser chooser=new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text", "txt");
            chooser.setFileFilter(filter);
            chooser.showSaveDialog(null);

            saveResultsPath=chooser.getSelectedFile().getAbsolutePath();
            saveResultstxt.setText(saveResultsPath);

        }catch (Exception e){}

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

    public void resetAll(){

        if(saveResultsPath!=null) {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            //delete the results file
            File results = new File(saveResultsPath + ".txt");
            results.delete();

            //delete from RAM
            rf=new ReadFile();
            searcher = new Searcher();
            this.results = new HashMap<>();
            loadPath="";
            saveResultsPath="";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Reset has been Completed");
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
