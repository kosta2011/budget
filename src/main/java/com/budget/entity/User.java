package com.budget.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "uuid", columnDefinition = "CHAR(36)")
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @Column(name = "password", nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Поле для Telegram chat_id (может быть null, если пользователь не привязал бота)
    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    // Лимит расходов на месяц (null означает, что лимит не задан)
    @Column(name = "expense_limit", precision = 12, scale = 2)
    private BigDecimal expenseLimit;

    @Column(name = "last_alert_month")
    private String lastAlertMonth; // например, "2026-05"

}
