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
                throw new IllegalStateException("Некорректный ответ от LLM");
            }
        } catch (Exception e) {
            log.error("Ошибка при вызове LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обращении к LLM", e);
        }
    }

    public String analyzeExpenses(List<String> expenses) {
        String expensesList = String.join("\n", expenses);

        String prompt = """
            Ты — опытный финансовый аналитик и AI-консультант. Твоя задача — детально изучить расходы пользователя и составить финансовый план.
            
            Входные данные (список расходов за выбранный период):
            %s
            
            Выполни следующие шаги:
            1. Посчитай общую сумму затрат.
            2. Выдели категорию, которая съедает больше всего бюджета.
            3. Найди любые подозрительные или нерациональные траты (аномалии).
            4. Сделай 2 глубоких вывода о финансовых привычках пользователя.
            5. Сформулируй 3 конкретных, практичных совета, как сократить расходы без потери качества жизни.
            
            Ответ верни СТРОГО в формате JSON. Не используй markdown-разметку (никаких ```json). Пиши только на русском языке.
            
            Формат ответа:
            {
              "total_spent_rub": 0.0,
              "top_expense_category": "Название категории",
              "anomaly_comment": "Краткий анализ подозрительных трат, либо 'Аномалий не обнаружено'",
              "financial_insights": [
                "Первый важный вывод о поведении пользователя",
                "Второй важный вывод о поведении пользователя"
              ],
              "practical_recommendations": [
                "Первый четкий совет по экономии денег",
                "Второй четкий совет по экономии денег",
                "Третий четкий совет по экономии денег"
              ]
            }
            """.formatted(expensesList);

        return askLlm(prompt);
    }

}