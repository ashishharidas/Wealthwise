package com.smartfinance.Controller.Admin;

import com.smartfinance.Models.Client;
import com.smartfinance.Models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientCellController implements Initializable {

    @FXML
    private Label fname_lbl;
    @FXML
    private Label lname_lbl;
    @FXML
    private Label pAddress_lbl;
    @FXML
    private Label sav_balance;
    @FXML
    private Label sav_acc_number;
    @FXML
    private Label wallet_balance;
    @FXML
    private Label wallet_acc_number;
    @FXML
    private Button delete_btn;

    private final Client client;

    public ClientCellController(Client client) {
        this.client = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fname_lbl.setText(client.getFirstName());
        lname_lbl.setText(client.getLastName());
        pAddress_lbl.setText(client.getPayeeAddress());
        if (client.getSavingsAccount() != null) {
            sav_balance.setText("₹" + String.format("%.2f", client.getSavingsAccount().getBalance()));
            sav_acc_number.setText(client.getSavingsAccount().getAccNumber());
        } else {
            sav_balance.setText("—");
            sav_acc_number.setText("—");
        }

        if (client.getWalletAccount() != null) {
            wallet_balance.setText("₹" + String.format("%.2f", client.getWalletAccount().getBalance()));
            wallet_acc_number.setText(client.getWalletAccount().getAccNumber());
        } else {
            wallet_balance.setText("—");
            wallet_acc_number.setText("—");
        }

        delete_btn.setOnAction(event -> {
            Model.getInstance().deleteClient(client);
        });
    }
}
