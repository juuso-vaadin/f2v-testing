package com.example.employees.ui;

import com.example.employees.Employee;
import com.example.employees.EmployeeService;
import com.example.employees.EmployeeStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route("employees")
@PageTitle("Employees")
@StyleSheet("employees-view.css")
class EmployeeListView extends VerticalLayout {

    private static final DateTimeFormatter START_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy",
            Locale.ENGLISH);

    private final EmployeeService employeeService;
    private final Grid<Employee> grid = new Grid<>();
    private final MasterDetailLayout masterDetail = new MasterDetailLayout();
    private final Component detail;

    private final H2 name = new H2();
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
    private Employee selectedEmployee;

    EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        addClassName("employees-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        detail = createDetail();
        masterDetail.addClassName("employees-view-content");
        masterDetail.setMaster(createGrid());
        masterDetail.setMasterSize("480px", true);
        masterDetail.setDetailSize("480px", true);
        masterDetail.setWidthFull();
        masterDetail.addDetailEscapePressListener(event -> grid.deselectAll());
        masterDetail.addBackdropClickListener(event -> grid.deselectAll());

        add(createTabs(), createToolbar(), masterDetail);
        setFlexGrow(1, masterDetail);

        employeeService.list().stream().filter(employee -> employee.id() == 2).findFirst().ifPresent(grid::select);
    }

    private Component createTabs() {
        var tabs = new Tabs(new Tab("Employee List"), new Tab("Organization Chart"));
        tabs.addClassName("employees-view-tabs");
        tabs.setWidthFull();
        return tabs;
    }

    private Component createToolbar() {
        var total = employeeService.totalEmployees();
        var logistics = employeeService.logisticsEmployees();

        var hired = new Span("+" + employeeService.hiredThisYear());
        hired.addClassName("employees-view-kpi-change-positive");
        var share = new Span(Math.round(100.0 * logistics / total) + "%");

        var kpis = new HorizontalLayout(
                createKpi("Total employees", total, hired, " this year"),
                createKpi("Logistics employees", logistics, share, " of all"));
        kpis.addClassName("employees-view-kpis");
        kpis.setSpacing(false);
        kpis.setAlignItems(FlexComponent.Alignment.CENTER);

        var export = new Button("Export", LumoIcon.UPLOAD.create());
        export.addClassName("aura-accent-neutral");
        var addEmployee = new Button("Add employee", LumoIcon.PLUS.create());
        addEmployee.addThemeVariants(ButtonVariant.PRIMARY);
        var actions = new HorizontalLayout(export, addEmployee);
        actions.addClassName("employees-view-actions");
        actions.setSpacing(false);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        var toolbar = new HorizontalLayout(kpis, actions);
        toolbar.addClassName("employees-view-toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private Component createKpi(String label, int value, Span change, String changeSuffix) {
        var labelText = new Span(label);
        labelText.addClassName("employees-view-kpi-label");
        var valueText = new Span(String.valueOf(value));
        valueText.addClassName("employees-view-kpi-value");
        var changeText = new Span(change, new Span(changeSuffix));
        changeText.addClassName("employees-view-kpi-change");

        var kpi = new VerticalLayout(labelText, valueText, changeText);
        kpi.addClassName("employees-view-kpi");
        kpi.setPadding(false);
        kpi.setSpacing(false);
        kpi.setWidth(null);
        return kpi;
    }

    private Component createGrid() {
        grid.addClassName("employees-view-grid");
        grid.addColumn(Employee::name).setHeader("Name").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Employee::department).setHeader("Department").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Employee::jobTitle).setHeader("Job title").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(employee -> createStatusBadge(employee.status())))
                .setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(employee -> START_DATE_FORMAT.format(employee.startDate())).setHeader("Start date")
                .setAutoWidth(true).setFlexGrow(0);
        grid.setItems(employeeService.list());
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> select(event.getValue()));
        return grid;
    }

    private Component createDetail() {
        name.addClassName("employees-view-name");
        serviceTime.addClassName("employees-view-service-time");
        var titleBlock = new VerticalLayout(name, serviceTime);
        titleBlock.addClassName("employees-view-title-block");
        titleBlock.setPadding(false);
        titleBlock.setSpacing(false);
        var header = new HorizontalLayout(titleBlock, assignedTasks);
        header.addClassName("employees-view-form-header");
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, titleBlock);

        // Only dates from 1.1.1960 to 31.12.2000 are allowed
        dateOfBirth.setMin(LocalDate.of(1960, 1, 1));
        dateOfBirth.setMax(LocalDate.of(2000, 12, 31));
        dateOfBirth.setI18n(new DatePicker.DatePickerI18n().setDateFormat("dd/MM/yyyy"));
        dateOfBirth.setWidth("264px");
        phone.setWidthFull();
        email.setWidthFull();

        var personal = new VerticalLayout(createRow(firstName, lastName), phone, email, dateOfBirth);
        personal.addClassName("employees-view-form-section");
        personal.setPadding(false);
        personal.setSpacing(false);

        department.setItems(employeeService.departments());
        jobTitle.setItems(employeeService.jobTitles());
        status.setItems(EmployeeStatus.values());
        status.setItemLabelGenerator(EmployeeStatus::getLabel);
        status.addThemeVariants(RadioGroupVariant.AURA_HORIZONTAL);

        var roleTitle = new H3("Role");
        roleTitle.addClassName("employees-view-section-title");
        var role = new VerticalLayout(roleTitle, createRow(department, jobTitle), status);
        role.addClassName("employees-view-form-section");
        role.setPadding(false);
        role.setSpacing(false);

        var content = new VerticalLayout(header, personal, role);
        content.addClassName("employees-view-form-content");
        content.setPadding(false);
        content.setSpacing(false);

        var scroller = new Scroller(content, Scroller.ScrollDirection.VERTICAL);
        scroller.addClassName("employees-view-form-scroller");
        scroller.setWidthFull();

        var remove = new Button("Remove", event -> confirmRemove());
        remove.addClassName("employees-view-remove");
        remove.addThemeVariants(ButtonVariant.ERROR);
        var cancel = new Button("Cancel", event -> showEmployee(selectedEmployee));
        cancel.addClassName("aura-accent-neutral");
        var save = new Button("Save changes", event -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);
        var actions = new HorizontalLayout(cancel, save);
        actions.addClassName("employees-view-form-actions");
        actions.setSpacing(false);

        var footer = new HorizontalLayout(remove, actions);
        footer.addClassName("employees-view-form-footer");
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        var form = new VerticalLayout(scroller, footer);
        form.addClassName("employees-view-form");
        form.setPadding(false);
        form.setSpacing(false);
        form.setSizeFull();
        form.setFlexGrow(1, scroller);

        var wrapper = new VerticalLayout(form);
        wrapper.addClassName("employees-view-detail");
        wrapper.setPadding(false);
        wrapper.setSizeFull();
        return wrapper;
    }

    private static HorizontalLayout createRow(Component... fields) {
        var row = new HorizontalLayout(fields);
        row.addClassName("employees-view-form-row");
        row.setSpacing(false);
        row.setWidthFull();
        for (var field : fields) {
            row.setFlexGrow(1, field);
        }
        return row;
    }

    private void select(Employee employee) {
        selectedEmployee = employee;
        if (employee == null) {
            masterDetail.setDetail(null);
            return;
        }
        showEmployee(employee);
        masterDetail.setDetail(detail);
    }

    private void showEmployee(Employee employee) {
        name.setText(employee.name());
        serviceTime.setText(formatServiceTime(employee.startDate()) + " in service");
        assignedTasks.setText(employee.assignedTasks() + " assigned tasks");
        firstName.setValue(employee.firstName());
        lastName.setValue(employee.lastName());
        phone.setValue(employee.phone());
        email.setValue(employee.email());
        dateOfBirth.setValue(employee.dateOfBirth());
        department.setValue(employee.department());
        jobTitle.setValue(employee.jobTitle());
        status.setValue(employee.status());
    }

    private void save() {
        if (dateOfBirth.isInvalid()) {
            return;
        }
        var updated = new Employee(selectedEmployee.id(), firstName.getValue(), lastName.getValue(),
                phone.getValue(), email.getValue(), dateOfBirth.getValue(), department.getValue(),
                jobTitle.getValue(), status.getValue(), selectedEmployee.startDate(),
                selectedEmployee.assignedTasks());
        employeeService.save(updated);
        grid.setItems(employeeService.list());
        grid.select(updated);
    }

    private void confirmRemove() {
        var employee = selectedEmployee;
        var dialog = new ConfirmDialog("Remove employee?", "Remove " + employee.name() + "?", "Remove", event -> {
            employeeService.delete(employee);
            grid.setItems(employeeService.list());
        });
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.open();
    }

    private static String formatServiceTime(LocalDate startDate) {
        var period = Period.between(startDate, LocalDate.now());
        return plural(period.getYears(), "year") + " " + plural(period.getMonths(), "month");
    }

    private static String plural(int count, String unit) {
        return count + " " + unit + (count == 1 ? "" : "s");
    }

    private static Badge createStatusBadge(EmployeeStatus status) {
        var badge = new Badge(status.getLabel());
        switch (status) {
            case ACTIVE -> {
                // The default badge uses the accent color
            }
            case ON_LEAVE -> badge.addClassName("aura-accent-neutral");
            case INACTIVE -> badge.addThemeVariants(BadgeVariant.ERROR);
        }
        return badge;
    }
}
