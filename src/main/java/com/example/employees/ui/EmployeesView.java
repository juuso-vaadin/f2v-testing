package com.example.employees.ui;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.example.employees.Employee;
import com.example.employees.EmployeeService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

@Route("employees")
@PageTitle("Employees")
@Menu(order = 2, title = "Employees")
@StyleSheet("employees-view.css")
class EmployeesView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    private final EmployeeService employeeService;
    private final ListDataProvider<Employee> employees;
    private final Grid<Employee> grid = new Grid<>();
    private final MasterDetailLayout layout = new MasterDetailLayout();

    EmployeesView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        employees = new ListDataProvider<>(employeeService.list());

        layout.setMaster(createGrid());
        layout.setMasterSize("50%", true);
        layout.setDetailSize("50%", true);
        layout.setWidthFull();
        layout.setMinHeight("0");
        grid.select(employeeService.list().stream().filter(employee -> employee.getId() == 1).findFirst().orElse(null));

        setPadding(false);
        setSpacing(false);
        setSizeFull();
        add(createTabs(), createToolbar(), layout);
        expand(layout);
    }

    private Component createTabs() {
        return new Tabs(new Tab("Employee List"), new Tab("Organization Chart"));
    }

    private Component createToolbar() {
        var kpis = new HorizontalLayout(createKpi("Total employees", "246", "+14", " this year"), createDivider(),
                createKpi("Logistics employees", "192", "78%", " of all"));
        kpis.setSpacing("var(--vaadin-gap-xl)");
        kpis.setAlignItems(FlexComponent.Alignment.STRETCH);

        var export = new Button("Export", LumoIcon.UPLOAD.create());
        var add = new Button("Add employee", LumoIcon.PLUS.create());
        add.addThemeVariants(ButtonVariant.PRIMARY);
        var actions = new HorizontalLayout(export, add);
        actions.setSpacing("var(--vaadin-gap-l)");

        var toolbar = new HorizontalLayout(kpis, actions);
        toolbar.addClassName("employees-toolbar");
        toolbar.setWidthFull();
        toolbar.setPadding(false);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private static Component createKpi(String label, String value, String trend, String trendCaption) {
        var caption = new Span(label);
        caption.addClassName("employees-kpi-label");
        var amount = new Span(value);
        amount.addClassName("employees-kpi-value");
        var highlight = new Span(trend);
        highlight.addClassName("employees-kpi-trend");
        var note = new Div(highlight, new Span(trendCaption));
        note.addClassName("employees-kpi-note");

        var kpi = new VerticalLayout(caption, amount, note);
        kpi.setPadding(false);
        kpi.setSpacing("var(--vaadin-gap-xs)");
        kpi.setWidth("200px");
        return kpi;
    }

    private static Component createDivider() {
        var divider = new Div();
        divider.addClassName("employees-kpi-divider");
        return divider;
    }

    private Component createGrid() {
        grid.addClassName("employees-grid");
        grid.setDataProvider(employees);
        grid.setSizeFull();
        grid.addColumn(Employee::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Employee::getDepartment).setHeader("Department").setAutoWidth(true);
        grid.addColumn(Employee::getJobTitle).setHeader("Job title").setAutoWidth(true);
        grid.addComponentColumn(employee -> new EmployeeStatusBadge(employee.getStatus())).setHeader("Status")
                .setAutoWidth(true);
        grid.addColumn(employee -> DATE.format(employee.getStartDate())).setHeader("Start date").setAutoWidth(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(event -> showDetail(event.getFirstSelectedItem().orElse(null)));
        return grid;
    }

    private void showDetail(Employee employee) {
        if (employee == null) {
            layout.setDetail(null);
            return;
        }
        var form = new EmployeeForm(employee, employeeService, () -> {
            employees.refreshItem(employee);
            showDetail(employee);
        }, () -> {
            grid.deselectAll();
            employees.refreshAll();
        });
        var detail = new VerticalLayout(form);
        detail.addClassName("employees-detail");
        detail.setSizeFull();
        detail.setSpacing(false);
        detail.expand(form);
        layout.setDetail(detail);
    }
}
