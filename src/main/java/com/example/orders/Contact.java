package com.example.orders;

/**
 * A person related to an order.
 *
 * @param imagePath path of the organisation logo, relative to the web root
 */
public record Contact(String name, String organisation, String imagePath) {
}
