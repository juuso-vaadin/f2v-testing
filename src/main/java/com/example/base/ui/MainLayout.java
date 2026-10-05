package com.example.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoIcon;

@Layout
public final class MainLayout extends AppLayout {

    private static final String USER_NAME = "Firstname Lastname";

    MainLayout() {
        setPrimarySection(Section.DRAWER);
        // A full-height layout keeps scrolling inside the views' content areas
        getStyle().setHeight("100%");

        var scroller = new Scroller(createNavigation());
        var drawer = new VerticalLayout(createLogo(), scroller, createUserButton());
        drawer.addClassName("app-drawer");
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing("var(--vaadin-gap-xl)");
        drawer.expand(scroller);
        addToDrawer(drawer);
    }

    private Component createLogo() {
        var logo = new Image("icons/acme-corp-logo.svg", "ACME Corp");
        logo.addClassName("app-logo");
        return logo;
    }

    private Component createNavigation() {
        var navigation = new VerticalLayout(
                createNavGroup(null, createItem("Dashboard", null, "dashboard")),
                createNavGroup("Sales", createItem("Orders", "orders", "orders"),
                        createItem("Deliveries", null, "deliveries"), createItem("Reports", null, "reports")),
                createNavGroup("Resources", createItem("Employees", "employees", "employees"),
                        createItem("Utilisation", null, "utilisation"), createItem("Payroll", null, "payroll")),
                createNavGroup("Admin", createItem("Access management", null, "access-management"),
                        createItem("Settings", null, "settings")));
        navigation.setPadding(false);
        navigation.setSpacing("var(--vaadin-gap-l)");
        return navigation;
    }

    private static SideNav createNavGroup(String label, SideNavItem... items) {
        var nav = new SideNav();
        nav.addClassName("app-nav");
        nav.setWidthFull();
        if (label != null) {
            nav.setLabel(label);
        }
        nav.addItem(items);
        return nav;
    }

    /** Only Orders and Employees have a view yet; the other entries are shown without a target. */
    private static SideNavItem createItem(String label, String path, String icon) {
        var svg = new SvgIcon("icons/nav-" + icon + ".svg");
        return path == null ? new SideNavItem(label, (String) null, svg) : new SideNavItem(label, path, svg);
    }

    private Component createUserButton() {
        var avatar = new Avatar(USER_NAME);
        avatar.setColorIndex(0);
        avatar.addThemeVariants(AvatarVariant.AURA_FILLED);
        var button = new Button(USER_NAME, avatar);
        button.addClassName("app-user");
        button.addThemeVariants(ButtonVariant.TERTIARY);
        button.setSuffixComponent(LumoIcon.DROPDOWN.create());
        button.setWidthFull();
        return button;
    }
}
