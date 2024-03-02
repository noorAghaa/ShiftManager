package com.example.myapplication.View;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;

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
        startTimer(30 *60* 1000); // Start a 30-minute timer
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
                sendBreakToFirestore();
            }
        }.start();
        Log.d("TimerService", "Countdown timer started"); // Log countdown timer started
    }


    private void sendBreakToFirestore() {
        // Get current user ID (You need to implement this according to your authentication system)
        String userId = getCurrentUserId();

        // Get current date
        Date currentDate = new Date();

        // Calculate the duration based on the starting time and current time
        long duration = System.currentTimeMillis() - startTimeMillis; // Assuming startTimeMillis is the time when the break started

        // Create a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new break document with user ID, date, and duration
        Map<String, Object> breakData = new HashMap<>();
        breakData.put("userId", userId);
        breakData.put("date", currentDate);
        breakData.put("duration in minutes", duration/1000/60);
        // Add the break document to the "Breaks" collection
        db.collection("Breaks")
                .add(breakData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("TimerService", "Break added to Firestore: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("TimerService", "Error adding break to Firestore", e);
                });
    }



    public String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null; // User is not logged in
        }
    }

}
