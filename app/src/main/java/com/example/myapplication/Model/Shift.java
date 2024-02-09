package com.example.myapplication.Model;

import com.example.myapplication.Controller.DateUtil;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Shift {
    private String day;
    private Date start_date; // Keeping it as Date
    private Date end_date;
    private String duration;
    private String userId; // Add user ID field

    // No-argument constructor required for Firebase
    public Shift() {
    }

    // Adjusted constructor to include userId
    public Shift(String day, Date start_date, String duration, String userId, Date end_date) {
        this.day = day;
        this.start_date = start_date;
        this.end_date = end_date;
        this.duration = duration;
        this.userId = userId; // Set user ID
    }

    public String getFormattedDay() {
        try {
            String[] parts = DateUtil.formatDate(start_date).split(" ");
            return parts[2];// Extract day
        } catch (Exception e) {
            return "Date format error";
        }
    }

    public static Date parseTimeString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            System.err.println("Time string is null or empty");
            return null; // Return null or throw a more specific exception if appropriate
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            return format.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Or handle error appropriately
        }
    }


    public String calculateDuration() {
        String[] myShiftDuration = duration.split(" - ");
        String startTime = myShiftDuration[0];
        String endTime = myShiftDuration[1];

        Date start = parseTimeString(startTime);
        Date end = parseTimeString(endTime);
        if (start == null || end == null) {
            return "Duration not available"; // Handle null start or end times gracefully
        }
        // Continue with duration calculation as before
        long durationMillis = end.getTime() - start.getTime();
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
    }


    // Getters (and setters if necessary)
    public String getDay() { return day; }
    public Date getStart_date() { return start_date; }
    public String getDuration() { return duration; }
    public String getUserId() { return userId; } // Getter for userId

    public Date getEnd_date() { return  end_date; }
}
