package com.example.shiftmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainCurrWork extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_curr_work);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = formatter.format(calendar.getTime());

        Button starting_button = findViewById(R.id.starting_button);

        starting_button.setText("Starting Time:\n"+formattedTime);
    }
}