package com.example.employees;

public enum EmployeeStatus {
    ACTIVE("Active"),
    ON_LEAVE("On leave"),
    INACTIVE("Inactive");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }

    /** Label used in status badges and the status radio group. */
    public String getLabel() {
        return label;
    }
}
