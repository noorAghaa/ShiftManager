package com.example.myapplication.Controller;

import java.util.List;

public interface EmployeeIdsCallback {
    void onEmployeeIdsFetched(List<String> employeeIds);
    void onError(Exception e);
}
