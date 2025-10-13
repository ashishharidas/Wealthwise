package com.smartfinance.Models;

import java.sql.*;
import java.time.LocalDate;

/**
 * DatabaseDriver - Handles all DB operations for Smart Finance app.
 * Includes auto table creation, client/account management, and transaction/investment support.
 */
public class DatabaseDriver {
    private Connection connection;

    public DatabaseDriver() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:mazebank.db");
            System.out.println("[DatabaseDriver] ✅ Connected to database successfully.");
            ensureTablesExist();
        } catch (SQLException e) {
            System.err.println("[DatabaseDriver] ❌ Connection failed!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ====================================================
    // TABLE CREATION
    // ====================================================
    private void ensureTablesExist() {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Clients (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    FirstName TEXT,
                    LastName TEXT,
                    PayeeAddress TEXT UNIQUE,
                    Password TEXT,
                    Date TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Admins (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Username TEXT UNIQUE,
                    Password TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS WalletAccounts (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Owner TEXT,
                    AccountNumber TEXT,
                    Balance REAL,
                    DepositLimit REAL,
                    Date TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS SavingsAccounts (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Owner TEXT,
                    AccountNumber TEXT,
                    Balance REAL,
                    TransactionLimit REAL,
                    date_created TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Transactions (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Sender TEXT,
                    Receiver TEXT,
                    Amount REAL,
                    Category TEXT,
                    Message TEXT,
                    Date TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Investments (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Owner TEXT,
                    InvestmentType TEXT,
                    AmountInvested REAL,
                    CurrentValue REAL,
                    DateInvested TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ClientReports (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    ClientName TEXT,
                    PayeeAddress TEXT,
                    IssueType TEXT,
                    Description TEXT,
                    DateReported TEXT,
                    Status TEXT DEFAULT 'Open'
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS GeneratedReports (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    ClientName TEXT,
                    PayeeAddress TEXT,
                    ReportType TEXT,
                    ReportContent TEXT,
                    DateGenerated TEXT
                )
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // ====================================================
    // AUTHENTICATION
    // ====================================================
    public ResultSet getClientsData(String payeeAddress, String password) {
        return executeQuery(
                "SELECT * FROM Clients WHERE PayeeAddress = ? AND Password = ?",
                payeeAddress, password
        );
    }

    public ResultSet getAdminData(String username, String password) {
        return executeQuery(
                "SELECT * FROM Admins WHERE Username = ? AND Password = ?",
                username, password
        );
    }

    // ====================================================
    // ACCOUNT FETCH
    // ====================================================
    public ResultSet getWalletAccount(String owner) {
        return executeQuery("SELECT * FROM WalletAccounts WHERE Owner = ?", owner);
    }

    public ResultSet getSavingsAccount(String owner) {
        return executeQuery("SELECT * FROM SavingsAccounts WHERE Owner = ?", owner);
    }

    // ====================================================
    // ACCOUNT CREATION
    // ====================================================
    public String getNextAccountNumber(String table) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(ID) FROM " + table)) {
            if (rs.next()) {
                int next = rs.getInt(1) + 1;
                // Format: 3201 XXXX (3201 is fixed, XXXX is unique 4-digit number)
                return String.format("3201 %04d", next);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "3201 0001";
    }

    public void createWalletAccount(String owner, double balance) {
        String accNumber = getNextAccountNumber("WalletAccounts");
        LocalDate date = LocalDate.now();
        executeUpdate(
                "INSERT INTO WalletAccounts (Owner, AccountNumber, Balance, DepositLimit, Date) VALUES (?, ?, ?, ?, ?)",
                owner, accNumber, balance, 10000, date.toString()
        );
    }

    public void createSavingsAccount(String owner, double balance) {
        String accNumber = getNextAccountNumber("SavingsAccounts");
        LocalDate date = LocalDate.now();
        executeUpdate(
                "INSERT INTO SavingsAccounts (Owner, AccountNumber, Balance, TransactionLimit, date_created) VALUES (?, ?, ?, ?, ?)",
                owner, accNumber, balance, 100000, date.toString()
        );
    }

    // ====================================================
    // CLIENT MANAGEMENT
    // ====================================================
    /**
     * Creates a new client with associated wallet and savings accounts.
     * This operation is atomic - either all records are created or none.
     * 
     * @param firstName Client's first name
     * @param lastName Client's last name
     * @param payeeAddress Unique payee address (used as client identifier)
     * @param password Client's password
     * @param walletBalance Initial wallet account balance
     * @param savingsBalance Initial savings account balance
     * @return true if client was created successfully, false otherwise
     */
    public boolean createClient(String firstName, String lastName, String payeeAddress, String password,
                                double walletBalance, double savingsBalance) {
        // Input validation
        if (firstName == null || firstName.trim().isEmpty()) {
            System.err.println("[createClient] ❌ First name cannot be empty");
            return false;
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            System.err.println("[createClient] ❌ Last name cannot be empty");
            return false;
        }
        if (payeeAddress == null || payeeAddress.trim().isEmpty()) {
            System.err.println("[createClient] ❌ Payee address cannot be empty");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            System.err.println("[createClient] ❌ Password cannot be empty");
            return false;
        }
        if (walletBalance < 0 || savingsBalance < 0) {
            System.err.println("[createClient] ❌ Initial balances cannot be negative");
            return false;
        }

        // Check if client already exists
        try (ResultSet rs = executeQuery("SELECT PayeeAddress FROM Clients WHERE PayeeAddress = ?", payeeAddress)) {
            if (rs != null && rs.next()) {
                System.err.println("[createClient] ❌ Client with payee address '" + payeeAddress + "' already exists");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[createClient] ❌ Error checking for existing client");
            e.printStackTrace();
            return false;
        }

        LocalDate date = LocalDate.now();
        
        try {
            connection.setAutoCommit(false);
            System.out.println("[createClient] 🔄 Creating client: " + firstName + " " + lastName + " (" + payeeAddress + ")");

            // Step 1: Create client record
            int clientRows = executeUpdate(
                    "INSERT INTO Clients (FirstName, LastName, PayeeAddress, Password, Date) VALUES (?, ?, ?, ?, ?)",
                    firstName, lastName, payeeAddress, password, date.toString()
            );
            
            if (clientRows == 0) {
                throw new SQLException("Failed to insert client record");
            }
            System.out.println("[createClient] ✅ Client record created");

            // Step 2: Create wallet account
            createWalletAccount(payeeAddress, walletBalance);
            System.out.println("[createClient] ✅ Wallet account created (Balance: $" + walletBalance + ")");

            // Step 3: Create savings account
            createSavingsAccount(payeeAddress, savingsBalance);
            System.out.println("[createClient] ✅ Savings account created (Balance: $" + savingsBalance + ")");

            // Commit transaction
            connection.commit();
            System.out.println("[createClient] ✅ Client creation completed successfully");
            return true;
            
        } catch (SQLException e) {
            System.err.println("[createClient] ❌ Error during client creation - rolling back transaction");
            try { 
                connection.rollback();
                System.err.println("[createClient] ↩️ Transaction rolled back successfully");
            } catch (SQLException rollbackEx) {
                System.err.println("[createClient] ❌ Failed to rollback transaction");
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try { 
                connection.setAutoCommit(true); 
            } catch (SQLException e) {
                System.err.println("[createClient] ⚠️ Failed to reset auto-commit");
                e.printStackTrace();
            }
        }
    }

    public ResultSet getAllClientsData() {
        return executeQuery("SELECT * FROM Clients");
    }

    public ResultSet searchClients(String searchText) {
        String query = "%" + searchText + "%";
        return executeQuery(
                "SELECT * FROM Clients WHERE PayeeAddress LIKE ? OR FirstName LIKE ? OR LastName LIKE ?",
                query, query, query
        );
    }

    public boolean deleteClient(String payeeAddress) {
        try {
            connection.setAutoCommit(false);

            executeUpdate("DELETE FROM WalletAccounts WHERE Owner = ?", payeeAddress);
            executeUpdate("DELETE FROM SavingsAccounts WHERE Owner = ?", payeeAddress);
            int deleted = executeUpdate("DELETE FROM Clients WHERE PayeeAddress = ?", payeeAddress);

            connection.commit();
            return deleted > 0;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ====================================================
    // TRANSACTIONS
    // ====================================================
    public void createTransaction(String sender, String receiver, double amount, String category, String message) {
        executeUpdate(
                "INSERT INTO Transactions (Sender, Receiver, Amount, Category, Message, Date) VALUES (?, ?, ?, ?, ?, datetime('now'))",
                sender, receiver, amount, category, message
        );
    }

    public ResultSet getTransactionsByPayee(String payeeAddress) {
        return executeQuery(
                "SELECT * FROM Transactions WHERE Sender = ? OR Receiver = ? ORDER BY Date DESC",
                payeeAddress, payeeAddress
        );
    }

    public ResultSet getExpendituresByCategory(String payeeAddress) {
        return executeQuery(
                "SELECT Category, SUM(Amount) as Total FROM Transactions WHERE Sender = ? GROUP BY Category",
                payeeAddress
        );
    }

    // ====================================================
    // INVESTMENTS
    // ====================================================
    public ResultSet getInvestments(String owner) {
        return executeQuery("SELECT * FROM Investments WHERE Owner = ? ORDER BY DateInvested DESC", owner);
    }

    public void createInvestment(String owner, String investmentType, double amountInvested, double currentValue) {
        executeUpdate(
                "INSERT INTO Investments (Owner, InvestmentType, AmountInvested, CurrentValue, DateInvested) VALUES (?, ?, ?, ?, datetime('now'))",
                owner, investmentType, amountInvested, currentValue
        );
    }

    public boolean updateInvestmentValue(int id, double newValue) {
        return executeUpdate("UPDATE Investments SET CurrentValue = ? WHERE ID = ?", newValue, id) > 0;
    }

    // ====================================================
    // BALANCE OPERATIONS
    // ====================================================
    public boolean depositToSavings(String owner, double amount) {
        return executeUpdate("UPDATE SavingsAccounts SET Balance = Balance + ? WHERE Owner = ?", amount, owner) > 0;
    }

    public boolean depositToWallet(String owner, double amount) {
        return executeUpdate("UPDATE WalletAccounts SET Balance = Balance + ? WHERE Owner = ?", amount, owner) > 0;
    }

    public void updateSavingsBalance(String owner, double newBalance) {
        executeUpdate("UPDATE SavingsAccounts SET Balance = ? WHERE Owner = ?", newBalance, owner);
    }

    public void updateWalletBalance(String owner, double newBalance) {
        executeUpdate("UPDATE WalletAccounts SET Balance = ? WHERE Owner = ?", newBalance, owner);
    }

    public boolean updateBalance(String table, String owner, double newBalance) {
        String sql = "UPDATE " + table + " SET Balance = ? WHERE Owner = ?";
        return executeUpdate(sql, newBalance, owner) > 0;
    }

    // ====================================================
    // CLIENT REPORTS
    // ====================================================
    public void createClientReport(String clientName, String payeeAddress, String issueType, String description) {
        executeUpdate(
                "INSERT INTO ClientReports (ClientName, PayeeAddress, IssueType, Description, DateReported, Status) VALUES (?, ?, ?, ?, datetime('now'), 'Open')",
                clientName, payeeAddress, issueType, description
        );
    }

    public ResultSet getAllClientReports() {
        return executeQuery("SELECT * FROM ClientReports ORDER BY DateReported DESC");
    }

    public ResultSet getClientReportsByPayee(String payeeAddress) {
        return executeQuery(
                "SELECT * FROM ClientReports WHERE PayeeAddress = ? ORDER BY DateReported DESC",
                payeeAddress
        );
    }

    public boolean updateReportStatus(int reportId, String status) {
        return executeUpdate("UPDATE ClientReports SET Status = ? WHERE ID = ?", status, reportId) > 0;
    }

    public ResultSet getAllClients() {
        return executeQuery("SELECT * FROM Clients ORDER BY Date DESC");
    }

    // ====================================================
    // GENERATED REPORTS
    // ====================================================
    public void createGeneratedReport(String clientName, String payeeAddress, String reportType, String reportContent) {
        executeUpdate(
                "INSERT INTO GeneratedReports (ClientName, PayeeAddress, ReportType, ReportContent, DateGenerated) VALUES (?, ?, ?, ?, datetime('now'))",
                clientName, payeeAddress, reportType, reportContent
        );
    }

    public ResultSet getAllGeneratedReports() {
        return executeQuery("SELECT * FROM GeneratedReports ORDER BY DateGenerated DESC");
    }

    public ResultSet getGeneratedReportsByPayee(String payeeAddress) {
        return executeQuery(
                "SELECT * FROM GeneratedReports WHERE PayeeAddress = ? ORDER BY DateGenerated DESC",
                payeeAddress
        );
    }

    public ResultSet getGeneratedReportsByType(String reportType) {
        return executeQuery(
                "SELECT * FROM GeneratedReports WHERE ReportType = ? ORDER BY DateGenerated DESC",
                reportType
        );
    }

    // ====================================================
    // HELPER METHODS
    // ====================================================
    private ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                stmt.setObject(i + 1, params[i]);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int executeUpdate(String sql, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                stmt.setObject(i + 1, params[i]);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


}
