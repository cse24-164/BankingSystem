package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;

public class CustomerProfileController {

    @FXML private Label customerIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;

    // Individual-specific
    @FXML private Label idNumberLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private VBox individualFields;

    // Company-specific
    @FXML private Label regNumberLabel;
    @FXML private VBox companyFields;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public void setCustomer(Customer customer) {
        customerIdLabel.setText(String.valueOf(customer.getCustomerId()));
        emailLabel.setText(customer.getEmail());
        phoneLabel.setText(customer.getPhoneNumber());
        addressLabel.setText(customer.getAddress());

        if (customer instanceof Individual ind) {
            nameLabel.setText(ind.getFirstName() + " " + ind.getSurname());
            idNumberLabel.setText(ind.getIdNumber());
            dobLabel.setText(dateFormat.format(ind.getDateOfBirth()));
            genderLabel.setText(ind.getGender());

            // Show individual fields, hide company fields
            individualFields.setVisible(true);
            companyFields.setVisible(false);

        } else if (customer instanceof Company comp) {
            nameLabel.setText(comp.getCompanyName());
            regNumberLabel.setText(comp.getRegistrationNumber());

            // Show company fields, hide individual fields
            companyFields.setVisible(true);
            individualFields.setVisible(false);
        }
    }
}
