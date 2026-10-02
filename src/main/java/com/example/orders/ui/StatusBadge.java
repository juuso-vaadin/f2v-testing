package com.example.orders.ui;

import com.example.orders.OrderStatus;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;

class StatusBadge extends Badge {

    StatusBadge(OrderStatus status) {
        super(status.getLabel());
        switch (status) {
        case IN_COLLECTION -> addClassName("aura-accent-blue");
        case DELIVERED -> addThemeVariants(BadgeVariant.FILLED);
        case CANCELLED -> addThemeVariants(BadgeVariant.ERROR);
        case RECEIVED, READY_FOR_DELIVERY -> addClassName("aura-accent-neutral");
        default -> {
        }
        }
    }
}
