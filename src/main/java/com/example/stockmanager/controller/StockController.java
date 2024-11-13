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

        sendRsiNotifications(summary.symbol(), rsi.prevRsi());
        sendMovingAverageNotifications(summary);
    }

    private String createSummaryMessage(StockSummary summary, RsiSummary rsi) {
        return String.format(
                "<(%s) %s 요약>\n - 전일종가: %.2f\n - 전일대비 변동률: %s\n - RSI : %.2f \n - 20일평균: %.2f \n - 60일평균: %.2f \n - 120일평균: %.2f",
                summary.baseDate(),
                summary.symbol(),
                summary.prevClose(),
                summary.prevCloseRate(),
                rsi.prevRsi(),
                summary.ma20(), summary.ma60(), summary.ma120()
        );
    }

    private void sendRsiNotifications(String symbol, double prevRsi) {
        if (prevRsi <= 35) {
            slackNotifier.sendNotification("종목명 : " + symbol + " 이 과매도 상태 입니다(RSI : " + prevRsi + ").");
        } else if (prevRsi >= 65) {
            slackNotifier.sendNotification("종목명 : " + symbol + " 이 과매수 상태 입니다(RSI : " + prevRsi + ").");
        }
    }

    private void sendMovingAverageNotifications(StockSummary summary) {
        String symbol = summary.symbol();
        double prevClose = summary.prevClose();

        if (prevClose <= summary.ma20()) {
            slackNotifier.sendNotification("종목명 : " + symbol + " 이 20일 평균가보다 낮습니다.");
        }
        if (prevClose <= summary.ma60()) {
            slackNotifier.sendNotification("종목명 : " + symbol + " 이 60일 평균가보다 낮습니다.");
        }
        if (prevClose <= summary.ma120()) {
            slackNotifier.sendNotification("종목명 : " + symbol + " 이 120일 평균가보다 낮습니다.");
        }
    }
}
