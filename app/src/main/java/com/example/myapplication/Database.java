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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class Database {

    public static final String USERS_TABLE = "Employees";

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


    public FirebaseUser getCurrentUser(){

        return mAuth.getCurrentUser();
    }

    public void logout() {
        this.mAuth.signOut();
    }

}