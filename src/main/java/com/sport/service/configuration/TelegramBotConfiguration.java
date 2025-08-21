package com.sport.service.configuration;
import com.sport.service.bot.SportPlacesAndEventsBot;
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
    TelegramBotsApi telegramBotsApi(SportPlacesAndEventsBot sportPlacesAndEventsBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(sportPlacesAndEventsBot);
            log.info("Telegram bot has been registered successfully!");
        } catch (TelegramApiException e) {
            log.error("Error occurred while registering Telegram bot!", e);
            throw e;
        }
        return botsApi;
    }
}