package com.example.orders.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.example.orders.Order;
import com.example.orders.Order.Contact;
import com.example.orders.Order.LineItem;
import com.example.orders.OrderStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;

class OrderDetails extends Composite<VerticalLayout> {

    private static final List<OrderStatus> STAGES = List.of(OrderStatus.RECEIVED, OrderStatus.IN_COLLECTION,
            OrderStatus.READY_FOR_DELIVERY, OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final DateTimeFormatter STAGE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);
    private static final DateTimeFormatter STAGE_TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    private final Order order;

    OrderDetails(Order order) {
        this.order = order;

        var content = getContent();
        content.addClassName("order-details");
        content.setPadding(false);
        content.setSpacing("var(--vaadin-gap-xl)");
        content.add(createHeader(), createContacts(), createSection("Order status", createStatusSteps()),
                createSection("Line items", createLineItems()));
    }

    static String formatMoney(double amount) {
        return String.format(Locale.GERMANY, "%.2f €", amount);
    }

    private Component createHeader() {
        var title = new H2("Order #" + order.number());
        title.addClassName("order-details-title");
        var date = new Span(DATE.format(order.date()));
        date.addClassName("order-details-date");

        var heading = new VerticalLayout(title, date);
        heading.setPadding(false);
        heading.setSpacing("var(--vaadin-gap-xs)");

        var header = new HorizontalLayout(heading, new StatusBadge(order.status()));
        header.setWidthFull();
        header.setSpacing("var(--vaadin-gap-xs)");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(heading);
        return header;
    }

    private Component createContacts() {
        var contacts = new HorizontalLayout(createContact("Customer contact", order.customerContact()),
                createContact("Sales representative", order.salesRepresentative()));
        contacts.setWidthFull();
        contacts.setSpacing("var(--vaadin-gap-xl)");
        contacts.setAlignItems(FlexComponent.Alignment.START);
        contacts.getChildren().forEach(contacts::expand);
        return contacts;
    }

    private Component createContact(String label, Contact contact) {
        var caption = new Span(label);
        caption.addClassName("order-details-caption");

        var card = new Card();
        card.addThemeVariants(CardVariant.OUTLINED, CardVariant.HORIZONTAL);
        card.addClassName("order-contact");
        card.setWidthFull();
        card.setMedia(new Image(contact.imagePath(), ""));
        card.setTitle(new Div(contact.name()));
        card.setSubtitle(new Div(contact.company()));

        var layout = new VerticalLayout(caption, card);
        layout.setPadding(false);
        layout.setSpacing("var(--vaadin-gap-s)");
        return layout;
    }

    private Component createSection(String title, Component content) {
        var heading = new H3(title);
        heading.addClassName("order-details-section-title");
        var section = new VerticalLayout(heading, content);
        section.setPadding(false);
        section.setSpacing("var(--vaadin-gap-l)");
        return section;
    }

    private Component createStatusSteps() {
        var steps = new HorizontalLayout();
        steps.setWidthFull();
        steps.setSpacing("var(--vaadin-gap-s)");
        steps.setAlignItems(FlexComponent.Alignment.CENTER);
        for (var stage : STAGES) {
            if (stage != STAGES.getFirst()) {
                var arrow = LumoIcon.ARROW_RIGHT.create();
                arrow.addClassName("order-step-arrow");
                steps.add(arrow);
            }
            steps.add(createStep(stage));
        }
        return steps;
    }

    private Component createStep(OrderStatus stage) {
        var time = order.statusTimes().get(stage);
        var reached = time != null && stage.ordinal() <= order.status().ordinal();

        var name = new Span(stage.getLabel());
        name.addClassName("order-step-name");
        var when = reached ? new Div(new Div(STAGE_DATE.format(time)), new Div(STAGE_TIME.format(time)))
                : new Div("Pending");
        when.addClassName("order-step-time");

        var step = new VerticalLayout(name, when);
        step.addClassName("order-step");
        step.setPadding(false);
        step.setSpacing(6, Unit.PIXELS);
        step.setWidth(null);
        if (!reached) {
            step.addClassName("order-step-pending");
        } else if (stage == order.status()) {
            step.addClassName("order-step-current");
        }
        return step;
    }

    private Component createLineItems() {
        var grid = new Grid<LineItem>();
        grid.setItems(order.items());
        grid.setAllRowsVisible(true);
        grid.addClassName("order-items");

        grid.addColumn(LineItem::sku).setHeader("SKU").setAutoWidth(true).setFooter(order.items().size() + " items");
        grid.addComponentColumn(OrderDetails::createProduct).setHeader("Product description").setAutoWidth(true);
        grid.addColumn(LineItem::quantity).setHeader("Qty").setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        grid.addColumn(item -> formatMoney(item.unitPrice())).setHeader("Unit price")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        grid.addColumn(item -> formatMoney(item.total())).setHeader("Line total").setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true).setFooter(formatMoney(order.total()));
        return grid;
    }

    private static Component createProduct(LineItem item) {
        var name = new Span(item.name());
        var description = new Span(item.description());
        description.addClassName("order-item-description");
        var product = new VerticalLayout(name, description);
        product.setPadding(false);
        product.setSpacing("var(--vaadin-gap-xs)");
        return product;
    }
}
