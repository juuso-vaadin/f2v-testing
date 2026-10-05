package com.example.employees.ui;

import com.example.employees.Employee;
import com.example.employees.EmployeeService;
import com.example.employees.EmployeeStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import org.jspecify.annotations.Nullable;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Master-detail view of employees: headcount figures, the employee list, and a form for the selected employee. The
 * selected employee's id is the optional URL parameter, so a selection can be linked to and navigated back to.
 */
@Route("employees")
@StyleSheet("employees-view.css")
public class EmployeesView extends Composite<Div> implements HasUrlParameter<Long>, HasDynamicTitle {

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    private final EmployeeService employeeService;

    private final Div kpis = new Div();
    private final Grid<Employee> grid = createGrid();
    private final EmployeeForm form;
    private final MasterDetailLayout layout = new MasterDetailLayout();
    private @Nullable Employee selectedEmployee;

    EmployeesView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        form = new EmployeeForm(employeeService.departments(), employeeService.jobTitles(), this::save,
                this::remove, () -> UI.getCurrent().navigate(EmployeesView.class));

        var listTab = new Tab("Employee List");
        var chartTab = new Tab("Organization Chart");
        var tabs = new Tabs(listTab, chartTab);
        tabs.addClassName("employees-view-tabs");

        var header = new Div(new DrawerToggle(), tabs);
        header.addClassName("employees-view-header");

        var list = new Div(createToolbar(), createMasterDetail());
        list.addClassName("employees-view-list");

