package com.example.myapplication.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class BreaksAdapter extends RecyclerView.Adapter<BreaksAdapter.ViewHolder> {
    private List<String> breakDatesList;
    public BreaksAdapter(List<String> breakDatesList) {
        this.breakDatesList = breakDatesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.break_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String breakDate = breakDatesList.get(position);
        holder.bind(breakDate);
    }

    @Override
    public int getItemCount() {
        return breakDatesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView breakTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            breakTextView = itemView.findViewById(R.id.breakTextView);
        }

        public void bind(String breakDate) {
            breakTextView.setText(breakDate);
        }
    }
}
