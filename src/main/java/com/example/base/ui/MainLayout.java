package com.example.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

@Layout
public final class MainLayout extends AppLayout {

    MainLayout() {
        addClassName("main-layout");
        setPrimarySection(Section.DRAWER);
        addToDrawer(createLogo(), createNavigation(), createUserMenu());
    }

    private Component createLogo() {
        var logo = new Image("icons/acme-logo.svg", "ACME Corp");
        logo.addClassName("nav-logo");
        return logo;
    }

    private Component createNavigation() {
        // Only Orders and Employees have views so far; the other entries are placeholders without a target
        var nav = new Div(
                section(null, item("Dashboard", null, "dashboard")),
                section("Sales", item("Orders", "orders", "orders"), item("Deliveries", null, "deliveries"),
                        item("Reports", null, "reports")),
                section("Resources", item("Employees", "employees", "employees"), item("Utilisation", null, "utilisation"),
                        item("Payroll", null, "payroll")),
                section("Admin", item("Access management", null, "access-management"),
                        item("Settings", null, "settings")));
        nav.addClassName("nav-sections");
        var scroller = new Scroller(nav);
        scroller.addClassName("nav-scroller");
        return scroller;
    }

    private static SideNav section(String label, SideNavItem... items) {
        var nav = new SideNav();
        if (label != null) {
            nav.setLabel(label);
        }
        nav.addItem(items);
        return nav;
    }

    private static SideNavItem item(String label, String path, String icon) {
        var item = path == null ? new SideNavItem(label) : new SideNavItem(label, path);
        item.setPrefixComponent(new SvgIcon("icons/" + icon + ".svg"));
        return item;
    }

    private Component createUserMenu() {
        var avatar = new Avatar("Firstname Lastname");
        var name = new Span("Firstname Lastname");
        name.addClassName("user-name");
        var chevron = new SvgIcon("icons/dropdown.svg");
        var user = new Div(avatar, name, chevron);
        user.addClassName("nav-user");
        return user;
    }
}
