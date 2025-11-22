package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;


import com.example.bankaccount.Customer;
import com.example.bankaccount.Individual;
import com.example.bankaccount.Company;
import com.example.bankaccount.Account;
import com.example.bankaccount.BankTeller;
import com.example.bankaccount.BankingService;
import com.example.bankaccount.JDBCCustomerDAO;
import com.example.bankaccount.JDBCAccountDAO;
import com.example.bankaccount.CustomerDAO;
import com.example.bankaccount.AccountDAO;

import java.util.Optional;


public class ExistingCustomerController {

    @FXML private TableView<Individual> individualTable;
    @FXML private TableView<Company> companyTable;
    @FXML private TableView<Account> accountsTable;

    @FXML private TableColumn<Individual, String> indNameCol;
    @FXML private TableColumn<Individual, String> indSurnameCol;
    @FXML private TableColumn<Individual, String> indIdCol;
    @FXML private TableColumn<Company, String> compNameCol;
    @FXML private TableColumn<Company, String> compRegCol;
    @FXML private TableColumn<Company, String> compTypeCol;
    @FXML private TableColumn<Account, String> accNumberCol;
    @FXML private TableColumn<Account, String> accTypeCol;
    @FXML private TableColumn<Account, Double> accBalanceCol;

    private CustomerDAO customerDAO = new JDBCCustomerDAO();
    private AccountDAO accountDAO = new JDBCAccountDAO();
    private BankTeller currentTeller;
    private BankingService bankingService;

    @FXML
    public void initialize() {
        setupColumns();
        loadCustomers();

        individualTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                loadAccountsForCustomer(newVal));

        companyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                loadAccountsForCustomer(newVal));
    }

    private void setupColumns() {
        indNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        indSurnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        indIdCol.setCellValueFactory(new PropertyValueFactory<>("idNumber"));

        compNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        compRegCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        compTypeCol.setCellValueFactory(new PropertyValueFactory<>("businessType"));

        accNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        accBalanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
    }

    private void loadCustomers() {
        individualTable.setItems(
                FXCollections.observableArrayList(customerDAO.getAllIndividuals())
        );
        companyTable.setItems(
                FXCollections.observableArrayList(customerDAO.getAllCompanies())
        );
    }

    private void loadAccountsForCustomer(Customer customer) {
        if (customer == null) {
            accountsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Account> accounts = currentTeller.getCustomerAccounts(customer.getCustomerId());
        accountsTable.setItems(FXCollections.observableArrayList(accounts));
    }

    @FXML
    private void handleCreateAccount() {
        Customer selected = getSelectedCustomer();

        if (selected == null) {
            showAlert("Please select a customer first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open Account for Existing Customer");

        ComboBox<String> accountTypeBox = new ComboBox<>();
        accountTypeBox.getItems().addAll("Savings", "Cheque", "Investment");
        accountTypeBox.setPromptText("Select account type");

        TextField branchField = new TextField();
        branchField.setPromptText("Branch");

        TextField depositField = new TextField();
        depositField.setPromptText("Initial Deposit");

        VBox layout = new VBox(10, accountTypeBox, branchField, depositField);
        layout.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {
                String accountType = accountTypeBox.getValue();
                String branch = branchField.getText();
                double initialDeposit = Double.parseDouble(depositField.getText());

                if (accountType == null || branch.isEmpty()) {
                    showAlert("Please fill all fields.");
                    return;
                }

                com.example.bankaccount.BankTeller teller = currentTeller;

                bankingService.createAccountForExistingCustomer(
                        teller,
                        selected,
                        accountType,
                        branch,
                        initialDeposit
                );

                showInfo("Account created successfully!");

            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        }
    }


    public Customer getSelectedCustomer() {
        Customer c = individualTable.getSelectionModel().getSelectedItem();
        if (c != null) return c;
        return companyTable.getSelectionModel().getSelectedItem();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
