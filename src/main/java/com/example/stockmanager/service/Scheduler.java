package com.example.stockmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final StockService stockService;

//
//    @Scheduled(cron = "* * * * * *")
//    public void dailyInform() {
//        stockService.getYesterdayClosePrice();
//    }
}
