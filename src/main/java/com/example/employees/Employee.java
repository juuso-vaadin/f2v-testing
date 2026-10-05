package com.example.employees;

import java.time.LocalDate;

public record Employee(long id, String firstName, String lastName, String phone, String email,
        LocalDate dateOfBirth, String department, String jobTitle, EmployeeStatus status, LocalDate startDate,
        int assignedTasks) {

    public String name() {
        return firstName + " " + lastName;
    }
}
