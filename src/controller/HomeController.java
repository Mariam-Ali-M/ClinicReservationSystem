package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class HomeController {

    // ربط الـ fx:id من FXML بالمتغيرات
    @FXML
    private TextField loginID;

    @FXML
    private TextField registerID;

    @FXML
    private TextField exitID;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button exitButton;

    // initialize method: ينفذ أول ما الصفحة تتحمل
    @FXML
    public void initialize() {
        System.out.println("HomeController initialized");
        // أي إعدادات أولية ممكن تتحط هنا
    }

    // Actions لكل زرار
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginID.getText();
        System.out.println("Login clicked: " + username);
        // هنا بعد كده هتنادي method تسجيل الدخول من Service class
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerID.getText();
        System.out.println("Register clicked: " + username);
        // هنا هتفتح نافذة Register أو form
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Exit clicked");
        System.exit(0); // يقفل التطبيق
    }
}
