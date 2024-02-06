package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainCurrWork extends AppCompatActivity {
    private String startTime;
    private String endTime;
    private Button startingButton;
    private Button endingButton;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_curr_work);

        database = new Database();

        startingButton = findViewById(R.id.starting_button);
        endingButton = findViewById(R.id.ending_button);
        Button endShiftButton = findViewById(R.id.end_button);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        startTime = formatter.format(calendar.getTime());
        endTime = "-";

        startingButton.setText("Starting Time:\n" + startTime);
        endingButton.setText("Ending Time:\n" + endTime);

        startingButton.setOnClickListener(view -> showTimePickerDialog(true));
        endingButton.setOnClickListener(view -> showTimePickerDialog(false));

        endShiftButton.setOnClickListener(view -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if(endTime.equals("-")) {
                endTime = formatter.format(calendar.getTime());
            }
            String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
            Date date = calendar.getTime();
            String duration = startTime + " - " + endTime;
            String userId = currentUser.getUid();

            Shift shift = new Shift(day, date, duration, userId);
            database.saveShiftData(shift); // Save shift with user reference


            Intent goToShifts = new Intent(MainCurrWork.this, MainShifts.class);
            goToShifts.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goToShifts);
        });
    }

    private void showTimePickerDialog(boolean isStartingTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            if (isStartingTime) {
                startTime = formattedTime;
                startingButton.setText("Starting Time:\n" + formattedTime);
            } else {
                endTime = formattedTime;
                endingButton.setText("Ending Time:\n" + formattedTime);
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }
}