package com.example.myapplication.Model;

public class SickDay {
    private String date;
    private String reason;

    // Constructor to create a SickDay object with just the date
    public SickDay(String date) {
        this.date = date;
        this.reason = ""; // Default reason is an empty string
    }

    // Constructor to create a SickDay object with both date and reason
    public SickDay(String date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
