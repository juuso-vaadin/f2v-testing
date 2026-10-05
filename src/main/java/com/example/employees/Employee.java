package com.example.employees;

import java.time.LocalDate;

public class Employee {

    private final int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String department;
    private String jobTitle;
    private EmployeeStatus status;
    private final LocalDate startDate;
    private final int assignedTasks;

    public Employee(int id, String firstName, String lastName, String phone, String email, LocalDate dateOfBirth,
            String department, String jobTitle, EmployeeStatus status, LocalDate startDate, int assignedTasks) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.jobTitle = jobTitle;
        this.status = status;
        this.startDate = startDate;
        this.assignedTasks = assignedTasks;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getAssignedTasks() {
        return assignedTasks;
    }
}
