package com.example.orders;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SalesFigures(int year, BigDecimal yearlyAverage, YearMonth month, BigDecimal monthlySales) {
}
