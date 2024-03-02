package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Locale;
import com.example.myapplication.Model.Database;


public class SalaryActivity extends AppCompatActivity {
    private Database database = new Database();

    private FirebaseFirestore db;
    private CollectionReference shiftsRef;
    private TextView salaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_work_menu);

        // Initialize Firestore and reference to shifts collection
        db = FirebaseFirestore.getInstance();
        shiftsRef = db.collection("shifts");

        salaryTextView = findViewById(R.id.salaryTextView);

        // Read shifts from Firestore and calculate salary
        calculateSalary();
    }
    private void calculateSalary() {
        // Get current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Declare totalHoursWorked as final
        final double[] totalHoursWorked = {0}; // Initialize as an array to make it mutable

        // Query Firestore to retrieve shifts for the current user
        database.fetchShifts(userId, new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {

                // Get the current month and year
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Adding 1 because Calendar.MONTH is zero-based

                // Iterate through each shift and calculate total hours worked
                for (Shift shift : shifts) {
                    // Extract shift date
                    Calendar shiftCalendar = Calendar.getInstance();
                    shiftCalendar.setTime(shift.getStart_date());
                    int shiftYear = shiftCalendar.get(Calendar.YEAR);
                    int shiftMonth = shiftCalendar.get(Calendar.MONTH) + 1; // Adding 1 because Calendar.MONTH is zero-based

                    // Check if the shift is within the current month
                    if (shiftYear == currentYear && shiftMonth == currentMonth) {
                        // Calculate duration in hours (assuming duration is in format "start - end")
                        String[] durationParts = shift.getDuration().split(" - ");
                        String startTime = durationParts[0];
                        String endTime = durationParts[1];
                        double hoursWorked = calculateHoursWorked(startTime, endTime);
                        totalHoursWorked[0] += hoursWorked;
                    }
                }

                // Fetch hourly salary from Firestore
                database.fetchSalary(userId, new Database.SalaryFetchCallback() {
                    @Override
                    public void onSalaryFetch(String hourlySalary) {
                        // Calculate salary based on total hours worked and hourly salary
                        double salary = totalHoursWorked[0] * Double.parseDouble(hourlySalary);

                        // Pass the salary back to the calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("salary", salary);
                        setResult(RESULT_OK, resultIntent);
                        finish();

                        // Display salary in TextView
                        salaryTextView.setText(String.format(Locale.getDefault(), "Salary: $%.2f", salary));
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error fetching salary
                        Toast.makeText(SalaryActivity.this, "Error fetching salary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle error fetching shifts
                Toast.makeText(SalaryActivity.this, "Error fetching shifts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    // Helper method to calculate hours worked between start and end time
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
