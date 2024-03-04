package com.example.myapplication.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.Model.Database;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class MainWorkMenu extends AppCompatActivity {

    private static final int REQUEST_CODE_SALARY = 1;
    private BroadcastReceiver broadcastReceiver;
    private Database database = new Database();

    private CountDownTimer countDownTimer;
    private TextView timerTextView;
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



        Intent intent = new Intent(MainWorkMenu.this, SalaryActivity.class);
        salaryActivityResultLauncher.launch(intent);
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






        // Initialize the timerTextView
        timerTextView = findViewById(R.id.timerTextView);
        // Initialize the broadcastReceiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long remainingTime = intent.getLongExtra("remaining_time", 0);
                updateTimerUI(remainingTime);
            }
        };
        // Register a BroadcastReceiver to receive timer updates
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("TIMER_UPDATE"));

        Button breaksBtn = findViewById(R.id.breakBtn);
        breaksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainWorkMenu", "Breaks button clicked"); // Log button click
                // Check if the TimerService is already running
                if (!isServiceRunning(TimerService.class, MainWorkMenu.this)) {
                    Intent serviceIntent = new Intent(MainWorkMenu.this, TimerService.class);
                    startService(serviceIntent);
                } else {
                    Log.d("MainWorkMenu", "TimerService is already running");
                }
            }
        });

        breaksBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("MainWorkMenu", "Breaks button long pressed"); // Log long press
                // Open the breaks activity
                Intent intent = new Intent(MainWorkMenu.this, BreaksActivity.class);
                startActivity(intent);
                return true; // Indicates that the long click event has been consumed
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



        Button monthlyCalcBtn = findViewById(R.id.monthlyCalcBtn);
        monthlyCalcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, MonthlyActivity.class);
                salaryActivityResultLauncher.launch(intent);
            }
        });


        // Find the hourlySalaryTextView in your activity or fragment
        TextView hourlySalaryTextView = findViewById(R.id.hourlySalaryTextView);

/// Find the hourlySalaryTextView in your activity or fragment

// Set the text of the hourlySalaryTextView
        hourlySalaryTextView.setText("Current hourly salary: Fetching..."); // Placeholder text until fetched
        fetchHourlySalary(); // Start fetching the hourly salaryurrent hourly salary

// Set the text of the hourlySalaryTextView
        fetchHourlySalary();



        BroadcastReceiver breakEndReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Write the end of the break to the database
                writeBreakEndToDatabase();
                // Start the BreaksActivity to display the updated breaks list
                startActivity(new Intent(MainWorkMenu.this, BreaksActivity.class));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(breakEndReceiver, new IntentFilter("BREAK_END"));
    }

    private void fetchHourlySalary() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.fetchSalary(userId, new Database.SalaryFetchCallback() {
            @Override
            public void onSalaryFetch(String hourlySalary) {
                // Convert hourly salary to double if needed
                double hourlySalaryDouble = Double.parseDouble(hourlySalary);
                // Update the UI with the fetched hourly salary
                updateHourlySalaryUI(hourlySalaryDouble);
            }

            @Override
            public void onError(Exception e) {
                // Handle errors
                Log.e("MainWorkMenu", "Error fetching hourly salary: " + e.getMessage());
            }
        });
    }

    private void updateHourlySalaryUI(double hourlySalary) {
        // Find the hourlySalaryTextView in your activity or fragment
        TextView hourlySalaryTextView = findViewById(R.id.hourlySalaryTextView);
        // Set the text of the hourlySalaryTextView
        hourlySalaryTextView.setText("Current hourly salary: $" + hourlySalary);
    }


    private void updateTimerUI(long remainingTime) {
        long minutes = remainingTime / 60000;
        long seconds = (remainingTime % 60000) / 1000;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }


    // Helper method to check if a service is running
    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // In MainWorkMenu.java
    private void writeBreakEndToDatabase() {
        // Write the end of the break to the database
        // You can use Firestore or any other database you are using
        // For example:
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("breaks")
                .document(userId)
                .update("end_time", FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> Log.d("Database", "Break end written to database"))
                .addOnFailureListener(e -> Log.e("Database", "Error writing break end to database: " + e.getMessage()));
    }

}





