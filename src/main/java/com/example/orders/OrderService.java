package com.example.orders;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Serves orders from in-memory sample data.
 */
@Service
public class OrderService {

    private static final Contact SALES_REPRESENTATIVE = new Contact("Firstname Lastname", "Acme Corp",
            "images/acme-logo-mark.svg");

    private final List<Order> orders = createSampleOrders();

    public List<Order> list() {
        return orders;
    }

    /**
     * Orders whose number or customer matches the given text, ignoring case, and whose status is one of the given
     * statuses. An empty status set matches every status.
     */
    public List<Order> search(String filter, Set<OrderStatus> statuses) {
        var needle = filter.trim().toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> statuses.isEmpty() || statuses.contains(order.status()))
                .filter(order -> needle.isEmpty()
                        || order.number().contains(needle)
                        || order.customer().organisation().toLowerCase(Locale.ROOT).contains(needle)
                        || order.customer().name().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public Optional<Order> get(long id) {
        return orders.stream().filter(order -> order.id() == id).findFirst();
    }

    public BigDecimal averageSales(int year) {
        return new BigDecimal("168640");
    }

    public BigDecimal monthlySales(int year, int month) {
        return new BigDecimal("174610");
    }

    private static List<Order> createSampleOrders() {
        var r500 = product("SKU-4613", "R-500 portable", "Portable radon meter");
        var rh77 = product("SKU-4431", "RH-77", "Fixed radon measurement unit");
        var sa1 = product("SKU-7464", "SA-1 Handheld S", "Portable air quality meter");
        var sa2 = product("SKU-8871", "SA-2 Rack TFF2", "Top tier air quality unit");

        var webuy = new Contact("John Smith", "WeBuyGlobal Inc", "images/webuyglobal-logo.jpg");

        return List.of(
                order(1, "10234", LocalDate.of(2026, 7, 3), OrderStatus.IN_COLLECTION,
                        new Contact("Maria Garcia", "GlobalTrade Solutions", null),
                        List.of(sa2.of(2, "860"), rh77.of(1, "340"), sa1.of(3, "130"))),
                order(2, "10235", LocalDate.of(2026, 7, 2), OrderStatus.IN_DELIVERY, webuy,
                        List.of(r500.of(2, "500"), rh77.of(4, "340"), sa1.of(5, "120"), sa2.of(1, "860"))),
                order(3, "10236", LocalDate.of(2026, 7, 1), OrderStatus.DELIVERED,
                        new Contact("Ahmed Khan", "MarketLink Enterprises", null),
                        List.of(r500.of(1, "500"), rh77.of(3, "340"), sa1.of(6, "120"), sa2.of(1, "860"))),
                order(4, "10239", LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED,
                        new Contact("Emma Wilson", "InterTrade Innovations", null),
                        List.of(r500.of(2, "500"), rh77.of(2, "340"), sa1.of(3, "120"), sa2.of(1, "860"))),
                order(5, "10238", LocalDate.of(2026, 6, 29), OrderStatus.IN_DELIVERY,
                        new Contact("Liam Chen", "TradeSphere Corp", null),
                        List.of(sa2.of(3, "860"), r500.of(2, "500"), rh77.of(1, "340"), sa1.of(2, "115"))),
                order(6, "10239", LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED,
                        new Contact("Sofia Rossi", "CommerceWave LLC", null),
                        List.of(r500.of(3, "500"), rh77.of(2, "340"), sa1.of(4, "142.50"))),
                order(7, "10240", LocalDate.of(2026, 6, 27), OrderStatus.IN_COLLECTION,
                        new Contact("Noah Becker", "NexGen Trading Co.", null),
                        List.of(r500.of(1, "500"), rh77.of(3, "340"), sa1.of(3, "120"), sa2.of(2, "860"))),
                order(8, "10237", LocalDate.of(2026, 6, 30), OrderStatus.RECEIVED, webuy,
                        List.of(r500.of(1, "500"), rh77.of(4, "340"), sa1.of(2, "120"), sa2.of(2, "860"))),
                order(9, "10237", LocalDate.of(2026, 6, 30), OrderStatus.RECEIVED, webuy,
                        List.of(r500.of(1, "500"), rh77.of(4, "340"), sa1.of(2, "120"), sa2.of(2, "860"))));
    }

    private static Order order(long id, String number, LocalDate date, OrderStatus status, Contact customer,
            List<LineItem> lineItems) {
        return new Order(id, number, date, status, customer, SALES_REPRESENTATIVE, history(status), lineItems);
    }

    /** The timeline of order #10235, cut off at the given status. */
    private static Map<OrderStatus, LocalDateTime> history(OrderStatus status) {
        var history = new EnumMap<OrderStatus, LocalDateTime>(OrderStatus.class);
        history.put(OrderStatus.RECEIVED, LocalDateTime.of(2027, 3, 8, 8, 14));
        if (status == OrderStatus.CANCELLED || status == OrderStatus.RECEIVED) {
            return history;
        }
        history.put(OrderStatus.IN_COLLECTION, LocalDateTime.of(2027, 3, 10, 13, 55));
        if (status == OrderStatus.IN_COLLECTION) {
            return history;
        }
        history.put(OrderStatus.READY_FOR_DELIVERY, LocalDateTime.of(2027, 3, 10, 15, 13));
        history.put(OrderStatus.IN_DELIVERY, LocalDateTime.of(2027, 3, 11, 8, 7));
        if (status == OrderStatus.DELIVERED) {
            history.put(OrderStatus.DELIVERED, LocalDateTime.of(2027, 3, 12, 10, 30));
        }
        return history;
    }

    private static Product product(String sku, String name, String description) {
        return new Product(sku, name, description);
    }

    private record Product(String sku, String name, String description) {

        LineItem of(int quantity, String unitPrice) {
            return new LineItem(sku, name, description, quantity, new BigDecimal(unitPrice));
        }
    }
}
