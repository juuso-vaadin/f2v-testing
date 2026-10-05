package com.example.employees;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serves employees from in-memory sample data.
 */
@Service
public class EmployeeService {

    private static final List<String> DEPARTMENTS = List.of("Deliveries", "Finance", "HR", "Marketing");

    private final List<Employee> employees = createSampleEmployees();

    public synchronized List<Employee> list() {
        return List.copyOf(employees);
    }

    public List<String> departments() {
        return DEPARTMENTS;
    }

    public synchronized List<String> jobTitles() {
        return employees.stream().map(Employee::jobTitle).distinct().sorted().toList();
    }

    /** Replaces the employee with the same id. */
    public synchronized void save(Employee employee) {
        employees.replaceAll(existing -> existing.id() == employee.id() ? employee : existing);
    }

    public synchronized void delete(Employee employee) {
        employees.removeIf(existing -> existing.id() == employee.id());
    }

    public int totalEmployees() {
        return 246;
    }

    public int hiredThisYear() {
        return 14;
    }

    public int logisticsEmployees() {
        return 192;
    }

    private static List<Employee> createSampleEmployees() {
        var employees = new ArrayList<Employee>();
        String[][] rows = {
                { "Henry", "Thompson", "Marketing", "Content strategist", "ACTIVE", "2021-03-12" },
                { "Liam", "Johnson", "Deliveries", "Logistics handler", "ACTIVE", "2022-04-05" },
                { "Justin", "Smith", "Deliveries", "Logistics handler", "ACTIVE", "2020-06-30" },
                { "Jordan", "Brown", "Finance", "Accountant", "ACTIVE", "2023-08-15" },
                { "Jacob", "Williams", "HR", "Recruiter", "ACTIVE", "2024-01-01" },
                { "Robert", "Davis", "Deliveries", "Logistics handler", "INACTIVE", "2020-02-29" },
                { "Maya", "Garcia", "Deliveries", "Logistics handler", "ON_LEAVE", "2021-11-11" },
                { "Andrew", "Martinez", "Deliveries", "Logistics handler", "ACTIVE", "2022-12-25" },
                { "Samantha", "Rodriguez", "Marketing", "Marketing lead", "ACTIVE", "2023-07-04" },
                { "Angel", "Wilson", "Deliveries", "Logistics handler", "ACTIVE", "2021-09-09" },
                { "Henry", "Thompson", "Deliveries", "Logistics handler", "ACTIVE", "2021-03-12" },
                { "Liam", "Johnson", "Finance", "CFO", "ACTIVE", "2022-04-05" },
                { "Justin", "Smith", "Deliveries", "Logistics handler", "ACTIVE", "2020-06-30" },
                { "Jordan", "Brown", "Deliveries", "Logistics handler", "INACTIVE", "2023-08-15" },
                { "Jacob", "Williams", "Deliveries", "Logistics handler", "ACTIVE", "2024-01-01" },
                { "Robert", "Davis", "Marketing", "Content strategist", "ACTIVE", "2020-02-29" },
                { "Maya", "Garcia", "Deliveries", "Logistics handler", "ACTIVE", "2021-11-11" },
                { "Andrew", "Martinez", "Deliveries", "Logistics handler", "ACTIVE", "2022-12-25" },
                { "Samantha", "Rodriguez", "Finance", "Finance manager", "ACTIVE", "2023-07-04" },
                { "Angel", "Wilson", "Deliveries", "Logistics handler", "ACTIVE", "2021-09-09" },
                { "Henry", "Thompson", "Deliveries", "Logistics handler", "ACTIVE", "2021-03-12" },
                { "Liam", "Johnson", "Deliveries", "Logistics handler", "ACTIVE", "2022-04-05" },
                { "Justin", "Smith", "Deliveries", "Logistics handler", "ACTIVE", "2020-06-30" },
                { "Jordan", "Brown", "Deliveries", "Logistics handler", "ACTIVE", "2023-08-15" },
                { "Jacob", "Williams", "Deliveries", "Logistics handler", "ACTIVE", "2024-01-01" },
                { "Robert", "Davis", "Deliveries", "Logistics handler", "ACTIVE", "2020-02-29" },
                { "Maya", "Garcia", "Deliveries", "Logistics handler", "ACTIVE", "2021-11-11" },
                { "Andrew", "Martinez", "Deliveries", "Logistics handler", "ACTIVE", "2022-12-25" },
                { "Samantha", "Rodriguez", "Deliveries", "Logistics handler", "ACTIVE", "2023-07-04" } };
        for (int i = 0; i < rows.length; i++) {
            var row = rows[i];
            var email = row[0].toLowerCase(Locale.ROOT) + "." + row[1].toLowerCase(Locale.ROOT) + "@example.com";
            var phone = "+91 555 %04d %03d".formatted(1200 + i * 7, 400 + i * 13);
            var dateOfBirth = LocalDate.of(1965 + (i * 3) % 35, 1 + i % 12, 1 + (i * 5) % 28);
            employees.add(new Employee(i + 1, row[0], row[1], phone, email, dateOfBirth, row[2], row[3],
                    EmployeeStatus.valueOf(row[4]), LocalDate.parse(row[5]), 3 + (i * 7) % 15));
        }
        // The employee selected in the design
        employees.set(1, new Employee(2, "Liam", "Johnson", "+91 555 1213 456", "liam.johnson@example.com",
                LocalDate.of(1972, 6, 22), "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 4, 5), 13));
        return employees;
    }
}
