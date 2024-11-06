package com.example.stockmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SlackNotifier {

    private final WebClient webClient;
    private final String baseUrl = "https://hooks.slack.com/services/";
    private final String myPersonalUrl = "T07SS08ELRJ/B07UY25FHJA/aVwWsAwSw0hNmxFxvo2uVv8y";

    public void sendNotification(String message) {
        webClient.post()
                .uri(baseUrl + myPersonalUrl)
                .bodyValue("{\"text\":\"" + message + "\"}")
                .retrieve()
                .bodyToMono(Void.class)
                .block();  // Blocking call to ensure synchronous execution
        ;
    }
}
