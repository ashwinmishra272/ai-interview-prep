package com.ashwin.aiinterviewprep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Bean
    public WebClient openAiWebClient() {
        // Increase buffer size if responses can be large
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
                .build();

        return WebClient.builder()
                .baseUrl(openaiBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}
