package com.example.sales.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.Locale;

@Route("orders")
@PageTitle("Orders")
@StyleSheet("orders-view.css")
class OrdersView extends Div {

    enum Status {
        RECEIVED("Received"), IN_COLLECTION("In collection"), READY_FOR_DELIVERY("Ready for delivery"),
        IN_DELIVERY("In delivery"), DELIVERED("Delivered"), CANCELLED("Cancelled");

        final String label;

        Status(String label) {
            this.label = label;
        }
    }

    record Line(String sku, String name, String description, int qty, int unitPrice) {
        int total() {
            return qty * unitPrice;
        }
    }

    record Order(int number, LocalDate date, Status status, String customer, List<Line> lines) {
        int total() {
            return lines.stream().mapToInt(Line::total).sum();
        }
    }

    private static final List<Line> SAMPLE_LINES = List.of(
            new Line("SKU-4613", "R-500 portable", "Portable radon meter", 2, 500),
            new Line("SKU-4431", "RH-77", "Fixed radon measurement unit", 4, 340),
            new Line("SKU-7464", "SA-1 Handheld S", "Portable air quality meter", 5, 120),
            new Line("SKU-8871", "SA-2 Rack TFF2", "Top tier air quality unit", 1, 860));

    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.US);

    private final List<Order> orders = List.of(
            order(10234, 7, 3, Status.IN_COLLECTION, "GlobalTrade Solutions"),
            order(10235, 7, 2, Status.IN_DELIVERY, "WeBuyGlobal Inc"),
            order(10236, 7, 1, Status.DELIVERED, "MarketLink Enterprises"),
            order(10239, 6, 28, Status.CANCELLED, "InterTrade Innovations"),
            order(10238, 6, 29, Status.IN_DELIVERY, "TradeSphere Corp"),
            order(10240, 6, 27, Status.IN_COLLECTION, "NexGen Trading Co."),
            order(10237, 6, 30, Status.DELIVERED, "CommerceWave LLC"));

    private final Div list = new Div();
    private final Div details = new Div();
    private final List<Card> cards = new ArrayList<>();
    private final Set<Status> statusFilter = EnumSet.noneOf(Status.class);
    private String query = "";

    OrdersView() {
        addClassName("orders-view");
        var content = new Div(createList(), details);
        content.addClassName("orders-content");
        add(createHeader(), content);
        details.addClassName("order-details");
        refreshList();
        select(orders.get(1));
    }

    private static Order order(int number, int month, int day, Status status, String customer) {
        // Vary the sample data per order so the list totals differ
        var lines = SAMPLE_LINES.subList(0, 1 + number % 4);
        return new Order(number, LocalDate.of(2026, month, day), status, customer, lines);
    }

    private Component createHeader() {
        var title = new VerticalLayout(new Span("Sales"), new H2("Orders"));
        title.setPadding(false);
        title.setSpacing(false);
        title.addClassName("orders-title");

        var divider = new Div();
        divider.addClassName("kpi-divider");
        var kpis = new Div(kpi("2026 average sales", "168 640 €"), divider, kpi("March 2026 sales", "174 610 €"));
        kpis.addClassName("orders-kpis");

        var header = new HorizontalLayout(title, kpis);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("orders-header");
        return header;
    }

    private static Component kpi(String label, String value) {
        var l = new Span(label);
        l.addClassName("kpi-label");
        var v = new Span(value);
        v.addClassName("kpi-value");
        var kpi = new Div(l, v);
        kpi.addClassName("kpi");
        return kpi;
    }

    private Component createList() {
        var search = new TextField();
        search.setPlaceholder("Search by order number or customer");
        search.setAriaLabel("Search orders");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setWidthFull();
        search.addValueChangeListener(e -> {
            query = e.getValue().toLowerCase();
            refreshList();
        });

        var filters = new Div();
        filters.addClassName("order-filters");
        for (var s : List.of(Status.RECEIVED, Status.IN_COLLECTION, Status.IN_DELIVERY, Status.DELIVERED,
                Status.CANCELLED)) {
            var b = new Badge(s.label);
            b.addThemeVariants(BadgeVariant.CONTRAST);
            b.getElement().setAttribute("role", "button");
            b.getElement().setAttribute("tabindex", "0");
            b.getElement().setAttribute("aria-pressed", "false");
            b.getElement().addEventListener("click", e -> toggleFilter(s, b));
            // Badges are not focusable buttons by default, so mirror native button keyboard behavior
            b.getElement().addEventListener("keydown", e -> toggleFilter(s, b))
                    .setFilter("event.key === 'Enter' || event.key === ' '");
            filters.add(b);
        }

        var top = new Div(search, filters);
        top.addClassName("order-list-top");
        list.addClassName("order-list");
        var wrapper = new Div(top, list);
        wrapper.addClassName("order-list-pane");
        return wrapper;
    }

    private void toggleFilter(Status status, Badge badge) {
        boolean active = statusFilter.add(status);
        if (!active) {
            statusFilter.remove(status);
        }
        if (active) {
            badge.addThemeVariants(BadgeVariant.FILLED);
        } else {
            badge.removeThemeVariants(BadgeVariant.FILLED);
        }
        badge.getElement().setAttribute("aria-pressed", String.valueOf(active));
        refreshList();
    }

    private void refreshList() {
        list.removeAll();
        cards.clear();
        for (var o : orders) {
            if (!statusFilter.isEmpty() && !statusFilter.contains(o.status())) {
                continue;
            }
            if (!query.isEmpty() && !("#" + o.number() + " " + o.customer()).toLowerCase().contains(query)) {
                continue;
            }
            var card = new Card();
            card.addClassName("order-card");
            card.setTitle(new Div("Order #" + o.number()));
            card.setSubtitle(new Div(DATE.format(o.date())));
            card.setHeaderSuffix(statusBadge(o.status()));
            var info = new Div(new Span(MONEY.format(o.total())), new Span("•"), new Span(o.customer()));
            info.addClassName("order-card-info");
            card.add(info);
            card.getElement().addEventListener("click", e -> select(o));
            card.getElement().setProperty("order", o.number());
            list.add(card);
            cards.add(card);
        }
    }

    private static Badge statusBadge(Status status) {
        var badge = new Badge(status.label);
        switch (status) {
        case IN_DELIVERY -> badge.addThemeVariants(BadgeVariant.SUCCESS);
        case DELIVERED -> badge.addThemeVariants(BadgeVariant.SUCCESS, BadgeVariant.FILLED);
        case CANCELLED -> badge.addThemeVariants(BadgeVariant.ERROR);
        case RECEIVED -> badge.addThemeVariants(BadgeVariant.CONTRAST);
        default -> {
        }
        }
        return badge;
    }

    private void select(Order o) {
        cards.forEach(c -> c.getElement().getClassList().set("selected",
                c.getElement().getProperty("order", 0) == o.number()));

        var title = new Div(new H2("Order #" + o.number()), new Span(DATE.format(o.date())));
        title.addClassName("order-title");
        var header = new Div(title, statusBadge(o.status()));
        header.addClassName("order-header");

        var contacts = new Div(
                contact("Customer contact", "John Smith", o.customer(), VaadinIcon.USER.create()),
                contact("Sales representative", "Firstname Lastname", "Acme Corp", null));
        contacts.addClassName("order-contacts");

        details.removeAll();
        details.add(header, contacts, new H3("Order status"), createStatusSteps(o), new H3("Line items"),
                createLines(o));
    }

    private static Component contact(String label, String name, String company, Component unused) {
        var card = new Card();
        var avatar = new Avatar(name);
        card.setHeaderPrefix(avatar);
        card.setTitle(new Div(name));
        card.setSubtitle(new Div(company));
        var l = new Span(label);
        l.addClassName("field-label");
        var wrapper = new Div(l, card);
        wrapper.addClassName("order-contact");
        return wrapper;
    }

    private static Component createStatusSteps(Order o) {
        var steps = List.of(Status.RECEIVED, Status.IN_COLLECTION, Status.READY_FOR_DELIVERY, Status.IN_DELIVERY,
                Status.DELIVERED);
        var times = List.of("08/03/2027 08:14", "10/03/2027 13:55", "10/03/2027 15:13", "11/03/2027 08:07",
                "Pending");
        var container = new Div();
        container.addClassName("order-steps");
        int current = steps.indexOf(o.status());
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                var arrow = VaadinIcon.ARROW_RIGHT.create();
                arrow.addClassName("step-arrow");
                container.add(arrow);
            }
            var label = new Span(steps.get(i).label.toUpperCase());
            label.addClassName("step-label");
            var time = new Span(current >= 0 && i > current ? "Pending" : times.get(i));
            var step = new Div(label, time);
            step.addClassName("order-step");
            if (current >= 0 && i == current) {
                step.addClassName("current");
            }
            if (current >= 0 && i > current) {
                step.addClassName("pending");
            }
            container.add(step);
        }
        return container;
    }

    private static Component createLines(Order o) {
        var grid = new Grid<Line>();
        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.setItems(o.lines());
        grid.addColumn(Line::sku).setHeader("SKU").setAutoWidth(true);
        grid.addComponentColumn(l -> {
            var d = new Span(l.description());
            d.addClassName("line-description");
            var div = new Div(new Span(l.name()), d);
            div.addClassName("line-product");
            return div;
        }).setHeader("Product description").setFlexGrow(2);
        grid.addColumn(Line::qty).setHeader("Qty").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(l -> MONEY.format(l.unitPrice())).setHeader("Unit price")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        var total = grid.addColumn(l -> MONEY.format(l.total())).setHeader("Line total")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        var footer = grid.appendFooterRow();
        footer.getCell(grid.getColumns().get(0)).setText(o.lines().size() + " items");
        footer.getCell(total).setText(MONEY.format(o.total()));
        return grid;
    }
}
