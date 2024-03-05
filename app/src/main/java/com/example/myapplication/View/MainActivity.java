package com.example.myapplication.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Controller.AuthCallBack;
import com.example.myapplication.Controller.UserCallBack;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    TextView forgotPassword;
    TextView login_BTN_signup;
    private EditText emailEditText, password_edit;
    private Button loginButton;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initVars();
    }

    private void findViews() {
        emailEditText = findViewById(R.id.loginEmail);
        password_edit = findViewById(R.id.login_password);
        login_BTN_signup = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.login_button);
    }

    private void initVars() {
        database = new Database();

        database.setAuthCallBack(new AuthCallBack() {
            public void onLoginComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    if (database.getCurrentUser() != null) {
                        // Fetch user data
                        String uid = database.getCurrentUser().getUid();
                        database.fetchUserData(uid);
                    } else {
                        // Handle the case where login failed
                        Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(MainActivity.this, "Success Login", Toast.LENGTH_SHORT).show();
                } else {
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCreateAccountComplete(boolean status, String err) {
            }
        });

        database.setUserCallBack(new UserCallBack() {
            @Override
            public void onUserFetchDataComplete(User customer) {
                if (customer != null) {
                    int type = customer.getAccount_type();
                    if (type == 0) {
                        Toast.makeText(MainActivity.this, "Hello Employee", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MainPreWork.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Hello Manager", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onUpdateComplete(Task<Void> task) {
            }
        });

        login_BTN_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = password_edit.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "request email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "request password", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform login
                    database.loginUser(email, password);
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (database.getCurrentUser() != null) {
            String uid = database.getCurrentUser().getUid();
            database.fetchUserData(uid);
        }
    }
}