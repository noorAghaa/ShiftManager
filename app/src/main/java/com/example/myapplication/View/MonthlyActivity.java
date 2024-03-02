package com.example.myapplication.View;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthlyActivity extends AppCompatActivity {

    private Database database;
    private Spinner monthSpinner;

    private TextView monthlySalaryTextView;
    private Spinner yearSpinner;
    private Button calculateSalaryBtn;
    private FirebaseFirestore firestore;
    private TextView totalHoursWorkedTextView; // Declare total hours worked TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly);
        database = new Database();

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        monthlySalaryTextView = findViewById(R.id.monthlySalaryTextView);
        calculateSalaryBtn = findViewById(R.id.calculateSalaryBtn);
        totalHoursWorkedTextView = findViewById(R.id.totalHoursWorkedTextView); // Initialize total hours worked TextView

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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Declare totalHoursWorked as final
        final double[] totalHoursWorked = {0}; // Initialize as an array to make it mutable

        // Query Firestore to retrieve shifts for the current user
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
                    }
                }

                // Fetch hourly salary from Firestore (assuming it's stored for each user)
                database.fetchSalary(userId, new Database.SalaryFetchCallback() {
                    @Override
                    public void onSalaryFetch(String hourlySalary) {
                        // Calculate monthly salary based on total hours worked and hourly salary
                        double monthlySalary = totalHoursWorked[0] * Double.parseDouble(hourlySalary);

                        // Display the calculated monthly salary in the TextView
                        String message = String.format(Locale.getDefault(), "Monthly Salary for %s %d: $%.2f",
                                new DateFormatSymbols().getMonths()[selectedMonth - 1], selectedYear, monthlySalary);
                        monthlySalaryTextView.setText(message);

                        // Set total hours worked
                        String totalHoursWorkedMessage = String.format(Locale.getDefault(), "Total Hours Worked: %.2f", totalHoursWorked[0]);
                        totalHoursWorkedTextView.setText(totalHoursWorkedMessage);
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

        // Calculate total minutes for start and end times
        int totalStartMinutes = startHours * 60 + startMinutes;
        int totalEndMinutes = endHours * 60 + endMinutes;

        // Calculate difference in minutes
        int totalMinutes = totalEndMinutes - totalStartMinutes;

        // Convert minutes to hours (1 hour = 60 minutes)
        double hoursWorked = totalMinutes / 60.0;

        return hoursWorked;
    }
}
