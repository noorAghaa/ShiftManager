package com.example.myapplication.View;

import android.app.AlertDialog;
import android.content.Context;
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

        holder.viewSalaryButton.setOnClickListener(v -> {
            final Database db = new Database();
            // Define an array to hold the current salary, allowing it to be final yet mutable
            final String[] currentSalary = new String[1];

            db.fetchSalary(employee.getMyId(), new Database.SalaryFetchCallback() {
                @Override
                public void onSalaryFetch(String hourlySalary) {
                    // Update currentSalary[0] with the fetched salary
                    currentSalary[0] = hourlySalary;
                    // Creating EditText dynamically to input new salary
                    final EditText salaryInput = new EditText(context);
                    salaryInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    salaryInput.setHint("Enter new salary");

                    // Preparing and showing the AlertDialog
                    new AlertDialog.Builder(context)
                            .setTitle("Update Salary")
                            .setMessage("Current Salary: " + currentSalary[0])
                            .setView(salaryInput) // Adding EditText to AlertDialog
                            .setPositiveButton("Update", (dialog, which) -> {
                                String newSalary = salaryInput.getText().toString();
                                // Implement the logic to update the salary in the database
                                db.updateSalary(employee.getMyId(), newSalary, new Database.SalaryUpdateCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Update the local currentSalary variable with the new salary
                                        currentSalary[0] = newSalary;
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
                public void onError(Exception e) {
                    Toast.makeText(context, "Error fetching salary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });



        holder.removeEmployeeButton.setOnClickListener(v -> {
            removeEmployee(employee.getMyId(), position);
        });

        holder.viewShiftsButton.setOnClickListener(v -> {
            if(listener != null) {
                listener.onEmployeeClicked(employee.getMyId());
            }
        });

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
