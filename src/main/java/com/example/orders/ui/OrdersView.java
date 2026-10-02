package com.example.orders.ui;

import com.example.orders.Contact;
import com.example.orders.LineItem;
import com.example.orders.Order;
import com.example.orders.OrderService;
import com.example.orders.OrderStatus;
import com.example.orders.OrderStep;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Master-detail view of sales orders: a searchable, filterable list on the left and the selected order on the right.
 * The selected order's number is the optional URL parameter, so a selection can be linked to and navigated back to.
 */
@Route("orders")
@StyleSheet("orders-view.css")
public class OrdersView extends Composite<Div> implements HasUrlParameter<Long>, HasDynamicTitle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final DateTimeFormatter STEP_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter STEP_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US);

    private final OrderService orderService;

    private final TextField search = new TextField();
    private final Map<OrderStatus, Button> statusFilters = new EnumMap<>(OrderStatus.class);
    private final Set<OrderStatus> selectedStatuses = EnumSet.noneOf(OrderStatus.class);
    private final Div orderList = new Div();
    private final Div details = new Div();
    private final Grid<LineItem> lineItemGrid = createLineItemGrid();
    private @Nullable Order selectedOrder;

    OrdersView(OrderService orderService) {
        this.orderService = orderService;

        var body = new Div(createListPanel(), details);
        body.addClassName("orders-view-body");
        details.addClassName("orders-view-details");

        getContent().addClassName("orders-view");
        getContent().add(createHeader(), body);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter @Nullable Long orderNumber) {
        selectedOrder = orderNumber != null
                ? orderService.get(orderNumber).orElse(null)
                : orderService.list(search.getValue(), selectedStatuses).stream().findFirst().orElse(null);
        refreshOrderList();
        refreshDetails();
    }

    @Override
    public String getPageTitle() {
        return selectedOrder != null ? "Order #" + selectedOrder.number() : "Orders";
    }

    // Header

    private Component createHeader() {
        var section = new Span("Sales");
        section.addClassName("orders-view-section");
        var title = new Div(section, new H1("Orders"));
        title.addClassName("orders-view-title");

        var figures = orderService.salesFigures();
        var kpis = new Div(
                createKpi(figures.year() + " average sales", figures.yearlyAverage()),
                createKpi(MONTH_FORMAT.format(figures.month()) + " sales", figures.monthlySales()));
        kpis.addClassName("orders-view-kpis");

        var header = new Div(new DrawerToggle(), title, kpis);
        header.addClassName("orders-view-header");
        return header;
    }

    private static Component createKpi(String label, BigDecimal value) {
        var labelSpan = new Span(label);
        labelSpan.addClassName("orders-view-kpi-label");
        var valueSpan = new Span(formatRoundedEuros(value));
        valueSpan.addClassName("orders-view-kpi-value");
        var kpi = new Div(labelSpan, valueSpan);
        kpi.addClassName("orders-view-kpi");
        return kpi;
    }

    // List

    private Component createListPanel() {
        search.setPlaceholder("Search by order number or customer");
        search.setAriaLabel("Search orders");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidthFull();
        search.addValueChangeListener(e -> refreshOrderList());

        var filters = new Div();
        filters.addClassName("orders-view-filters");
        filters.getElement().setAttribute("role", "group");
        filters.getElement().setAttribute("aria-label", "Filter by status");
        for (var status : OrderStatus.values()) {
            var filter = new Button(status.getLabel(), e -> toggleStatusFilter(status));
            filter.addClassNames("orders-view-filter", statusClassName(status));
            filter.getElement().setAttribute("aria-pressed", "false");
            statusFilters.put(status, filter);
            filters.add(filter);
        }

        var toolbar = new Div(search, filters);
        toolbar.addClassName("orders-view-list-toolbar");

        orderList.addClassName("orders-view-list-items");
        var panel = new Div(toolbar, orderList);
        panel.addClassName("orders-view-list");
        return panel;
    }

    private void toggleStatusFilter(OrderStatus status) {
        if (!selectedStatuses.remove(status)) {
            selectedStatuses.add(status);
        }
        var pressed = selectedStatuses.contains(status);
        statusFilters.get(status).getElement().setAttribute("aria-pressed", String.valueOf(pressed));
        refreshOrderList();
    }

    private void refreshOrderList() {
        var orders = orderService.list(search.getValue(), selectedStatuses);
        orderList.removeAll();
        orders.forEach(order -> orderList.add(createOrderCard(order)));
        if (orders.isEmpty()) {
            var empty = new Div("No orders match the search");
            empty.addClassName("orders-view-empty");
            orderList.add(empty);
        }
    }

    private Component createOrderCard(Order order) {
        var card = new Card();
        card.setTitle("Order #" + order.number(), 3);
        card.setSubtitle(DATE_FORMAT.format(order.date()));
        card.setHeaderSuffix(createStatusBadge(order.status()));

        var separator = new Span("•");
        separator.getElement().setAttribute("aria-hidden", "true");
        var summary = new Div(new Span(formatEuros(order.total())), separator,
                new Span(order.customer().organization()));
        summary.addClassName("orders-view-card-summary");
        card.add(summary);

        var link = new RouterLink(OrdersView.class, order.number());
        link.addClassName("orders-view-card");
        link.add(card);
        if (selectedOrder != null && selectedOrder.number() == order.number()) {
            link.getElement().setAttribute("aria-current", "true");
        }
        return link;
    }

    // Details

    private void refreshDetails() {
        details.removeAll();
        var order = selectedOrder;
        if (order == null) {
            var empty = new Div("Select an order to see its details");
            empty.addClassName("orders-view-empty");
            details.add(empty);
            return;
        }

        var title = new H2("Order #" + order.number());
        var date = new Span(DATE_FORMAT.format(order.date()));
        date.addClassName("orders-view-details-date");
        var titleBlock = new Div(title, date);
        var header = new Div(titleBlock, createStatusBadge(order.status()));
        header.addClassName("orders-view-details-header");

        var contacts = new Div(
                createContact("Customer contact", order.customer()),
                createContact("Sales representative", order.salesRepresentative()));
        contacts.addClassName("orders-view-contacts");

        lineItemGrid.setItems(order.lineItems());
        var footer = lineItemGrid.getFooterRows().getFirst();
        var count = order.lineItems().size();
        footer.getCell(lineItemGrid.getColumnByKey("sku")).setText(count + (count == 1 ? " item" : " items"));
        footer.getCell(lineItemGrid.getColumnByKey("total")).setText(formatEuros(order.total()));

        details.add(header, contacts,
                createSection("Order status", createStatusSteps(order)),
                createSection("Line items", lineItemGrid));
    }

    private static Component createSection(String title, Component content) {
        var section = new Div(new H3(title), content);
        section.addClassName("orders-view-details-section");
        return section;
    }

    private static Component createContact(String label, Contact contact) {
        var card = new Card();
        card.addThemeVariants(CardVariant.OUTLINED);
        card.setTitle(contact.name(), 4);
        card.setSubtitle(contact.organization());
        if (contact.imageUrl() != null) {
            card.setHeaderPrefix(new Image(contact.imageUrl(), ""));
        }

        var labelSpan = new Span(label);
        labelSpan.addClassName("orders-view-contact-label");
        var wrapper = new Div(labelSpan, card);
        wrapper.addClassName("orders-view-contact");
        return wrapper;
    }

    private static Component createStatusSteps(Order order) {
        var steps = new Div();
        steps.addClassName("orders-view-steps");
        steps.getElement().setAttribute("role", "list");

        var lastReached = order.stepTimes().keySet().stream().max(Enum::compareTo).orElse(null);
        var cancelled = order.status() == OrderStatus.CANCELLED;
        for (var step : OrderStep.values()) {
            var label = new Span(step.getLabel());
            label.addClassName("orders-view-step-label");
            var time = order.stepTimes().get(step);
            var info = time != null
                    ? new Span(new Span(STEP_DATE_FORMAT.format(time)), new Span(STEP_TIME_FORMAT.format(time)))
                    : new Span(cancelled ? "Cancelled" : "Pending");
            info.addClassName("orders-view-step-info");

            var stepBox = new Div(label, info);
            stepBox.addClassName("orders-view-step");
            if (time == null) {
                stepBox.addClassName("pending");
            } else if (step == lastReached && !cancelled && step != OrderStep.DELIVERED) {
                stepBox.addClassName("current");
            }

            // The arrow leading to a step is kept with it, so that it wraps onto the next line together with it
            var item = new Div();
            item.addClassName("orders-view-step-item");
            item.getElement().setAttribute("role", "listitem");
            if (stepBox.hasClassName("current")) {
                item.getElement().setAttribute("aria-current", "step");
            }
            if (step.ordinal() > 0) {
                var arrow = VaadinIcon.ARROW_RIGHT.create();
                arrow.addClassName("orders-view-step-arrow");
                arrow.getElement().setAttribute("aria-hidden", "true");
                item.add(arrow);
            }
            item.add(stepBox);
            steps.add(item);
        }
        return steps;
    }

    private static Grid<LineItem> createLineItemGrid() {
        var grid = new Grid<LineItem>();
        grid.addClassName("orders-view-line-items");
        grid.addThemeVariants(GridVariant.NO_BORDER);
        grid.addColumn(item -> item.product().sku()).setHeader("SKU").setKey("sku").setAutoWidth(true)
                .setFlexGrow(0);
        grid.addComponentColumn(item -> {
            var name = new Span(item.product().name());
            var description = new Span(item.product().description());
            description.addClassName("orders-view-product-description");
            var product = new Div(name, description);
            product.addClassName("orders-view-product");
            return product;
        }).setHeader("Product description").setFlexGrow(1);
        grid.addColumn(LineItem::quantity).setHeader("Qty").setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(item -> formatEuros(item.product().unitPrice())).setHeader("Unit price").setAutoWidth(true)
                .setFlexGrow(0).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(item -> formatEuros(item.total())).setHeader("Line total").setKey("total")
                .setAutoWidth(true).setFlexGrow(0).setTextAlign(ColumnTextAlign.END);
        grid.appendFooterRow();
        grid.setAllRowsVisible(true);
        return grid;
    }

    // Shared

    private static Badge createStatusBadge(OrderStatus status) {
        var badge = new Badge(status.getLabel());
        badge.addClassName(statusClassName(status));
        switch (status) {
            case IN_DELIVERY -> badge.addThemeVariants(BadgeVariant.SUCCESS);
            // Not SUCCESS: in Aura it fixes the fill color with a specificity the brand color can't override
            case DELIVERED -> badge.addThemeVariants(BadgeVariant.FILLED);
            case CANCELLED -> badge.addThemeVariants(BadgeVariant.ERROR);
            default -> {
            }
        }
        return badge;
    }

    private static String statusClassName(OrderStatus status) {
        return "status-" + status.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Formats as in "3820,00 €". */
    private static String formatEuros(BigDecimal amount) {
        var symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("0.00", symbols).format(amount) + " €";
    }

    /** Formats as in "168 640 €". */
    private static String formatRoundedEuros(BigDecimal amount) {
        var symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", symbols).format(amount) + " €";
    }
}
