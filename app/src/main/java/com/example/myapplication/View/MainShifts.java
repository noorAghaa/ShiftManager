package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Controller.ShiftAdapter;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainShifts extends AppCompatActivity {
    private final List<Shift> allShifts = new ArrayList<>();
    private ShiftAdapter adapter;
    private Button selectDateButton;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_shifts);

        database = new Database();
        setupRecyclerView();
        setupSelectDateButton();
        // Retrieve the employee email passed from ViewEmployeesActivity
        String employeeId = getIntent().getStringExtra("EXTRA_EMPLOYEE_ID");
        if (employeeId != null) {
            fetchShiftsForManager(employeeId);
        } else {
            fetchShifts();
        }
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

    @SuppressLint("SetTextI18n")
    private void showMonthYearPickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_month_picker);
        dialog.setTitle("Select Month and Year");

        final NumberPicker monthPicker = dialog.findViewById(R.id.monthPicker);
        final NumberPicker yearPicker = dialog.findViewById(R.id.yearPicker);
        Button buttonSet = dialog.findViewById(R.id.okButton);

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
            filterShiftsByYearAndMonth(selectedYear, selectedMonth);
        });

        dialog.show();
    }

    private void fetchShifts() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        database.fetchShifts(userId, new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                allShifts.clear();
                allShifts.addAll(shifts);
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void fetchShiftsForManager(String employeeId) {
        database.fetchShiftsForManager(employeeId, new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                allShifts.clear();
                allShifts.addAll(shifts);
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
            cal.setTime(shift.getStart_date());
            int shiftYear = cal.get(Calendar.YEAR);
            int shiftMonth = cal.get(Calendar.MONTH) + 1; // Adjust for 1-based indexing
            if (shiftYear == year && shiftMonth == month) {
                filteredShifts.add(shift);
            }
        }
        adapter.updateData(filteredShifts);
    }
}