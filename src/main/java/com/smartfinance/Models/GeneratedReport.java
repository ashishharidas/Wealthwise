package com.smartfinance.Models;

import javafx.beans.property.*;

public class GeneratedReport {
    private final IntegerProperty id;
    private final StringProperty clientName;
    private final StringProperty payeeAddress;
    private final StringProperty reportType;
    private final StringProperty reportContent;
    private final StringProperty dateGenerated;

    public GeneratedReport(int id, String clientName, String payeeAddress, String reportType, 
                          String reportContent, String dateGenerated) {
        this.id = new SimpleIntegerProperty(id);
        this.clientName = new SimpleStringProperty(clientName);
        this.payeeAddress = new SimpleStringProperty(payeeAddress);
        this.reportType = new SimpleStringProperty(reportType);
        this.reportContent = new SimpleStringProperty(reportContent);
        this.dateGenerated = new SimpleStringProperty(dateGenerated);
    }

    // ID
    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    // Client Name
    public String getClientName() {
        return clientName.get();
    }

    public StringProperty clientNameProperty() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName.set(clientName);
    }

    // Payee Address
    public String getPayeeAddress() {
        return payeeAddress.get();
    }

    public StringProperty payeeAddressProperty() {
        return payeeAddress;
    }

    public void setPayeeAddress(String payeeAddress) {
        this.payeeAddress.set(payeeAddress);
    }

    // Report Type
    public String getReportType() {
        return reportType.get();
    }

    public StringProperty reportTypeProperty() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType.set(reportType);
    }

    // Report Content
    public String getReportContent() {
        return reportContent.get();
    }

    public StringProperty reportContentProperty() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent.set(reportContent);
    }

    // Date Generated
    public String getDateGenerated() {
        return dateGenerated.get();
    }

    public StringProperty dateGeneratedProperty() {
        return dateGenerated;
    }

    public void setDateGenerated(String dateGenerated) {
        this.dateGenerated.set(dateGenerated);
    }
}