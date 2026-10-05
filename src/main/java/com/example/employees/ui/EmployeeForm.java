package com.example.employees.ui;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

import com.example.employees.Employee;
import com.example.employees.EmployeeService;
import com.example.employees.EmployeeStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

class EmployeeForm extends Composite<VerticalLayout> {

    private static final LocalDate EARLIEST_BIRTH_DATE = LocalDate.of(1960, 1, 1);
    private static final LocalDate LATEST_BIRTH_DATE = LocalDate.of(2000, 12, 31);

    private final Employee employee;
    private final EmployeeService employees;
    private final Runnable onSaved;
    private final Runnable onRemoved;
    private final Binder<Employee> binder = new Binder<>(Employee.class);

    EmployeeForm(Employee employee, EmployeeService employees, Runnable onSaved, Runnable onRemoved) {
        this.employee = employee;
        this.employees = employees;
        this.onSaved = onSaved;
        this.onRemoved = onRemoved;

        var content = new VerticalLayout(createHeader(), createPersonalDetails(), createRole());
        content.addClassName("employee-form-content");
        content.setPadding(false);
        content.setSpacing("32px");

        var layout = getContent();
        layout.addClassName("employee-form");
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(content, createFooter());
        layout.expand(content);

        binder.readBean(employee);
    }

    private Component createHeader() {
        var name = new H2(employee.getName());
        name.addClassName("employee-form-title");
        var service = new Span(formatService(employee.getStartDate()));
        service.addClassName("employee-form-caption");

        var heading = new VerticalLayout(name, service);
        heading.setPadding(false);
        heading.setSpacing("var(--vaadin-gap-xs)");

        var header = new HorizontalLayout(heading, new Badge(employee.getAssignedTasks() + " assigned tasks"));
        header.setWidthFull();
        header.setSpacing("var(--vaadin-gap-xs)");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(heading);
        return header;
    }

    private Component createPersonalDetails() {
        var firstName = new TextField("First name");
        var lastName = new TextField("Last name");
        var phone = new TextField("Phone");
        var email = new TextField("Email");
        var dateOfBirth = new DatePicker("Date of Birth");
        dateOfBirth.setLocale(Locale.UK);
        dateOfBirth.setMin(EARLIEST_BIRTH_DATE);
        dateOfBirth.setMax(LATEST_BIRTH_DATE);
        dateOfBirth.setWidth("264px");

        binder.forField(firstName).bind(Employee::getFirstName, Employee::setFirstName);
        binder.forField(lastName).bind(Employee::getLastName, Employee::setLastName);
        binder.forField(phone).bind(Employee::getPhone, Employee::setPhone);
        binder.forField(email).bind(Employee::getEmail, Employee::setEmail);
        binder.forField(dateOfBirth).bind(Employee::getDateOfBirth, Employee::setDateOfBirth);

        var form = createFormLayout();
        form.add(firstName, 1);
        form.add(lastName, 1);
        form.add(phone, 2);
        form.add(email, 2);
        form.add(dateOfBirth, 2);
        return form;
    }

    private Component createRole() {
        var heading = new H3("Role");
        heading.addClassName("employee-form-section-title");

        var department = new ComboBox<String>("Department");
        department.setItems(employees.departments());
        var jobTitle = new ComboBox<String>("Job title");
        jobTitle.setItems(employees.jobTitles());
        var status = new RadioButtonGroup<EmployeeStatus>("Status");
        status.setItems(EmployeeStatus.values());
        status.setItemLabelGenerator(EmployeeStatus::getLabel);
        status.addThemeVariants(RadioGroupVariant.AURA_HORIZONTAL);

        binder.forField(department).bind(Employee::getDepartment, Employee::setDepartment);
        binder.forField(jobTitle).bind(Employee::getJobTitle, Employee::setJobTitle);
        binder.forField(status).bind(Employee::getStatus, Employee::setStatus);

        var form = createFormLayout();
        form.add(department, 1);
        form.add(jobTitle, 1);
        form.add(status, 2);

        var role = new VerticalLayout(heading, form);
        role.setPadding(false);
        role.setSpacing("var(--vaadin-gap-l)");
        return role;
    }

    private static FormLayout createFormLayout() {
        var form = new FormLayout();
        form.setResponsiveSteps(new ResponsiveStep("0", 2));
        form.setColumnSpacing("var(--vaadin-gap-l)");
        form.setRowSpacing("var(--vaadin-gap-l)");
        form.setWidthFull();
        return form;
    }

    private Component createFooter() {
        var remove = new Button("Remove", event -> confirmRemove());
        remove.addThemeVariants(ButtonVariant.ERROR);

        var cancel = new Button("Cancel", event -> binder.readBean(employee));
        var save = new Button("Save changes", event -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);

        var actions = new HorizontalLayout(cancel, save);
        actions.setSpacing("var(--vaadin-gap-m)");

        var footer = new HorizontalLayout(remove, actions);
        footer.addClassName("employee-form-footer");
        footer.setWidthFull();
        footer.setPadding(false);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return footer;
    }

    private void save() {
        if (binder.writeBeanIfValid(employee)) {
            onSaved.run();
        }
    }

    private void confirmRemove() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Remove " + employee.getName() + "?");
        dialog.setText("The employee will be permanently removed from the list.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            employees.remove(employee);
            onRemoved.run();
        });
        dialog.open();
    }

    private static String formatService(LocalDate startDate) {
        var service = Period.between(startDate, LocalDate.now());
        return plural(service.getYears(), "year") + " " + plural(service.getMonths(), "month") + " in service";
    }

    private static String plural(int amount, String unit) {
        return amount + " " + unit + (amount == 1 ? "" : "s");
    }
}
