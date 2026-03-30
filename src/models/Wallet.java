package models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a wallet (student balance & transaction history) in the BarkBites system
 * Maps to the 'wallets' Firestore collection
 */
public class Wallet {
    private String student_id;              // Primary identifier (links to users)
    private double balance;                 // Current balance in currency
    private double total_spent;             // Lifetime spending
    private List<Transaction> transactions; // Transaction history
    private Instant last_transaction;       // When last transaction occurred

    // Constructor
    public Wallet() {
        this.transactions = new ArrayList<>();
    }

    public Wallet(String student_id, double initial_balance) {
        this.student_id = student_id;
        this.balance = initial_balance;
        this.total_spent = 0;
        this.transactions = new ArrayList<>();
        this.last_transaction = Instant.now();
    }

    // Getters and Setters
    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTotal_spent() {
        return total_spent;
    }

    public void setTotal_spent(double total_spent) {
        this.total_spent = total_spent;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Instant getLast_transaction() {
        return last_transaction;
    }

    public void setLast_transaction(Instant last_transaction) {
        this.last_transaction = last_transaction;
    }

    /**
     * Add a transaction and update balance
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        this.last_transaction = transaction.getDate();
        
        if ("order".equals(transaction.getType())) {
            this.balance -= transaction.getAmount();
            this.total_spent += transaction.getAmount();
        } else if ("recharge".equals(transaction.getType())) {
            this.balance += transaction.getAmount();
        }
    }

    /**
     * Check if wallet has sufficient balance
     */
    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "student_id='" + student_id + '\'' +
                ", balance=" + balance +
                ", total_spent=" + total_spent +
                ", transaction_count=" + transactions.size() +
                '}';
    }

    /**
     * Nested class representing a transaction
     */
    public static class Transaction {
        private String type;        // "order" or "recharge"
        private double amount;      // Amount in currency
        private Instant date;       // When transaction occurred
        private String order_id;    // Reference to order (if type == "order")
        private String admin;       // Who recharged (if type == "recharge")

        // Constructor
        public Transaction() {
        }

        public Transaction(String type, double amount, String order_id) {
            this.type = type;
            this.amount = amount;
            this.order_id = order_id;
            this.date = Instant.now();
        }

        public Transaction(String type, double amount) {
            this.type = type;
            this.amount = amount;
            this.date = Instant.now();
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public Instant getDate() {
            return date;
        }

        public void setDate(Instant date) {
            this.date = date;
        }

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getAdmin() {
            return admin;
        }

        public void setAdmin(String admin) {
            this.admin = admin;
        }

        @Override
        public String toString() {
            return type + " - $" + amount + " on " + date;
        }
    }
}
