package com.example.orders.ui;

import com.example.orders.Contact;
import com.example.orders.LineItem;
import com.example.orders.Order;
import com.example.orders.OrderService;
import com.example.orders.OrderStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Route("orders")
@PageTitle("Orders")
@StyleSheet("orders-view.css")
class OrderListView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter STEP_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter STEP_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final OrderService orderService;
    private final VirtualList<Order> orderList = new VirtualList<>();
    private final VerticalLayout details = new VerticalLayout();
    private final TextField search = new TextField();
    private final Set<OrderStatus> statusFilter = EnumSet.noneOf(OrderStatus.class);
    private Order selectedOrder;

    OrderListView(OrderService orderService) {
        this.orderService = orderService;
        addClassName("orders-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        var content = new HorizontalLayout(createListPanel(), createDetailsPanel());
        content.addClassName("orders-view-content");
        content.setSpacing(false);
        content.setWidthFull();
        add(createHeader(), content);
        setFlexGrow(1, content);

        orderService.list().stream().filter(order -> order.number().equals("10235")).findFirst()
                .ifPresent(this::select);
    }

    private Component createHeader() {
        var section = new Span("Sales");
        section.addClassName("orders-view-section");
        var title = new H1("Orders");
        title.addClassName("orders-view-title");
        var titleBlock = new VerticalLayout(section, title);
        titleBlock.addClassName("orders-view-title-block");
        titleBlock.setPadding(false);
        titleBlock.setWidth(null);

        var kpis = new HorizontalLayout(
                createKpi("2026 average sales", orderService.averageSales(2026)),
                createKpi("March 2026 sales", orderService.monthlySales(2026, 3)));
        kpis.addClassName("orders-view-kpis");
        kpis.setSpacing(false);

        var header = new HorizontalLayout(titleBlock, kpis);
        header.addClassName("orders-view-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private Component createKpi(String label, BigDecimal value) {
        var labelText = new Span(label);
        labelText.addClassName("orders-view-kpi-label");
        var valueText = new Span(formatKpi(value));
        valueText.addClassName("orders-view-kpi-value");
        var kpi = new VerticalLayout(labelText, valueText);
        kpi.addClassName("orders-view-kpi");
        kpi.setPadding(false);
        kpi.setWidth(null);
        return kpi;
    }

    private Component createListPanel() {
        search.setPlaceholder("Search by order number or customer");
        search.setAriaLabel("Search by order number or customer");
        search.setPrefixComponent(LumoIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.addValueChangeListener(event -> refreshList());
        search.setWidthFull();

        var searchRow = new VerticalLayout(search);
        searchRow.addClassName("orders-view-search");
        searchRow.setPadding(false);

        var statusChips = new HorizontalLayout();
        statusChips.addClassName("orders-view-status-chips");
        statusChips.setSpacing(false);
        for (var status : List.of(OrderStatus.RECEIVED, OrderStatus.IN_COLLECTION, OrderStatus.IN_DELIVERY,
                OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
            statusChips.add(createStatusChip(status));
        }

        var chipScroller = new Scroller(statusChips, Scroller.ScrollDirection.HORIZONTAL);
        chipScroller.addClassName("orders-view-status-chip-scroller");
        chipScroller.setWidthFull();

        var filters = new VerticalLayout(searchRow, chipScroller);
        filters.addClassName("orders-view-filters");
        filters.setPadding(false);

        orderList.addClassName("orders-view-list-items");
        orderList.setItems(orderService.list());
        orderList.setRenderer(new ComponentRenderer<>(this::createOrderCard));
        orderList.setItemAccessibleNameGenerator(order -> "Order #" + order.number());

        var panel = new VerticalLayout(filters, orderList);
        panel.addClassName("orders-view-list");
        panel.setPadding(false);
        panel.setSpacing(false);
        panel.setWidth("360px");
        panel.setHeightFull();
        panel.setFlexGrow(1, orderList);
        return panel;
    }

    /** A badge that toggles filtering by its status; active filters use the filled variant. */
    private Component createStatusChip(OrderStatus status) {
        var chip = new Badge(status.getLabel());
        chip.addClassNames("aura-accent-neutral", "orders-view-status-chip");
        chip.setAriaRole("button");
        chip.getElement().setAttribute("tabindex", "0");
        chip.getElement().setAttribute("aria-pressed", "false");

        Runnable toggle = () -> {
            var active = !statusFilter.remove(status);
            if (active) {
                statusFilter.add(status);
            }
            chip.setThemeVariant(BadgeVariant.FILLED, active);
            chip.getElement().setAttribute("aria-pressed", String.valueOf(active));
            refreshList();
        };
        chip.getElement().addEventListener("click", event -> toggle.run());
        chip.getElement().addEventListener("keydown", event -> toggle.run())
                .setFilter("event.key === 'Enter' || event.key === ' '").preventDefault();
        return chip;
    }

    private void refreshList() {
        orderList.setItems(orderService.search(search.getValue(), statusFilter));
    }

    private Component createOrderCard(Order order) {
        var card = new Card();
        card.addClassName("orders-view-order-card");
        card.setClassName("selected", order.equals(selectedOrder));
        card.setTitle("Order #" + order.number());
        card.setSubtitle(DATE_FORMAT.format(order.date()));
        card.setHeaderSuffix(createStatusBadge(order.status()));

        var amount = new Span(formatAmount(order.total()));
        var separator = new Span("•");
        separator.getElement().setAttribute("aria-hidden", "true");
        var customer = new Span(order.customer().organisation());
        var summary = new HorizontalLayout(amount, separator, customer);
        summary.addClassName("orders-view-order-summary");
        summary.setSpacing(false);
        card.add(summary);

        card.getElement().addEventListener("click", event -> select(order));
        return card;
    }

    private Component createDetailsPanel() {
        details.addClassName("orders-view-details");
        details.setPadding(false);

        var scroller = new Scroller(details, Scroller.ScrollDirection.VERTICAL);
        scroller.addClassName("orders-view-details-scroller");
        scroller.setHeightFull();
        return scroller;
    }

    private void select(Order order) {
        selectedOrder = order;
        orderList.getDataProvider().refreshAll();
        showDetails(order);
    }

    private void showDetails(Order order) {
        details.removeAll();

        var title = new H2("Order #" + order.number());
        title.addClassName("orders-view-order-title");
        var date = new Span(DATE_FORMAT.format(order.date()));
        date.addClassName("orders-view-order-date");
        var titleBlock = new VerticalLayout(title, date);
        titleBlock.addClassName("orders-view-order-title-block");
        titleBlock.setPadding(false);
        var header = new HorizontalLayout(titleBlock, createStatusBadge(order.status()));
        header.addClassName("orders-view-order-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, titleBlock);

        var contacts = new HorizontalLayout(
                createContact("Customer contact", order.customer()),
                createContact("Sales representative", order.salesRepresentative()));
        contacts.addClassName("orders-view-contacts");
        contacts.setSpacing(false);
        contacts.setWidthFull();

        // The timeline is wider than the panel in the design; it scrolls sideways on its own
        var timeline = new Scroller(createStatusTimeline(order), Scroller.ScrollDirection.HORIZONTAL);
        timeline.addClassName("orders-view-timeline-scroller");
        timeline.setWidthFull();

        details.add(header, contacts, createSectionTitle("Order status"), timeline, createLineItems(order));
    }

    private Component createContact(String label, Contact contact) {
        var labelText = new Span(label);
        labelText.addClassName("orders-view-contact-label");

        var card = new Card();
        card.addClassName("orders-view-contact-card");
        card.addThemeVariants(CardVariant.OUTLINED, CardVariant.HORIZONTAL);
        if (contact.imagePath() != null) {
            var image = new Image(contact.imagePath(), contact.organisation());
            var media = new Div(image);
            media.addClassName("orders-view-contact-media");
            card.setMedia(media);
        }
        card.setTitle(contact.name());
        card.setSubtitle(contact.organisation());

        var block = new VerticalLayout(labelText, card);
        block.addClassName("orders-view-contact");
        block.setPadding(false);
        block.setSpacing(false);
        block.setWidth(null);
        block.setAlignItems(FlexComponent.Alignment.STRETCH);
        return block;
    }

    private Component createSectionTitle(String text) {
        var title = new H3(text);
        title.addClassName("orders-view-section-title");
        return title;
    }

    private Component createStatusTimeline(Order order) {
        var timeline = new HorizontalLayout();
        timeline.addClassName("orders-view-timeline");
        timeline.setSpacing(false);
        timeline.setAlignItems(FlexComponent.Alignment.CENTER);

        var steps = OrderStatus.timeline();
        for (int i = 0; i < steps.length; i++) {
            var step = steps[i];
            var reachedAt = order.statusHistory().get(step);
            if (i > 0) {
                var arrow = LumoIcon.ARROW_RIGHT.create();
                arrow.addClassName("orders-view-timeline-arrow");
                arrow.setClassName("pending", reachedAt == null);
                timeline.add(arrow);
            }
            var isCurrent = reachedAt != null && (i == steps.length - 1 || order.statusHistory().get(steps[i + 1]) == null);
            timeline.add(createStep(step, reachedAt, isCurrent));
        }
        return timeline;
    }

    private Component createStep(OrderStatus step, LocalDateTime reachedAt, boolean current) {
        var label = new Span(step.getStepLabel());
        label.addClassName("orders-view-step-label");
        var time = reachedAt == null
                ? new Span("Pending")
                : new Span(STEP_DATE_FORMAT.format(reachedAt) + "\n" + STEP_TIME_FORMAT.format(reachedAt));
        time.addClassName("orders-view-step-time");

        var stepLayout = new VerticalLayout(label, time);
        stepLayout.addClassName("orders-view-step");
        stepLayout.setClassName("current", current);
        stepLayout.setClassName("pending", reachedAt == null);
        stepLayout.setPadding(false);
        stepLayout.setWidth(null);
        return stepLayout;
    }

    private Component createLineItems(Order order) {
        var grid = new Grid<LineItem>();
        grid.addClassName("orders-view-line-items");
        grid.setItems(order.lineItems());
        grid.setAllRowsVisible(true);
        grid.addColumn(LineItem::sku).setHeader("SKU").setAutoWidth(true).setFlexGrow(0)
                .setFooter(order.lineItems().size() + " items");
        grid.addColumn(new ComponentRenderer<>(this::createProduct)).setHeader("Product description")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(LineItem::quantity).setHeader("Qty").setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(item -> formatAmount(item.unitPrice())).setHeader("Unit price").setAutoWidth(true)
                .setFlexGrow(0).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(item -> formatAmount(item.lineTotal())).setHeader("Line total").setAutoWidth(true)
                .setFlexGrow(0).setTextAlign(ColumnTextAlign.END).setFooter(formatAmount(order.total()));

        var section = new VerticalLayout(createSectionTitle("Line items"), grid);
        section.addClassName("orders-view-line-items-section");
        section.setPadding(false);
        section.setAlignItems(FlexComponent.Alignment.STRETCH);
        return section;
    }

    private Component createProduct(LineItem item) {
        var name = new Span(item.productName());
        name.addClassName("orders-view-product-name");
        var description = new Span(item.productDescription());
        description.addClassName("orders-view-product-description");
        var product = new VerticalLayout(name, description);
        product.addClassName("orders-view-product");
        product.setPadding(false);
        return product;
    }

    private static Badge createStatusBadge(OrderStatus status) {
        var badge = new Badge(status.getLabel());
        switch (status) {
            case RECEIVED -> badge.addClassName("aura-accent-neutral");
            case IN_COLLECTION, READY_FOR_DELIVERY -> badge.addClassName("aura-accent-blue");
            case IN_DELIVERY -> {
                // The default badge uses the accent color
            }
            case DELIVERED -> badge.addThemeVariants(BadgeVariant.FILLED);
            case CANCELLED -> badge.addThemeVariants(BadgeVariant.ERROR);
        }
        return badge;
    }

    private static String formatAmount(BigDecimal amount) {
        var symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("0.00", symbols).format(amount) + " €";
    }

    private static String formatKpi(BigDecimal amount) {
        var symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", symbols).format(amount) + " €";
    }
}
