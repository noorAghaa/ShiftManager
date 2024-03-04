package com.example.myapplication.View;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Database;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder> {

    private Context context;
    private List<User> employeesList;
    private OnEmployeeClickListener listener;
    private Map<String, String> updatedSalaries = new HashMap<>();


    public interface OnEmployeeClickListener {
        void onEmployeeClicked(String userId);
    }

    public EmployeesAdapter(Context context, List<User> employeesList, OnEmployeeClickListener listener) {
        this.context = context;
        this.employeesList = employeesList;
        this.listener = listener;
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
        String fullName = employee.getFirstname() + " " + employee.getLastname();
        holder.nameTextView.setText(fullName);

        // Check if there's an updated salary in the map
        String updatedSalary = updatedSalaries.get(employee.getMyId());
        if (updatedSalary != null) {
            // If there's an updated salary, use it directly without fetching
            holder.viewSalaryButton.setOnClickListener(v -> {
                showUpdateSalaryDialog(employee, updatedSalary);
            });
        } else {
            // If there's no updated salary in the map, fetch from the database
            holder.viewSalaryButton.setOnClickListener(v -> {
                final Database db = new Database();
                db.fetch_Salary(employee.getMyId(), new Database.SalaryFetchCallback() {
                    @Override
                    public void onSalaryFetch(String hourlySalary) {
                        showUpdateSalaryDialog(employee, hourlySalary);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Error fetching salary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }



        holder.removeEmployeeButton.setOnClickListener(v -> {
            removeEmployee(employee.getMyId(), position);
        });

        holder.viewShiftsButton.setOnClickListener(v -> {
            if(listener != null) {
                listener.onEmployeeClicked(employee.getMyId());
            }
        });

    }

    private void showUpdateSalaryDialog(User employee, String currentSalary) {
        // Creating EditText dynamically to input new salary
        final EditText salaryInput = new EditText(context);
        salaryInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        salaryInput.setHint("Enter new salary");

        // Preparing and showing the AlertDialog
        new AlertDialog.Builder(context)
                .setTitle("Update Salary")
                .setMessage("Current Salary: " + currentSalary)
                .setView(salaryInput)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newSalary = salaryInput.getText().toString();
                    final Database db = new Database();
                    db.updateSalary(employee.getMyId(), newSalary, new Database.SalaryUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            // Store the new salary in the map
                            updatedSalaries.put(employee.getMyId(), newSalary);
                            Toast.makeText(context, "Salary updated successfully.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Failed to update salary.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public int getItemCount() {
        return employeesList.size();
    }

    private void removeEmployee(String userId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Employees").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    employeesList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Employee removed successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error removing employee.", Toast.LENGTH_SHORT).show());
    }

    public void updateData(List<User> newEmployees) {
        employeesList.clear();
        employeesList.addAll(newEmployees);
        notifyDataSetChanged();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        Button viewSalaryButton, removeEmployeeButton, viewShiftsButton;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.employeeNameTextView);
            viewSalaryButton = itemView.findViewById(R.id.viewSalaryButton);
            removeEmployeeButton = itemView.findViewById(R.id.removeEmployeeButton);
            viewShiftsButton = itemView.findViewById(R.id.viewShiftsButton); // Initialize the new button
        }
    }
}
