package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class representing the Clinic Reservation System GUI.
 * Opens Home.fxml as the first screen.
 */
public class ClinicReservationSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Clinic Reservation System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args); // يشغل التطبيق
    }
}
