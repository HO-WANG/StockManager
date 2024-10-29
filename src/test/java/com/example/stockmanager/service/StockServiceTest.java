package com.example.stockmanager.service;

import com.example.stockmanager.config.WebClientConfig;
import com.example.stockmanager.controller.StockController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockServiceTest {

    @Autowired
    StockService stockService;

    @Test
    void getDailySummary() {
    }

    void getRsi() {
        stockService.getRsi("QQQ", StockController.ALPHA_API_KEY);
    }
}