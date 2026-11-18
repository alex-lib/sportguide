package com.sport.service.configurations;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import com.sport.service.services.ai.AiProviderRouter;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiConfiguration {

    @Bean
    public AiProviderRouter aiProviderRouter(AiProviderProperties properties, ChatClient chatClient, Clock clock) {
        var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(120));
        return new AiProviderRouter(RestClient.builder().requestFactory(requestFactory), properties,
                prompt -> chatClient.prompt().user(prompt).call().content(), clock);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient.builder(model).build();
    }
}
