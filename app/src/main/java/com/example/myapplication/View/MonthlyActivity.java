package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MonthlyActivity extends AppCompatActivity {
    private static final double EXTRA_HOURS_MULTIPLIER = 1.5; // 150% increase for extra hours
    private static final double REGULAR_WORKING_HOURS_PER_DAY = 8; // Assuming 8 hours per day is regular working hours
    private Database database;
    private Spinner monthSpinner;
    private TextView monthlySalaryTextView;
    private TextView hourlySalaryTextView;
    private Spinner yearSpinner;
    private TextView totalHoursWorkedTextView; // Declare total hours worked TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly);
        database = new Database();

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        monthlySalaryTextView = findViewById(R.id.monthlySalaryTextView);
        Button calculateSalaryBtn = findViewById(R.id.calculateSalaryBtn);
        totalHoursWorkedTextView = findViewById(R.id.totalHoursWorkedTextView); // Initialize total hours worked TextView
        hourlySalaryTextView = findViewById(R.id.hourlySalaryTextView); // Initialize hourly salary TextView

        // Set up the month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Set up the year spinner
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, generateYearList(currentYear - 5, currentYear + 5));
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Set click listener for calculate button
        calculateSalaryBtn.setOnClickListener(view -> {
            // Get the selected year and month from the spinners
            int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
            int selectedMonth = monthSpinner.getSelectedItemPosition() + 1; // Default to selected month

            // Call the calculateMonthlySalary method with selected year and month
            calculateMonthlySalary(selectedYear, selectedMonth);
        });
    }

    // Method to generate a list of years from startYear to endYear
    private List<String> generateYearList(int startYear, int endYear) {
        List<String> years = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    private void calculateMonthlySalary(int selectedYear, int selectedMonth) {
        // Get current user's ID
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Declare total hours worked, regular hours, and extra hours as final
        final double[] totalHoursWorked = {0}; // Initialize as an array to make it mutable
        final double[] regularHours = {0};
        final double[] extraHours = {0};

        // Query Fire store to retrieve shifts for the current user
        database.fetchShifts(userId, new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                // Iterate through each shift and calculate total hours worked for the selected month
                for (Shift shift : shifts) {
                    // Extract shift date
                    Calendar shiftCalendar = Calendar.getInstance();
                    shiftCalendar.setTime(shift.getStart_date());
                    int shiftYear = shiftCalendar.get(Calendar.YEAR);
                    int shiftMonth = shiftCalendar.get(Calendar.MONTH) + 1; // Adding 1 because Calendar.MONTH is zero-based

                    // Check if the shift is within the selected month and year
                    if (shiftYear == selectedYear && shiftMonth == selectedMonth) {
                        // Calculate duration in hours (assuming duration is in format "start - end")
                        String[] durationParts = shift.getDuration().split(" - ");
                        String startTime = durationParts[0];
                        String endTime = durationParts[1];
                        double hoursWorked = calculateHoursWorked(startTime, endTime);
                        totalHoursWorked[0] += hoursWorked;

                        // Check if the hours worked exceed regular working hours
                        if (hoursWorked <= REGULAR_WORKING_HOURS_PER_DAY) {
                            regularHours[0] += hoursWorked;
                        } else {
                            extraHours[0] += hoursWorked - REGULAR_WORKING_HOURS_PER_DAY;
                            regularHours[0] += REGULAR_WORKING_HOURS_PER_DAY;
                        }
                    }
                }

                // Fetch hourly salary from Fire store (assuming it's stored for each user)
                database.fetchSalary(userId, new Database.SalaryFetchCallback() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSalaryFetch(String hourlySalary) {
                        // Calculate salary for regular hours
                        double regularSalary = regularHours[0] * Double.parseDouble(hourlySalary);

                        // Calculate salary for extra hours with extra multiplier
                        double extraSalary = extraHours[0] * Double.parseDouble(hourlySalary) * EXTRA_HOURS_MULTIPLIER;

                        // Display the calculated monthly salary in the TextView
                        String message = String.format(Locale.getDefault(), "Monthly Salary for %s %d:\n" +
                                        "Regular Hours: %.2f\nRegular Salary: $%.2f\n" +
                                        "Extra Hours: %.2f\nExtra hours Salary: $%.2f\n" +
                                        "total salary:%.2f\n",
                                new DateFormatSymbols().getMonths()[selectedMonth - 1], selectedYear,
                                regularHours[0], regularSalary, extraHours[0], extraSalary, (extraSalary + regularSalary));
                        monthlySalaryTextView.setText(message);

                        // Set total hours worked
                        String totalHoursWorkedMessage = String.format(Locale.getDefault(), "Total Hours Worked: %.2f", totalHoursWorked[0]);
                        totalHoursWorkedTextView.setText(totalHoursWorkedMessage);

                        // Set hourly salary
                        hourlySalaryTextView.setText("hourly salary: $" + hourlySalary);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error fetching salary
                        Toast.makeText(MonthlyActivity.this, "Error fetching salary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle error fetching shifts
                Toast.makeText(MonthlyActivity.this, "Error fetching shifts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateHoursWorked(String startTime, String endTime) {
        // Split start time and end time strings to extract hours and minutes
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        // Parse hours and minutes
        int startHours = Integer.parseInt(startParts[0]);
        int startMinutes = Integer.parseInt(startParts[1]);
        int endHours = Integer.parseInt(endParts[0]);
        int endMinutes = Integer.parseInt(endParts[1]);

        // Convert start and end times to total minutes
        int totalStartMinutes = startHours * 60 + startMinutes;
        int totalEndMinutes = endHours * 60 + endMinutes;

        // Check if end time is before start time (indicating overnight work)
        if (totalEndMinutes < totalStartMinutes) {
            // Add 24 hours to end time to represent the next day
            totalEndMinutes += 24 * 60;
        }

        // Calculate difference in minutes
        int totalMinutes = totalEndMinutes - totalStartMinutes;

        return totalMinutes / 60.0;
    }
}