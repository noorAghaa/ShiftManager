package com.example.myapplication.View;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.SickDay;
import com.example.myapplication.R;
import com.example.myapplication.Controller.SickDaysAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SickDaysActivity extends AppCompatActivity {

    private CollectionReference userSickDaysRef;
    private RecyclerView recyclerView;
    private SickDaysAdapter adapter;
    private List<SickDay> sickDaysList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sick_days);

        // Get current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        // Initialize Firestore reference
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userSickDaysRef = db.collection("sick_days").document(userId).collection("entries");

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView_sick_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sickDaysList = new ArrayList<>();
        adapter = new SickDaysAdapter(sickDaysList);
        recyclerView.setAdapter(adapter);

        // Load sick days from Firestore
        loadSickDays();
    }

    // Method to show DatePickerDialog and save selected sick day to Firestore
    // Method to show DatePickerDialog and save selected sick day to Firestore
    public void pickSickDate(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    // Show a dialog to input the reason
                    showReasonDialog(selectedDate);
                }, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Method to show a dialog for entering the reason
    private void showReasonDialog(String selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Reason");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String reason = input.getText().toString();
            // Save selectedDate and reason to Firestore under the current user's sick_days node
            SickDay sickDay = new SickDay(selectedDate, reason);
            userSickDaysRef.add(sickDay)
                    .addOnSuccessListener(documentReference -> {
                        // Successfully added sick day
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add sick day
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Method to retrieve sick days from Firestore and update RecyclerView
    private void loadSickDays() {
        // Get current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        // Reference to the "entries" collection in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference entriesRef = db.collection("sick_days").document(userId).collection("entries");

        // Query to retrieve all sick days for the current user
        entriesRef.orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear the existing list of sick days
                    sickDaysList.clear();

                    // Iterate through the retrieved documents and add sick days to the list
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        String reason = document.getString("reason"); // Retrieve the reason from Firestore
                        sickDaysList.add(new SickDay(date, reason)); // Create a SickDay object with both date and reason
                    }

                    // Notify the adapter that the dataset has changed
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("SickDaysActivity", "Error retrieving sick days: " + e.getMessage());
                });
    }}

