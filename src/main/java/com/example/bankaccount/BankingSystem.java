package com.example.bankaccount;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;


public class BankingSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Looking for FXML...");

        BankingService bankingService = new BankingService();

        InterestScheduler scheduler = new InterestScheduler(bankingService);
        scheduler.start();

        URL fxmlUrl = getClass().getResource("mainmenu.fxml");

        if (fxmlUrl != null) {
            System.out.println("FXML found! Loading...");
            Parent root = FXMLLoader.load(fxmlUrl);
            primaryStage.setTitle("Banking System");
            primaryStage.setScene(new Scene(root, 1024, 768));
            primaryStage.show();
        } else {
            System.out.println("FXML not found. Check: src/main/resources/com/example/bankaccount/mainmenu.fxml");
            createSimpleUI(primaryStage);
        }
    }

    private void createSimpleUI(Stage primaryStage) {
        Label label = new Label(" Banking System\n\nFXML file missing!\nCheck the file location.");
        VBox root = new VBox(label);
        root.setStyle("-fx-padding: 40; -fx-alignment: center;");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}