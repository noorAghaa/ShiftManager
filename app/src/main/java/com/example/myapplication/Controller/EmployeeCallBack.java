package com.example.myapplication.Controller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public interface EmployeeCallBack {
    void onAddICustomerComplete(Task<Void> task);
    void onFetchCustomerComplete(ArrayList<User> customers);

}