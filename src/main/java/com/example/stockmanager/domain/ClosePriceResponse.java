package com.example.stockmanager.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClosePriceResponse {

    @JsonProperty("Meta Data")
    private MetaData metaData;

    @JsonProperty("Time Series (Daily)")
    private Map<String, DailyData> timeSeries;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        @JsonProperty("3. Last Refreshed")
        private String lastRefreshed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyData {
        @JsonProperty("5. adjusted close")
        private double close;
    }
}
