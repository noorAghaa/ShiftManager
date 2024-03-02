package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.example.myapplication.Controller.UserCallBack;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainPreWork extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView userInfoTextView;
    private Database database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pre_work);

        // Initialize the TextView (make sure to add this TextView in your layout file)
        userInfoTextView = findViewById(R.id.helo_user);

        database = new Database(); // Initialize your Database instance
        mAuth = FirebaseAuth.getInstance();
        fetchAndDisplayUserData(); // Fetch and display user data
        Button logoutButton = findViewById(R.id.now_button); // Initialize the logoutButton
        Button start_button = findViewById(R.id.start_button);
        Button shifts_button = findViewById(R.id.shift_button);
        Button workmenu_button = findViewById(R.id.work_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent curr_activity = new Intent(getApplicationContext(), MainCurrWork.class);
                startActivity(curr_activity);
            }
        });

        shifts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent curr_activity = new Intent(MainPreWork.this, MainShifts.class);
                startActivity(curr_activity);
            }
        });

        workmenu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent curr_activity = new Intent(MainPreWork.this, MainWorkMenu.class);
                startActivity(curr_activity);
            }
        });



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
    }
    private void fetchAndDisplayUserData() {
        FirebaseUser currentUser = database.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            database.fetchUserData(uid); // Fetch user data

            database.setUserCallBack(new UserCallBack() {
                @Override
                public void onUserFetchDataComplete(User user) {
                    // This method is called when user data is fetched successfully
                    if (user != null) {
                        // Display the fetched user data in the TextView
                        String userInfo = "Name: " + user.getFirstname() + " " + user.getLastname() + "\nAccount Type: " + (user.getAccount_type() == 1 ? "Proprietor/Manager" : "Employee");
                        userInfoTextView.setText(userInfo); // Set the text of userInfoTextView to display user info
                    }
                }

                @Override
                public void onUpdateComplete(Task<Void> task) {

                }

            });
        }
    }
}