package com.smartfinance.Controller.Admin;

import com.smartfinance.Models.ClientReport;
import com.smartfinance.Models.GeneratedReport;
import com.smartfinance.Models.Model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class reportAdminController implements Initializable {

    @FXML
    private Button all_reports_btn;
    
    @FXML
    private Button client_reports_btn;
    
    @FXML
    private Button refresh_btn;
    
    @FXML
    private Button generated_reports_btn;
    
    @FXML
    private TableView<ClientReport> reports_table;
    
    @FXML
    private TableColumn<ClientReport, Integer> id_col;
    
    @FXML
    private TableColumn<ClientReport, String> client_col;
    
    @FXML
    private TableColumn<ClientReport, String> issue_type_col;
    
    @FXML
    private TableColumn<ClientReport, String> description_col;
    
    @FXML
    private TableColumn<ClientReport, String> date_col;
    
    @FXML
    private TableColumn<ClientReport, String> status_col;
    
    @FXML
    private ComboBox<String> status_combo;
    
    @FXML
    private Button update_status_btn;
    
    @FXML
    private Label status_message_label;
    
    @FXML
    private Label total_reports_label;
    
    @FXML
    private Label open_reports_label;
    
    @FXML
    private Label fixed_reports_label;
    
    private ObservableList<ClientReport> reportsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        id_col.setCellValueFactory(new PropertyValueFactory<>("id"));
        client_col.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        issue_type_col.setCellValueFactory(new PropertyValueFactory<>("issueType"));
        description_col.setCellValueFactory(new PropertyValueFactory<>("description"));
        date_col.setCellValueFactory(new PropertyValueFactory<>("dateReported"));
        status_col.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Initialize status combo box
        status_combo.setItems(FXCollections.observableArrayList(
            "Open",
            "In Progress",
            "Fixed",
            "Closed",
            "Won't Fix"
        ));
        
        // Load all reports by default
        onAllReports();
    }

    @FXML
    private void onAllReports() {
        reportsList.clear();
        
        Model model = Model.getInstance();
        
        try (ResultSet rs = model.getDatabaseDriver().getAllClientReports()) {
            int totalCount = 0;
            int openCount = 0;
            int fixedCount = 0;
            
            while (rs != null && rs.next()) {
                int id = rs.getInt("ID");
                String clientName = rs.getString("ClientName");
                String payeeAddress = rs.getString("PayeeAddress");
                String issueType = rs.getString("IssueType");
                String description = rs.getString("Description");
                String dateReported = rs.getString("DateReported");
                String status = rs.getString("Status");
                
                ClientReport clientReport = new ClientReport(
                    id, clientName, payeeAddress, issueType, 
                    description, dateReported, status
                );
                
                reportsList.add(clientReport);
                totalCount++;
                
                if ("Open".equalsIgnoreCase(status)) {
                    openCount++;
                } else if ("Fixed".equalsIgnoreCase(status)) {
                    fixedCount++;
                }
            }
            
            reports_table.setItems(reportsList);
            total_reports_label.setText(String.valueOf(totalCount));
            open_reports_label.setText(String.valueOf(openCount));
            fixed_reports_label.setText(String.valueOf(fixedCount));
            
            if (totalCount == 0) {
                status_message_label.setText("No reports found");
                status_message_label.setStyle("-fx-text-fill: gray;");
            } else {
                status_message_label.setText("");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            status_message_label.setText("Error loading reports: " + e.getMessage());
            status_message_label.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            e.printStackTrace();
            status_message_label.setText("Error: " + e.getMessage());
            status_message_label.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onClientReports() {
        StringBuilder report = new StringBuilder();
        report.append("CLIENT STATISTICS\n");
        report.append("=================\n\n");
        
        Model model = Model.getInstance();
        
        try (ResultSet rs = model.getDatabaseDriver().getAllClients()) {
            int clientCount = 0;
            double totalBalance = 0;
            
            while (rs != null && rs.next()) {
                clientCount++;
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String payeeAddress = rs.getString("PayeeAddress");
                String dateCreated = rs.getString("Date");
                
                report.append(String.format("Client #%d: %s %s\n", clientCount, firstName, lastName));
                report.append(String.format("  Payee Address: %s\n", payeeAddress));
                report.append(String.format("  Date Created: %s\n", dateCreated));
                
                // Get account balances
                try (ResultSet walletRs = model.getDatabaseDriver().getWalletAccount(payeeAddress)) {
                    if (walletRs != null && walletRs.next()) {
                        double walletBalance = walletRs.getDouble("Balance");
                        totalBalance += walletBalance;
                        report.append(String.format("  Wallet Balance: ₹%.2f\n", walletBalance));
                    }
                }
                
                try (ResultSet savingsRs = model.getDatabaseDriver().getSavingsAccount(payeeAddress)) {
                    if (savingsRs != null && savingsRs.next()) {
                        double savingsBalance = savingsRs.getDouble("Balance");
                        totalBalance += savingsBalance;
                        report.append(String.format("  Savings Balance: ₹%.2f\n", savingsBalance));
                    }
                }
                
                report.append("\n");
            }
            
            report.append("\nSUMMARY:\n");
            report.append(String.format("Total Clients: %d\n", clientCount));
            report.append(String.format("Total System Balance: ₹%.2f\n", totalBalance));
            
        } catch (SQLException e) {
            e.printStackTrace();
            report.append("Error generating client statistics: ").append(e.getMessage());
        }
        
        // Display in a dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Client Statistics");
        alert.setHeaderText("System Client Statistics");
        
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void onRefresh() {
        onAllReports();
    }
    
    @FXML
    private void onUpdateStatus() {
        ClientReport selectedReport = reports_table.getSelectionModel().getSelectedItem();
        String newStatus = status_combo.getValue();
        
        if (selectedReport == null) {
            status_message_label.setText("Please select a report first");
            status_message_label.setStyle("-fx-text-fill: red;");
            return;
        }
        
        if (newStatus == null || newStatus.isEmpty()) {
            status_message_label.setText("Please select a status");
            status_message_label.setStyle("-fx-text-fill: red;");
            return;
        }
        
        Model model = Model.getInstance();
        boolean success = model.getDatabaseDriver().updateReportStatus(selectedReport.getId(), newStatus);
        
        if (success) {
            status_message_label.setText("✓ Status updated to: " + newStatus);
            status_message_label.setStyle("-fx-text-fill: green;");
            
            // Update the local object
            selectedReport.setStatus(newStatus);
            
            // Refresh the table to show updated data
            reports_table.refresh();
            
            // Update counters
            onAllReports();
            
            // Clear selection and combo box
            status_combo.setValue(null);
            
            // Clear message after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> status_message_label.setText(""));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } else {
            status_message_label.setText("Failed to update status");
            status_message_label.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void onGeneratedReports() {
        Model model = Model.getInstance();
        
        try (ResultSet rs = model.getDatabaseDriver().getAllGeneratedReports()) {
            ObservableList<GeneratedReport> generatedReportsList = FXCollections.observableArrayList();
            
            while (rs != null && rs.next()) {
                int id = rs.getInt("ID");
                String clientName = rs.getString("ClientName");
                String payeeAddress = rs.getString("PayeeAddress");
                String reportType = rs.getString("ReportType");
                String reportContent = rs.getString("ReportContent");
                String dateGenerated = rs.getString("DateGenerated");
                
                GeneratedReport generatedReport = new GeneratedReport(
                    id, clientName, payeeAddress, reportType, 
                    reportContent, dateGenerated
                );
                
                generatedReportsList.add(generatedReport);
            }
            
            if (generatedReportsList.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Generated Reports");
                alert.setHeaderText("No Reports Found");
                alert.setContentText("No financial reports have been generated by clients yet.");
                alert.showAndWait();
                return;
            }
            
            // Create a dialog to display generated reports
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Client Generated Financial Reports");
            dialog.setHeaderText("View all financial reports generated by clients");
            
            // Create table view for generated reports
            TableView<GeneratedReport> table = new TableView<>();
            table.setItems(generatedReportsList);
            table.setPrefHeight(400);
            table.setPrefWidth(800);
            
            TableColumn<GeneratedReport, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(50);
            
            TableColumn<GeneratedReport, String> clientCol = new TableColumn<>("Client");
            clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
            clientCol.setPrefWidth(150);
            
            TableColumn<GeneratedReport, String> typeCol = new TableColumn<>("Report Type");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
            typeCol.setPrefWidth(150);
            
            TableColumn<GeneratedReport, String> dateCol = new TableColumn<>("Date Generated");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("dateGenerated"));
            dateCol.setPrefWidth(150);
            
            TableColumn<GeneratedReport, Void> actionCol = new TableColumn<>("Action");
            actionCol.setPrefWidth(100);
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                
                {
                    viewBtn.setOnAction(event -> {
                        GeneratedReport report = getTableView().getItems().get(getIndex());
                        showReportContent(report);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewBtn);
                    }
                }
            });
            
            table.getColumns().addAll(idCol, clientCol, typeCol, dateCol, actionCol);
            
            // Add table to dialog
            GridPane grid = new GridPane();
            grid.add(table, 0, 0);
            GridPane.setHgrow(table, Priority.ALWAYS);
            GridPane.setVgrow(table, Priority.ALWAYS);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.setResizable(true);
            dialog.showAndWait();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Failed to load generated reports: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void showReportContent(GeneratedReport report) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(report.getReportType());
        alert.setHeaderText("Client: " + report.getClientName() + " | Date: " + report.getDateGenerated());
        
        TextArea textArea = new TextArea(report.getReportContent());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setPrefRowCount(25);
        textArea.setPrefColumnCount(60);
        
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        
        alert.getDialogPane().setContent(expContent);
        alert.setResizable(true);
        alert.showAndWait();
    }
}