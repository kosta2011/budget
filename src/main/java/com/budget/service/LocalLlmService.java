package com.budget.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LocalLlmService {

    private static final Logger log = LoggerFactory.getLogger(LocalLlmService.class);
    private final RestTemplate restTemplate;
    private final String llmUrl;
    private final String modelName;

    public LocalLlmService(RestTemplate restTemplate,
                           @Value("${llm.url}") String llmUrl,
                           @Value("${llm.model}") String modelName) {
        this.restTemplate = restTemplate;
        this.llmUrl = llmUrl;
        this.modelName = modelName;
    }

    public String askLlm(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("options", Map.of("temperature", 0.7));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(llmUrl, entity, Map.class);
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            } else {
                log.error("Некорректный ответ от LLM: {}", response);
                return "Извините, не удалось получить ответ от нейросети.";
            }
        } catch (Exception e) {
            log.error("Ошибка при вызове LLM: {}", e.getMessage(), e);
            return "Произошла ошибка при обращении к нейросети: " + e.getMessage();
        }
    }

    public String analyzeExpenses(List<String> expenses) {
        String prompt = "Ты — финансовый помощник. Проанализируй следующие расходы пользователя и дай краткий, дружелюбный совет на русском языке. Расходы:\n"
                + String.join("\n", expenses)
                + "\n\nСовет:";
        return askLlm(prompt);
    }
}