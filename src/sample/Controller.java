package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.scene.control.Alert;

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


    public void setStage(Stage primaryStage){
        stage=primaryStage;
    }

    public void browseCorpus(){

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Browse");
        File selectedDirectory = chooser.showDialog(stage);
        corpusPath=(selectedDirectory.toString());
        corpBrowse.setText(selectedDirectory.toString());


    }
    public void browseDest(){

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Browse");
        File selectedDirectory = chooser.showDialog(stage);
        destinationDirectory=(selectedDirectory.toString());
        destBrowse.setText(selectedDirectory.toString());
    }

    public void browsesave(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Save");
        File selectedDirectory = chooser.showDialog(stage);
        savePath=selectedDirectory.toString();
        savetxt.setText(savePath);
    }

    public void browseload(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Load");
        File selectedDirectory = chooser.showDialog(stage);
        loadPath=selectedDirectory.toString();
        loadtxt.setText(loadPath);
    }

    public void save(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                try {
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
                    e.printStackTrace();
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
            rf.setDestinationDirectory(destinationDirectory);
            rf.setStopwordsPath(corpusPath+"/stop_words");
            rf.setCtrl();
            rf.read(corpusPath+"/corpus");
            btn_start.setDisable(false);
            btn_reset.setDisable(false);
            browse1.setDisable(false);
            browse2.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Indexing Process has Completed");
            alert.show();

        }catch (NullPointerException e){
      //     e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("File Path Invalid");
            alert.show();
            btn_start.setDisable(false);
            btn_reset.setDisable(false);
            browse1.setDisable(false);
            browse2.setDisable(false);
        }
        catch (FileNotFoundException e){
        //    e.printStackTrace();
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

    public void reset(){

            if(destinationDirectory!=null) {
                btn_start.setDisable(true);
                btn_reset.setDisable(true);
                browse1.setDisable(true);
                browse2.setDisable(true);
                cacheB.setDisable(true);
                dictB.setDisable(true);

                char c = 'a';
                while (c <= 'z') {
                    File posting = new File(destinationDirectory + "/" + c + ".txt");
                    posting.delete();
                    c++;
                }
                File nonLetters = new File(destinationDirectory + "/nonLetters.txt");
                File documents = new File(destinationDirectory + "/documents.txt");
                nonLetters.delete();
                documents.delete();

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

            HashMap<String, HashSet<TermInDoc>> cache = rf.ctrl.indexer.cache.getCache();

            //sort the terms in the cache lexicographically
            SortedSet<String> sortedKeys = new TreeSet<String>(cache.keySet());

         //   StringBuilder cacheDetails = new StringBuilder();
            ObservableList<String> items= FXCollections.observableArrayList();
            int counter=1;
            for (String term : sortedKeys) {
                StringBuilder cacheDetails = new StringBuilder();
                cacheDetails.append(counter+") "+term + ": ");
                for (TermInDoc tid : cache.get(term)) {
                    cacheDetails.append(tid.toString());

                }
                items.add(cacheDetails.toString());
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
