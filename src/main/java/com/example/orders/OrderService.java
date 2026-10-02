package com.example.orders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.orders.Order.Contact;
import com.example.orders.Order.LineItem;

/** Sample data matching the order list in the design. */
@Service
public class OrderService {

    private static final Contact SALES_REPRESENTATIVE = new Contact("Firstname Lastname", "Acme Corp",
            "images/acme-logo.svg");

    private static final List<LineItem> ITEMS = List.of(
            new LineItem("SKU-4613", "R-500 portable", "Portable radon meter", 2, 500),
            new LineItem("SKU-4431", "RH-77", "Fixed radon measurement unit", 4, 340),
            new LineItem("SKU-7464", "SA-1 Handheld S", "Portable air quality meter", 5, 120),
            new LineItem("SKU-8871", "SA-2 Rack TFF2", "Top tier air quality unit", 1, 860));

    private static final Map<OrderStatus, LocalDateTime> STATUS_TIMES = new EnumMap<>(Map.of(
            OrderStatus.RECEIVED, LocalDateTime.of(2027, 3, 8, 8, 14),
            OrderStatus.IN_COLLECTION, LocalDateTime.of(2027, 3, 10, 13, 55),
            OrderStatus.READY_FOR_DELIVERY, LocalDateTime.of(2027, 3, 10, 15, 13),
            OrderStatus.IN_DELIVERY, LocalDateTime.of(2027, 3, 11, 8, 7)));

    private final List<Order> orders = List.of(
            order(10234, LocalDate.of(2026, 7, 3), OrderStatus.IN_COLLECTION, "GlobalTrade Solutions", 2450),
            order(10235, LocalDate.of(2026, 7, 2), OrderStatus.IN_DELIVERY, "WeBuyGlobal Inc", 3820),
            order(10236, LocalDate.of(2026, 7, 1), OrderStatus.DELIVERED, "MarketLink Enterprises", 3100),
            order(10239, LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED, "InterTrade Innovations", 2900),
            order(10238, LocalDate.of(2026, 6, 29), OrderStatus.IN_DELIVERY, "TradeSphere Corp", 4150),
            order(10239, LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED, "CommerceWave LLC", 2750),
            order(10240, LocalDate.of(2026, 6, 27), OrderStatus.IN_COLLECTION, "NexGen Trading Co.", 3600));

    public List<Order> list() {
        return orders;
    }

    private static Order order(int number, LocalDate date, OrderStatus status, String customer, double total) {
        var contact = new Contact("John Smith", customer, "images/john-smith.png");
        return new Order(number, date, status, customer, total, contact, SALES_REPRESENTATIVE, STATUS_TIMES, ITEMS);
    }
}
