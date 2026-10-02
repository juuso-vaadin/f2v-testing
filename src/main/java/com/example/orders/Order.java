package com.example.orders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * A sales order.
 *
 * @param stepTimes when the order reached each step; steps not yet reached are absent
 */
public record Order(long number, LocalDate date, OrderStatus status, Contact customer, Contact salesRepresentative,
        Map<OrderStep, LocalDateTime> stepTimes, List<LineItem> lineItems) {

    public BigDecimal total() {
        return lineItems.stream().map(LineItem::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