        // TODO Replace the placeholder once the organization chart is designed
        var chart = new Div("The organization chart is not available yet");
        chart.addClassName("employees-view-empty");
        chart.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            list.setVisible(e.getSelectedTab() == listTab);
            chart.setVisible(e.getSelectedTab() == chartTab);
        });

        getContent().addClassName("employees-view");
        getContent().add(header, list, chart);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter @Nullable Long employeeId) {
        selectedEmployee = employeeId != null ? employeeService.get(employeeId).orElse(null) : null;
        refreshList();
        showDetail(selectedEmployee);
    }

    @Override
    public String getPageTitle() {
        return selectedEmployee != null ? selectedEmployee.getName() : "Employees";
    }

    // Toolbar

    private Component createToolbar() {
        kpis.addClassName("employees-view-kpis");

        var exportButton = new Button("Export", new SvgIcon("icons/upload.svg"));
        // The anchor around the button does the download, so only the anchor takes focus
        exportButton.setTabIndex(-1);
        var export = new Anchor(createExportHandler(), "");
        export.addClassName("employees-view-export");
        export.add(exportButton);

        var add = new Button("Add employee", new SvgIcon("icons/plus.svg"), e -> addEmployee());
        add.addThemeVariants(ButtonVariant.PRIMARY);

        var actions = new Div(export, add);
        actions.addClassName("employees-view-actions");

        var toolbar = new Div(kpis, actions);
        toolbar.addClassName("employees-view-toolbar");
        return toolbar;
    }

    private void refreshKpis() {
        var figures = employeeService.figures();
        var joined = new Span("+" + figures.joinedThisYear());
        joined.addClassName("employees-view-kpi-increase");
        kpis.removeAll();
        kpis.add(createKpi("Total employees", figures.total(), joined, new Span(" this year")),
                createKpi("Logistics employees", figures.logistics(),
                        new Span(figures.logisticsPercentage() + "% of all")));
    }

    private static Component createKpi(String label, int value, Component... details) {
        var labelSpan = new Span(label);
        labelSpan.addClassName("employees-view-kpi-label");
        var valueSpan = new Span(String.valueOf(value));
        valueSpan.addClassName("employees-view-kpi-value");
        var detailSpan = new Span(details);
        detailSpan.addClassName("employees-view-kpi-detail");
        var kpi = new Div(labelSpan, valueSpan, detailSpan);
        kpi.addClassName("employees-view-kpi");
        return kpi;
    }

    private DownloadHandler createExportHandler() {
        return event -> {
            event.setFileName("employees.csv");
            event.setContentType("text/csv");
            try (var writer = new OutputStreamWriter(event.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write("Name,Department,Job title,Status,Start date,Phone,Email\n");
                for (var employee : employeeService.list()) {
                    writer.write(Stream.of(employee.getName(), employee.getDepartment(), employee.getJobTitle(),
                                    employee.getStatus().getLabel(), employee.getStartDate().toString(),
                                    employee.getPhone(), employee.getEmail())
                            .map(EmployeesView::toCsvValue)
                            .reduce((a, b) -> a + "," + b).orElse("") + "\n");
                }
            }
        };
    }

    private static String toCsvValue(@Nullable String value) {
        var text = Objects.requireNonNullElse(value, "");
        return text.matches(".*[,\"\\n].*") ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }

    // List and details

    private Component createMasterDetail() {
        layout.addClassName("employees-view-master-detail");
        layout.setMaster(grid);
        layout.setMasterSize("480px", true);
        layout.setDetailSize("584px");
        layout.addBackdropClickListener(e -> UI.getCurrent().navigate(EmployeesView.class));
        layout.addDetailEscapePressListener(e -> UI.getCurrent().navigate(EmployeesView.class));
        return layout;
    }

    private Grid<Employee> createGrid() {
        var grid = new Grid<Employee>();
        grid.addClassName("employees-view-grid");
        grid.addThemeVariants(GridVariant.NO_BORDER);
        grid.addColumn(Employee::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Employee::getDepartment).setHeader("Department").setAutoWidth(true);
        grid.addColumn(Employee::getJobTitle).setHeader("Job title").setAutoWidth(true);
        grid.addComponentColumn(employee -> createStatusBadge(employee.getStatus())).setHeader("Status")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(employee -> DATE_FORMAT.format(employee.getStartDate())).setHeader("Start date")
                .setAutoWidth(true);
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.isFromClient()) {
                var employee = e.getValue();
                if (employee != null && employee.getId() != null) {
                    UI.getCurrent().navigate(EmployeesView.class, employee.getId());
                } else {
                    UI.getCurrent().navigate(EmployeesView.class);
                }
            }
        });
        return grid;
    }

    private void refreshList() {
        GridListDataView<Employee> dataView = grid.setItems(employeeService.list());
        dataView.setIdentifierProvider(Employee::getId);
        if (selectedEmployee != null) {
            grid.select(selectedEmployee);
        } else {
            grid.deselectAll();
        }
        refreshKpis();
    }

    private void showDetail(@Nullable Employee employee) {
        if (employee == null) {
            layout.setDetail(null);
            return;
        }
        form.setEmployee(employee);
        layout.setDetail(form);
    }

    private void addEmployee() {
        UI.getCurrent().navigate(EmployeesView.class);
        showDetail(new Employee());
        form.focus();
    }

    private void save(Employee employee) {
        var saved = employeeService.save(employee);
        Notification.show(saved.getName() + " saved", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.SUCCESS);
        UI.getCurrent().navigate(EmployeesView.class, saved.getId());
    }

    private void remove(Employee employee) {
        if (employee.getId() != null) {
            employeeService.delete(employee.getId());
        }
        Notification.show(employee.getName() + " removed", 3000, Notification.Position.BOTTOM_START);
        UI.getCurrent().navigate(EmployeesView.class);
        // Navigating to the same view with the same (empty) parameter does not refresh it
        refreshList();
    }

    private static Badge createStatusBadge(EmployeeStatus status) {
        var badge = new Badge(status.getLabel());
        badge.addClassName("status-" + status.name().toLowerCase(Locale.ROOT).replace('_', '-'));
        switch (status) {
            case INACTIVE -> badge.addThemeVariants(BadgeVariant.ERROR);
            case ON_LEAVE -> badge.addThemeVariants(BadgeVariant.CONTRAST);
            default -> {
            }
        }
        return badge;
    }
}
