package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.Database;
import com.example.myapplication.R;

public class EmployeeActivity extends AppCompatActivity {

    TextView textView;
    Button btnBack;
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);


        findViews();
        initPlay();
    }

    private void findViews() {
        btnBack = findViewById(R.id.back_button);
    }


    public void initPlay(){

        database = new Database();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EmployeeActivity.this,"Back",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EmployeeActivity.this,MainActivity.class);
                startActivity(intent);
                database.logout();
                finish();
            }
        });


    }
}