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

    Button addNewEmmBut, logoutButton;
    private FirebaseAuth mAuth; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        addNewEmmBut = findViewById(R.id.addNewEmployee);
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        addNewEmmBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddPreApprovedEmployeeActivity.class);
                startActivity(intent);
            }
        });

    }
}
