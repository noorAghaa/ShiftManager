package com.example.myapplication;

import java.util.Date;


public class Shift {
    private final String day;
    private final Date date; // Use java.util.Date
    private final String duration;

    public Shift(String day, String dateString, String duration) {
        this.day = day;
        this.duration = duration;
        this.date = DateUtil.parseDate(dateString); // Use DateUtil for parsing
    }

    // Getters
    public String getDay() {
        return day;
    }

    public String getDate() {
        return DateUtil.formatDate(date); // Use DateUtil for formatting
    }

    public String getDuration() {
        return duration;
    }
}
