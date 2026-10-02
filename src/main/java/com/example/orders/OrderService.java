package com.example.orders;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Serves orders from an in-memory sample data set.
 */
// TODO Replace the sample data with a real data source
@Service
public class OrderService {

    private static final Contact SALES_REPRESENTATIVE = new Contact("Firstname Lastname", "Acme Corp",
            "images/acme-logo.svg");

    private static final LineItem.Product R_500 = new LineItem.Product("SKU-4613", "R-500 portable",
            "Portable radon meter", new BigDecimal("500.00"));
    private static final LineItem.Product RH_77 = new LineItem.Product("SKU-4431", "RH-77",
            "Fixed radon measurement unit", new BigDecimal("340.00"));
    private static final LineItem.Product SA_1 = new LineItem.Product("SKU-7464", "SA-1 Handheld S",
            "Portable air quality meter", new BigDecimal("120.00"));
    private static final LineItem.Product SA_2 = new LineItem.Product("SKU-8871", "SA-2 Rack TFF2",
            "Top tier air quality unit", new BigDecimal("860.00"));
    private static final LineItem.Product IS_1 = new LineItem.Product("SKU-5120", "IS-1 Installation",
            "On-site installation service", new BigDecimal("250.00"));

    private final List<Order> orders = new ArrayList<>();

    OrderService() {
        orders.add(order(10234, LocalDate.of(2026, 7, 3), OrderStatus.IN_COLLECTION,
                customer("Maria Garcia", "GlobalTrade Solutions"), OrderStep.IN_COLLECTION,
                List.of(R_500.times(2), RH_77.times(1), SA_2.times(1), IS_1.times(1))));
        orders.add(order(10235, LocalDate.of(2026, 7, 2), OrderStatus.IN_DELIVERY,
                new Contact("John Smith", "WeBuyGlobal Inc", "images/webuyglobal.png"), OrderStep.IN_DELIVERY,
                List.of(R_500.times(2), RH_77.times(4), SA_1.times(5), SA_2.times(1))));
        orders.add(order(10236, LocalDate.of(2026, 7, 1), OrderStatus.DELIVERED,
                customer("Liam Chen", "MarketLink Enterprises"), OrderStep.DELIVERED,
                List.of(R_500.times(1), RH_77.times(3), SA_1.times(6), SA_2.times(1))));
        orders.add(order(10239, LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED,
                customer("Aisha Patel", "InterTrade Innovations"), OrderStep.ORDER_RECEIVED,
                List.of(R_500.times(2), RH_77.times(2), SA_1.times(3), SA_2.times(1))));
        orders.add(order(10238, LocalDate.of(2026, 6, 29), OrderStatus.IN_DELIVERY,
                customer("Noah Williams", "TradeSphere Corp"), OrderStep.IN_DELIVERY,
                List.of(R_500.times(1), RH_77.times(1), SA_1.times(4), SA_2.times(3), IS_1.times(1))));
        orders.add(order(10241, LocalDate.of(2026, 6, 28), OrderStatus.CANCELLED,
                customer("Emma Johnson", "CommerceWave LLC"), OrderStep.IN_COLLECTION,
                List.of(R_500.times(1), RH_77.times(3), SA_1.times(1), SA_2.times(1), IS_1.times(1))));
        orders.add(order(10240, LocalDate.of(2026, 6, 27), OrderStatus.IN_COLLECTION,
                customer("Oliver Brown", "NexGen Trading Co."), OrderStep.IN_COLLECTION,
                List.of(R_500.times(1), RH_77.times(3), SA_1.times(3), SA_2.times(2))));
        orders.add(order(10237, LocalDate.of(2026, 6, 30), OrderStatus.RECEIVED,
                new Contact("John Smith", "WeBuyGlobal Inc", "images/webuyglobal.png"), OrderStep.ORDER_RECEIVED,
                List.of(R_500.times(1), RH_77.times(4), SA_1.times(2), SA_2.times(2))));
    }

    /**
     * Lists orders, most recently placed first.
     *
     * @param searchTerm matched against the order number and the customer's name and organization; blank matches all
     * @param statuses   the statuses to include; empty includes all
     */
    public List<Order> list(String searchTerm, Set<OrderStatus> statuses) {
        var term = searchTerm.strip().toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> statuses.isEmpty() || statuses.contains(order.status()))
                .filter(order -> term.isEmpty() || matches(order, term))
                .sorted(Comparator.comparing(Order::date).reversed())
                .toList();
    }

    public Optional<Order> get(long number) {
        return orders.stream().filter(order -> order.number() == number).findFirst();
    }

    public SalesFigures salesFigures() {
        return new SalesFigures(2026, new BigDecimal("168640"), YearMonth.of(2026, 3), new BigDecimal("174610"));
    }

    private static boolean matches(Order order, String term) {
        return String.valueOf(order.number()).contains(term)
                || order.customer().name().toLowerCase(Locale.ROOT).contains(term)
                || order.customer().organization().toLowerCase(Locale.ROOT).contains(term);
    }

    private static Contact customer(String name, String organization) {
        return new Contact(name, organization, null);
    }

    /**
     * Creates an order whose steps up to and including {@code lastStep} have been reached, a couple of days apart.
     */
    private static Order order(long number, LocalDate date, OrderStatus status, Contact customer, OrderStep lastStep,
            List<LineItem> lineItems) {
        var stepTimes = new EnumMap<OrderStep, LocalDateTime>(OrderStep.class);
        var time = date.atTime(8, 14);
        for (var step : OrderStep.values()) {
            if (step.compareTo(lastStep) > 0) {
                break;
            }
            stepTimes.put(step, time);
            time = time.plusDays(1).plusHours(3).plusMinutes(17);
        }
        return new Order(number, date, status, customer, SALES_REPRESENTATIVE, stepTimes, lineItems);
    }
}
