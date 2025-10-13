package com.smartfinance.Models;

/**
 * ClientReport - Model class for client issue reports
 */
public class ClientReport {
    private final int id;
    private final String clientName;
    private final String payeeAddress;
    private final String issueType;
    private final String description;
    private final String dateReported;
    private String status;

    public ClientReport(int id, String clientName, String payeeAddress, String issueType, 
                       String description, String dateReported, String status) {
        this.id = id;
        this.clientName = clientName;
        this.payeeAddress = payeeAddress;
        this.issueType = issueType;
        this.description = description;
        this.dateReported = dateReported;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPayeeAddress() {
        return payeeAddress;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public String getDateReported() {
        return dateReported;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}