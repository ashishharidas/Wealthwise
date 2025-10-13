package com.smartfinance.Controller.Client;

import com.smartfinance.Models.Budget;
import com.smartfinance.Models.Client;
import com.smartfinance.Models.Model;
import com.smartfinance.Models.Transaction;
import com.smartfinance.Views.CategoryTransaction;
import com.smartfinance.Views.TransactionCellFactory;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.net.URL;

public class DashboardController implements Initializable {

    public Text user_name;
    public Label login_date;

    public Label checking_acc_bal;
    public Label checking_acc_num;
    public Label saving_acc_bal;
    public Label saving_acc_num;
    public Label income_lbl;
    public Label expense_lbl;

    public ListView<Transaction> transaction_listview;
    public TextField payee_fld;
    public TextField amount_fld;
    public ChoiceBox<String> category_transaction;
    public TextArea message_fld;
    public Button send_money_btn;
    public Button refresh_btn; // ✅ new refresh button

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Client client = Model.getInstance().getLoggedInClient();

        if (client != null) {
            // Display name and login date
            user_name.setText(client.getFirstName() + " " + client.getLastName());
            login_date.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

            // Load balances
            updateAccountLabels(client);

            // Load transactions
            loadTransactionHistory(client);

            // Load and monitor budgets
            refreshBudgetAlerts(client);

            // Set up send money button
            send_money_btn.setOnAction(event -> onSendMoney());

            // ✅ Set up refresh button
            if (refresh_btn != null) {
                refresh_btn.setOnAction(event -> onRefresh());
            }

            // Populate category choices with dynamic list (including defaults)
            category_transaction.setItems(CategoryTransaction.getAvailableCategories());
        }
    }

    private void refreshBudgetAlerts(Client client) {
        if (client == null) {
            return;
        }

        Model model = Model.getInstance();
        String owner = client.getPayeeAddress();

        for (Budget budget : model.getBudgets()) {
            double remaining = model.getRemainingBudgetForCurrentMonth(owner, budget.getCategory());

            if (remaining <= 0) {
                showAlert(Alert.AlertType.WARNING,
                        "Budget Exceeded",
                        String.format("You have exceeded the budget for %s this month.", budget.getCategory()));
            } else if (remaining <= budget.getAmount() * 0.1) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Budget Alert",
                        String.format("You are close to exceeding the budget for %s. Only ₹%.2f left this month.",
                                budget.getCategory(), remaining));
            }
        }
    }

    // ✅ Helper: Refresh client data
    private void onRefresh() {
        Model model = Model.getInstance();
        model.refreshClientData();

        Client client = model.getLoggedInClient();
        if (client != null) {
            updateAccountLabels(client);
            loadTransactionHistory(client);
            refreshBudgetAlerts(client);
            Model.getInstance().notifyClientDataRefreshed();
        }

        System.out.println("Dashboard refreshed successfully!");
    }

    // ✅ Helper: Update account number and balance labels
    private void updateAccountLabels(Client client) {
        if (client.getWalletAccount() != null) {
            checking_acc_num.setText(client.getWalletAccount().getAccNumber());
            checking_acc_bal.setText("₹" + String.format("%.2f", client.getWalletAccount().getBalance()));
        }

        if (client.getSavingsAccount() != null) {
            saving_acc_num.setText(client.getSavingsAccount().getAccNumber());
            saving_acc_bal.setText("₹" + String.format("%.2f", client.getSavingsAccount().getBalance()));
        }

        // Calculate income and expense
        double income = 0.0;
        double expense = 0.0;
        String clientAddress = client.getPayeeAddress();
        for (Transaction t : client.getTransactionHistory()) {
            if (clientAddress.equals(t.getReceiver())) {
                income += t.getAmount();
            } else if (clientAddress.equals(t.getSender())) {
                expense += t.getAmount();
            }
        }
        income_lbl.setText("₹" + String.format("%.2f", income));
        expense_lbl.setText("₹" + String.format("%.2f", expense));
    }

    // ✅ Helper: Load transaction history from model
    private void loadTransactionHistory(Client client) {
        List<Transaction> transactions = client.getTransactionHistory();
        if (transactions != null && !transactions.isEmpty()) {
            transaction_listview.setItems(FXCollections.observableArrayList(transactions));
            transaction_listview.setCellFactory(param -> new TransactionCellFactory());
        } else {
            transaction_listview.setItems(FXCollections.observableArrayList());
        }
    }

    // ✅ Send money event
    private void onSendMoney() {
        String payeeAddress = payee_fld.getText().trim();
        String amountText = amount_fld.getText().trim();
        String category = category_transaction.getValue() != null ? category_transaction.getValue().trim() : "";
        String message = message_fld.getText().trim();

        if (payeeAddress.isEmpty() || amountText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing fields", "Please fill in payee address and amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid amount", "Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid input", "Please enter a valid number for amount.");
            return;
        }

        // Set default category if empty
        if (category.isEmpty()) {
            category = "Transfer";
        }

        Client sender = Model.getInstance().getLoggedInClient();
        if (sender == null || sender.getSavingsAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "No account", "No logged in client or savings account found.");
            return;
        }

        String senderAddress = sender.getPayeeAddress();

        // Refresh client data to get latest balances
        Model.getInstance().refreshClientData();
        updateAccountLabels(sender);

        double currentBalance = sender.getSavingsAccount().getBalance();

        if (currentBalance < amount) {
            showAlert(Alert.AlertType.WARNING, "Insufficient Balance", "Not enough funds to complete the transfer.");
            return;
        }

        Model model = Model.getInstance();
        if (!model.clientExists(payeeAddress)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Payee", "Receiver address not found in system.");
            return;
        }

        Optional<Budget> matchingBudget = model.findBudget(category);
        if (matchingBudget.isPresent()) {
            double monthlyLimit = matchingBudget.get().getAmount();
            double spentThisMonth = model.getBudgetSpentForCurrentMonth(senderAddress, category);
            double remaining = monthlyLimit - spentThisMonth;

            if (remaining <= 0) {
                showAlert(Alert.AlertType.WARNING,
                        "Budget Exceeded",
                        String.format("You have already exceeded the budget for %s. Transaction cancelled.", category));
                return;
            }

            if (amount > remaining) {
                showAlert(Alert.AlertType.WARNING,
                        "Budget Limit",
                        String.format("This transaction exceeds your remaining budget for %s (left ₹%.2f). Transaction cancelled.",
                                category, remaining));
                return;
            }
        }

        boolean success = model.transferMoney(senderAddress, payeeAddress, amount, category, message);

        if (success) {
            model.refreshClientData();
            updateAccountLabels(sender);
            loadTransactionHistory(sender);

            payee_fld.clear();
            amount_fld.clear();
            category_transaction.setValue(null);
            message_fld.clear();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Money transferred successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Transfer Failed", "Could not complete the transaction.");
        }
    }

    // ✅ Helper for popups
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
