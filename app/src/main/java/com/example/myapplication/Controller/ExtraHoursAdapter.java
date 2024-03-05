package com.example.myapplication.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Shift;
import com.example.myapplication.R;

import java.util.List;

public class ExtraHoursAdapter extends RecyclerView.Adapter<ExtraHoursAdapter.ExtraHoursViewHolder> {
    private List<Shift> shiftList;
    private Context context;

    public ExtraHoursAdapter(Context context, List<Shift> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ExtraHoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift, parent, false);
        return new ExtraHoursViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExtraHoursViewHolder holder, int position) {
        Shift shift = shiftList.get(position);
        String[] myShiftDuration = shift.getDuration().split(" - ");
        // Set shift date
        holder.shiftDateTextView.setText(shift.getStart_date().toString());

        // Set start and end time
        holder.startTimeTextView.setText(myShiftDuration[0]);
        holder.endTimeTextView.setText(myShiftDuration[1]);

        // Calculate and set duration
        holder.durationTextView.setText("Duration: " + shift.calculateDuration());
    }

    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    static class ExtraHoursViewHolder extends RecyclerView.ViewHolder {
        TextView shiftDateTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;
        TextView durationTextView;

        public ExtraHoursViewHolder(@NonNull View itemView) {
            super(itemView);
            shiftDateTextView = itemView.findViewById(R.id.shiftDateTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
        }
    }
}
