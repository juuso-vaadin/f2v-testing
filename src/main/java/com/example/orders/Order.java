package com.example.orders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @param statusHistory when the order reached each status of {@link OrderStatus#timeline()}
 */
public record Order(long id, String number, LocalDate date, OrderStatus status, Contact customer,
        Contact salesRepresentative, Map<OrderStatus, LocalDateTime> statusHistory, List<LineItem> lineItems) {

    public BigDecimal total() {
        return lineItems.stream().map(LineItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
