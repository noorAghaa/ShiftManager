package com.example.myapplication.View;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Controller.ExtraHoursAdapter;
import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ExtraHoursActivity extends AppCompatActivity {

    private RecyclerView extraHoursRecyclerView;
    private ExtraHoursAdapter extraHoursAdapter;
    private List<Shift> extraHoursShifts;

    private Database database; // Declare Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_hours);

        // Initialize Database instance
        database = new Database();

        // Initialize RecyclerView
        extraHoursRecyclerView = findViewById(R.id.recyclerViewExtraHours);
        extraHoursRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list to hold shifts with extra hours
        extraHoursShifts = new ArrayList<>();

        // Create adapter with empty list initially
        extraHoursAdapter = new ExtraHoursAdapter(this, extraHoursShifts);
        extraHoursRecyclerView.setAdapter(extraHoursAdapter);

        // Call method to fetch shifts with extra hours
        fetchShiftsWithExtraHours();
    }

    private void fetchShiftsWithExtraHours() {
        // Call the getAllShifts method from the Database instance
        database.getAllShifts(new Database.ShiftDataCallback() {
            @Override
            public void onShiftDataFetched(List<Shift> shifts) {
                // Iterate through all shifts and filter out those with extra hours
                for (Shift shift : shifts) {
                    if (shift.calculateDurationInHours() > 8) {
                        extraHoursShifts.add(shift);
                    }
                }
                // Notify the adapter that the data set has changed
                extraHoursAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Handle error fetching shifts
                Log.e("ExtraHoursActivity", "Error fetching shifts: " + e.getMessage());
            }
        });
    }
}
