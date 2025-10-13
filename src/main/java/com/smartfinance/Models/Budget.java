package com.smartfinance.Models;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a monthly budget category configured by a client.
 */
public class Budget {
    private final StringProperty owner;
    private final StringProperty category;
    private final DoubleProperty amount;
    private final DoubleProperty spent;
    private final StringProperty creationDate;

    public Budget(String owner, String category, double amount, double spent, String creationDate) {
        this.owner = new SimpleStringProperty(this, "owner", owner);
        this.category = new SimpleStringProperty(this, "category", category);
        this.amount = new SimpleDoubleProperty(this, "amount", amount);
        this.spent = new SimpleDoubleProperty(this, "spent", spent);
        this.creationDate = new SimpleStringProperty(this, "creationDate", creationDate);
    }

    public Budget(String owner, String category, double amount) {
        this(owner, category, amount, 0.0, LocalDate.now().toString());
    }

    public String getOwner() {
        return owner.get();
    }

    public StringProperty ownerProperty() {
        return owner;
    }

    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public double getAmount() {
        return amount.get();
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(double newAmount) {
        this.amount.set(newAmount);
    }

    public double getSpent() {
        return spent.get();
    }

    public DoubleProperty spentProperty() {
        return spent;
    }

    public void setSpent(double newSpent) {
        this.spent.set(newSpent);
    }

    public String getCreationDate() {
        return creationDate.get();
    }

    public StringProperty creationDateProperty() {
        return creationDate;
    }

    public void setCreationDate(String newDate) {
        this.creationDate.set(newDate);
    }

    @Override
    public String toString() {
        return String.format("%s (₹%.2f)", getCategory(), getAmount());
    }
}