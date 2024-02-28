package com.example.myapplication.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import java.util.List;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder> {

    private Context context;
    private List<User> employeesList;

    public EmployeesAdapter(Context context, List<User> employeesList) {
        this.context = context;
        this.employeesList = employeesList;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.employee_item, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        User employee = employeesList.get(position);
        // Concatenate the first name and the last name with a space in between
        String fullName = employee.getFirstname() + " " + employee.getLastname();
        holder.nameTextView.setText(fullName);
        // Set other employee details here
    }


    @Override
    public int getItemCount() {
        return employeesList.size();
    }

    public void updateData(List<User> newEmployees) {
        employeesList.clear();
        employeesList.addAll(newEmployees);
        notifyDataSetChanged(); // Notify any registered observers that the data set has changed.
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        // Add other views here

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.employeeNameTextView);
            // Initialize other views here
        }
    }
}
