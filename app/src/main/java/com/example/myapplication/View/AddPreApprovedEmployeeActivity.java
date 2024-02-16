package com.example.myapplication.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.Model.Database;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class AddPreApprovedEmployeeActivity extends AppCompatActivity {

    private EditText emailEditText, salaryEditText;
    private Button addButton;
    private LinearLayout emailsContainer;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pre_approved_employee);

        emailEditText = findViewById(R.id.emailEditText);
        salaryEditText = findViewById(R.id.salaryEditText);
        addButton = findViewById(R.id.addButton);
        emailsContainer = findViewById(R.id.emailsContainer);

        database = new Database();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String salary = salaryEditText.getText().toString().trim();
                if (!email.isEmpty() && !salary.isEmpty()) {
                    String managerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    database.addPreApprovedEmail(email, salary, managerId, new Database.PreApprovedEmailCallback() {
                        @Override
                        public void onSuccess() {
                            addEmailToView(email);
                            Toast.makeText(AddPreApprovedEmployeeActivity.this, "Email added successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddPreApprovedEmployeeActivity.this, "Failed to add email", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AddPreApprovedEmployeeActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addEmailToView(String email) {
        View emailView = getLayoutInflater().inflate(R.layout.email_item, null);
        TextView emailTextView = emailView.findViewById(R.id.emailTextView);
        ImageButton removeButton = emailView.findViewById(R.id.removeButton);

        emailTextView.setText(email);
        removeButton.setOnClickListener(v -> {
            emailsContainer.removeView(emailView);
            database.removePreApprovedEmail(email, new Database.PreApprovedEmailCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddPreApprovedEmployeeActivity.this, "Email removed successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPreApprovedEmployeeActivity.this, "Failed to remove email", Toast.LENGTH_SHORT).show();
                }
            });
        });

        emailsContainer.addView(emailView);
    }
}
