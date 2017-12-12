package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

      //  FXMLLoader fxmlLoader = FXMLLoader.load(getClass().getResource("sample.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("sample.fxml"));
        fxmlLoader.setController(new Controller());
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 800, 700));

        Controller ctrl = new Controller();
        ctrl.setStage(primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
