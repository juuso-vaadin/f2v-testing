package com.example.orders;

import org.jspecify.annotations.Nullable;

/**
 * A person on either side of an order.
 *
 * @param imageUrl URL of an image shown next to the contact, typically the organization's logo
 */
public record Contact(String name, String organization, @Nullable String imageUrl) {
}
