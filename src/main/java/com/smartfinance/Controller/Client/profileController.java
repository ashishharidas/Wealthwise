package com.smartfinance.Controller.Client;

import com.smartfinance.Models.DatabaseDriver;
import com.smartfinance.Models.Model;
import com.smartfinance.Models.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class profileController implements Initializable {

    @FXML private Label first_name_lbl;
    @FXML private Label last_name_lbl;
    @FXML private Label payee_address_lbl;
    @FXML private Label reg_date_lbl;
    @FXML private Label wallet_acc_num_lbl;
    @FXML private Label wallet_balance_lbl;
    @FXML private Label savings_acc_num_lbl;
    @FXML private Label savings_balance_lbl;
    @FXML private Button refresh_btn;
    @FXML private PieChart expenditure_pie_chart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateProfile();
        refresh_btn.setOnAction(e -> refresh());
    }

    private void populateProfile() {
        Client client = Model.getInstance().getLoggedInClient();
        if (client != null) {
            first_name_lbl.setText(client.getFirstName());
            last_name_lbl.setText(client.getLastName());
            payee_address_lbl.setText(client.getPayeeAddress());
            reg_date_lbl.setText(client.getDateCreated().toString());
            populateAccountDetails(client);
            populateExpenditureChart(client);
        }
    }

    private void populateAccountDetails(Client client) {
        DatabaseDriver db = Model.getInstance().getDatabaseDriver();
        String payeeAddress = client.getPayeeAddress();
        try (ResultSet wallet = db.getWalletAccount(payeeAddress)) {
            if (wallet != null && wallet.next()) {
                wallet_acc_num_lbl.setText(wallet.getString("AccountNumber"));
                wallet_balance_lbl.setText("₹" + String.format("%.2f", wallet.getDouble("Balance")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (ResultSet savings = db.getSavingsAccount(payeeAddress)) {
            if (savings != null && savings.next()) {
                savings_acc_num_lbl.setText(savings.getString("AccountNumber"));
                savings_balance_lbl.setText("₹" + String.format("%.2f", savings.getDouble("Balance")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateExpenditureChart(Client client) {
        expenditure_pie_chart.getData().clear();
        DatabaseDriver db = Model.getInstance().getDatabaseDriver();
        String payeeAddress = client.getPayeeAddress();
        try (ResultSet rs = db.getExpendituresByCategory(payeeAddress)) {
            while (rs != null && rs.next()) {
                String category = rs.getString("Category");
                double total = rs.getDouble("Total");
                expenditure_pie_chart.getData().add(new PieChart.Data(category, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void refresh() {
        populateProfile();
    }
}
