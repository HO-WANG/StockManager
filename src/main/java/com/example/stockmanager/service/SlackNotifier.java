package com.example.stockmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SlackNotifier {

    private final WebClient webClient;
    private final String webhookUrl = "https://hooks.slack.com/services/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";

    public void sendNotification(String message) {
        webClient.post()
                .uri(webhookUrl)
                .bodyValue("{\"text\":\"" + message + "\"}")
                .retrieve()
                .bodyToMono(Void.class)
                .block();  // Blocking call to ensure synchronous execution
        ;
    }
}
