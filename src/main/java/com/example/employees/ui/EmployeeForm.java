package com.example.employees.ui;

import com.example.employees.Employee;
import com.example.employees.EmployeeStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Consumer;

/**
 * Edits one employee. Changes reach the employee only when the user saves; the owner decides what saving, removing
 * and cancelling do through the callbacks given to the constructor.
 */
class EmployeeForm extends Composite<Div> {

    static final LocalDate EARLIEST_DATE_OF_BIRTH = LocalDate.of(1960, 1, 1);
    static final LocalDate LATEST_DATE_OF_BIRTH = LocalDate.of(2000, 12, 31);

    private final H2 title = new H2();
    private final Span serviceTime = new Span();
    private final Badge assignedTasks = new Badge();

    private final TextField firstName = new TextField("First name");
    private final TextField lastName = new TextField("Last name");
    private final TextField phone = new TextField("Phone");
    private final EmailField email = new EmailField("Email");
    private final DatePicker dateOfBirth = new DatePicker("Date of Birth");
    private final ComboBox<String> department = new ComboBox<>("Department");
    private final ComboBox<String> jobTitle = new ComboBox<>("Job title");
    private final RadioButtonGroup<EmployeeStatus> status = new RadioButtonGroup<>("Status");

    private final Button remove = new Button("Remove");
    private final Binder<Employee> binder = new Binder<>();
    private Employee employee = new Employee();

    EmployeeForm(List<String> departments, List<String> jobTitles, Consumer<Employee> onSave,
            Consumer<Employee> onRemove, Runnable onCancel) {
        configureFields(departments, jobTitles);
        bindFields();

        remove.addThemeVariants(ButtonVariant.ERROR);
        remove.addClassName("employee-form-remove");
        remove.addClickListener(e -> confirmRemove(onRemove));

        var cancel = new Button("Cancel", e -> onCancel.run());

        var save = new Button("Save changes", e -> {
            if (binder.writeBeanIfValid(employee)) {
                onSave.accept(employee);
            }
        });
        save.addThemeVariants(ButtonVariant.PRIMARY);

        var actions = new Div(cancel, save);
        actions.addClassName("employee-form-actions");
        var footer = new Div(remove, actions);
        footer.addClassName("employee-form-footer");

        var content = new Div(createHeader(), createPersonalSection(), createRoleSection());
        content.addClassName("employee-form-content");

        getContent().addClassName("employee-form");
        getContent().add(content, footer);
    }

    /** Shows the given employee, discarding any unsaved changes to the previous one. */
    void setEmployee(Employee employee) {
        this.employee = employee;
        binder.readBean(employee);

        var isNew = employee.getId() == null;
        title.setText(isNew ? "New employee" : employee.getName());
        serviceTime.setText(isNew ? "Not yet in service" : formatServiceTime(employee.getStartDate()));
        serviceTime.setVisible(!isNew);
        var tasks = employee.getAssignedTasks();
        assignedTasks.setText(tasks + (tasks == 1 ? " assigned task" : " assigned tasks"));
        assignedTasks.setVisible(!isNew);
        remove.setVisible(!isNew);
    }

    void focus() {
        firstName.focus();
    }

    private Component createHeader() {
        serviceTime.addClassName("employee-form-service-time");
        assignedTasks.addClassName("employee-form-tasks");
        var titleBlock = new Div(title, serviceTime);
        titleBlock.addClassName("employee-form-title");
        var header = new Div(titleBlock, assignedTasks);
        header.addClassName("employee-form-header");
        return header;
    }

    private Component createPersonalSection() {
        var section = new Div(createRow(firstName, lastName), phone, email, dateOfBirth);
        section.addClassName("employee-form-section");
        return section;
    }

    private Component createRoleSection() {
        var section = new Div(new H3("Role"), createRow(department, jobTitle), status);
        section.addClassName("employee-form-section");
        return section;
    }

    private static Component createRow(Component... fields) {
        var row = new Div(fields);
        row.addClassName("employee-form-row");
        return row;
    }

    private void configureFields(List<String> departments, List<String> jobTitles) {
        for (var field : List.of(firstName, lastName, phone)) {
            field.setWidthFull();
        }
        email.setWidthFull();

        dateOfBirth.addClassName("employee-form-date-of-birth");
        dateOfBirth.setMin(EARLIEST_DATE_OF_BIRTH);
        dateOfBirth.setMax(LATEST_DATE_OF_BIRTH);
        dateOfBirth.setInitialPosition(LocalDate.of(1980, 1, 1));
        dateOfBirth.setI18n(new DatePicker.DatePickerI18n()
                .setDateFormat("dd/MM/yyyy")
                .setMinErrorMessage("Date of birth can't be earlier than 01/01/1960")
                .setMaxErrorMessage("Date of birth can't be later than 31/12/2000"));

        department.setItems(departments);
        department.setWidthFull();
        jobTitle.setItems(jobTitles);
        jobTitle.setWidthFull();

        status.addThemeVariants(RadioGroupVariant.AURA_HORIZONTAL);
        status.setItems(EmployeeStatus.values());
        status.setItemLabelGenerator(EmployeeStatus::getLabel);
    }

    private void bindFields() {
        binder.forField(firstName).asRequired("Enter a first name")
                .bind(Employee::getFirstName, Employee::setFirstName);
        binder.forField(lastName).asRequired("Enter a last name")
                .bind(Employee::getLastName, Employee::setLastName);
        binder.forField(phone).bind(Employee::getPhone, Employee::setPhone);
        binder.forField(email).withValidator(new EmailValidator("Enter a valid email address", true))
                .bind(Employee::getEmail, Employee::setEmail);
        binder.forField(dateOfBirth).bind(Employee::getDateOfBirth, Employee::setDateOfBirth);
        binder.forField(department).asRequired("Select a department")
                .bind(Employee::getDepartment, Employee::setDepartment);
        binder.forField(jobTitle).asRequired("Select a job title")
                .bind(Employee::getJobTitle, Employee::setJobTitle);
        binder.forField(status).asRequired("Select a status")
                .bind(Employee::getStatus, Employee::setStatus);
    }

    private void confirmRemove(Consumer<Employee> onRemove) {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Remove " + employee.getName() + "?");
        dialog.setText("The employee is removed permanently. This can't be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> onRemove.accept(employee));
        dialog.open();
    }

    /** Formats as in "4 years 3 months in service". */
    private static String formatServiceTime(LocalDate startDate) {
        var period = Period.between(startDate, LocalDate.now());
        if (period.isNegative()) {
            return "Starts " + EmployeesView.DATE_FORMAT.format(startDate);
        }
        var years = period.getYears();
        var months = period.getMonths();
        if (years == 0 && months == 0) {
            return "Less than a month in service";
        }
        var text = new StringBuilder();
        if (years > 0) {
            text.append(years).append(years == 1 ? " year" : " years");
        }
        if (months > 0) {
            text.append(text.isEmpty() ? "" : " ").append(months).append(months == 1 ? " month" : " months");
        }
        return text.append(" in service").toString();
    }
}
