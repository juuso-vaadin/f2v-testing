package com.example.employees.ui;

import com.example.employees.EmployeeStatus;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;

class EmployeeStatusBadge extends Badge {

    EmployeeStatusBadge(EmployeeStatus status) {
        super(status.getLabel());
        switch (status) {
        case INACTIVE -> addThemeVariants(BadgeVariant.ERROR);
        case ON_LEAVE -> addClassName("aura-accent-neutral");
        default -> {
        }
        }
    }
}
