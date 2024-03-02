package com.example.myapplication.Model;

import android.icu.util.Calendar;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.Controller.AuthCallBack;
import com.example.myapplication.Controller.UserCallBack;
import com.example.myapplication.Controller.UsersCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Database {

    public static final String USERS_TABLE = "Employees";
    public static final String SHIFTS_TABLE = "Shifts"; // Define shifts collection name
    public static final String MANAGERS_TABLE = "Managers";
    public static final String PRE_APPROVED_EMAILS_TABLE = "PreApprovedEmails";

  //  private int month; // Declare month as a class-level variable



    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AuthCallBack authCallBack;
    private UserCallBack userCallBack;

    public Database(){
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //this.month=0;
    }

    public void setAuthCallBack(AuthCallBack authCallBack){
        this.authCallBack = authCallBack;
    }

    public void setUserCallBack(UserCallBack userCallBack){
        this.userCallBack = userCallBack;
    }

    public void loginUser(String email, String password){
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
                        if(getCurrentUser() != null){
                            String userId = getCurrentUser().getUid();
                            userData.setKeyOn(userId);
                            saveUserData(userData);

                            // Check if the account is a manager account
                            if (userData.getAccount_type() == 1) {
                                // Add an entry for this manager in the Managers table with an empty list of employees
                                addManagerWithNoEmployees(userId);
                            } else{
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
                        }else {
                            authCallBack.onCreateAccountComplete(false, task.getException().getMessage().toString());
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
                    callback.onCreateAccountComplete(false, task.getException().getMessage());
                }
            });
        }
    }


    public void removePreApprovedEmail(String email, PreApprovedEmailCallback callback) {
        db.collection(PRE_APPROVED_EMAILS_TABLE).document(email).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }



//    public void createAccountWithPhoneNumber(SignupActivity activity, String phoneNumber, String password, User userData) {
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phoneNumber)       // Phone number without '+' and country code
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout for code verification
//                        .setActivity(activity)             // Pass your activity here
//                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                            @Override
//                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
//                                mAuth.signInWithCredential(credential)
//                                        .addOnCompleteListener(task -> {
//                                            if (task.isSuccessful()) {
//                                                userData.setKeyOn(mAuth.getCurrentUser().getUid());
//                                                saveUserData(userData);
//                                            } else {
//                                                authCallBack.onCreateAccountComplete(false, Objects.requireNonNull(task.getException()).getMessage());
//                                            }
//                                        });
//                            }
//
//                            @Override
//                            public void onVerificationFailed(@NonNull FirebaseException e) {
//                                // Handle verification failure
//                                authCallBack.onCreateAccountComplete(false, e.getMessage());
//                            }
//
//                            // ... (other callback methods)
//                        })
//                        .build();
//
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }


    public void saveUserData(User user){
        this.db.collection(USERS_TABLE).document(user.getKey()).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            authCallBack.onCreateAccountComplete(true, "");
                        else
                            authCallBack.onCreateAccountComplete(false, Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public void updateUser(User user){
        this.db.collection(USERS_TABLE).document(user.getKey()).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userCallBack.onUpdateComplete(task);
                    }
                });
    }

    public void fetchUserData(String uid) {
        db.collection(USERS_TABLE).document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle Firestore exception
                    // You may want to log the error or notify the user
                    return;
                }

                if (value != null && value.exists()) {
                    // Document exists, process its data
                    User user = value.toObject(User.class);
                    if (user != null) {
                        user.setKey(value.getId());
                        userCallBack.onUserFetchDataComplete(user);
                    } else {
                        // Handle unexpected null value for user
                        // You may want to log an error or notify the user
                    }
                } else {
                    // Document does not exist or is empty
                    // You may want to log an error or handle this case accordingly
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

    public interface ShiftDataCallback {
        void onShiftDataFetched(List<Shift> shifts);
        void onError(Exception e);
    }

    public FirebaseUser getCurrentUser(){

        return mAuth.getCurrentUser();
    }

    public void logout() {
        this.mAuth.signOut();
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

    public void fetchEmployeesUnderManager(String managerId, UsersCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Managers")
                .whereEqualTo("managerId", managerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> employees = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User employee = document.toObject(User.class);
                        employees.add(employee);
                    }
                    callback.onUsersFetchDataComplete(employees);
                })
                .addOnFailureListener(e -> callback.onError(e));
    }


    public interface PreApprovedEmailCallback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
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
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }



    public interface SalaryFetchCallback {
        void onSalaryFetch(String hourlySalary);
        void onError(Exception e);
    }


    public void checkUserIdExists(String userId, UserIdCheckCallback callback) {
        db.collection(USERS_TABLE).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User ID exists
                        callback.onUserIdExists(true);
                    } else {
                        // User ID does not exist
                        callback.onUserIdExists(false);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    callback.onFailure(e);
                });
    }

    public interface UserIdCheckCallback {
        void onUserIdExists(boolean exists);
        void onFailure(Exception e);
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

    public interface UserExistsCallback {
        void onUserExistsCheckComplete(boolean exists);
        void onUserExistsCheckFailure(Exception e);
    }


    // In Database.java
    public void fetchShiftsForMonth(String userId, int year, int month, ShiftDataCallback callback) {
        Log.d("Database", "Fetching shifts for Year: " + year + ", Month: " + month + ", User ID: " + userId);

        // Get a reference to your Firestore collection containing shifts
        CollectionReference shiftsRef = FirebaseFirestore.getInstance().collection("Shifts");

        // Calculate the start and end dates of the month
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.MONTH, month + 1);
        Date endDate = calendar.getTime();

        // Query shifts for the specified user and date range
        Query query = shiftsRef.whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate);

        // Execute the query
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("Database", "Shifts query successful");
            List<Shift> shifts = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                // Convert Firestore document to Shift object
                Shift shift = document.toObject(Shift.class);
                shifts.add(shift);
            }

            // Pass the list of shifts to the callback
            callback.onShiftDataFetched(shifts);
        }).addOnFailureListener(e -> {
            Log.e("Database", "Error fetching shifts: " + e.getMessage());
            // Handle any errors
            callback.onError(e);
        });
    }




    public void getAllShifts(ShiftDataCallback callback) {
        db.collection(SHIFTS_TABLE)
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


}