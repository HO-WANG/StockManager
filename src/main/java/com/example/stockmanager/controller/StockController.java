package com.example.stockmanager.controller;

import com.example.stockmanager.domain.RsiSummary;
import com.example.stockmanager.domain.StockSummary;
import com.example.stockmanager.service.SlackNotifier;
import com.example.stockmanager.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class StockController {

    public final static String ALPHA_API_KEY = "KWLVGUFU7Q5R7I4K";
    private final StockService stockService;
    private final SlackNotifier slackNotifier;
    //            "NNKFEZCKJY3ILJX4";  // API 키 설정

    public StockController(StockService stockService, SlackNotifier slackNotifier) {
        this.stockService = stockService;
        this.slackNotifier = slackNotifier;
    }

    @GetMapping("/api/notify")
    @Scheduled(cron = "0 0 7 * * *")
    public void notifyStockSummaries() {
        List<String> tickers = List.of("QQQ", "SCHD");

        tickers.forEach(this::processTicker);
    }

    private void processTicker(String ticker) {
        StockSummary summary = stockService.getDailySummary(ticker, ALPHA_API_KEY);
        RsiSummary rsi = stockService.getRsi(ticker, ALPHA_API_KEY);
        

        String message = createSummaryMessage(summary, rsi);
        slackNotifier.sendNotification(message);
        log.info("message : {} ", message);
    }

    private String createSummaryMessage(StockSummary summary, RsiSummary rsi) {
        String rsiStatus = "";
        if (rsi.prevRsi() <= 35) {
            rsiStatus = " (과매도!)";
        } else if (rsi.prevRsi() >= 65) {
            rsiStatus = " (과매수!)";
        }

        String ma20Status = summary.prevClose() <= summary.ma20() ? " (종가보다 높음)" : "";
        String ma60Status = summary.prevClose() <= summary.ma60() ? " (종가보다 높음)" : "";
        String ma120Status = summary.prevClose() <= summary.ma120() ? " (종가보다 높음)" : "";

        return String.format(
                "<(%s) %s 요약>\n - 전일종가: %.2f (%.2f%%)\n - RSI : %.2f%s \n - 20일평균: %.2f%s \n - 60일평균: %.2f%s \n - 120일평균: %.2f%s",
                summary.baseDate(),
                summary.symbol(),
                summary.prevClose(),
                summary.prevCloseRate(),
                rsi.prevRsi(),
                rsiStatus,
                summary.ma20(), ma20Status,
                summary.ma60(), ma60Status,
                summary.ma120(), ma120Status
        );
    }
}
