package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.myapplication.Model.Database;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class ManagerActivity extends AppCompatActivity {

    Button addNewEmmBut, logoutButton, viewEmployeesButton; // Declare the logoutButton
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        addNewEmmBut = findViewById(R.id.addNewEmployee);
        logoutButton = findViewById(R.id.logoutButton); // Initialize the logoutButton
        viewEmployeesButton = findViewById(R.id.viewEmployeesButton); // Initialize the viewEmployeesButton
        mAuth = FirebaseAuth.getInstance();

        addNewEmmBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddPreApprovedEmployeeActivity.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout the user
                mAuth.signOut();
                // Redirect to Login Activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                startActivity(intent);
                finish();
            }
        });

        // Set onClickListener for the viewEmployeesButton
        viewEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ViewEmployeesActivity
                Intent intent = new Intent(ManagerActivity.this, ViewEmployeesActivity.class);
                startActivity(intent);
            }
        });
    }
}

