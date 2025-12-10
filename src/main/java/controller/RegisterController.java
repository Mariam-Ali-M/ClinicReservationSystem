package controller;

import dao.PatientDAO;
import dao.PractitionerDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Patient;
import model.Practitioner;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class RegisterController {

    @FXML private CheckBox patientCheck;
    @FXML private CheckBox doctorCheck;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;

    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Hyperlink loginLink;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private DatePicker dobPicker;

    @FXML
    public void initialize() {
        patientCheck.setOnAction(e -> {
            if (patientCheck.isSelected()) doctorCheck.setSelected(false);
        });
        doctorCheck.setOnAction(e -> {
            if (doctorCheck.isSelected()) patientCheck.setSelected(false);
        });
        genderComboBox.getItems().addAll("Male", "Female", "Other");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadPage("/home.fxml", event, "Clinic Reservation System");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        loadPage("/login.fxml", event, "Login");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String gender = genderComboBox.getValue();
        LocalDate dob = dobPicker.getValue();

        if (!patientCheck.isSelected() && !doctorCheck.isSelected()) {
            showAlert("Error", "Please select Patient or Practitioner.");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || gender == null || dob == null) {
            showAlert("Error", "All fields are required, including gender and date of birth.");
            return;
        }

        if (!name.matches("^[A-Za-z][A-Za-z ]{2,}$")) {
            showAlert("Error", "Full Name must start with a letter and be at least 3 characters.");
            return;
        }
        if (!email.matches("^[A-Za-z][A-Za-z_]{2,}[0-9]*@[A-Za-z0-9.-]+$")) {
            showAlert("Error", "Invalid email format.");
            return;
        }
        if (!phone.matches("^01[0-2,5][0-9]{8}$")) {
            showAlert("Error", "Invalid phone number.");
            return;
        }
        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters.");
            return;
        }

        try {
            PatientDAO patientDAO = new PatientDAO();
            PractitionerDAO practitionerDAO = new PractitionerDAO();

            boolean exists = false;
            if (patientCheck.isSelected()) {
                for (Patient p : patientDAO.getAll()) {
                    if (p.getEmail().equalsIgnoreCase(email) || p.getPhone().equals(phone)) {
                        exists = true;
                        break;
                    }
                }
            } else {
                for (Practitioner d : practitionerDAO.getAll()) {
                    if (d.getEmail().equalsIgnoreCase(email) || d.getPhone().equals(phone)) {
                        exists = true;
                        break;
                    }
                }
            }

            if (exists) {
                showAlert("Warning", "This email or phone is already registered.");
                return;
            }
            if (patientCheck.isSelected()) {
                Patient patient = new Patient(0, name, phone, email, password, gender, dob);
                patientDAO.add(patient);
            } else {
                Practitioner practitioner = new Practitioner(0, name, phone, email, password, gender, dob);
                practitionerDAO.add(practitioner);
            }

            showAlert("Success", "Account created successfully!");
            loadPage("/login.fxml", event, "Login");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Registration failed: " + e.getMessage());
        }
    }

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}