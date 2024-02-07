package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainShifts extends AppCompatActivity {
    TextView etMonthYear;
    private ShiftAdapter adapter;
    private final List<Shift> allShifts = new ArrayList<>();
    private Button selectDateButton;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_shifts);

        database = new Database();
        setupRecyclerView();
        setupSelectDateButton();
        fetchShifts();
    }

    private void setupRecyclerView() {
        RecyclerView shiftsRecyclerView = findViewById(R.id.shiftsRecyclerView);
        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShiftAdapter(new ArrayList<>());
        shiftsRecyclerView.setAdapter(adapter);
    }

    private void setupSelectDateButton() {
        selectDateButton = findViewById(R.id.selectDateButton);
        selectDateButton.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void showMonthYearPickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_month_picker);
        dialog.setTitle("Select Month and Year");

        final NumberPicker monthPicker = dialog.findViewById(R.id.monthPicker);
        final NumberPicker yearPicker = dialog.findViewById(R.id.yearPicker);
        Button buttonSet = dialog.findViewById(R.id.okButton);
        Button selectDateBtn = dialog.findViewById(R.id.selectDateButton);

        // Set month values
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(new DateFormatSymbols().getMonths());
        monthPicker.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);

        // Set year values
        int year = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(year - 100);
        yearPicker.setMaxValue(year + 100);
        yearPicker.setValue(year);

        // Set button click listener
        buttonSet.setOnClickListener(v -> {
            dialog.dismiss();
            int selectedYear = yearPicker.getValue();
            int selectedMonth = monthPicker.getValue();

            selectDateButton.setText(new DateFormatSymbols().getMonths()[selectedMonth - 1] + " " + selectedYear);
            filterShiftsByYearAndMonth(selectedYear,selectedMonth);
        });

        dialog.show();
    }

    private void fetchShifts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.fetchShifts(userId, new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                allShifts.clear();
                allShifts.addAll(shifts);
                // Optionally call filterShiftsByYearAndMonth here with a default or current year and month
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void filterShiftsByYearAndMonth(int year, int month) {
        List<Shift> filteredShifts = new ArrayList<>();
        for (Shift shift : allShifts) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(shift.getDate());
            int shiftYear = cal.get(Calendar.YEAR);
            int shiftMonth = cal.get(Calendar.MONTH) + 1; // Adjust for 1-based indexing
            if (shiftYear == year && shiftMonth == month) {
                filteredShifts.add(shift);
            }
        }
        adapter.updateData(filteredShifts);
    }
}
