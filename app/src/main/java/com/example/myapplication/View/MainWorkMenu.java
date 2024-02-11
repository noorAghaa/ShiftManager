package com.example.myapplication.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.Locale;

public class MainWorkMenu extends AppCompatActivity {

    private ActivityResultLauncher<Intent> salaryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_work_menu);

        // Initialize the ActivityResultLauncher
        salaryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double salary = data.getDoubleExtra("salary", 0.0);
                            TextView salaryTextView = findViewById(R.id.salaryTextView);
                            salaryTextView.setText(String.format(Locale.getDefault(), "Salary: $%.2f", salary));
                        }
                    }
                }
        );

        // Button declarations and onClickListeners
        Button exhBtn = findViewById(R.id.exhBtn);
        exhBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, ExtraHoursActivity.class);
                startActivity(intent);
            }
        });

        Button breaksBtn = findViewById(R.id.breakBtn);
        breaksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, BreaksActivity.class);
                startActivity(intent);
            }
        });

        Button travelexBtn = findViewById(R.id.treBtn);
        travelexBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, TravelingExpensesActivity.class);
                startActivity(intent);
            }
        });

        Button sickDaysBtn = findViewById(R.id.sidBtn);
        sickDaysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, SickDaysActivity.class);
                startActivity(intent);
            }
        });

        Button startCalcBtn = findViewById(R.id.stcaBtn);
        startCalcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, SalaryActivity.class);
                salaryActivityResultLauncher.launch(intent);
            }
        });
    }





}
