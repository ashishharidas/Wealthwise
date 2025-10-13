package com.smartfinance.Models;

import com.smartfinance.Views.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// Forcing a file update
public class Model {
    private final ViewFactory viewFactory;
    private final DatabaseDriver databaseDriver;
    private static Model model;

    private boolean clientLoginSuccessFlag;
    private boolean adminLoginSuccessFlag;
    private Client loggedInClient;

    private Consumer<Client> clientCreatedListener;
    private Consumer<Client> clientDeletedListener;
    private Runnable onClientDataRefreshed;

    private ObservableList<Budget> budgetLimitCache;
    private ObservableList<String> budgetCategories;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.databaseDriver = new DatabaseDriver();
        this.budgetLimitCache = FXCollections.observableArrayList();
        this.budgetCategories = FXCollections.observableArrayList();
    }

    public static synchronized Model getInstance() {
        if (model == null) model = new Model();
        return model;
    }

    public ViewFactory getViewFactory() { return viewFactory; }
    public DatabaseDriver getDatabaseDriver() { return databaseDriver; }

    public ObservableList<Budget> getBudgets() {
        return budgetLimitCache;
    }

    public ObservableList<String> getBudgetCategories() {
        return budgetCategories;
    }

    // =====================================================
    // ACCOUNT LOADERS (auto-fetched from DB)
    // =====================================================
    public WalletAccount getWalletAccount(String owner) {
        try (ResultSet rs = databaseDriver.getWalletAccount(owner)) {
            if (rs != null && rs.next()) {
                return new WalletAccount(
                        owner,
                        rs.getString("AccountNumber"),
                        rs.getDouble("Balance"),
                        (int) rs.getDouble("DepositLimit"),
                        rs.getString("Date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SavingsAccount getSavingsAccount(String owner) {
        try (ResultSet rs = databaseDriver.getSavingsAccount(owner)) {
            if (rs != null && rs.next()) {
                return new SavingsAccount(
                        owner,
                        rs.getString("AccountNumber"),
                        rs.getDouble("Balance"),
                        rs.getDouble("TransactionLimit"),
                        rs.getString("Date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================
    // CLIENT MANAGEMENT
    // =====================================================
    public boolean createNewClient(String firstName, String lastName, String payeeAddress, String password,
                                   double walletBalance, double savingsBalance) {
        LocalDate date = LocalDate.now();

        boolean created = databaseDriver.createClient(firstName, lastName, payeeAddress, password, walletBalance, savingsBalance);

        if (!created) return false;

        if (clientCreatedListener != null) {
            WalletAccount wallet = getWalletAccount(payeeAddress);
            SavingsAccount savings = getSavingsAccount(payeeAddress);
            Client newClient = new Client(firstName, lastName, payeeAddress, wallet, savings, date);
            newClient.setTransactionHistory(loadClientTransactions(payeeAddress));
            loadBudgetsForClient(payeeAddress);
            clientCreatedListener.accept(newClient);
        } else {
            loadBudgetsForClient(payeeAddress);
        }
        return true;
    }


    public boolean deleteClient(Client client) {
        try {
            boolean deleted = databaseDriver.deleteClient(client.getPayeeAddress());
            if (deleted && clientDeletedListener != null) {
                clientDeletedListener.accept(client);
            }
            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // LOGIN HANDLERS
    // =====================================================
    public void evaluateClientCred(String payeeAddress, String password) {
        try (ResultSet rs = databaseDriver.getClientsData(payeeAddress, password)) {
            clientLoginSuccessFlag = rs != null && rs.next();
            if (clientLoginSuccessFlag) {
                String fName = rs.getString("FirstName");
                String lName = rs.getString("LastName");
                String pAddress = rs.getString("PayeeAddress");
                LocalDate date = LocalDate.parse(rs.getString("Date"));

                WalletAccount wallet = getWalletAccount(pAddress);
                SavingsAccount savings = getSavingsAccount(pAddress);

                loggedInClient = new Client(fName, lName, pAddress, wallet, savings, date);
                loggedInClient.setTransactionHistory(loadClientTransactions(pAddress));
                loadBudgetsForClient(pAddress);
                loadBudgetCategoriesForClient(pAddress);
            } else {
                budgetLimitCache.clear();
                budgetCategories.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            clientLoginSuccessFlag = false;
            budgetLimitCache.clear();
        }
    }

    public void evaluateAdminCred(String username, String password) {
        try (ResultSet rs = databaseDriver.getAdminData(username, password)) {
            adminLoginSuccessFlag = rs != null && rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            adminLoginSuccessFlag = false;
        }
    }

    // =====================================================
    // FLAGS & LISTENERS
    // =====================================================
    public boolean getClientLoginSuccessFlag() { return clientLoginSuccessFlag; }
    public boolean getAdminLoginSuccessFlag() { return adminLoginSuccessFlag; }
    public Client getLoggedInClient() { return loggedInClient; }

    public void setClientCreatedListener(Consumer<Client> listener) { this.clientCreatedListener = listener; }
    public void setClientDeletedListener(Consumer<Client> listener) { this.clientDeletedListener = listener; }
    public void setOnClientDataRefreshed(Runnable listener) { this.onClientDataRefreshed = listener; }

    // =====================================================
    // CLIENT UTILITIES
    // =====================================================
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        try (ResultSet rs = databaseDriver.getAllClientsData()) {
            while (rs != null && rs.next()) {
                String fName = rs.getString("FirstName");
                String lName = rs.getString("LastName");
                String pAddress = rs.getString("PayeeAddress");
                LocalDate date = LocalDate.parse(rs.getString("Date"));

                WalletAccount wallet = getWalletAccount(pAddress);
                SavingsAccount savings = getSavingsAccount(pAddress);

                Client client = new Client(fName, lName, pAddress, wallet, savings, date);
                client.setTransactionHistory(loadClientTransactions(pAddress));
                clients.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public boolean clientExists(String payeeAddress) {
        try (ResultSet rs = databaseDriver.searchClients(payeeAddress)) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // TRANSACTIONS
    // =====================================================
    public boolean transferMoney(String senderAddress, String receiverAddress, double amount, String category, String message) {
        // Prevent self-transfer
        if (senderAddress != null && senderAddress.equalsIgnoreCase(receiverAddress)) {
            return false;
        }
        
        DatabaseDriver db = getDatabaseDriver();
        try {
            db.getConnection().setAutoCommit(false);

            try (ResultSet senderSavings = db.getSavingsAccount(senderAddress)) {
                if (senderSavings == null || !senderSavings.next()) return false;

                double senderBalance = senderSavings.getDouble("Balance");
                if (senderBalance < amount) return false;

                db.updateBalance("SavingsAccounts", senderAddress, senderBalance - amount);
            }

            try (ResultSet receiverWallet = db.getWalletAccount(receiverAddress)) {
                if (receiverWallet != null && receiverWallet.next()) {
                    double receiverBalance = receiverWallet.getDouble("Balance");
                    db.updateBalance("WalletAccounts", receiverAddress, receiverBalance + amount);
                } else {
                    // Create wallet account if it doesn't exist
                    db.createWalletAccount(receiverAddress, amount);
                }
            }

            db.createTransaction(senderAddress, receiverAddress, amount, category, message);

            db.getConnection().commit();
            return true;
        } catch (SQLException e) {
            try { db.getConnection().rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { db.getConnection().setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // =====================================================
    // LOAD TRANSACTIONS
    // =====================================================
    public ObservableList<Transaction> loadClientTransactions(String payeeAddress) {
        ObservableList<Transaction> list = FXCollections.observableArrayList();
        try (ResultSet rs = databaseDriver.getTransactionsByPayee(payeeAddress)) {
            while (rs != null && rs.next()) {
                String category = rs.getString("Category");
                if (category == null || category.trim().isEmpty()) category = "Transfer";

                String dateStr = rs.getString("Date");
                LocalDate date = (dateStr != null) ?
                        LocalDate.parse(dateStr.split(" ")[0]) : LocalDate.now();

                list.add(new Transaction(
                        rs.getString("Sender"),
                        rs.getString("Receiver"),
                        rs.getDouble("Amount"),
                        category,
                        date,
                        rs.getString("Message")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void loadBudgetsForClient(String owner) {
        loadBudgetsForClient(owner, true);
    }

    public void loadBudgetsForClient(String owner, boolean synchronizeSpent) {
        budgetLimitCache.clear();
        try (ResultSet rs = databaseDriver.getBudgetsForOwner(owner)) {
            while (rs != null && rs.next()) {
                String category = rs.getString("Category");
                double recordedSpent = rs.getDouble("BudgetSpent");

                if (synchronizeSpent) {
                    double currentSpent = databaseDriver.getSpentAmountForCurrentMonth(owner, category);
                    if (Double.compare(currentSpent, recordedSpent) != 0) {
                        databaseDriver.updateBudgetSpent(owner, category, currentSpent);
                        recordedSpent = currentSpent;
                    }
                }

                budgetLimitCache.add(new Budget(
                        rs.getString("Owner"),
                        category,
                        rs.getDouble("BudgetAmount"),
                        recordedSpent,
                        rs.getString("CreationDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadBudgetCategoriesForClient(String owner) {
        budgetCategories.setAll(databaseDriver.getDistinctBudgetCategories(owner));
    }

    public boolean saveBudget(String owner, String category, double amount) {
        boolean result = databaseDriver.createOrUpdateBudget(owner, category, amount, 0.0);
        if (result) {
            loadBudgetsForClient(owner);
            loadBudgetCategoriesForClient(owner);
        }
        return result;
    }

    public boolean deleteBudget(String owner, String category) {
        boolean result = databaseDriver.deleteBudget(owner, category);
        if (result) {
            loadBudgetsForClient(owner);
            loadBudgetCategoriesForClient(owner);
        }
        return result;
    }

    public Optional<Budget> findBudget(String category) {
        return budgetLimitCache.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .findFirst();
    }

    public double getRemainingBudgetForCurrentMonth(String owner, String category) {
        Double limit = databaseDriver.getBudgetLimit(owner, category);
        if (limit == null) {
            return Double.POSITIVE_INFINITY;
        }

        double spentThisMonth = databaseDriver.getSpentAmountForCurrentMonth(owner, category);
        databaseDriver.updateBudgetSpent(owner, category, spentThisMonth);

        double remaining = limit - spentThisMonth;
        return remaining;
    }

    public double getBudgetSpentForCurrentMonth(String owner, String category) {
        return databaseDriver.getSpentAmountForCurrentMonth(owner, category);
    }

    // =====================================================
    // DASHBOARD REFRESH
    // =====================================================
    public void refreshClientData() {
        if (loggedInClient != null) {
            String payee = loggedInClient.getPayeeAddress();
            loggedInClient.walletAccountProperty().set(getWalletAccount(payee));
            loggedInClient.savingsAccountProperty().set(getSavingsAccount(payee));
            loggedInClient.setTransactionHistory(loadClientTransactions(payee));
            loadBudgetsForClient(payee);
            loadBudgetCategoriesForClient(payee);

            if (onClientDataRefreshed != null) onClientDataRefreshed.run();
        }
    }

    public void notifyClientDataRefreshed() {
        if (onClientDataRefreshed != null) {
            onClientDataRefreshed.run();
        }
    }
}
