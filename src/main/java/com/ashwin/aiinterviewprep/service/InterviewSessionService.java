package com.ashwin.aiinterviewprep.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class InterviewSessionService {

    private final WebClient webClient;
    private String interviewContext = "";

    public InterviewSessionService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
                .build();
    }

    public String startInterview(String resumeText, String jdText) {
        interviewContext = """
                You are an expert interviewer. Conduct a mock interview based on this candidate's resume and job description.
                
                === Resume ===
                """ + resumeText + """

                === Job Description ===
                """ + jdText + """

                Start the interview by asking your first question. Focus on both technical and product thinking aspects.
                """;

        return askGPT(interviewContext);
    }

    public String getNextQuestion(String userAnswer) {
        interviewContext += "\nCandidate's answer: " + userAnswer + "\nNow ask the next question logically based on this.";
        return askGPT(interviewContext);
    }

    private String askGPT(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content", "You are an intelligent interview agent."),
                    Map.of("role", "user", "content", prompt)
            });

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("choices") == null) {
                return "Sorry, I couldn’t generate a question.";
            }

            var choices = (java.util.List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while generating the interview question.";
        }
    }
}
