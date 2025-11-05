package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class AccountHistoryController {

    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, Integer> accountNumberColumn;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, String> customerNameColumn;
    @FXML private TableColumn<Account, Double> balanceColumn;
    @FXML private TableColumn<Account, String> branchColumn;
    @FXML private TableColumn<Account, String> statusColumn;
    @FXML private Label totalAccountsLabel;

    private BankTeller currentTeller;
    private AccountDAO accountDAO;
    private ObservableList<Account> accountsData = FXCollections.observableArrayList();

    public void setBankTeller(BankTeller teller) {
        this.currentTeller = teller;
    }

    public void setAccountDAO(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
        loadRecentAccounts();
    }

    @FXML
    private void initialize() {
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerDisplayName"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        branchColumn.setCellValueFactory(new PropertyValueFactory<>("branch"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        accountsTable.setItems(accountsData);
    }

    private void loadRecentAccounts() {
        if (accountDAO != null) {
            List<Account> recentAccounts = accountDAO.getRecentlyCreatedAccounts(20);
            accountsData.setAll(recentAccounts);
            totalAccountsLabel.setText("Total Accounts: " + recentAccounts.size());
        }
    }

    @FXML
    private void handleRefresh() {
        loadRecentAccounts();
    }

    @FXML
    private void handleClose() {
        accountsTable.getScene().getWindow().hide();
    }
}