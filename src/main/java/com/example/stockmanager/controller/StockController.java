package com.example.stockmanager.controller;

import com.example.stockmanager.service.SlackNotifier;
import com.example.stockmanager.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

        tickers.forEach(ticker -> {
            Map<String, String> summary = stockService.getDailySummary(ticker, ALPHA_API_KEY);
            Map<String, String> rsi = stockService.getRsi(ticker, ALPHA_API_KEY);

            String symbol = summary.get("symbol");
            double prevClose = Double.parseDouble(summary.get("prevClose"));
            double ma20 = Double.parseDouble(summary.get("ma20"));
            double ma60 = Double.parseDouble(summary.get("ma60"));
            double ma120 = Double.parseDouble(summary.get("ma120"));
            double prevRsi = Double.parseDouble(rsi.get("prevRsi"));


            String message = String.format(
                    "<%s 종목 요약>\n - 전일종가: %.2f (기준일자: %s) \n - 20일평균: %.2f \n - 60일평균: %.2f \n - 120일평균: %.2f \n - RSI : %.2f(기준일자: %s)",
                    symbol,
                    prevClose, summary.get("baseDate"),
                    ma20, ma60, ma120,
                    prevRsi, rsi.get("baseDate")
            );
            slackNotifier.sendNotification(message);  // 동기적 Slack 알림 전송

            log.info("message : {} ", message);

            if (prevRsi <= 35) {
                slackNotifier.sendNotification("종목명 : " + symbol + " 이 과매도 상태 입니다(RSI : " + prevRsi + ").");
            } else if (prevRsi >= 65) {
                slackNotifier.sendNotification("종목명 : " + symbol + " 이 과매수 상태 입니다(RSI : " + prevRsi + ").");
            }

            if (prevClose <= ma20) {
                slackNotifier.sendNotification("종목명 : " + symbol + " 이 20일 평균가보다 낮습니다.");
            }
            if (prevClose <= ma60) {
                slackNotifier.sendNotification("종목명 : " + symbol + " 이 60일 평균가보다 낮습니다.");
            }
            if (prevClose <= ma120) {
                slackNotifier.sendNotification("종목명 : " + symbol + " 이 120일 평균가보다 낮습니다.");
            }


        });
    }

    @Scheduled(cron = "0 0 * * * *")
    public void slackTest() {
        slackNotifier.sendNotification("1시간 마다 슬랙 웹훅 테스트...");
    }
}
