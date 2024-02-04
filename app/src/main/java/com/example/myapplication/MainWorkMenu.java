package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainWorkMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_work_menu);



        Button exhBtn = findViewById(R.id.exhBtn);
        exhBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, ExtraHoursActivity.class);
                startActivity(intent);
            }
        });


        Button salaryBtn = findViewById(R.id.salaryBtn);
        salaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, SalaryActivity.class);
                startActivity(intent);
            }
        });

        Button breaksBtn  = findViewById(R.id.breakBtn);
        breaksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, BreaksActivity.class);
                startActivity(intent);
            }
        });

        Button travelexBtn = findViewById(R.id.treBtn);
        travelexBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, TravelingExpensesActivity.class);
                startActivity(intent);
            }
        });

        Button sickDaysBtn = findViewById(R.id.sidBtn);
        sickDaysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, SickDaysActivity.class);
                startActivity(intent);
            }
        });

        Button startCalcBtn = findViewById(R.id.stcaBtn);
        startCalcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(MainWorkMenu.this, SalaryActivity.class);
                startActivity(intent);
            }
        });


    }
}