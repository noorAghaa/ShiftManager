package com.example.myapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ViewHolder> {
    private final List<Shift> shiftList;

    public ShiftAdapter(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shift_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shift shift = shiftList.get(position);
        holder.dayTextView.setText(shift.getDay() + " - " + shift.getFormattedDay());
        holder.startAndEndTextView.setText(shift.getDuration());
        holder.durationTextView.setText(shift.calculateDuration()); // Use formatted date range
    }

    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Shift> newShifts) {
        shiftList.clear();
        shiftList.addAll(newShifts);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView durationTextView;
        TextView startAndEndTextView;

        public ViewHolder(View view) {
            super(view);
            dayTextView = view.findViewById(R.id.dayTextView);
            startAndEndTextView = view.findViewById(R.id.startAndEndTextView);
            durationTextView = view.findViewById(R.id.durationTextView);
        }
    }
}
