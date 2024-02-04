package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainPreWork extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pre_work);

        Button start_button = findViewById(R.id.start_button);
        Button shift_button = findViewById(R.id.shift_button);
        Button workmenu_button = findViewById(R.id.work_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent curr_activity = new Intent(getApplicationContext(), MainCurrWork.class);
                startActivity(curr_activity);
            }
        });

        shift_button.setOnClickListener(new View.OnClickListener() {
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
    }
}