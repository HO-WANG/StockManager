package com.example.stockmanager.domain;

public record StockSummary(
    String symbol,
    String baseDate,
    double prevClose,
    double ma20,
    double ma60,
    double ma120,
    String prevCloseRate
) {} 