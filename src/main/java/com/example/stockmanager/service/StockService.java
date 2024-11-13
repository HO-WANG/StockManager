package com.example.stockmanager.service;

import com.example.stockmanager.domain.ClosePriceResponse;
import com.example.stockmanager.domain.RsiResponse;
import com.example.stockmanager.domain.RsiSummary;
import com.example.stockmanager.domain.StockSummary;
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

    private static final String BASE_URL = "https://www.alphavantage.co";
    private static final String DEFAULT_RSI_PERIOD = "14";
    private static final String DAILY_INTERVAL = "daily";

    private final WebClient webClient;

    public StockSummary getDailySummary(String symbol, String apiKey) {
        ClosePriceResponse response = fetchFromApi(
                buildDailySummaryParams(symbol, apiKey),
                ClosePriceResponse.class
        );
        
        return processDailySummaryResponse(response, symbol);
    }

    public RsiSummary getRsi(String symbol, String apiKey) {
        RsiResponse response = fetchFromApi(
                buildRsiParams(symbol, apiKey),
                RsiResponse.class
        );
        
        return processRsiResponse(response, symbol);
    }

    private <T> T fetchFromApi(Map<String, String> queryParams, Class<T> responseType) {
        return webClient.get()
                .uri(BASE_URL, uriBuilder -> {
                    uriBuilder.path("/query");
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    private Map<String, String> buildDailySummaryParams(String symbol, String apiKey) {
        return Map.of(
                "function", "TIME_SERIES_DAILY",
                "symbol", symbol,
                "apikey", apiKey,
                "outputsize", "full"
        );
    }

    private Map<String, String> buildRsiParams(String symbol, String apiKey) {
        return Map.of(
                "function", "RSI",
                "symbol", symbol,
                "interval", DAILY_INTERVAL,
                "time_period", DEFAULT_RSI_PERIOD,
                "series_type", "close",
                "apikey", apiKey
        );
    }

    private StockSummary processDailySummaryResponse(ClosePriceResponse response, String symbol) {
        List<Double> closingPrices = extractClosingPrices(response);
        double prevClose = closingPrices.get(0);
        double twoDaysAgoClose = closingPrices.get(1);
        
        double fluctuationRate = ((prevClose - twoDaysAgoClose) / twoDaysAgoClose) * 100;
        String formattedFluctuationRate = String.format("%.2f", fluctuationRate);

        return new StockSummary(
                symbol,
                response.getMetaData().getLastRefreshed(),
                prevClose,
                calculateMovingAverage(closingPrices, 20),
                calculateMovingAverage(closingPrices, 60),
                calculateMovingAverage(closingPrices, 120),
                formattedFluctuationRate
        );
    }

    private RsiSummary processRsiResponse(RsiResponse response, String symbol) {
        double prevRsi = response.getTimeSeries().values().stream()
                .map(RsiResponse.TechnicalAnalysis::getRsi)
                .findFirst()
                .orElse(0.0);

        return new RsiSummary(
                symbol,
                response.getMetaData().getLastRefreshed(),
                prevRsi
        );
    }

    private List<Double> extractClosingPrices(ClosePriceResponse response) {
        return response.getTimeSeries().values().stream()
                .map(ClosePriceResponse.DailyData::getClose)
                .toList();
    }

    private double calculateMovingAverage(List<Double> prices, int days) {
        return prices.stream()
                .limit(days)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}

