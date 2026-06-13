package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TodayWeather;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final OpenMeteoClientService openMeteoClientService;
    private final NotificationSenderService notificationSenderService;
    private final SubscriberService subscriberService;
    private final NotificationCreatorService notificationCreatorService;

    @Scheduled(cron = Constants.CRON_SEND_WEATHER, zone = Constants.TIME_ZONE)
    public void createWeatherNotification() {
        try {
            TodayWeather weather = openMeteoClientService.getTodayWeather();
            String message = notificationCreatorService.createWeatherNotification(weather);

            List<Long> subscriberIds = subscriberService
                    .getSubscribersWhoWantGetEvents()
                    .stream()
                    .map(Subscriber::getId)
                    .toList();

            notificationSenderService.sendWeatherNotification(message, subscriberIds);
        } catch (FeignException e) {
            log.error("Failed to get weather from OpenMeteo", e);
        } catch (Exception e) {
            log.error("Failed to send weather notification", e);
        }
    }
}