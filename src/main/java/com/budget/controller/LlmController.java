package com.budget.controller;

import com.budget.entity.User;
import com.budget.entity.Transaction;
import com.budget.service.LocalLlmService;
import com.budget.service.TransactionService;
import com.budget.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LocalLlmService llmService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public LlmController(LocalLlmService llmService, TransactionService transactionService, TransactionRepository transactionRepository) {
        this.llmService = llmService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String prompt) {
        return llmService.askLlm(prompt);
    }

    @PostMapping("/analyze-my-expenses")
    public ResponseEntity<String> analyzeMyExpenses(@RequestParam(required = false) LocalDate dateFrom,
                                                    @RequestParam(required = false) LocalDate dateTo) {
        User currentUser = getCurrentUser();
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<String> expenses = transactionService.getExpenseDescriptionsForUser(currentUser.getUuid(), start, end);
        if (expenses.isEmpty()) {
            return ResponseEntity.ok("За выбранный период нет расходов.");
        }
        String analysis = llmService.analyzeExpenses(expenses);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/compare-expenses")
    public ResponseEntity<String> compareExpenses(@RequestParam LocalDate period1Start, @RequestParam LocalDate period1End,
                                                  @RequestParam LocalDate period2Start, @RequestParam LocalDate period2End) {
        User user = getCurrentUser();
        List<String> expenses1 = transactionService.getExpenseDescriptionsForUser(user.getUuid(), period1Start, period1End);
        List<String> expenses2 = transactionService.getExpenseDescriptionsForUser(user.getUuid(), period2Start, period2End);

        String prompt = "Ты — финансовый AI-аналитик. Сравни два периода расходов пользователя и верни детальный анализ в формате JSON.\n\n" +
                "## Период А: " + period1Start + " – " + period1End + "\n" +
                "Расходы:\n" + String.join("\n", expenses1) + "\n\n" +
                "## Период Б: " + period2Start + " – " + period2End + "\n" +
                "Расходы:\n" + String.join("\n", expenses2) + "\n\n" +
                "## Требуемый формат JSON (обязательно строго соблюдай):\n" +
                "{\n" +
                "  \"comparison\": {\n" +
                "    \"period_a\": {\n" +
                "      \"total\": число,\n" +
                "      \"transaction_count\": число,\n" +
                "      \"by_category\": [{\"category\": \"название\", \"total\": число, \"percentage\": число}]\n" +
                "    },\n" +
                "    \"period_b\": {\n" +
                "      \"total\": число,\n" +
                "      \"transaction_count\": число,\n" +
                "      \"by_category\": [{\"category\": \"название\", \"total\": число, \"percentage\": число}]\n" +
                "    },\n" +
                "    \"changes\": {\n" +
                "      \"total_change_percentage\": число,\n" +
                "      \"direction\": \"increase|decrease\",\n" +
                "      \"biggest_increase\": {\"category\": \"название\", \"difference\": число},\n" +
                "      \"biggest_decrease\": {\"category\": \"название\", \"difference\": число}\n" +
                "    },\n" +
                "    \"insights\": [\"конкретный инсайт1\", \"инсайт2\"],\n" +
                "    \"recommendations\": [\"рекомендация1\", \"рекомендация2\"]\n" +
                "  }\n" +
                "}\n\n" +
                "Отвечай только JSON, без лишнего текста.";

        return ResponseEntity.ok(llmService.askLlm(prompt));
    }

    @GetMapping("/predict-weekly")
    public ResponseEntity<String> predictWeeklyBudget() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1);
        List<String> lastMonthExpenses = transactionService.getExpenseDescriptionsForUser(user.getUuid(), lastMonthStart, now);

        String prompt = "Ты — финансовый AI-ассистент. На основе расходов пользователя за последний месяц спрогнозируй бюджет на следующую неделю. Верни ответ в формате JSON.\n\n" +
                "## Расходы за последний месяц (" + lastMonthStart + " – " + now + "):\n" +
                String.join("\n", lastMonthExpenses) + "\n\n" +
                "## Формат JSON:\n" +
                "{\n" +
                "  \"prediction\": {\n" +
                "    \"weekly_forecast\": число,\n" +
                "    \"daily_breakdown\": [{\"day\": \"ПН\", \"amount\": число}, ...],\n" +
                "    \"confidence\": \"high|medium|low\",\n" +
                "    \"assumptions\": [\"предположение1\", \"предположение2\"],\n" +
                "    \"advice\": \"совет по экономии на следующей неделе\"\n" +
                "  }\n" +
                "}\n\n" +
                "Отвечай только JSON.";

        return ResponseEntity.ok(llmService.askLlm(prompt));
    }

    @GetMapping("/advice-for-category")
    public ResponseEntity<String> adviceForCategory(@RequestParam String categoryName,
                                                    @RequestParam(required = false) LocalDate dateFrom,
                                                    @RequestParam(required = false) LocalDate dateTo) {
        User user = getCurrentUser();
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<Transaction> transactions = transactionRepository.findExpensesByUserAndCategory(user.getUuid(), categoryName, start, end);
        if (transactions.isEmpty()) {
            return ResponseEntity.ok("Нет расходов по категории " + categoryName);
        }
        BigDecimal total = transactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String details = transactions.stream()
                .map(t -> t.getTransactionDate() + ": " + t.getAmount() + " руб. (" + (t.getDescription() != null ? t.getDescription() : "без описания") + ")")
                .collect(Collectors.joining("\n"));

        String prompt = "Ты — финансовый консультант. Проанализируй расходы пользователя по категории '" + categoryName + "' за период " + start + " – " + end +
                ".\nОбщая сумма: " + total + " руб.\nДетали транзакций:\n" + details +
                "\n\nВерни ответ в формате JSON:\n" +
                "{\n" +
                "  \"analysis\": {\n" +
                "    \"category\": \"" + categoryName + "\",\n" +
                "    \"period\": \"" + start + " – " + end + "\",\n" +
                "    \"total_spent\": " + total + ",\n" +
                "    \"transaction_count\": " + transactions.size() + ",\n" +
                "    \"average_per_transaction\": " + total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP) + ",\n" +
                "    \"insights\": [\"инсайт1\", \"инсайт2\"],\n" +
                "    \"recommendations\": [\"рекомендация1\", \"рекомендация2\"],\n" +
                "    \"potential_savings\": число,\n" +
                "    \"saving_tips\": [\"совет1\", \"совет2\"]\n" +
                "  }\n" +
                "}\n\nОтвечай только JSON.";

        return ResponseEntity.ok(llmService.askLlm(prompt));
    }
}
