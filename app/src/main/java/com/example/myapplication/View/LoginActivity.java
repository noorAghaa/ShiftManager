package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Controller.AuthCallBack;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.example.myapplication.Controller.UserCallBack;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {


    private EditText IdText, password_edit,edit_phoneNumber;
    private Button login_btn;
    TextView login_register;
    TextView forgotPassword;
    private Button loginButton;
    TextView login_BTN_signup;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        initVars();
    }


    private void findViews() {
        IdText = findViewById(R.id.loginId);
        password_edit = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_button);
        login_register = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.forgotPassword);
        edit_phoneNumber = findViewById(R.id.edit_phoneNumber);
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
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(LoginActivity.this,"Success Login",Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage().toString();
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCreateAccountComplete(boolean status, String err) {

            }
        });

        database.setUserCallBack(new UserCallBack() {
            @Override
            public void onUserFetchDataComplete(User customer) {
                if (customer!=null) {
                    int type = customer.getAccount_type();
//                    if(type==0) {
//                        Toast.makeText(LoginAct.this,"Hello Employee",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(LoginAct.this, EmployeeActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }else{
//                        Toast.makeText(LoginAct.this,"Hello Manager",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(LoginAct.this, EmployeeActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
                }
            }

            @Override
            public void onUpdateComplete(Task<Void> task) {}
        });


        login_BTN_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        Nexus 7 API 34 is already running. If that is not the case, delete C:\Users\ASUS\.android\avd\Nexus_7_API_34.avd\*.lock and try again.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = IdText.getText().toString().trim();
                String password = password_edit.getText().toString().trim();
                String phoneNumber = edit_phoneNumber.getText().toString().trim();

                if(email.isEmpty() ){
                    Toast.makeText(LoginActivity.this,"request email",Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(LoginActivity.this,"request password",Toast.LENGTH_SHORT).show();
                }
                else {
                    // Perform login
                    database.loginUser(email, password);
                }
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });


        if(database.getCurrentUser() != null){
            String uid = database.getCurrentUser().getUid();
            database.fetchUserData(uid);
        }
    }
}