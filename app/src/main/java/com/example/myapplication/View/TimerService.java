package com.example.myapplication.View;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimerService extends Service {

    private long startTimeMillis; // Variable to store the start time of the break

    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get the duration of the break from the intent (default to 30 minutes)
        long duration = intent.getLongExtra("duration", 60 * 1000);
        startTimer(duration); // Start the timer with the specified duration
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startTimer(long duration) {
        // Set the start time when the timer starts
        startTimeMillis = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                // Broadcast remaining time
                Intent intent = new Intent("TIMER_UPDATE");
                intent.putExtra("remaining_time", millisUntilFinished);
                LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(intent);
                Log.d("TimerService", "Broadcast sent with remaining time: " + millisUntilFinished); // Log broadcast sent
            }

            public void onFinish() {
                // Timer finished, handle accordingly
                endBreakAndWriteToFirestore();
            }
        }.start();
        Log.d("TimerService", "Countdown timer started"); // Log countdown timer started
    }

    private void endBreakAndWriteToFirestore() {
        // Get current user ID
        String userId = getCurrentUserId();

        // Get current date
        Date currentDate = new Date();

        // Calculate the duration based on the starting time and current time
        long duration = System.currentTimeMillis() - startTimeMillis;

        // Create a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new break document with user ID, start and end time, and duration
        Map<String, Object> breakData = new HashMap<>();
        breakData.put("userId", userId);
        breakData.put("start_time", new Date(startTimeMillis));
        breakData.put("end_time", currentDate);
        breakData.put("duration_in_minutes", duration / 1000 / 60);

        // Add the break document to the "breaks" collection
        db.collection("breaks")
                .add(breakData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("TimerService", "Break added to Firestore: " + documentReference.getId());
                    // Send a broadcast to indicate that the break has ended and been written to Firestore
                    Intent intent = new Intent("BREAK_END");
                    LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("TimerService", "Error adding break to Firestore", e);
                });
    }

    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null; // User is not logged in
        }
    }
}
