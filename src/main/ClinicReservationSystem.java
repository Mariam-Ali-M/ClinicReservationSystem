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

    private static void launch(String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void start(Stage primaryStage) throws Exception {
        // تحميل FXML
        Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));

        // إعداد Scene و Stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Clinic Reservation System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // يشغل التطبيق
    }

    private Object getClass() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
