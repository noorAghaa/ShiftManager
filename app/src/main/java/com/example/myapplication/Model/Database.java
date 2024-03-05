package com.example.myapplication.Model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.Controller.AuthCallBack;
import com.example.myapplication.Controller.UserCallBack;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Database {

    public static final String USERS_TABLE = "Employees";
    public static final String SHIFTS_TABLE = "Shifts"; // Define shifts collection name
    public static final String MANAGERS_TABLE = "Managers";
    public static final String PRE_APPROVED_EMAILS_TABLE = "PreApprovedEmails";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthCallBack authCallBack;
    private UserCallBack userCallBack;

    public Database() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void logout() {
        this.mAuth.signOut();
    }

    public void setAuthCallBack(AuthCallBack authCallBack) {
        this.authCallBack = authCallBack;
    }

    public void setUserCallBack(UserCallBack userCallBack) {
        this.userCallBack = userCallBack;
    }

    public FirebaseUser getCurrentUser() {

        return mAuth.getCurrentUser();
    }

    public void loginUser(String email, String password) {
        this.mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        authCallBack.onLoginComplete(task);

                    }
                });
    }

    public void createAccount(String email, String password, User userData) {
        this.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (getCurrentUser() != null) {
                            String userId = getCurrentUser().getUid();
                            userData.setKeyOn(userId);
                            saveUserData(userData);

                            // Check if the account is a manager account
                            if (userData.getAccount_type() == 1) {
                                // Add an entry for this manager in the Managers table with an empty list of employees
                                addManagerWithNoEmployees(userId);
                            } else {
                                // Regular employee account creation
                                // Retrieve managerId and salary from preApprovedEmails
                                db.collection(PRE_APPROVED_EMAILS_TABLE).document(email).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String managerId = documentSnapshot.getString("managerId");
                                                String salary = documentSnapshot.getString("salary");

                                                // Add employee to their manager's list
                                                addEmployeeToManagerList(userId, managerId);

                                                // Add salary to Salaries collection
                                                addSalaryToSalariesCollection(userId, salary);

                                                removePreApprovedEmail(email, new Database.PreApprovedEmailCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d("Database", "Pre-approved email removed successfully.");
                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Database", "Failed to remove pre-approved email: " + e.getMessage());
                                                    }
                                                });

                                                authCallBack.onCreateAccountComplete(true, "");
                                            } else {
                                                // Email not found in preApprovedEmails
                                                authCallBack.onCreateAccountComplete(false, "Employee's email not pre-approved.");
                                            }
                                        })
                                        .addOnFailureListener(e -> authCallBack.onCreateAccountComplete(false, e.getMessage()));

                            }

                            authCallBack.onCreateAccountComplete(true, "");
                        } else {
                            authCallBack.onCreateAccountComplete(false, Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void addEmployeeToManagerList(String userId, String managerId) {
        db.collection(MANAGERS_TABLE).document(managerId)
                .update("employeeIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> Log.d("Database", "Employee " + userId + " added to manager's list: " + managerId))
                .addOnFailureListener(e -> Log.e("Database", "Error adding employee to manager's list", e));
    }

    private void addSalaryToSalariesCollection(String userId, String salary) {
        Map<String, Object> salaryData = new HashMap<>();
        salaryData.put("salary", salary);

        db.collection("Salaries").document(userId)
                .set(salaryData)
                .addOnSuccessListener(aVoid -> Log.d("Database", "Salary for user " + userId + " added successfully"))
                .addOnFailureListener(e -> Log.e("Database", "Error adding salary for user " + userId, e));
    }

    private void addManagerWithNoEmployees(String managerId) {
        // Create a new manager document with an empty list of employee IDs
        Map<String, Object> managerData = new HashMap<>();
        managerData.put("employeeIds", new ArrayList<String>());

        db.collection(MANAGERS_TABLE).document(managerId).set(managerData)
                .addOnSuccessListener(aVoid -> Log.d("Database", "Manager added with no employees"))
                .addOnFailureListener(e -> Log.w("Database", "Error adding manager", e));
    }

    public void checkAndCreateAccount(final String email, final String password, final User userData, final AuthCallBack callback) {
        if (userData.getAccount_type() == 1) {
            // This block is for creating an account directly if the user is a manager
            createAccount(email, password, userData);
        } else {
            // Check for pre-approved emails for employees
            db.collection(PRE_APPROVED_EMAILS_TABLE).document(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Email is pre-approved, proceed to create account
                        createAccount(email, password, userData);
                    } else {
                        // Email not pre-approved, notify the user
                        Log.w("Database", "Email not pre-approved for account creation");
                        callback.onCreateAccountComplete(false, "Your email has not been approved by a manager.");
                    }
                } else {
                    Log.e("Database", "Error checking pre-approved emails", task.getException());
                    callback.onCreateAccountComplete(false, Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }
    }

    public void removePreApprovedEmail(String email, PreApprovedEmailCallback callback) {
        db.collection(PRE_APPROVED_EMAILS_TABLE).document(email).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void saveUserData(User user) {
        this.db.collection(USERS_TABLE).document(user.getKey()).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            authCallBack.onCreateAccountComplete(true, "");
                        else
                            authCallBack.onCreateAccountComplete(false, Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public void fetchUserData(String uid) {
        db.collection(USERS_TABLE).document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                if (value != null && value.exists()) {
                    // Document exists, process its data
                    User user = value.toObject(User.class);
                    if (user != null) {
                        user.setKey(value.getId());
                        userCallBack.onUserFetchDataComplete(user);
                    }
                }
            }
        });
    }

    public void fetchShifts(String userId, final ShiftDataCallback callback) {
        db.collection(SHIFTS_TABLE)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Shift> shifts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Shift shift = document.toObject(Shift.class);
                            shifts.add(shift);
                        }
                        callback.onShiftDataFetched(shifts);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void fetchShiftsForManager(String userId, final ShiftDataCallback callback) {
        db.collection(SHIFTS_TABLE)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Shift> shifts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Shift shift = document.toObject(Shift.class);
                            shifts.add(shift);
                        }
                        callback.onShiftDataFetched(shifts);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void addCompanyDetails(String name, String address, String phone, String website, String managerId, SetCompanyDetailsCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("companyName", name);
        data.put("companyAddress", address);
        data.put("companyPhone", phone);
        data.put("companyWebsite", website);

        db.collection(MANAGERS_TABLE).document(managerId).set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchCompanyInfo(String managerId, CompanyInfoCallback callback) {
        db.collection(MANAGERS_TABLE).document(managerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document was found, extract the data
                        String companyAddress = document.getString("companyAddress");
                        String companyName = document.getString("companyName");
                        String companyPhone = document.getString("companyPhone");
                        String companyWebsite = document.getString("companyWebsite");

                        // Construct the result string
                        String companyInfo = "Company Address: " + companyAddress + "\nCompany Name: " + companyName + "\nCompany Phone: " + companyPhone + "\nCompany Website: " + companyWebsite;

                        // Use the callback to return the data
                        callback.onCallback(companyInfo);
                    } else {
                        // Document does not exist, use the callback to handle the error
                        callback.onError("No such document");
                    }
                } else {
                    // Task failed with an exception, use the callback to handle the error
                    callback.onError("get failed with " + task.getException());
                }
            }
        });
    }

    public void saveShiftData(Shift shift) {
        // Assuming you already have the user's ID in the shift object
        db.collection(SHIFTS_TABLE).add(shift)
                .addOnSuccessListener(documentReference -> {
                    // success
                })
                .addOnFailureListener(e -> {
                    // failure
                });
    }

    public void addPreApprovedEmail(String email, String salary, String managerId, PreApprovedEmailCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("salary", salary);
        data.put("managerId", managerId);

        db.collection(PRE_APPROVED_EMAILS_TABLE).document(email).set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchSalary(String userId, SalaryFetchCallback callback) {
        db.collection("Salaries").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hourlySalary = documentSnapshot.getString("salary");
                        if (hourlySalary != null) {
                            callback.onSalaryFetch(hourlySalary);
                        } else {
                            callback.onError(new NullPointerException("Hourly salary is null"));
                        }
                    } else {
                        callback.onError(new NullPointerException("Document does not exist"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void fetch_Salary(String userId, SalaryFetchCallback callback) {
        db.collection("Salaries").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hourlySalary = documentSnapshot.getString("salary");
                        if (hourlySalary != null) {
                            callback.onSalaryFetch(hourlySalary);
                        } else {
                            callback.onError(new NullPointerException("Hourly salary is null"));
                        }
                    } else {
                        callback.onError(new NullPointerException("Document does not exist"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void checkUserExists(String email, final UserExistsCallback callback) {
        db.collection(USERS_TABLE)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = !task.getResult().isEmpty();
                        callback.onUserExistsCheckComplete(exists);
                    } else {
                        // Handle failure
                        callback.onUserExistsCheckFailure(task.getException());
                    }
                });
    }

    public void updateSalary(String userId, String newSalary, SalaryUpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> salaryUpdate = new HashMap<>();
        salaryUpdate.put("salary", newSalary); // Assuming 'salary' is the field name in your Firestore collection

        db.collection("Salaries") // Correcting the collection name to 'Salaries'
                .document(userId)
                .set(salaryUpdate) // Use .set() to overwrite the document or create it if it doesn't exist
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface CompanyInfoCallback {
        void onCallback(String companyInfo);

        void onError(String message);
    }

    public interface SetCompanyDetailsCallback {
        void onSuccess();

        void onFailure(@NonNull Exception e);
    }

    public interface ShiftDataCallback {
        void onShiftDataFetched(List<Shift> shifts);

        void onError(Exception e);
    }

    public interface PreApprovedEmailCallback {
        void onSuccess();

        void onFailure(@NonNull Exception e);
    }

    public interface SalaryFetchCallback {
        void onSalaryFetch(String hourlySalary);

        void onError(Exception e);
    }

    public interface UserExistsCallback {
        void onUserExistsCheckComplete(boolean exists);

        void onUserExistsCheckFailure(Exception e);
    }

    public interface SalaryUpdateCallback {
        void onSuccess();

        void onFailure(Exception e);
    }
}