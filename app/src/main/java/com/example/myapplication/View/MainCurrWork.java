package com.example.myapplication.View;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        if (currentUser != null && end_Time_Date != null) {
            String userId = currentUser.getUid();

            // First, fetch existing shifts
            database.fetchShifts(userId, new Database.ShiftDataCallback() {
                @Override
                public void onShiftDataFetched(List<Shift> shifts) {
                    // Then, check if the new shift overlaps
                    if (!doesShiftOverlap(start_Time_Date, end_Time_Date, shifts)) {
                        // No overlap, proceed to save the new shift
                        saveNewShift(userId);
                    } else {
                        // Handle overlap case
                        runOnUiThread(() -> Toast.makeText(MainCurrWork.this, "Shift overlaps with an existing shift.", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("MainCurrWork", "Failed to fetch shifts", e);
                }
            });
        }
    }

    private boolean doesShiftOverlap(Date newStart, Date newEnd, List<Shift> existingShifts) {
        Log.d("OverlapCheck", "Checking new shift: " + newStart + " to " + newEnd);
        for (Shift existingShift : existingShifts) {
            Date existingStart = existingShift.getStart_date();
            Date existingEnd = existingShift.getEnd_date();
            Log.d("OverlapCheck", "Against existing shift: " + existingStart + " to " + existingEnd);
            if (newStart.before(existingEnd) && newEnd.after(existingStart)) {
                Log.d("OverlapCheck", "Overlap found.");
                return true; // Overlap found
            }
        }
        Log.d("OverlapCheck", "No overlap found.");
        return false; // No overlap
    }

    private void saveNewShift(String userId) {
        String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(start_Time_Date);
        String duration = formatTime(start_Time_Date) + " - " + formatTime(end_Time_Date);

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