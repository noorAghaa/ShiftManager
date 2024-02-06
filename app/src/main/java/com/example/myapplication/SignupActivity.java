package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignupActivity extends AppCompatActivity {


    EditText firstname, lastname, signupEmail, signupPassword, phoneNumber;
    EditText Id_Number;
    TextView txtV_button_back;
    Button signupButton;
    ImageButton backButton;
    Switch accountTypeSwitch;

    private Database database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        findV();
        init();
    }

    private void findV(){
        firstname = findViewById(R.id.firstName);
        signupEmail = findViewById(R.id.signup_email);
        lastname = findViewById(R.id.lastName);
        signupPassword = findViewById(R.id.signup_password);
        txtV_button_back = findViewById(R.id.loginRedirectText_ls);
        signupButton = findViewById(R.id.signupButton);
        backButton = findViewById(R.id.customBackButton);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        Id_Number = findViewById(R.id.Id_Number);
        accountTypeSwitch = findViewById(R.id.switchAccountType);
    }


    private void init(){
        database = new Database();
        database.setAuthCallBack(new AuthCallBack() {
            @Override
            public void onLoginComplete(Task<AuthResult> task) {

            }

            @Override
            public void onCreateAccountComplete(boolean status, String err) {
//                signup_PB_loading.setVisibility(View.INVISIBLE);
                if(status){
                    Toast.makeText(SignupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
//                    database.logout();
//                    finish();
                }else{
                    Toast.makeText(SignupActivity.this, err, Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInput()){
                    Toast.makeText(SignupActivity.this, "Error CheckInput", Toast.LENGTH_LONG).show();

                    return;
                }

                Toast.makeText(SignupActivity.this, "Success", Toast.LENGTH_SHORT).show();

                User employee = new User();
                employee.setEmail(signupEmail.getText().toString());
                employee.setFirstname(firstname.getText().toString());
                employee.setLastname(lastname.getText().toString());
                employee.setPhoneNumber(phoneNumber.getText().toString());
                employee.setPassword(signupPassword.getText().toString());

                int accountType = accountTypeSwitch.isChecked() ? 1 : 0;
                employee.setAccount_type(accountType);

                String password = signupPassword.getText().toString().trim();
                String id = Id_Number.getText().toString().trim();
                String email = signupEmail.getText().toString();

                database.createAccount(email,password,employee);
//                database.createAccountWithPhoneNumber(SignupActivity.this,employee.getPhoneNumber(), password, employee);
            }
        });


        txtV_button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    private boolean checkInput() {
        User customer = new User();
        customer.setEmail(signupEmail.getText().toString());
        customer.setFirstname(firstname.getText().toString());
        customer.setLastname(lastname.getText().toString());
        customer.setPhoneNumber(phoneNumber.getText().toString());
        customer.setPassword(signupPassword.getText().toString());


        String password = customer.getPassword();
        String confirmPassword = customer.getPassword();

        if(!customer.isValid()) {
            Toast.makeText(SignupActivity.this, "Please fill all user info!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.length() < 8){
            Toast.makeText(SignupActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!password.equals(confirmPassword)){
            Toast.makeText(SignupActivity.this, "mismatch between password and confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }
}