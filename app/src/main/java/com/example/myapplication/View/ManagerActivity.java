package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Controller.UserCallBack;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ManagerActivity extends AppCompatActivity {

    Button addNewEmmBut, logoutButton, viewEmployeesButton, setCompanyDetails; // Declare the logoutButton
    TextView managerDetails;
    private FirebaseAuth mAuth;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        // Initialize the TextView (make sure to add this TextView in your layout file)
        managerDetails = findViewById(R.id.managerDetails);
        database = new Database(); // Initialize your Database instance
        fetchAndDisplayManagerData(); // Fetch and display user data

        addNewEmmBut = findViewById(R.id.addNewEmployee);
        logoutButton = findViewById(R.id.logoutButton); // Initialize the logoutButton
        viewEmployeesButton = findViewById(R.id.viewEmployeesButton); // Initialize the viewEmployeesButton
        setCompanyDetails = findViewById(R.id.companyDetails);
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

        setCompanyDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ViewEmployeesActivity
                Intent intent = new Intent(ManagerActivity.this, SetCompanyDetails.class);
                startActivity(intent);
            }
        });
    }

    private void fetchAndDisplayManagerData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            database.fetchUserData(uid); // Fetch user data

            database.setUserCallBack(new UserCallBack() {
                @Override
                public void onUserFetchDataComplete(User user) {
                    // This method is called when user data is fetched successfully
                    if (user != null) {
                        String userInfo = "Name: " + user.getFirstname() + " " + user.getLastname() + "\nAccount Type: " + (user.getAccount_type() == 1 ? "Manager" : "Employee") + "\n";

                        database.fetchCompanyInfo(uid, new Database.CompanyInfoCallback() {
                            @Override
                            public void onCallback(String companyInfo) {
                                managerDetails.setText(userInfo + companyInfo);
                                Log.d("Firestore", companyInfo);
                            }

                            @Override
                            public void onError(String message) {
                                // Handle errors here
                                Log.d("Firestore", message);
                            }
                        });
                    }
                }

                @Override
                public void onUpdateComplete(Task<Void> task) {

                }

            });
        }
    }
}

