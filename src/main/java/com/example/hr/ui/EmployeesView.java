package com.example.hr.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route("employees")
@PageTitle("Employees")
@StyleSheet("employees-view.css")
class EmployeesView extends Div {

    enum Status {
        ACTIVE("Active"), ON_LEAVE("On leave"), INACTIVE("Inactive");

        final String label;

        Status(String label) {
            this.label = label;
        }
    }

    record Employee(String firstName, String lastName, String department, String jobTitle, Status status,
            String phone, LocalDate birthDate, int tasks, String tenure) {
        String name() {
            return firstName + " " + lastName;
        }

        String email() {
            return (firstName + "." + lastName).toLowerCase() + "@example.com";
        }
    }

    private static final List<String> DEPARTMENTS = List.of("Deliveries", "Finance", "HR", "Marketing");
    private static final List<String> JOB_TITLES = List.of("Accountant", "CFO", "Content strategist",
            "Finance manager", "Logistics handler", "Marketing lead", "Recruiter");

    private final List<Employee> employees = List.of(
            employee("Henry", "Thompson", "Marketing", "Content strategist", Status.ACTIVE),
            employee("Liam", "Johnson", "Deliveries", "Logistics handler", Status.ACTIVE),
            employee("Justin", "Smith", "Deliveries", "Logistics handler", Status.ACTIVE),
            employee("Jordan", "Brown", "Finance", "Accountant", Status.ACTIVE),
            employee("Jacob", "Williams", "HR", "Recruiter", Status.ACTIVE),
            employee("Robert", "Davis", "Deliveries", "Logistics handler", Status.INACTIVE),
            employee("Maya", "Garcia", "Deliveries", "Logistics handler", Status.ON_LEAVE),
            employee("Andrew", "Martinez", "Deliveries", "Logistics handler", Status.ACTIVE),
            employee("Samantha", "Rodriguez", "Marketing", "Marketing lead", Status.ACTIVE),
            employee("Angel", "Wilson", "Deliveries", "Logistics handler", Status.ACTIVE),
            employee("Olivia", "Anderson", "Finance", "CFO", Status.ACTIVE),
            employee("Noah", "Taylor", "Deliveries", "Logistics handler", Status.INACTIVE),
            employee("Emma", "Moore", "Finance", "Finance manager", Status.ACTIVE),
            employee("Lucas", "Clark", "Deliveries", "Logistics handler", Status.ACTIVE));

    private final Grid<Employee> grid = new Grid<>();
    private final MasterDetailLayout layout = new MasterDetailLayout();

    private final H2 detailName = new H2();
    private final Span detailTenure = new Span();
    private final Badge detailTasks = new Badge();
    private final TextField firstName = new TextField("First name");
    private final TextField lastName = new TextField("Last name");
    private final TextField phone = new TextField("Phone");
    private final EmailField email = new EmailField("Email");
    private final DatePicker birthDate = new DatePicker("Date of Birth");
    private final Select<String> department = new Select<>();
    private final Select<String> jobTitle = new Select<>();
    private final RadioButtonGroup<Status> status = new RadioButtonGroup<>("Status");
    private final Div detail = new Div();

    EmployeesView() {
        addClassName("employees-view");
        add(createTabs(), createToolbar(), createContent());
        grid.select(employees.get(1));
    }

    private static Employee employee(String first, String last, String department, String jobTitle, Status status) {
        int seed = (first + last).length();
        return new Employee(first, last, department, jobTitle, status, "+91 555 1213 456",
                LocalDate.of(1972, 6, 22).plusDays(seed * 37L), 13, "4 years 3 months in service");
    }

    private Component createTabs() {
        var tabs = new Tabs(new Tab("Employee List"), new Tab("Organization Chart"));
        tabs.addClassName("employees-tabs");
        return tabs;
    }

    private Component createToolbar() {
        var kpis = new Div(kpi("Total employees", "246", "+14", " this year"), new Div(),
                kpi("Logistics employees", "192", "78%", " of all"));
        kpis.addClassName("employees-kpis");
        kpis.getChildren().skip(1).findFirst().ifPresent(d -> d.addClassName("kpi-divider"));

        var export = new Button("Export", VaadinIcon.UPLOAD_ALT.create());
        var add = new Button("Add employee", VaadinIcon.PLUS.create());
        add.addThemeVariants(ButtonVariant.PRIMARY);
        var actions = new Div(export, add);
        actions.addClassName("employees-actions");

        var toolbar = new Div(kpis, actions);
        toolbar.addClassName("employees-toolbar");
        return toolbar;
    }

