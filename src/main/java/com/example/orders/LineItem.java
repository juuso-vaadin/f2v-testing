package com.example.orders;

import java.math.BigDecimal;

public record LineItem(Product product, int quantity) {

    public BigDecimal total() {
        return product.unitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public record Product(String sku, String name, String description, BigDecimal unitPrice) {

        public LineItem times(int quantity) {
            return new LineItem(this, quantity);
        }
    }
}
