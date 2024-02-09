package com.example.myapplication.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class SickDaysAdapter extends RecyclerView.Adapter<SickDaysAdapter.ViewHolder> {

    private List<String> sickDaysList;

    // Constructor to initialize the dataset
    public SickDaysAdapter(List<String> sickDaysList) {
        this.sickDaysList = sickDaysList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sick_day, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String sickDay = sickDaysList.get(position);
        holder.sickDayTextView.setText(sickDay);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sickDaysList.size();
    }

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView sickDayTextView;

        public ViewHolder(View view) {
            super(view);
            sickDayTextView = view.findViewById(R.id.textView_sick_day);
        }
    }
}
