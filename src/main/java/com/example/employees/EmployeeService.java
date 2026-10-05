package com.example.employees;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Serves employees from an in-memory sample data set. Every employee handed out is a copy, so callers can edit it
 * freely and decide whether to {@link #save} it.
 */
// TODO Replace the sample data with a real data source
@Service
public class EmployeeService {

    /** The department whose employees are counted as logistics employees. */
    public static final String LOGISTICS_DEPARTMENT = "Deliveries";

    private static final List<String> DEPARTMENTS = List.of("Deliveries", "Finance", "HR", "Marketing");
    private static final List<String> JOB_TITLES = List.of("Accountant", "CFO", "Content strategist",
            "Finance manager", "Logistics handler", "Marketing lead", "Recruiter");

    private final List<Employee> employees = new ArrayList<>();
    private long nextId = 1;

    EmployeeService() {
        add("Henry", "Thompson", "Marketing", "Content strategist", EmployeeStatus.ACTIVE,
                LocalDate.of(2021, 3, 12), LocalDate.of(1985, 2, 14), 4);
        add("Liam", "Johnson", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 4, 5), LocalDate.of(1972, 6, 22), 13).setPhone("+91 555 1213 456");
        add("Justin", "Smith", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2020, 6, 30), LocalDate.of(1990, 9, 3), 7);
        add("Jordan", "Brown", "Finance", "Accountant", EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 8, 15), LocalDate.of(1988, 11, 27), 2);
        add("Jacob", "Williams", "HR", "Recruiter", EmployeeStatus.ACTIVE,
                LocalDate.of(2024, 1, 1), LocalDate.of(1993, 4, 8), 5);
        add("Robert", "Davis", "Deliveries", "Logistics handler", EmployeeStatus.INACTIVE,
                LocalDate.of(2020, 2, 29), LocalDate.of(1979, 1, 19), 0);
        add("Maya", "Garcia", "Deliveries", "Logistics handler", EmployeeStatus.ON_LEAVE,
                LocalDate.of(2021, 11, 11), LocalDate.of(1995, 7, 30), 0);
        add("Andrew", "Martinez", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 12, 25), LocalDate.of(1983, 3, 5), 9);
        add("Samantha", "Rodriguez", "Marketing", "Marketing lead", EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 7, 4), LocalDate.of(1981, 10, 12), 6);
        add("Angel", "Wilson", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2021, 9, 9), LocalDate.of(1997, 5, 21), 11);
        add("Olivia", "Anderson", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2026, 3, 2), LocalDate.of(1998, 8, 16), 3);
        add("Ethan", "Clark", "Finance", "CFO", EmployeeStatus.ACTIVE,
                LocalDate.of(2019, 5, 20), LocalDate.of(1970, 12, 1), 8);
        add("Sophia", "Lewis", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2020, 10, 14), LocalDate.of(1991, 2, 9), 10);
        add("Lucas", "Walker", "Deliveries", "Logistics handler", EmployeeStatus.INACTIVE,
                LocalDate.of(2023, 8, 15), LocalDate.of(1987, 6, 25), 0);
        add("Mia", "Hall", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2024, 1, 15), LocalDate.of(1996, 1, 4), 4);
        add("Noah", "Allen", "Marketing", "Content strategist", EmployeeStatus.ACTIVE,
                LocalDate.of(2026, 5, 18), LocalDate.of(1999, 3, 17), 2);
        add("Ava", "Young", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 2, 7), LocalDate.of(1984, 9, 29), 12);
        add("James", "King", "Deliveries", "Logistics handler", EmployeeStatus.ON_LEAVE,
                LocalDate.of(2021, 6, 1), LocalDate.of(1976, 4, 11), 0);
        add("Isabella", "Wright", "Finance", "Finance manager", EmployeeStatus.ACTIVE,
                LocalDate.of(2020, 9, 28), LocalDate.of(1982, 7, 7), 5);
        add("Benjamin", "Scott", "Deliveries", "Logistics handler", EmployeeStatus.ACTIVE,
                LocalDate.of(2026, 8, 24), LocalDate.of(2000, 10, 30), 1);
    }

    /** Lists all employees in the order they were added. */
    public synchronized List<Employee> list() {
        return employees.stream().map(Employee::new).toList();
    }

    public synchronized Optional<Employee> get(long id) {
        return employees.stream().filter(employee -> employee.getId() == id).findFirst().map(Employee::new);
    }

    /**
     * Saves the employee, adding it if it is new.
     *
     * @return a copy of the saved employee, which has an identifier
     */
    public synchronized Employee save(Employee employee) {
        var saved = new Employee(employee);
        var id = saved.getId();
        if (id == null) {
            saved.setId(nextId++);
            employees.add(saved);
        } else {
            employees.replaceAll(existing -> id.equals(existing.getId()) ? saved : existing);
        }
        return new Employee(saved);
    }

    public synchronized void delete(long id) {
        employees.removeIf(employee -> employee.getId() == id);
    }

    public List<String> departments() {
        return DEPARTMENTS;
    }

    public List<String> jobTitles() {
        return JOB_TITLES;
    }

    /** Headcount figures over all employees, including inactive ones. */
    public synchronized EmployeeFigures figures() {
        var year = LocalDate.now().getYear();
        var joinedThisYear = employees.stream().filter(e -> e.getStartDate().getYear() == year).count();
        var logistics = employees.stream().filter(e -> LOGISTICS_DEPARTMENT.equals(e.getDepartment())).count();
        return new EmployeeFigures(employees.size(), (int) joinedThisYear, (int) logistics);
    }

    private Employee add(String firstName, String lastName, String department, String jobTitle,
            EmployeeStatus status, LocalDate startDate, LocalDate dateOfBirth, int assignedTasks) {
        var employee = new Employee();
        employee.setId(nextId++);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail((firstName + "." + lastName).toLowerCase(Locale.ROOT) + "@example.com");
        employee.setPhone("+91 555 %04d %03d".formatted(1000 + employees.size() * 37, 100 + employees.size() * 13));
        employee.setDateOfBirth(dateOfBirth);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setStatus(status);
        employee.setStartDate(startDate);
        employee.setAssignedTasks(assignedTasks);
        employees.add(employee);
        return employee;
    }
}
