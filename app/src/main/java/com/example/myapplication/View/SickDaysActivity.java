package com.example.myapplication.View;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Controller.SickDaysAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SickDaysActivity extends AppCompatActivity {

    private DatabaseReference userSickDaysRef;
    private RecyclerView recyclerView;
    private SickDaysAdapter adapter;
    private List<String> sickDaysList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sick_days);

        // Get current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        // Initialize Firebase Database reference for the current user's sick days
        userSickDaysRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("sickDays");

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView_sick_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sickDaysList = new ArrayList<>();
        adapter = new SickDaysAdapter(sickDaysList);
        recyclerView.setAdapter(adapter);

        // Load sick days from Firebase Database
        loadSickDays();
    }

    // Method to show DatePickerDialog and save selected sick day to Firebase Database
    public void pickSickDate(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    // Save selectedDate to Firebase Database under the current user's sickDays node
                    String key = userSickDaysRef.push().getKey();
                    userSickDaysRef.child(key).setValue(selectedDate);
                }, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Method to retrieve sick days from Firebase Database and update RecyclerView
    private void loadSickDays() {
        userSickDaysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sickDaysList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String sickDay = snapshot.getValue(String.class);
                    sickDaysList.add(sickDay);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
