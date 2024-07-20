package com.example.telegram_hospital_bot.config;

import com.example.telegram_hospital_bot.service.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBotService);
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // Log the error and return a null or throw a runtime exception
            throw new RuntimeException("Failed to initialize Telegram bot", e);
        }
    }
}
