package com.example.stockmanager.service;

import com.example.stockmanager.domain.ClosePriceResponse;
import com.example.stockmanager.domain.RsiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final WebClient webClient;

    public Map<String, String> getDailySummary(String symbol, String apiKey) {
        ClosePriceResponse response = webClient.get()
                .uri("https://www.alphavantage.co", uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .queryParam("outputsize", "full")
                        .build())
                .retrieve()
                .bodyToMono(ClosePriceResponse.class)
                .block();

        Map<String, ClosePriceResponse.DailyData> timeSeries = response.getTimeSeries();
        List<Double> closingPrices = timeSeries.values().stream()
                .map(ClosePriceResponse.DailyData::getClose)
                .toList();

        double prevClose = closingPrices.get(0);
        double ma20 = calculateMovingAverage(closingPrices, 20);
        double ma60 = calculateMovingAverage(closingPrices, 60);
        double ma120 = calculateMovingAverage(closingPrices, 120);

        return Map.of(
                "symbol", symbol,
                "baseDate", response.getMetaData().getLastRefreshed(),
                "prevClose", String.valueOf(prevClose),
                "ma20", String.valueOf(ma20),
                "ma60", String.valueOf(ma60),
                "ma120", String.valueOf(ma120)
        );


    }

    private double calculateMovingAverage(List<Double> prices, int days) {
        return prices.stream()
                .limit(days)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public Map<String, String> getRsi(String symbol, String apiKey) {
        RsiResponse response = webClient.get()
                .uri("https://www.alphavantage.co", uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "RSI")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "daily")
                        .queryParam("time_period", "14") // RSI 는 보통 관찰기간 14일
                        .queryParam("series_type", "close") // RSI 는 보통 종가 기준으로 계산
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(RsiResponse.class)
                .block();

        Map<String, RsiResponse.TechnicalAnalysis> timeSeries = response.getTimeSeries();
        List<Double> rsiData = timeSeries.values().stream()
                .map(RsiResponse.TechnicalAnalysis::getRsi)
                .toList();

        double prevRsi = rsiData.get(0);

        return Map.of(
                "symbol", symbol,
                "baseDate", response.getMetaData().getLastRefreshed(),
                "prevRsi", String.valueOf(prevRsi)
        );


    }
}
