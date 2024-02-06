package com.example.myapplication;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Shift {
    private String day;
    private Date date; // Keeping it as Date
    private String duration;
    private String userId; // Add user ID field

    // Adjusted constructor to include userId
    public Shift(String day, Date date, String duration, String userId) {
        this.day = day;
        this.date = date;
        this.duration = duration;
        this.userId = userId; // Set user ID
    }

    // Getters (and setters if necessary)
    public String getDay() { return day; }
    public Date getDate() { return date; }
    public String getDuration() { return duration; }
    public String getUserId() { return userId; } // Getter for userId
}
