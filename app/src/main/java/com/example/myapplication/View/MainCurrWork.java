package com.example.myapplication.View;

import androidx.appcompat.app.AppCompatActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainCurrWork extends AppCompatActivity {
    private SimpleDateFormat timeFormatter;
    private Button startingButton;
    private Button endingButton;
    private Database database;
    private Date start_Time_Date;
    private Date end_Time_Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_curr_work);

        initializeComponents();
        setupButtons();
    }

    private void initializeComponents() {
        database = new Database();
        startingButton = findViewById(R.id.starting_button);
        endingButton = findViewById(R.id.ending_button);

        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));

        start_Time_Date = getCurrentTime();
        end_Time_Date = null; // Set as null initially

        updateButtonLabels();
    }

    private void setupButtons() {
        startingButton.setOnClickListener(view -> showTimePickerDialog(true));
        endingButton.setOnClickListener(view -> showTimePickerDialog(false));
        Button endShiftButton = findViewById(R.id.end_button);
        endShiftButton.setOnClickListener(view -> endShift());
    }

    private void updateButtonLabels() {
        startingButton.setText(String.format("Starting Time:\n%s", formatTime(start_Time_Date)));
        endingButton.setText(String.format("Ending Time:\n%s", end_Time_Date == null ? "-" : formatTime(end_Time_Date)));
    }

    private Date getCurrentTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        return calendar.getTime();
    }

    private String formatTime(Date time) {
        return timeFormatter.format(time);
    }

    private void endShift() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && end_Time_Date == null) {
            end_Time_Date = getCurrentTime();
        }

        String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(start_Time_Date);
        String duration = formatTime(start_Time_Date) + " - " + formatTime(end_Time_Date);
        String userId = currentUser.getUid();

        Shift shift = new Shift(day, start_Time_Date, duration, userId, end_Time_Date);
        database.saveShiftData(shift);

        navigateToPreMain();
    }

    private void navigateToPreMain() {
        Intent goToPreMain = new Intent(MainCurrWork.this, MainPreWork.class);
        goToPreMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goToPreMain);
    }

    private void showTimePickerDialog(boolean isStartingTime) {
        Calendar timePickerCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        int hour = timePickerCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = timePickerCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minuteOfHour) -> {
            timePickerCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            timePickerCalendar.set(Calendar.MINUTE, minuteOfHour);
            timePickerCalendar.set(Calendar.SECOND, 0);
            timePickerCalendar.set(Calendar.MILLISECOND, 0);

            Date time = timePickerCalendar.getTime();

            if (isStartingTime) {
                start_Time_Date = time;
            } else {
                end_Time_Date = time;
            }
            updateButtonLabels();
        }, hour, minute, true);

        timePickerDialog.show();
    }
}
