package com.ashwin.aiinterviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GPTClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final String projectId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GPTClient(WebClient openAiWebClient,
                     @Value("${openai.api.key:}") String apiKey,
                     @Value("${openai.chat-model:gpt-5-mini}") String model,
                     @Value("${openai.project.id:}") String projectId) {
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
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("OpenAI-Project", projectId)
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
            throw new RuntimeException("OpenAI API error: status=" + wcre.getStatusCode().value() +
                    " body=" + wcre.getResponseBodyAsString(), wcre);
        } catch (Exception e) {
            throw new RuntimeException("Failed calling OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Streams the GPT response token-by-token using OpenAI's streaming API.
     * Each element in the returned Flux is a single content token (delta).
     */
    public Flux<String> askChatStream(String systemPrompt, String userPrompt, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            return Flux.error(new IllegalStateException("OpenAI API key is not configured."));
        }

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature,
                "stream", true
        );

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header("OpenAI-Project", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .filter(event -> event.data() != null && !event.data().isBlank() && !"[DONE]".equals(event.data().trim()))
                .mapNotNull(event -> extractDeltaContent(event.data()))
                .filter(token -> !token.isEmpty());
    }

    private String extractDeltaContent(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode choices = node.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String content = choices.get(0).path("delta").path("content").asText(null);
                return content != null ? content : "";
            }
        } catch (Exception ignored) {}
        return "";
    }
}
