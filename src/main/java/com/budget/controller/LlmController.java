package com.budget.controller;

import com.budget.entity.User;
import com.budget.entity.Transaction;
import com.budget.service.LocalLlmService;
import com.budget.service.TransactionService;
import com.budget.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

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
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        String prompt = "Сравни расходы пользователя за два периода.\nПериод А: " + period1Start + " - " + period1End + "\n" + String.join("\n", expenses1) +
                "\n\nПериод Б: " + period2Start + " - " + period2End + "\n" + String.join("\n", expenses2) +
                "\n\nВыдели основные изменения, дай рекомендации.";
        return ResponseEntity.ok(llmService.askLlm(prompt));
    }

    @GetMapping("/predict-weekly")
    public ResponseEntity<String> predictWeeklyBudget() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1);
        List<String> lastMonthExpenses = transactionService.getExpenseDescriptionsForUser(user.getUuid(), lastMonthStart, now);
        String prompt = "На основе расходов за последний месяц:\n" + String.join("\n", lastMonthExpenses) +
                "\n\nСпрогнозируй примерные расходы на следующую неделю и дай совет, как уложиться в бюджет.";
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
        String prompt = String.format("Пользователь потратил на категорию '%s' за период %s - %s сумму %.2f руб. Дай совет, как сократить расходы на эту категорию.",
                categoryName, start, end, total);
        return ResponseEntity.ok(llmService.askLlm(prompt));
    }
}
