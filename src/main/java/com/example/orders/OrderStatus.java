package com.example.orders;

public enum OrderStatus {
    RECEIVED("Received"),
    IN_COLLECTION("In collection"),
    READY_FOR_DELIVERY("Ready for delivery"),
    IN_DELIVERY("In delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
