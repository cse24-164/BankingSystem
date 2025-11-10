package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainMenuController {
    @FXML
    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Banking System - Login");
            loginStage.setScene(new Scene(root, 1024, 768));
            loginStage.setResizable(true);
            loginStage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open login screen: " + e.getMessage());
        }
    }

    @FXML
    private void exitApp() {
        System.exit(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}