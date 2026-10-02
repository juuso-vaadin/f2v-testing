package com.example.orders;

/**
 * Status of an order as shown to the user. {@link #CANCELLED} can follow any of the other statuses.
 */
public enum OrderStatus {
    RECEIVED("Received"),
    IN_COLLECTION("In collection"),
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
