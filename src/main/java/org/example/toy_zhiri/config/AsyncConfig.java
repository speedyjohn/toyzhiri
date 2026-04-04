package org.example.toy_zhiri.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Включает поддержку @Async для асинхронных операций.
 * Используется в EmailNotificationService для неблокирующей отправки писем.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}