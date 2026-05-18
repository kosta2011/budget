package com.budget.service;

import com.budget.entity.User;
import com.budget.repository.UserRepository;
import com.budget.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseMonitorService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final TransactionService transactionService;

    // Запускается каждый час (cron: 0 0 * * * *)
    @Scheduled(cron = "0 0 * * * *")
    public void checkExpensesForAllUsers() {
        List<User> users = userRepository.findAllByTelegramChatIdIsNotNull();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (User user : users) {
            if (user.getTelegramChatId() == null || user.getTelegramChatId().isBlank()) continue;
            if (user.getExpenseLimit() == null) continue;

            BigDecimal totalExpenses = transactionService.getExpenseSumForUser(user.getUuid(), startOfMonth, now);
            if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

            if (totalExpenses.compareTo(user.getExpenseLimit()) > 0) {
                // Отправляем только один раз в месяц
                if (!currentMonth.equals(user.getLastAlertMonth())) {
                    String message = String.format(
                            "Превышение лимита расходов!\nЗа период %s - %s потрачено %.2f при лимите %.2f.",
                            startOfMonth, now, totalExpenses, user.getExpenseLimit()
                    );
                    telegramNotificationService.sendMessage(user.getTelegramChatId(), message);
                    user.setLastAlertMonth(currentMonth);
                    userRepository.save(user);
                }
            }
        }
    }
}
