package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.io.*;

import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.jar.JarFile;

import javafx.scene.control.Alert;
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

    String corpusPath;
    String destinationDirectory;
    private Stage stage;
    private ReadFile rf;
    String savePath;
    String loadPath;

    public Controller() {
        corpusPath=new String() ;
        destinationDirectory=new String() ;
        corpBrowse=new TextField();
        rf=new ReadFile();
    }

    private int calculateNumOfDocs(){
        return rf.ctrl.parser.docNumber;
    }



    private long calcCacheSize(){
        File file = new File(destinationDirectory+"/"+rf.ctrl.getDirectory()+"/cacheTxt.txt");
        return file.length();
    }

    private long calcDictionairySize(){
        File file = new File(destinationDirectory+"/"+rf.ctrl.getDirectory()+"/dictionairyTxt.txt");
        return file.length();
    }

    private long calcPostingSize(){
        File dir = new File(destinationDirectory+"/"+rf.ctrl.getDirectory());
        long length = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                length += file.length();
        }
        return length;
    }

    public void setStage(Stage primaryStage){
        stage=primaryStage;
    }

    public void browseCorpus(){

        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Browse");
            File selectedDirectory = chooser.showDialog(stage);
            corpusPath = (selectedDirectory.toString());
            corpBrowse.setText(selectedDirectory.toString());
        }catch (Exception e){}

    }
    public void browseDest(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Browse");
            File selectedDirectory = chooser.showDialog(stage);
            destinationDirectory = (selectedDirectory.toString());
            destBrowse.setText(selectedDirectory.toString());
        }catch (Exception e){}

    }

    public void browsesave(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Save");
            File selectedDirectory = chooser.showDialog(stage);
            savePath = selectedDirectory.toString();
            savetxt.setText(savePath);
        }catch (Exception e){}
    }

    public void browseload(){
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Load");
            File selectedDirectory = chooser.showDialog(stage);
            loadPath = selectedDirectory.toString();
            loadtxt.setText(loadPath);
        }catch (Exception e){}
    }

    public void save(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                try {
                    if(savePath==null){
                        savePath=destinationDirectory;
                    }
                    ObjectOutputStream outCache = new ObjectOutputStream(new FileOutputStream(savePath+"/cache.ser"));
                    outCache.writeObject(rf.ctrl.indexer.cache);
                    outCache.flush();
                    outCache.close();

                    ObjectOutputStream outDict = new ObjectOutputStream(new FileOutputStream(savePath+"/dictionairy.ser"));
                    outDict.writeObject(rf.ctrl.indexer.dictionairy);
                    outDict.flush();
                    outDict.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Cache and Dictionairy have been saved");
                    alert.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Please Enter a Path");
                    alert.show();;
                }
            }});
    }

    public void load(){
        //  Platform.runLater(new Runnable(){
        //    @Override
        //    public void run() {
        try {
            ObjectInputStream inCache = new ObjectInputStream(new FileInputStream(loadPath+"/cache.ser"));
            Cache cache = (Cache) inCache.readObject();
            inCache.close();
            rf.ctrl.indexer.cache=cache;

            ObjectInputStream inDict = new ObjectInputStream(new FileInputStream(loadPath+"/dictionairy.ser"));
            rf.ctrl.indexer.dictionairy = (HashMap<String,TermInDictionairy>)inDict.readObject();
            inDict.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Cache and Dictionairy have been loaded");
            alert.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please Enter a Path");
            alert.show();;
        }
        //    }});
    }


    public void run(){
        Platform.runLater(new Runnable(){
            @Override
            public void run(){
                try {

                    btn_start.setDisable(true);
                    btn_reset.setDisable(true);
                    browse1.setDisable(true);
                    browse2.setDisable(true);
                    if(corpusPath.toString().length()==0){
                        corpusPath=corpBrowse.getText();
                    }
                    if(destinationDirectory.toString().length()==0){
                        destinationDirectory=destBrowse.getText();
                    }
                    rf.ctrl.indexer.numberOfTempPostingFiles=0;
                    rf.ctrl.setWithStemming(stembox.isSelected());
                    rf.setDestinationDirectory(destinationDirectory);
                    rf.setStopwordsPath(corpusPath+"/stop_words");
                    rf.setCtrl();
                    rf.ctrl.parser.setWriter();
                    rf.read(corpusPath+"/corpus");
                    saveCacheToText();
                    saveDictToText();
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

    public void saveCacheToText(){
        try {
            File cache = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/cacheTxt.txt");
            PrintWriter writer = new PrintWriter(cache);
            HashMap<String, String> currentCache = rf.ctrl.indexer.cache.getCache();
            SortedSet<String> sortedKeys = new TreeSet<String>(currentCache.keySet());
            for (String term : sortedKeys) {
                writer.println(term + ": " + currentCache.get(term));
                writer.flush();
            }
            writer.close();
        }catch (Exception e){}
    }
    public void saveDictToText(){
        try {
            File dict = new File(destinationDirectory + "/" + rf.ctrl.getDirectory() + "/dictionairyTxt.txt");
            PrintWriter writer = new PrintWriter(dict);
            HashMap<String,TermInDictionairy> currentDict = rf.ctrl.indexer.getDictionairy();
            SortedSet<String> sortedKeys = new TreeSet<String>(currentDict.keySet());
            for (String term : sortedKeys) {
                writer.println(currentDict.get(term).toString());
                writer.flush();
            }
            writer.close();
        }catch (Exception e){}
    }

    public void reset(){

        if(destinationDirectory!=null) {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);
//DELETE THE DIRECTORIES
          /*      char c = 'a';
                while (c <= 'z') {
                    File posting = new File(destinationDirectory + "/" + c + ".txt");
                    posting.delete();
                    c++;
                }
                File nonLetters = new File(destinationDirectory + "/nonLetters.txt");
                File documents = new File(destinationDirectory + "/documents.txt");
                nonLetters.delete();
                documents.delete();*/
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
            if(savePath!=null){
                File cache = new File(savePath+"/"+rf.ctrl.getDirectory()+"/cache.ser");
                File dict = new File(savePath+"/"+rf.ctrl.getDirectory()+"/dictionairy.ser");
                cache.delete();
                dict.delete();
            }
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

    public void displayCache() {

        try {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            HashMap<String, String> cache = rf.ctrl.indexer.cache.getCache();

            //sort the terms in the cache lexicographically
            SortedSet<String> sortedKeys = new TreeSet<String>(cache.keySet());

            //   StringBuilder cacheDetails = new StringBuilder();
            ObservableList<String> items= FXCollections.observableArrayList();
            int counter=1;
            for (String term : sortedKeys) {
                /*StringBuilder cacheDetails = new StringBuilder();
                cacheDetails.append(counter+") "+term + ": ");
                for (TermInDoc tid : cache.get(term).keySet()) {
                    cacheDetails.append(tid.toString());

                }*/
                items.add(term+ ": "+cache.get(term));
                counter++;
                //  cacheDetails.append("\n");
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
        }
        catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Must Index First In Order to Display Cache!");
            alert.show();
        }
    }

    public void displayDictionairy(){

        try {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            cacheB.setDisable(true);
            dictB.setDisable(true);

            HashMap<String, TermInDictionairy> dictionairy = rf.ctrl.indexer.getDictionairy();

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
        }
        catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Must Index First In Order to Display Cache!");
            alert.show();
        }
    }




}
