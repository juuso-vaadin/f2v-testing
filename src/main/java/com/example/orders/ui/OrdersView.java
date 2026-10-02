package com.example.orders.ui;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import com.example.orders.Order;
import com.example.orders.OrderService;
import com.example.orders.OrderStatus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

@Route("orders")
@PageTitle("Orders")
@Menu(order = 1, title = "Orders")
@StyleSheet("orders-view.css")
class OrdersView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final Set<OrderStatus> FILTERS = EnumSet.of(OrderStatus.RECEIVED, OrderStatus.IN_COLLECTION,
            OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private final ListDataProvider<Order> orders;
    private final Set<OrderStatus> activeStatuses = EnumSet.noneOf(OrderStatus.class);
    private final MasterDetailLayout layout = new MasterDetailLayout();
    private String searchTerm = "";
    private Order selected;

    OrdersView(OrderService orderService) {
        orders = new ListDataProvider<>(orderService.list());
        orders.setFilter(this::matches);

        layout.setMaster(createMaster());
        layout.setMasterSize("360px");
        layout.setDetailSize("500px", true);
        layout.setWidthFull();
        layout.setMinHeight("0");
        select(orderService.list().stream().filter(order -> order.number() == 10235).findFirst().orElse(null));

        addClassName("orders-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        add(createHeader(), layout);
        expand(layout);
    }

    private Component createHeader() {
        var section = new Span("Sales");
        section.addClassName("orders-view-section");
        var title = new H1("Orders");
        var heading = new VerticalLayout(section, title);
        heading.setPadding(false);
        heading.setSpacing("var(--vaadin-gap-xs)");

        var kpis = new HorizontalLayout(createKpi("2026 average sales", "168 640 €"), createDivider(),
                createKpi("March 2026 sales", "174 610 €"));
        kpis.addClassName("orders-view-kpis");
        kpis.setSpacing("var(--vaadin-gap-xl)");
        kpis.setAlignItems(FlexComponent.Alignment.CENTER);

        var header = new HorizontalLayout(heading, kpis);
        header.addClassName("orders-view-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private static Component createKpi(String label, String value) {
        var caption = new Span(label);
        caption.addClassName("orders-kpi-label");
        var amount = new Span(value);
        amount.addClassName("orders-kpi-value");
        var kpi = new VerticalLayout(caption, amount);
        kpi.setPadding(false);
        kpi.setSpacing("var(--vaadin-gap-xs)");
        kpi.setWidth(null);
        return kpi;
    }

    private static Component createDivider() {
        var divider = new Div();
        divider.addClassName("orders-kpi-divider");
        return divider;
    }

    private Component createMaster() {
        var search = new TextField();
        search.setPlaceholder("Search by order number or customer");
        search.setAriaLabel("Search orders");
        search.setPrefixComponent(LumoIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidthFull();
        search.addClassName("orders-search");
        search.addValueChangeListener(event -> {
            searchTerm = event.getValue().trim().toLowerCase(Locale.ROOT);
            orders.refreshAll();
        });

        var statuses = new HorizontalLayout();
        statuses.addClassName("orders-statuses");
        statuses.setWidthFull();
        statuses.setSpacing("var(--vaadin-gap-xs)");
        FILTERS.forEach(status -> statuses.add(createFilter(status)));

        var filters = new VerticalLayout(search, statuses);
        filters.addClassName("orders-filters");
        filters.setPadding(false);
        filters.setSpacing("var(--vaadin-gap-m)");

        var list = new VirtualList<Order>();
        list.addClassName("orders-list");
        list.setDataProvider(orders);
        list.setRenderer(new ComponentRenderer<>(this::createListItem));
        list.setSizeFull();

        var master = new VerticalLayout(filters, list);
        master.setPadding(false);
        master.setSpacing(false);
        master.setSizeFull();
        master.expand(list);
        return master;
    }

    private Component createFilter(OrderStatus status) {
        var badge = new Badge(status.getLabel());
        badge.addClassNames("orders-filter", "aura-accent-neutral");
        badge.getElement().setAttribute("role", "button");
        badge.getElement().setAttribute("tabindex", "0");
        badge.getElement().addEventListener("click", event -> {
            if (!activeStatuses.remove(status)) {
                activeStatuses.add(status);
            }
            badge.getElement().setAttribute("aria-pressed", String.valueOf(activeStatuses.contains(status)));
            badge.getClassNames().set("orders-filter-active", activeStatuses.contains(status));
            orders.refreshAll();
        });
        return badge;
    }

    private Component createListItem(Order order) {
        var card = new Card();
        card.addClassName("orders-list-item");
        card.getClassNames().set("orders-list-item-selected", order.equals(selected));
        card.setTitle(new Div("Order #" + order.number()));
        card.setSubtitle(new Div(DATE.format(order.date())));
        card.setHeaderSuffix(new StatusBadge(order.status()));

        var summary = new HorizontalLayout(new Span(OrderDetails.formatMoney(order.total())), new Span("•"),
                new Span(order.customer()));
        summary.addClassName("orders-list-item-summary");
        summary.setSpacing("10px");
        card.add(summary);
        card.getElement().addEventListener("click", event -> select(order));
        return card;
    }

    private boolean matches(Order order) {
        var matchesStatus = activeStatuses.isEmpty() || activeStatuses.contains(order.status());
        var matchesSearch = searchTerm.isEmpty() || String.valueOf(order.number()).contains(searchTerm)
                || order.customer().toLowerCase(Locale.ROOT).contains(searchTerm);
        return matchesStatus && matchesSearch;
    }

    private void select(Order order) {
        selected = order;
        layout.setDetail(order == null ? null : new Scroller(new OrderDetails(order)));
        orders.refreshAll();
    }
}
