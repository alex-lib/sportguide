package com.sport.service.configurations;

import com.sport.service.bot.SportGuideBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramBotConfiguration {

    @Bean
    TelegramBotsApi telegramBotsApi(SportGuideBot sportGuideBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(sportGuideBot);
            log.info("Telegram bot has been launched successfully!");
        } catch (TelegramApiException e) {
            log.error("Error occurred while launching Telegram bot!", e);
            throw e;
        }
        return botsApi;
    }
}