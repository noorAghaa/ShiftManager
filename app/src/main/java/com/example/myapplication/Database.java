package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class Database {

    public static final String USERS_TABLE = "Employees";
    public static final String SHIFTS_TABLE = "Shifts"; // Define shifts collection name

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AuthCallBack authCallBack;
    private UserCallBack userCallBack;

    public Database(){
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
                            userData.setKeyOn(getCurrentUser().getUid());
                            saveUserData(userData);
                        }else {
                            authCallBack.onCreateAccountComplete(false, task.getException().getMessage().toString());
                        }
                    }
                });
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
}