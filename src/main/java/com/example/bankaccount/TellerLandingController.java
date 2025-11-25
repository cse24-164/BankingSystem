package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.bankaccount.ExistingCustomerController;


public class TellerLandingController {

    @FXML private Label welcomeLabel;
    @FXML private Label branchLabel;
    @FXML private Label timeLabel;

    private BankTeller currentTeller;

    private BankingService bankingService;

    public void setBankTeller(BankTeller teller) {
        this.currentTeller = teller;
        this.bankingService = new BankingService();
        updateDashboard();
        startClock();
    }

    private void updateDashboard() {
        if (currentTeller != null) {
            welcomeLabel.setText("Welcome, " + currentTeller.getFirstName());
            branchLabel.setText("Branch: " + currentTeller.getBranchCode());
        }
    }

    private void startClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    String time = timeFormat.format(new Date());
                    javafx.application.Platform.runLater(() -> timeLabel.setText(time));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    @FXML
    private void openIndividualAccount() {
        Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
        currentStage.close();

        openAccountScreen("individualAccount.fxml", "Open Individual Account", IndividualAccountController.class);
    }

    @FXML
    private void openCompanyAccount() {
        Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
        currentStage.close();

        openAccountScreen("companyAccount.fxml", "Open Company Account", com.example.bankaccount.CompanyAccountController.class);
    }

    public void addAccountOpened(String accountNumber, String customerName) {
        System.out.println("Account opened - Number: " + accountNumber + ", Customer: " + customerName);
    }

    public void addCustomerCreated(String customerName) {
        System.out.println("New customer created: " + customerName);
    }


    @FXML
    private void openAccountForExistingCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("existingCustomerOverview.fxml"));
            Parent root = loader.load();

            ExistingCustomerController controller = loader.getController();
            controller.setBankTeller(currentTeller);

            Stage stage = new Stage();
            stage.setTitle("View Existing Customers - " + currentTeller.getFullName());
            stage.setScene(new Scene(root, 1024, 768));
            stage.show();

            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showAlert("Error", "Cannot open existing customer screen: " + e.getMessage());
        }
    }


    //generic method for loading screen
    private <T> void openAccountScreen(String fxmlFile, String title, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof IndividualAccountController) {
                ((IndividualAccountController) controller).setBankTeller(currentTeller);
                ((IndividualAccountController) controller).setLandingController(this);
            } else if (controller instanceof CompanyAccountController) {
                ((CompanyAccountController) controller).setBankTeller(currentTeller);
                ((CompanyAccountController) controller).setLandingController(this);
            }
            else if (controller instanceof ExistingCustomerController) {
                ((ExistingCustomerController) controller).setBankTeller(currentTeller);

            }

            Stage stage = new Stage();
            stage.setTitle(title + " - " + currentTeller.getFullName());
            stage.setScene(new Scene(root, 1024, 768));
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Cannot open " + title.toLowerCase() + " screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}