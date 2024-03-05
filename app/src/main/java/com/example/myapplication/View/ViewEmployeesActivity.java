package com.example.myapplication.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewEmployeesActivity extends AppCompatActivity {
    private EmployeesAdapter adapter;
    private ArrayList<User> employeesList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvNoEmployees; // TextView for showing the no employees message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employees);

        RecyclerView employeesRecyclerView = findViewById(R.id.employeesRecyclerView);
        tvNoEmployees = findViewById(R.id.tvNoEmployees); // Initialize the TextView
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeesAdapter(this, employeesList, userId -> {
            Intent intent = new Intent(ViewEmployeesActivity.this, MainShifts.class);
            intent.putExtra("EXTRA_EMPLOYEE_ID", userId);
            startActivity(intent);
        });
        employeesRecyclerView.setAdapter(adapter);

        String managerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        fetchEmployeeIdsForManager(managerId);
    }

    private void fetchEmployeeIdsForManager(String managerId) {
        db.collection("Managers").document(managerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> employeeIds = (List<String>) documentSnapshot.get("employeeIds");
                if (employeeIds != null && !employeeIds.isEmpty()) {
                    fetchEmployeeDetails(employeeIds);
                } else {
                    Log.e("ViewEmployeesActivity", "No employees found for manager.");
                    tvNoEmployees.setVisibility(View.VISIBLE); // Show the message
                }
            } else {
                Log.e("ViewEmployeesActivity", "Manager document does not exist.");
                tvNoEmployees.setVisibility(View.VISIBLE); // Show the message
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ViewEmployeesActivity.this, "Error fetching employees.", Toast.LENGTH_SHORT).show();
            tvNoEmployees.setVisibility(View.VISIBLE); // Show the message in case of failure too
        });
    }

    private void fetchEmployeeDetails(List<String> employeeIds) {
        List<User> employees = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(employeeIds.size());

        for (String id : employeeIds) {
            db.collection("Employees").document(id).get().addOnSuccessListener(documentSnapshot -> {
                User employee = documentSnapshot.toObject(User.class);
                if (employee != null) {
                    employee.setMyId(documentSnapshot.getId());
                    employees.add(employee);
                }
                if (counter.decrementAndGet() == 0) {
                    if (employees.isEmpty()) {
                        tvNoEmployees.setVisibility(View.VISIBLE); // Show message if no employees were successfully fetched
                    } else {
                        tvNoEmployees.setVisibility(View.GONE); // Hide message
                        adapter.updateData(employees);
                    }
                }
            }).addOnFailureListener(e -> {
                if (counter.decrementAndGet() == 0) {
                    if (employees.isEmpty()) {
                        tvNoEmployees.setVisibility(View.VISIBLE); // Show message if no employees were successfully fetched
                    } else {
                        tvNoEmployees.setVisibility(View.GONE); // Hide message
                        adapter.updateData(employees);
                    }
                }
            });
        }
    }
}