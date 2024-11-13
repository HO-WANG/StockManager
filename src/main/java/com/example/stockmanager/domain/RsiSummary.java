package com.example.stockmanager.domain;

public record RsiSummary(
    String symbol,
    String baseDate,
    double prevRsi
) {} 