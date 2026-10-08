package com.example.examplefeature;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    private String orderNumber;
    private LocalDate date;
    private String status;
    private BigDecimal amount;
    private String customer;

    public Order(String orderNumber, LocalDate date, String status, BigDecimal amount, String customer) {
        this.orderNumber = orderNumber;
        this.date = date;
        this.status = status;
        this.amount = amount;
        this.customer = customer;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomer() {
        return customer;
    }
}
