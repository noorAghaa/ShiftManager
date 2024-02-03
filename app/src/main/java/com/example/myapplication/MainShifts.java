package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainShifts extends AppCompatActivity {

    private ShiftAdapter adapter;
    private final List<Shift> allShifts = new ArrayList<>();
    private final List<Shift> displayedShifts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_month_spinner);

        setupRecyclerView();
        setupSpinner();
        populateShifts();
    }

    private void setupRecyclerView() {
        RecyclerView shiftsRecyclerView = findViewById(R.id.shiftsRecyclerView);
        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShiftAdapter(displayedShifts);
        shiftsRecyclerView.setAdapter(adapter);
    }

    private void setupSpinner() {
        Spinner monthSpinner = findViewById(R.id.monthSpinner);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterShiftsByMonth(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void populateShifts() {
        // Initialize your shifts here
        allShifts.add(new Shift("Monday", "03-01-2023", "8 hours"));
        allShifts.add(new Shift("Tuesday", "10-01-2023", "9 hours"));
        allShifts.add(new Shift("Tuesday", "10-02-2023", "12 hours"));
        allShifts.add(new Shift("Tuesday", "10-03-2023", "15 hours"));
        allShifts.add(new Shift("Tuesday", "10-04-2023", "20 hours"));
        allShifts.add(new Shift("Tuesday", "10-05-2023", "24 hours"));
        allShifts.add(new Shift("Tuesday", "10-06-2023", "22 hours"));
        allShifts.add(new Shift("Tuesday", "10-07-2023", "6 hours"));
        allShifts.add(new Shift("Tuesday", "10-08-2023", "3 hours"));
        allShifts.add(new Shift("Tuesday", "10-09-2023", "7 hours"));
        allShifts.add(new Shift("Tuesday", "10-10-2023", "1 hours"));
        allShifts.add(new Shift("Tuesday", "10-11-2023", "10 hours"));
        allShifts.add(new Shift("Tuesday", "10-12-2023", "46 hours"));
        // Add more shifts as needed for testing

        // Make displayedShifts a copy of allShifts
        displayedShifts.clear(); // Clear it first to ensure it's empty before adding all items
        displayedShifts.addAll(allShifts);

        // If your adapter might have been initialized before this point,
        // ensure the displayed list in the adapter is updated.
        // This might not be necessary if populateShifts is only called in onCreate before setting the adapter.
        adapter.updateData(displayedShifts);
    }

    private void filterShiftsByMonth(String selectedMonth) {
        List<Shift> filteredShifts = new ArrayList<>();
        for (Shift shift : allShifts) {
            String dateString = shift.getDate(); // This is a string in "dd-MM-yyyy" format
            Date date = DateUtil.parseDate(dateString); // Convert the string to a Date object for comparison
            if (date != null) { // Check if the date conversion was successful
                String monthName = DateUtil.getMonthName(date);
                if (monthName.equalsIgnoreCase(selectedMonth)) {
                    filteredShifts.add(shift);
                }
            }
        }
        adapter.updateData(filteredShifts);
    }
}