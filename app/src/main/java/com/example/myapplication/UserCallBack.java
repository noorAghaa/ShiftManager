package com.example.myapplication;

import com.google.android.gms.tasks.Task;

public interface UserCallBack {
    void onUserFetchDataComplete(User user);
    void onUpdateComplete(Task<Void> task);

}