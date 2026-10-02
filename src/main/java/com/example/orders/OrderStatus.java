package com.example.orders;

public enum OrderStatus {
    RECEIVED("Received", "Order received"),
    IN_COLLECTION("In collection", "In collection"),
    READY_FOR_DELIVERY("Ready for delivery", "Ready for delivery"),
    IN_DELIVERY("In delivery", "In delivery"),
    DELIVERED("Delivered", "Delivered"),
    CANCELLED("Cancelled", "Cancelled");

    private final String label;
    private final String stepLabel;

    OrderStatus(String label, String stepLabel) {
        this.label = label;
        this.stepLabel = stepLabel;
    }

    /** Label used in status badges and filters. */
    public String getLabel() {
        return label;
    }

    /** Label used in the order status timeline. */
    public String getStepLabel() {
        return stepLabel;
    }

    /** The statuses an order moves through, in order. */
    public static OrderStatus[] timeline() {
        return new OrderStatus[] { RECEIVED, IN_COLLECTION, READY_FOR_DELIVERY, IN_DELIVERY, DELIVERED };
    }
}
