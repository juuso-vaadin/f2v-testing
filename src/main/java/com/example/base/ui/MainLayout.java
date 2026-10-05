package com.example.base.ui;

import com.example.employees.ui.EmployeesView;
import com.example.orders.ui.OrdersView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.ScrollerVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import org.jspecify.annotations.Nullable;

@Layout
public final class MainLayout extends AppLayout {

    MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createApplicationHeader(), createApplicationDrawer(), createApplicationFooter());
    }

    private Component createApplicationHeader() {
        var logo = new Image("images/acme-corp-logo.svg", "ACME Corp");
        logo.addClassName("app-logo");
        var header = new Div(logo);
        header.addClassName("app-header");
        return header;
    }

    private Component createApplicationDrawer() {
        var dashboard = new SideNav();
        dashboard.addItem(createItem("Dashboard", "dashboard", null));

        var sales = new SideNav("Sales");
        var orders = createItem("Orders", "orders", OrdersView.class);
        // Keeps Orders current while an order is selected, as in orders/10235
        orders.setMatchNested(true);
        sales.addItem(orders, createItem("Deliveries", "deliveries", null),
                createItem("Reports", "reports", null));

        var resources = new SideNav("Resources");
        var employees = createItem("Employees", "employees", EmployeesView.class);
        // Keeps Employees current while an employee is selected, as in employees/2
        employees.setMatchNested(true);
        resources.addItem(employees,
                createItem("Utilisation", "utilisation", null),
                createItem("Payroll", "payroll", null));

        var admin = new SideNav("Admin");
        admin.addItem(createItem("Access management", "access-management", null),
                createItem("Settings", "settings", null));

        var navigation = new Div(dashboard, sales, resources, admin);
        navigation.addClassName("app-navigation");
        var scroller = new Scroller(navigation);
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        scroller.addClassName("app-navigation-scroller");
        return scroller;
    }

    /**
     * Creates a navigation item. Items without a view are shown, as in the design, but do not navigate anywhere.
     */
    // TODO Link the remaining items once their views exist
    private static SideNavItem createItem(String label, String icon, @Nullable Class<? extends Component> view) {
        var item = view != null ? new SideNavItem(label, view) : new SideNavItem(label);
        item.setPrefixComponent(new SvgIcon("icons/nav/" + icon + ".svg"));
        return item;
    }

    private Component createApplicationFooter() {
        // TODO Replace with the signed-in user once the application has authentication
        var avatar = new Avatar("Firstname Lastname");
        avatar.addClassName("app-user-avatar");
        var name = new Span("Firstname Lastname");
        name.addClassName("app-user-name");
        var user = new Div(avatar, name);
        user.addClassName("app-user");

        var menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.TERTIARY);
        menu.addClassName("app-user-menu");
        var userItem = menu.addItem(user);
        userItem.getSubMenu().addItem("Profile");
        userItem.getSubMenu().addItem("Sign out");
        return menu;
    }
}
