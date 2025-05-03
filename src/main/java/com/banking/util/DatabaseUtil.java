package com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.banking.config.DatabaseConfig;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    
    /**
     * Initialize the database schema
     * Creates all necessary tables if they don't exist
     * 
     * @return true if initialized successfully, false otherwise
     */
    public static boolean initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Get connection
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            conn = dbConfig.getConnection();
            
            // Create sequences
            stmt = conn.createStatement();
            
            // Create customer_id_seq
            stmt.execute(
                "CREATE SEQUENCE IF NOT EXISTS customer_id_seq " +
                "INCREMENT 1 " +
                "START 1000 " +
                "MINVALUE 1000 " +
                "MAXVALUE 9999999 " +
                "CACHE 1"
            );
            
            // Create account_id_seq
            stmt.execute(
                "CREATE SEQUENCE IF NOT EXISTS account_id_seq " +
                "INCREMENT 1 " +
                "START 10000 " +
                "MINVALUE 10000 " +
                "MAXVALUE 9999999 " +
                "CACHE 1"
            );
            
            // Create transaction_id_seq
            stmt.execute(
                "CREATE SEQUENCE IF NOT EXISTS transaction_id_seq " +
                "INCREMENT 1 " +
                "START 100000 " +
                "MINVALUE 100000 " +
                "MAXVALUE 9999999999 " +
                "CACHE 1"
            );
            
            // Create customers table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id INT PRIMARY KEY DEFAULT nextval('customer_id_seq'), " +
                "first_name VARCHAR(50) NOT NULL, " +
                "last_name VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "phone VARCHAR(20), " +
                "address VARCHAR(255), " +
                "date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))" +
                ")"
            );
            
            // Create accounts table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_id INT PRIMARY KEY DEFAULT nextval('account_id_seq'), " +
                "customer_id INT NOT NULL, " +
                "account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CURRENT')), " +
                "balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL, " +
                "interest_rate DECIMAL(5, 2) DEFAULT 0.00, " +
                "date_opened TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')), " +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create transactions table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id BIGINT PRIMARY KEY DEFAULT nextval('transaction_id_seq'), " +
                "account_id INT NOT NULL, " +
                "transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')), " +
                "amount DECIMAL(15, 2) NOT NULL, " +
                "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "description VARCHAR(255), " +
                "recipient_account_id INT, " +
                "FOREIGN KEY (account_id) REFERENCES accounts(account_id), " +
                "FOREIGN KEY (recipient_account_id) REFERENCES accounts(account_id)" +
                ")"
            );
            
            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_customer_email ON customers(email)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_account_customer ON accounts(customer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transaction_account ON transactions(account_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transaction_date ON transactions(transaction_date)");
            
            // Create views
            
            // Account summary view
            stmt.execute(
                "CREATE OR REPLACE VIEW account_summary AS " +
                "SELECT " +
                "    a.account_id, " +
                "    a.account_type, " +
                "    c.customer_id, " +
                "    c.first_name, " +
                "    c.last_name, " +
                "    a.balance, " +
                "    a.date_opened, " +
                "    a.status " +
                "FROM " +
                "    accounts a " +
                "JOIN " +
                "    customers c ON a.customer_id = c.customer_id"
            );
            
            // Transaction history view
            stmt.execute(
                "CREATE OR REPLACE VIEW transaction_history AS " +
                "SELECT " +
                "    t.transaction_id, " +
                "    t.account_id, " +
                "    a.account_type, " +
                "    c.customer_id, " +
                "    c.first_name, " +
                "    c.last_name, " +
                "    t.transaction_type, " +
                "    t.amount, " +
                "    t.transaction_date, " +
                "    t.description, " +
                "    t.recipient_account_id " +
                "FROM " +
                "    transactions t " +
                "JOIN " +
                "    accounts a ON t.account_id = a.account_id " +
                "JOIN " +
                "    customers c ON a.customer_id = c.customer_id"
            );
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Reset the database (for testing)
     * Drops all tables and sequences
     * 
     * @return true if reset successfully, false otherwise
     */
    public static boolean resetDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Get connection
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            conn = dbConfig.getConnection();
            
            // Drop tables and sequences
            stmt = conn.createStatement();
            
            // Drop views first
            stmt.execute("DROP VIEW IF EXISTS transaction_history CASCADE");
            stmt.execute("DROP VIEW IF EXISTS account_summary CASCADE");
            
            // Drop tables
            stmt.execute("DROP TABLE IF EXISTS transactions CASCADE");
            stmt.execute("DROP TABLE IF EXISTS accounts CASCADE");
            stmt.execute("DROP TABLE IF EXISTS customers CASCADE");
            
            // Drop sequences
            stmt.execute("DROP SEQUENCE IF EXISTS transaction_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS account_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS customer_id_seq CASCADE");
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error resetting database: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}