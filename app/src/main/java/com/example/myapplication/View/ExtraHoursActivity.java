package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Controller.ExtraHoursAdapter;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ExtraHoursActivity extends AppCompatActivity {
    private RecyclerView extraHoursRecyclerView;
    private ExtraHoursAdapter extraHoursAdapter;
    private List<Shift> extraHoursShifts;
    private Database database; // Declare Database instance
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private int selectedYear;
    private int selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_hours);

        // Initialize Database instance
        database = new Database();

        // Initialize RecyclerView
        extraHoursRecyclerView = findViewById(R.id.extraHoursRecyclerView);
        extraHoursRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list to hold shifts with extra hours
        extraHoursShifts = new ArrayList<>();

        // Create adapter with empty list initially
        extraHoursAdapter = new ExtraHoursAdapter(this, extraHoursShifts);
        extraHoursRecyclerView.setAdapter(extraHoursAdapter);

        // Initialize Spinners for month and year selection
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);

        // Set up month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Set up year spinner (assuming you have a method to generate years)
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, generateYearList());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Set listener for month and year selection
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedMonth = position + 1; // Month index starts from 0
                fetchShiftsWithExtraHours();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
                fetchShiftsWithExtraHours();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        // Call method to fetch shifts with extra hours
        fetchShiftsWithExtraHours();
    }

    private void fetchShiftsWithExtraHours() {
        // Clear the existing list of shifts before fetching new shifts
        extraHoursShifts.clear();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Call the method to fetch all shifts for the current user
        database.fetchShifts(userId, new Database.ShiftDataCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                // Iterate through all shifts and filter out those with extra hours
                for (Shift shift : shifts) {
                    Calendar shiftCalendar = Calendar.getInstance();
                    shiftCalendar.setTime(shift.getStart_date());
                    int shiftYear = shiftCalendar.get(Calendar.YEAR);
                    int shiftMonth = shiftCalendar.get(Calendar.MONTH) + 1; // Adding 1 because Calendar.MONTH is zero-based

                    // Check if the shift is within the selected month and year
                    if (shiftYear == selectedYear && shiftMonth == selectedMonth) {
                        if (shift.calculateDurationInHours() > 8) {
                            extraHoursShifts.add(shift);
                        }
                    }
                }
                // Notify the adapter that the data set has changed
                extraHoursAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Handle error fetching shifts
                Log.e("ExtraHoursActivity", "Error fetching shifts: " + e.getMessage());
            }
        });
    }

    // Method to generate a list of years (customize as per your requirements)
    private List<String> generateYearList() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        return years;
    }
}
