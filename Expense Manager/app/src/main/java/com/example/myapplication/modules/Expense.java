// Expense.java - Model class for expense data
package com.example.myapplication.modules;

public class Expense {
    private String expenseId;
    private String userId;
    private double amount;
    private String category;
    private String date;
    private String note;
    private long timestamp;

    public Expense() {
        // Default constructor required for Firebase
    }

    public Expense(String expenseId, String userId, double amount, String category, String date, String note, long timestamp) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}