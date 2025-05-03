package com.banking;

import com.banking.util.DatabaseUtil;

/**
 * Main entry point for the Banking System application
 */
public class BankingSystemMain {

    public static void main(String[] args) {
        System.out.println("Banking System Initializing...");

        // Initialize database
        boolean dbInitialized = DatabaseUtil.initializeDatabase();
        if (!dbInitialized) {
            System.err.println("Failed to initialize database. Exiting...");
            System.exit(1);
        }

        System.out.println("Database initialized successfully.");

        // Start the application
        InteractiveBankingApplication.main(args);
    }
}
