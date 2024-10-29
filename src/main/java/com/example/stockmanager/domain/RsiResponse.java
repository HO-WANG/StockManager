package com.example.stockmanager.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RsiResponse {

    @JsonProperty("Meta Data")
    private MetaData metaData;

    @JsonProperty("Technical Analysis: RSI")
    private Map<String, TechnicalAnalysis> timeSeries;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        @JsonProperty("3: Last Refreshed")
        private String lastRefreshed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TechnicalAnalysis {
        @JsonProperty("RSI")
        private double rsi;
    }
}