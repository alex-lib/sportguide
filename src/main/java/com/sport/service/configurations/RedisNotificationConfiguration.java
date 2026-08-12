package com.sport.service.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.sport.service.constants.Constants;
import com.sport.service.redis_store.notifications_broker.NotificationListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisNotificationConfiguration {
    private final NotificationListener listener;

    @Bean(name = "notificationRedisTemplate")
    public RedisTemplate<String, Object> notificationRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.sport.service.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, weatherTopic());
        container.addMessageListener(listener, eventTopic());
        container.addMessageListener(listener, subscriberToAdminTopic());
        container.addMessageListener(listener, adminToSubscriberTopic());
        return container;
    }

    @Bean
    public ChannelTopic weatherTopic() {
        return new ChannelTopic(Constants.WEATHER_CHANNEL_NAME);
    }

    @Bean
    public ChannelTopic eventTopic() {
        return new ChannelTopic(Constants.EVENT_CHANNEL_NAME);
    }

    @Bean
    public ChannelTopic subscriberToAdminTopic() {
        return new ChannelTopic(Constants.SUBSCRIBER_TO_ADMIN_CHANNEL_NAME);
    }

    @Bean
    public ChannelTopic adminToSubscriberTopic() {
        return new ChannelTopic(Constants.ADMIN_TO_SUBSCRIBER_CHANNEL_NAME);
    }
}