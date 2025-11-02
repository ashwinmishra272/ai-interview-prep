package com.ashwin.aiinterviewprep.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GPTClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final String projectId; // <--- ADD THIS

    public GPTClient(WebClient openAiWebClient,
                     @Value("${openai.api.key:}") String apiKey,
                     @Value("${openai.chat-model:gpt-5-mini}") String model,
                     @Value("${openai.project.id:}") String projectId) {  // <--- ADD THIS
        this.webClient = openAiWebClient;
        this.apiKey = apiKey;
        this.model = model;
        this.projectId = projectId;
    }

    public String askChat(String systemPrompt, String userPrompt, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured.");
        }

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature
//                "max_tokens", maxTokens
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("OpenAI-Project", projectId) // <--- ADD THIS LINE
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from OpenAI");
            }

            Object choicesObj = response.get("choices");
            if (choicesObj instanceof List && !((List<?>) choicesObj).isEmpty()) {
                Object first = ((List<?>) choicesObj).get(0);
                if (first instanceof Map) {
                    Object message = ((Map<?, ?>) first).get("message");
                    if (message instanceof Map) {
                        Object content = ((Map<?, ?>) message).get("content");
                        return content == null ? "" : content.toString().trim();
                    }
                }
            }

            return "";
        } catch (WebClientResponseException wcre) {
            throw new RuntimeException("OpenAI API error: status=" + wcre.getRawStatusCode() +
                    " body=" + wcre.getResponseBodyAsString(), wcre);
        } catch (Exception e) {
            throw new RuntimeException("Failed calling OpenAI: " + e.getMessage(), e);
        }
    }
}
