package com.example.myapplication.Controller;

import com.example.myapplication.Model.User;
import java.util.List;

public interface UsersCallback {
    void onUsersFetchDataComplete(List<User> users);
    void onError(Exception e);
}

