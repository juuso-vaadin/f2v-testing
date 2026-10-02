package com.example.orders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record Order(int number, LocalDate date, OrderStatus status, String customer, double total, Contact customerContact,
        Contact salesRepresentative, Map<OrderStatus, LocalDateTime> statusTimes, List<LineItem> items) {

    public record Contact(String name, String company, String imagePath) {
    }

    public record LineItem(String sku, String name, String description, int quantity, double unitPrice) {
        public double total() {
            return quantity * unitPrice;
        }
    }
}
