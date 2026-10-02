package com.example.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoIcon;

@Layout
@StyleSheet("main-layout.css")
public final class MainLayout extends AppLayout {

    MainLayout() {
        setPrimarySection(Section.DRAWER);

        var navigation = new VerticalLayout(createLogo(), createNavigation(), createUserMenu());
        navigation.addClassName("main-nav");
        navigation.setPadding(false);
        navigation.setSpacing(false);
        navigation.setSizeFull();
        addToDrawer(navigation);
    }

    private Component createLogo() {
        var logo = new Image("images/acme-logo.svg", "ACME Corp");
        var header = new HorizontalLayout(logo);
        header.addClassName("main-nav-logo");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private Component createNavigation() {
        var groups = new VerticalLayout(
                createGroup(null, item("Dashboard", "dashboard")),
                createGroup("Sales", item("Orders", "orders"), item("Deliveries", "deliveries"),
                        item("Reports", "reports")),
                createGroup("Resources", item("Employees", "employees"), item("Utilisation", "utilisation"),
                        item("Payroll", "payroll")),
                createGroup("Admin", item("Access management", "access-management"),
                        item("Settings", "settings")));
        groups.addClassName("main-nav-groups");
        groups.setPadding(false);
        groups.setSpacing(false);
        groups.setAlignItems(FlexComponent.Alignment.STRETCH);

        var scroller = new Scroller(groups);
        scroller.addClassName("main-nav-scroller");
        scroller.setWidthFull();
        return scroller;
    }

    private static SideNav createGroup(String label, SideNavItem... items) {
        var nav = label == null ? new SideNav() : new SideNav(label);
        nav.addItem(items);
        return nav;
    }

    private static SideNavItem item(String label, String path) {
        return new SideNavItem(label, path, new SvgIcon("icons/nav/" + path + ".svg"));
    }

    private Component createUserMenu() {
        // TODO Replace with the signed-in user once the application has authentication
        var avatar = new Avatar("Firstname Lastname");
        avatar.setColorIndex(0);
        avatar.addThemeVariants(AvatarVariant.AURA_FILLED);

        var user = new Button("Firstname Lastname");
        user.addClassName("main-nav-user");
        user.addThemeVariants(ButtonVariant.TERTIARY);
        user.setPrefixComponent(avatar);
        user.setSuffixComponent(LumoIcon.DROPDOWN.create());
        user.setWidthFull();
        return user;
    }
}
