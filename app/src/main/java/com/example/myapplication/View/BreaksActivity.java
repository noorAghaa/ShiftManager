package com.example.myapplication.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

public class BreaksActivity extends AppCompatActivity {

    private ListView breaksListView;
    private ArrayAdapter<String> breaksAdapter;
    private List<String> breakDatesList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breaks);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        breaksListView = findViewById(R.id.breaksListView);

        // Initialize list to store break dates
        breakDatesList = new ArrayList<>();

        // Initialize adapter for ListView
        breaksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, breakDatesList);
        breaksListView.setAdapter(breaksAdapter);

        // Fetch break dates from Firestore
        fetchBreakDates();
    }

    private void fetchBreakDates() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference breaksRef = db.collection("Breaks");

            // Query breaks collection for the current user
            breaksRef.whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // Get break date and add it to the list
                                    String breakDate = document.getString("breakDate");
                                    breakDatesList.add(breakDate);
                                }
                                // Update the ListView with break dates
                                breaksAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("BreaksActivity", "Error getting breaks: ", task.getException());
                                Toast.makeText(BreaksActivity.this, "Error fetching breaks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
