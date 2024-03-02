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

import com.example.myapplication.R;

import java.util.Locale;

public class MainWorkMenu extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;

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

        Button startCalcBtn = findViewById(R.id.stcaBtn);
        startCalcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWorkMenu.this, SalaryActivity.class);
                salaryActivityResultLauncher.launch(intent);
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

        // Get the current hourly salary from wherever you are fetching it
        double currentHourlySalary = 20.0; // For example, replace this with your actual hourly salary value

        // Set the text of the hourlySalaryTextView
        hourlySalaryTextView.setText("Current hourly salary: $" + currentHourlySalary);


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

}





