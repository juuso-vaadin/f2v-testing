package com.example.orders;

import java.math.BigDecimal;

public record LineItem(String sku, String productName, String productDescription, int quantity,
        BigDecimal unitPrice) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
