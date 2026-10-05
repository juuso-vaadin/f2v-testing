package com.example.employees;

/**
 * Headcount figures shown above the employee list.
 *
 * @param total          the number of employees
 * @param joinedThisYear the number of employees who started during the current calendar year
 * @param logistics      the number of employees in the {@link EmployeeService#LOGISTICS_DEPARTMENT logistics department}
 */
public record EmployeeFigures(int total, int joinedThisYear, int logistics) {

    /** The share of logistics employees of all employees, as a whole percentage. */
    public int logisticsPercentage() {
        return total == 0 ? 0 : Math.round(100f * logistics / total);
    }
}
