package com.example.employees;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/** Sample data matching the employee list in the design. */
@Service
public class EmployeeService {

    private static final String[] NAMES = { "Henry Thompson", "Liam Johnson", "Justin Smith", "Jordan Brown",
            "Jacob Williams", "Robert Davis", "Maya Garcia", "Andrew Martinez", "Samantha Rodriguez", "Angel Wilson" };
    private static final LocalDate[] START_DATES = { LocalDate.of(2021, 3, 12), LocalDate.of(2022, 4, 5),
            LocalDate.of(2020, 6, 30), LocalDate.of(2023, 8, 15), LocalDate.of(2024, 1, 1), LocalDate.of(2020, 2, 29),
            LocalDate.of(2021, 11, 11), LocalDate.of(2022, 12, 25), LocalDate.of(2023, 7, 4),
            LocalDate.of(2021, 9, 9) };
    private static final int ROWS = 28;

    private final List<Employee> employees = new ArrayList<>();

    public EmployeeService() {
        for (var row = 0; row < ROWS; row++) {
            employees.add(employee(row));
        }
    }

    public List<Employee> list() {
        return employees;
    }

    public List<String> departments() {
        return employees.stream().map(Employee::getDepartment).distinct().sorted().toList();
    }

    public List<String> jobTitles() {
        return employees.stream().map(Employee::getJobTitle).distinct().sorted().toList();
    }

    public void remove(Employee employee) {
        employees.remove(employee);
    }

    private static Employee employee(int row) {
        var index = row % NAMES.length;
        var parts = NAMES[index].split(" ");
        var isLiam = index == 1;
        var department = switch (row) {
        case 0, 8, 15 -> "Marketing";
        case 3, 11, 18 -> "Finance";
        case 4 -> "HR";
        default -> "Deliveries";
        };
        var jobTitle = switch (row) {
        case 0, 15 -> "Content strategist";
        case 3 -> "Accountant";
        case 4 -> "Recruiter";
        case 8 -> "Marketing lead";
        case 11 -> "CFO";
        case 18 -> "Finance manager";
        default -> "Logistics handler";
        };
        var status = switch (row) {
        case 5, 13 -> EmployeeStatus.INACTIVE;
        case 6 -> EmployeeStatus.ON_LEAVE;
        default -> EmployeeStatus.ACTIVE;
        };
        // Contact details and task counts are not shown for other rows, so they are made up
        return new Employee(row, parts[0], parts[1], isLiam ? "+91 555 1213 456" : "+91 555 1213 " + (100 + row * 7),
                (parts[0] + "." + parts[1] + "@example.com").toLowerCase(),
                isLiam ? LocalDate.of(1972, 6, 22) : LocalDate.of(1960 + (row * 7) % 41, 1 + row % 12, 1 + row % 28),
                department, jobTitle, status, START_DATES[index], isLiam ? 13 : 1 + (row * 5) % 17);
    }
}
