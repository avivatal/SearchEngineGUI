package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
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
    StringProperty corpusPath;
    StringProperty destinationDirectory;
    private Stage stage;
    private ReadFile rf;

    public Controller() {
        corpusPath=new SimpleStringProperty() ;
        destinationDirectory=new SimpleStringProperty() ;
    }

    public void setStage(Stage primaryStage){
        stage=primaryStage;
    }

    public void browseCorpus(){

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Browse");
        File selectedDirectory = chooser.showDialog(stage);
        corpusPath.set(selectedDirectory.toString());

    }
    public void browseDest(){

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Browse");
        File selectedDirectory = chooser.showDialog(stage);
        destinationDirectory.set(selectedDirectory.toString());
    }

    public void run(){
        Thread thread = new Thread(()-> {
        try {
            btn_start.setDisable(true);
            btn_reset.setDisable(true);
            browse1.setDisable(true);
            browse2.setDisable(true);
            rf = new ReadFile(corpusPath.get()+"/stop_words.txt",destinationDirectory.get());
            rf.read(corpusPath.get()+"/corpus");
            btn_start.setDisable(false);
            btn_reset.setDisable(false);
            browse1.setDisable(false);
            browse2.setDisable(false);

        }catch (NullPointerException e){
      //     e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("File Path Invalid");
            alert.show();
        }
        catch (FileNotFoundException e){
        //    e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("File Path Invalid");
            alert.show();
        }

    });
        thread.start();
        try {
            thread.join();
        }
        catch (InterruptedException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("File Path Invalid");
            alert.show();
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Indexing Process has Completed");
        alert.show();
    }

    public void reset(){

            if(destinationDirectory.get()!=null) {
                btn_start.setDisable(true);
                btn_reset.setDisable(true);
                browse1.setDisable(true);
                browse2.setDisable(true);
                cacheB.setDisable(true);
                dictB.setDisable(true);

                char c = 'a';
                while (c <= 'z') {
                    File posting = new File(destinationDirectory.get() + "/" + c + ".txt");
                    posting.delete();
                    c++;
                }
                File nonLetters = new File(destinationDirectory.get() + "/nonLetters.txt");
                File documents = new File(destinationDirectory.get() + "/documents.txt");
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
                TermDetails.append(counter+") "+term + ": "+dictionairy.get(term).getNumberOfAppearancesInCorpus()+" Occurrences in Corpus");

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
