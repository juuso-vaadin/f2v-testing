package com.example.orders;

/**
 * The steps an order passes through on its way to the customer, in order.
 */
public enum OrderStep {
    ORDER_RECEIVED("Order received"),
    IN_COLLECTION("In collection"),
    READY_FOR_DELIVERY("Ready for delivery"),
    IN_DELIVERY("In delivery"),
    DELIVERED("Delivered");

    private final String label;

    OrderStep(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
