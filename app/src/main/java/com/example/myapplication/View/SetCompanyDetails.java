package com.example.myapplication.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.Database;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SetCompanyDetails extends AppCompatActivity {
    private EditText companyName, companyAddress, companyPhoneNumber, companyWebsite;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_company_deatils);

        companyName = findViewById(R.id.companyNameEditText);
        companyAddress = findViewById(R.id.companyAddressEditText);
        companyPhoneNumber = findViewById(R.id.companyPhoneNumberEditText);
        companyWebsite = findViewById(R.id.companyWebsiteEditText);
        Button addButton = findViewById(R.id.addButton);

        database = new Database();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = companyName.getText().toString().trim();
                String address = companyAddress.getText().toString().trim();
                String phone = companyPhoneNumber.getText().toString().trim();
                String website = companyWebsite.getText().toString().trim();

                String managerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                database.addCompanyDetails(name, address, phone, website, managerId, new Database.SetCompanyDetailsCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(SetCompanyDetails.this, "Details added successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetCompanyDetails.this, ManagerActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetCompanyDetails.this, "Failed to add details", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}