package com.smartfinance.Controller.Admin;

import com.smartfinance.Models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateClientController implements Initializable {

    @FXML
    private TextField first_name;
    @FXML
    private TextField last_name;
    @FXML
    private TextField password;
    @FXML
    private TextField payee_address;
    @FXML
    private TextField wallet_balance;
    @FXML
    private TextField account_balance;
    @FXML
    private Button create_client_button;
    @FXML
    private Label error_lbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        create_client_button.setOnAction(event -> onCreateClient());
    }

    private void onCreateClient() {
        String firstName = first_name.getText().trim();
        String lastName = last_name.getText().trim();
        String pAddress = payee_address.getText().trim();
        String pass = password.getText().trim();

        // ✅ FIX: Remove invalid double assignment (cannot assign TextField to double)
        double walletbalance;
        double savingsbalance;

        try {
            walletbalance = Double.parseDouble(wallet_balance.getText().trim());
            savingsbalance = Double.parseDouble(account_balance.getText().trim());
        } catch (NumberFormatException e) {
            error_lbl.setText("⚠ Please enter valid numbers for Wallet and Savings balance");
            return;
        }

        // ✅ Validation for empty fields
        if (firstName.isEmpty() || lastName.isEmpty() || pAddress.isEmpty() || pass.isEmpty()) {
            error_lbl.setText("⚠ Please fill all the fields");
            return;
        }

        // ✅ Create client (assuming Model handles DB + account creation)
        boolean success = Model.getInstance().createNewClient(firstName, lastName, pAddress, pass, walletbalance, savingsbalance);

        if (success) {
            error_lbl.setText("✅ Client created successfully!");
            clearFields();
        } else {
            error_lbl.setText("❌ Failed to create client. Try again.");
        }
    }

    private void clearFields() {
        first_name.clear();
        last_name.clear();
        payee_address.clear();
        password.clear();
        wallet_balance.clear();
        account_balance.clear();
    }
}
