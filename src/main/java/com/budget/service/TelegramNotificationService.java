package com.budget.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

@Service
public class TelegramNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramNotificationService(RestTemplateBuilder restTemplateBuilder,
                                       @Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
                    return factory;
                })
                .build();
    }

    public void sendMessage(String chatId, String message) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("chat_id", chatId, "text", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (RestClientException e) {
            // В сообщение лога не включаем детали исключения, чтобы не раскрыть токен
            log.error("Failed to send Telegram message to chatId: {}", chatId, e);
        }
    }
}