    private static Component kpi(String label, String value, String note, String noteRest) {
        var l = new Span(label);
        l.addClassName("kpi-label");
        var v = new Span(value);
        v.addClassName("kpi-value");
        var n = new Span(note);
        n.addClassName("kpi-note");
        var kpi = new Div(l, v, new Div(n, new Span(noteRest)));
        kpi.addClassName("kpi");
        return kpi;
    }

    private Component createContent() {
        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.addColumn(Employee::name).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Employee::department).setHeader("Department").setAutoWidth(true);
        grid.addColumn(Employee::jobTitle).setHeader("Job title").setAutoWidth(true);
        grid.addComponentColumn(e -> statusBadge(e.status())).setHeader("Status").setAutoWidth(true);
        grid.addColumn(e -> e.email()).setHeader("Email").setAutoWidth(true);
        grid.setItems(employees);
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(e -> showDetail(e.getValue()));

        layout.setMaster(grid);
        layout.setMasterSize("480px");
        layout.setDetailSize("540px");
        layout.setExpandMaster(true);
        layout.addDetailEscapePressListener(e -> grid.deselectAll());
        layout.addBackdropClickListener(e -> grid.deselectAll());
        layout.setSizeFull();
        layout.addClassName("employees-layout");

        createDetail();
        return layout;
    }

    private static Badge statusBadge(Status status) {
        var badge = new Badge(status.label);
        switch (status) {
        case ACTIVE -> badge.addThemeVariants(BadgeVariant.SUCCESS);
        case INACTIVE -> badge.addThemeVariants(BadgeVariant.ERROR);
        case ON_LEAVE -> badge.addThemeVariants(BadgeVariant.CONTRAST);
        }
        return badge;
    }

    private void createDetail() {
        detailTasks.addThemeVariants(BadgeVariant.SUCCESS);
        var title = new Div(detailName, detailTenure);
        title.addClassName("detail-title");
        var header = new Div(title, detailTasks);
        header.addClassName("detail-header");

        firstName.setWidthFull();
        lastName.setWidthFull();
        var names = new Div(firstName, lastName);
        names.addClassName("detail-row");
        phone.setWidthFull();
        email.setWidthFull();
        birthDate.setLocale(java.util.Locale.UK);

        department.setLabel("Department");
        department.setItems(DEPARTMENTS);
        department.setWidthFull();
        jobTitle.setLabel("Job title");
        jobTitle.setItems(JOB_TITLES);
        jobTitle.setWidthFull();
        var role = new Div(department, jobTitle);
        role.addClassName("detail-row");

        status.setItems(Status.values());
        status.setItemLabelGenerator(s -> s.label);

        var remove = new Button("Remove");
        remove.addThemeVariants(ButtonVariant.ERROR);
        var cancel = new Button("Cancel", e -> grid.deselectAll());
        var save = new Button("Save changes");
        save.addThemeVariants(ButtonVariant.PRIMARY);
        var right = new Div(cancel, save);
        right.addClassName("detail-actions-end");
        var footer = new Div(remove, right);
        footer.addClassName("detail-footer");

        var body = new Div(header, names, phone, email, birthDate, new H3("Role"), role, status);
        body.addClassName("detail-body");
        detail.add(body, footer);
        detail.addClassName("employee-detail");
    }

    private void showDetail(Employee e) {
        if (e == null) {
            layout.setDetail(null);
            return;
        }
        detailName.setText(e.name());
        detailTenure.setText(e.tenure());
        detailTasks.setText(e.tasks() + " assigned tasks");
        firstName.setValue(e.firstName());
        lastName.setValue(e.lastName());
        phone.setValue(e.phone());
        email.setValue(e.email());
        birthDate.setValue(e.birthDate());
        department.setValue(e.department());
        jobTitle.setValue(e.jobTitle());
        status.setValue(e.status());
        layout.setDetail(detail);
    }
}
