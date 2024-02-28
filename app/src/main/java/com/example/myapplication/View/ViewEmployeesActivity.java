package com.example.myapplication.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.View.EmployeesAdapter;
import com.example.myapplication.Controller.EmployeeIdsCallback;
import com.example.myapplication.Controller.UsersCallback;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewEmployeesActivity extends AppCompatActivity {

    private RecyclerView employeesRecyclerView;
    private EmployeesAdapter adapter;
    private ArrayList<User> employeesList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employees);

        employeesRecyclerView = findViewById(R.id.employeesRecyclerView);
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeesAdapter(this, employeesList);
        employeesRecyclerView.setAdapter(adapter);

        String managerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchEmployeeIdsForManager(managerId);
    }

    private void fetchEmployeeIdsForManager(String managerId) {
        db.collection("Managers").document(managerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> employeeIds = (List<String>) documentSnapshot.get("employeeIds");
                if (employeeIds != null) {
                    fetchEmployeeDetails(employeeIds);
                } else {
                    Log.e("ViewEmployeesActivity", "No employees found for manager.");
                }
            } else {
                Log.e("ViewEmployeesActivity", "Manager document does not exist.");
            }
        }).addOnFailureListener(e -> Toast.makeText(ViewEmployeesActivity.this, "Error fetching employees.", Toast.LENGTH_SHORT).show());
    }

    private void fetchEmployeeDetails(List<String> employeeIds) {
        List<User> employees = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(employeeIds.size());

        for (String id : employeeIds) {
            db.collection("Employees").document(id).get().addOnSuccessListener(documentSnapshot -> {
                User employee = documentSnapshot.toObject(User.class);
                if (employee != null) {
                    employees.add(employee);
                }
                if (counter.decrementAndGet() == 0) {
                    adapter.updateData(employees);
                }
            }).addOnFailureListener(e -> {
                if (counter.decrementAndGet() == 0) {
                    adapter.updateData(employees);
                }
            });
        }
    }
}
