package com.example.examplefeature.ui;

import com.example.base.ui.ViewTitle;
import com.example.examplefeature.Order;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "")
@PageTitle("Orders")
@Menu(order = 0, icon = "icons/clipboard-check.svg", title = "Orders")
public class OrderListView extends VerticalLayout {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    OrderListView() {
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        add(createHeader());
        add(createContent());
    }

    private Component createHeader() {
        var header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(true);
        header.setWidth("100%");
        header.addClassName("order-list-header");

        var title = new ViewTitle("Orders");
        header.add(title);

        var kpiContainer = new HorizontalLayout();
        kpiContainer.setSpacing(true);
        kpiContainer.setPadding(false);
        kpiContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        kpiContainer.setWidth("100%");
        kpiContainer.addClassName("kpi-container");

        kpiContainer.add(createKpiCard("2026 average sales", "168 640 €"));
        kpiContainer.add(createDivider());
        kpiContainer.add(createKpiCard("March 2026 sales", "174 610 €"));

        header.add(kpiContainer);
        return header;
    }

    private Component createKpiCard(String label, String value) {
        var card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(false);
        card.addClassName("kpi-card");

        var labelSpan = new Span(label);
        labelSpan.addClassName("kpi-label");

        var valueSpan = new Span(value);
        valueSpan.addClassName("kpi-value");

        card.add(labelSpan, valueSpan);
        return card;
    }

    private Component createDivider() {
        var divider = new Div();
        divider.addClassName("kpi-divider");
        return divider;
    }

    private Component createContent() {
        var content = new HorizontalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setSizeFull();
        content.addClassName("order-list-content");

        var filterPanel = createFilterPanel();
        content.add(filterPanel);

        return content;
    }

    private Component createFilterPanel() {
        var panel = new VerticalLayout();
        panel.setSpacing(false);
        panel.setPadding(true);
        panel.setWidth("360px");
        panel.addClassName("filter-panel");

        var searchField = new TextField();
        searchField.setPlaceholder("Search by order number or customer");
        searchField.setWidth("100%");
        searchField.addClassName("filter-search");

        panel.add(searchField);

        var filterBadges = new HorizontalLayout();
        filterBadges.setSpacing(true);
        filterBadges.setPadding(true);
        filterBadges.setWidth("100%");
        filterBadges.getStyle().set("overflow-x", "auto");
        filterBadges.addClassName("filter-badges");

        var badges = Arrays.asList("Received", "In collection", "In delivery", "Delivered", "Cancelled");
        for (String badgeText : badges) {
            var badge = new Badge(badgeText);
            filterBadges.add(badge);
        }

        panel.add(filterBadges);

        var ordersList = createOrdersList();
        panel.add(ordersList);

        return panel;
    }

    private Component createOrdersList() {
        var container = new VerticalLayout();
        container.setSpacing(false);
        container.setPadding(false);
        container.setSizeFull();
        container.addClassName("orders-list");

        var orders = getSampleOrders();

        var scroller = new Scroller();
        scroller.setSizeFull();
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        var listContent = new VerticalLayout();
        listContent.setSpacing(false);
        listContent.setPadding(false);
        listContent.setWidth("100%");

        for (Order order : orders) {
            listContent.add(createOrderCard(order));
        }

        scroller.setContent(listContent);
        container.add(scroller);

        return container;
    }

    private Component createOrderCard(Order order) {
        var card = new Div();
        card.addClassName("order-card");

        var header = new HorizontalLayout();
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");

        var titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        var orderNumber = new Paragraph("Order #" + order.getOrderNumber());
        orderNumber.addClassName("order-number");

        var orderDate = new Paragraph(order.getDate().format(dateFormatter));
        orderDate.addClassName("order-date");

        titleSection.add(orderNumber, orderDate);

        var statusBadge = createStatusBadge(order.getStatus());

        header.add(titleSection, statusBadge);
        header.setFlexGrow(1, titleSection);

        var contentText = new Paragraph(order.getAmount().toPlainString() + " € • " + order.getCustomer());
        contentText.addClassName("order-content");

        card.add(header, contentText);
        return card;
    }

    private Badge createStatusBadge(String status) {
        var badge = new Badge(status);
        switch (status) {
            case "In delivery":
                badge.addThemeVariants(BadgeVariant.SUCCESS);
                break;
            case "Delivered":
                badge.addThemeVariants(BadgeVariant.SUCCESS);
                break;
            case "Cancelled":
                badge.addThemeVariants(BadgeVariant.ERROR);
                break;
            default:
                break;
        }
        return badge;
    }

    private List<Order> getSampleOrders() {
        return Arrays.asList(
            new Order("10234", LocalDate.of(2026, 7, 3), "In collection", new BigDecimal("2450.00"), "GlobalTrade Solutions"),
            new Order("10235", LocalDate.of(2026, 7, 2), "In delivery", new BigDecimal("1890.50"), "TechVision Inc"),
            new Order("10236", LocalDate.of(2026, 7, 1), "Delivered", new BigDecimal("3200.00"), "InnovateLabs Ltd"),
            new Order("10237", LocalDate.of(2026, 6, 30), "Received", new BigDecimal("1550.75"), "GlobalTrade Solutions"),
            new Order("10238", LocalDate.of(2026, 6, 29), "Cancelled", new BigDecimal("2100.00"), "SecureNet Corp")
        );
    }
}
