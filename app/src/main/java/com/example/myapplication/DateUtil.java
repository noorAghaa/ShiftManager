package com.example.myapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static final SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    public static Date parseDate(String dateString) {
        try {
            return originalFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Consider proper error handling or default value
        }
    }

    public static String formatDate(Date date) {
        return originalFormat.format(date);
    }

    public static String getMonthName(Date date) {
        return monthFormat.format(date);
    }
}
