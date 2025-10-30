package com.sport.service.web.actuator;

import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@WebEndpoint(id = "metricsofusers")
@RequiredArgsConstructor
public class UserMetrics {

    private final SubscriberService subscriberService;

    @ReadOperation
    public Map<String, Object> totalCountOfUsers() {
        return Map.of("Total count of users who started bot:", subscriberService.getUsersCount(),
                "Total count of subscriptions to receive notifications about events:", subscriberService.getSubscriptionsCount(),
                "Timestamp:", LocalDateTime.now());
    }
}