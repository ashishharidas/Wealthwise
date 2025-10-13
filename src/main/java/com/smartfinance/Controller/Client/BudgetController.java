package com.smartfinance.Controller.Client;

import com.smartfinance.Models.Budget;
import com.smartfinance.Models.Client;
import com.smartfinance.Models.Model;
import com.smartfinance.Views.CategoryTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {
    @FXML
    private Button create_budget_btn;
    @FXML
    private ListView<Budget> budget_list;
    @FXML
    private ComboBox<String> category_budget;
    @FXML
    private TextField amount_budget;
    @FXML
    private ComboBox<String> change_budget;
    @FXML
    private TextField change_amount;
    @FXML
    private Button change_btn;
    @FXML
    private ComboBox<String> delete_category;
    @FXML
    private Button delete_btn;

    private final Model model = Model.getInstance();
    private Client loggedInClient;
    private ObservableList<Budget> budgets = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loggedInClient = model.getLoggedInClient();
        budget_list.setItems(budgets);

        bindCategoryInputs();
        refreshBudgetList();

        // ✅ Create budget
        create_budget_btn.setOnAction(event -> onCreateBudget());

        // ✅ Change budget (supports selection OR category name)
        change_btn.setOnAction(event -> onUpdateBudget());

        // ✅ Delete budget (by selection OR category name)
        delete_btn.setOnAction(event -> onDeleteBudget());

        budget_list.setCellFactory(list -> new ListCell<Budget>() {
            @Override
            protected void updateItem(Budget budget, boolean empty) {
                super.updateItem(budget, empty);
                if (empty || budget == null) {
                    setText(null);
                } else {
                    double monthlyLimit = budget.getAmount();
                    double remaining = monthlyLimit - budget.getSpent();
                    setText(String.format("%s : Budget ₹%.2f | Left ₹%.2f", budget.getCategory(), monthlyLimit, remaining));
                }
            }
        });
    }

    private void bindCategoryInputs() {
        ObservableList<String> categories = CategoryTransaction.getAvailableCategories();

        configureCategoryCombo(category_budget, categories);
        configureCategoryCombo(change_budget, categories);
        configureCategoryCombo(delete_category, categories);

        budget_list.getSelectionModel().selectedItemProperty().addListener((obs, oldBudget, newBudget) -> {
            if (newBudget != null) {
                change_budget.setValue(newBudget.getCategory());
                delete_category.setValue(newBudget.getCategory());
            }
        });
    }

    private void configureCategoryCombo(ComboBox<String> comboBox, ObservableList<String> categories) {
        comboBox.setEditable(true);
        comboBox.setItems(categories);
    }

    private String resolveCategoryInput(ComboBox<String> comboBox) {
        String text = comboBox.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            text = comboBox.getValue();
        }
        return text == null ? "" : text.trim();
    }

    private void clearComboBox(ComboBox<String> comboBox) {
        comboBox.getSelectionModel().clearSelection();
        comboBox.setValue(null);
        comboBox.getEditor().clear();
    }

    private void refreshBudgetList() {
        budgets.clear();
        if (loggedInClient == null) {
            return;
        }
        budgets.addAll(model.getBudgets());
    }

    private void onCreateBudget() {
        if (loggedInClient == null) {
            showWarning("No Client", "You must be logged in to create a budget.");
            return;
        }

        String category = resolveCategoryInput(category_budget);
        String amountValue = amount_budget.getText().trim();

        if (category.isEmpty() || amountValue.isEmpty()) {
            showWarning("Missing Fields", "Please enter both category and amount.");
            return;
        }

        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(amountValue);
            if (monthlyLimit <= 0) {
                showWarning("Invalid Amount", "Monthly limit must be greater than zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            showWarning("Invalid Number", "Please enter a numeric value for amount.");
            return;
        }

        boolean saved = model.saveBudget(loggedInClient.getPayeeAddress(), category, monthlyLimit);
        if (saved) {
            refreshBudgetList();
            clearComboBox(category_budget);
            amount_budget.clear();
            showInfo("Budget Saved", "Budget for category '" + category + "' saved successfully.");
        } else {
            showWarning("Save Failed", "Unable to save budget. It may already exist or there was a database error.");
        }
    }

    private void onUpdateBudget() {
        if (loggedInClient == null) {
            showWarning("No Client", "You must be logged in to update a budget.");
            return;
        }

        String categoryToChange = "";
        String newAmount = change_amount.getText().trim();

        Budget selectedBudget = budget_list.getSelectionModel().getSelectedItem();
        if (selectedBudget != null) {
            categoryToChange = selectedBudget.getCategory();
        } else {
            categoryToChange = resolveCategoryInput(change_budget);
        }

        if (categoryToChange.isEmpty() || newAmount.isEmpty()) {
            showWarning("Invalid Input", "Please enter the category (or select) and new amount.");
            return;
        }

        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(newAmount);
            if (monthlyLimit <= 0) {
                showWarning("Invalid Amount", "Monthly limit must be greater than zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            showWarning("Invalid Number", "Please enter a numeric value for amount.");
            return;
        }

        boolean saved = model.saveBudget(loggedInClient.getPayeeAddress(), categoryToChange, monthlyLimit);
        if (saved) {
            refreshBudgetList();
            clearComboBox(change_budget);
            change_amount.clear();
            showInfo("Budget Updated", "Budget for category '" + categoryToChange + "' updated successfully.");
        } else {
            showWarning("Update Failed", "Unable to update budget. Please try again.");
        }
    }

    private void onDeleteBudget() {
        if (loggedInClient == null) {
            showWarning("No Client", "You must be logged in to delete a budget.");
            return;
        }

        Budget selectedBudget = budget_list.getSelectionModel().getSelectedItem();
        String categoryToDelete = resolveCategoryInput(delete_category);

        if (selectedBudget != null) {
            categoryToDelete = selectedBudget.getCategory();
        }

        if (categoryToDelete.isEmpty()) {
            showWarning("No Category", "Please select a budget or enter a category to delete.");
            return;
        }

        boolean deleted = model.deleteBudget(loggedInClient.getPayeeAddress(), categoryToDelete);
        if (deleted) {
            refreshBudgetList();
            clearComboBox(delete_category);
            showInfo("Budget Deleted", "Budget for category '" + categoryToDelete + "' deleted successfully.");
        } else {
            showWarning("Delete Failed", "Unable to delete budget. Please try again.");
        }
    }

    /**
     * Utility method to show warning alerts.
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
