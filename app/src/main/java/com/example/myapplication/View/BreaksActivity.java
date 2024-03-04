package com.example.myapplication.View;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Controller.BreaksAdapter;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BreaksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private RecyclerView breaksRecyclerView;
    private BreaksAdapter breaksAdapter;
    private List<String> breakDatesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breaks);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        breaksRecyclerView = findViewById(R.id.breaksRecyclerView);

        // Initialize RecyclerView
        breaksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        breakDatesList = new ArrayList<>();
        breaksAdapter = new BreaksAdapter(breakDatesList);
        breaksRecyclerView.setAdapter(breaksAdapter);

        // Initialize Spinners
        initializeMonthSpinner();
        initializeYearSpinner();

        // Fetch break dates
        fetchBreakDates();
    }

    private void initializeMonthSpinner() {
        // Populate monthSpinner with month names
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
    }

    private void initializeYearSpinner() {
        // Populate yearSpinner with years (e.g., from 2020 to current year)
        List<String> years = new ArrayList<>();
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        for (int i = 2020; i <= currentYear; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
    }

    private void fetchBreakDates() {
        // Get the selected month and year from spinners
        int selectedMonth = monthSpinner.getSelectedItemPosition();
        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());

        // Calculate the start and end timestamps for the selected month
        long startOfMonth = getStartOfMonth(selectedMonth, selectedYear);
        long endOfMonth = getEndOfMonth(selectedMonth, selectedYear);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference breaksRef = db.collection("breaks");

            // Query breaks collection for the current user within the selected month
            breaksRef.whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("start_time", startOfMonth)
                    .whereLessThanOrEqualTo("start_time", endOfMonth)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // Get start time and end time of the break
                                    Date startTime = document.getDate("start_time");
                                    Date endTime = document.getDate("end_time");

                                    // Format the dates as per your requirement
                                    String formattedStartTime = DateFormat.format("MMM dd, yyyy 'at' HH:mm:ss", startTime).toString();
                                    String formattedEndTime = DateFormat.format("MMM dd, yyyy 'at' HH:mm:ss", endTime).toString();

                                    // Add the formatted break date to the list
                                    breakDatesList.add(formattedStartTime + " - " + formattedEndTime);
                                }
                                // Update the RecyclerView with break dates
                                breaksAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("BreaksActivity", "Error getting breaks: ", task.getException());
                                Toast.makeText(BreaksActivity.this, "Error fetching breaks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private String getUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            // Handle the case where the user is not logged in
            return null;
        }
    }

    private long getStartOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfMonth(int selectedMonth, int selectedYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);

        // Set the day to the last day of the selected month
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);

        // Set the time to the last moment of the day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        // Return the timestamp of the last moment of the day
        return calendar.getTimeInMillis();
    }


}
