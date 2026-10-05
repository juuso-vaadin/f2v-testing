package com.example.employees;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * An employee of the company. Mutable, so that it can be edited in a form; {@link EmployeeService} hands out copies, so
 * edits only take effect once the copy is saved.
 */
public class Employee {

    private @Nullable Long id;
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String email = "";
    private @Nullable LocalDate dateOfBirth;
    private @Nullable String department;
    private @Nullable String jobTitle;
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    private LocalDate startDate = LocalDate.now();
    private int assignedTasks;

    public Employee() {
    }

    Employee(Employee other) {
        id = other.id;
        firstName = other.firstName;
        lastName = other.lastName;
        phone = other.phone;
        email = other.email;
        dateOfBirth = other.dateOfBirth;
        department = other.department;
        jobTitle = other.jobTitle;
        status = other.status;
        startDate = other.startDate;
        assignedTasks = other.assignedTasks;
    }

    /** The identifier, or {@code null} for an employee that has not been saved yet. */
    public @Nullable Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return (firstName + " " + lastName).strip();
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

    public @Nullable LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@Nullable LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public @Nullable String getDepartment() {
        return department;
    }

    public void setDepartment(@Nullable String department) {
        this.department = department;
    }

    public @Nullable String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(@Nullable String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    /** The date the employee started working for the company. */
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /** The number of tasks currently assigned to the employee. */
    public int getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(int assignedTasks) {
        this.assignedTasks = assignedTasks;
    }
}
