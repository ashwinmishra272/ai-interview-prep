package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.IEvaluatorAgent;
import com.ashwin.aiinterviewprep.model.AnswerEvaluation;
import com.ashwin.aiinterviewprep.service.GPTClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EvaluatorAgentGPTImpl implements IEvaluatorAgent {

    private static final Logger log = LoggerFactory.getLogger(EvaluatorAgentGPTImpl.class);

    private final GPTClient gptClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluatorAgentGPTImpl(GPTClient gptClient) {
        this.gptClient = gptClient;
    }

    @Override
    public AnswerEvaluation evaluate(String questionId, String questionText, String answerText) {
        String systemPrompt = """
                You are an expert technical interviewer.
                You must evaluate candidate answers fairly and concisely.
                Output structured feedback in JSON with:
                {
                  "score": integer (1-5),
                  "feedback": "short comment (max 2 lines)",
                  "improvement": "one suggestion to improve answer"
                }
                """;

        String userPrompt = String.format("""
                Evaluate this interview response.

                Question: "%s"
                Candidate Answer: "%s"
                """, questionText, answerText);

        String rawResponse = gptClient.askChat(systemPrompt, userPrompt, 1);

        int score = 0;
        String feedback = "";
        String improvement = "";

        try {
            String jsonPart = extractJson(rawResponse);
            JsonNode node = objectMapper.readTree(jsonPart);
            score = node.path("score").asInt(0);
            feedback = node.path("feedback").asText("");
            improvement = node.path("improvement").asText("");
        } catch (Exception e) {
            log.warn("Failed to parse GPT evaluation response: {}", rawResponse, e);
            feedback = "Evaluation parsing failed. Raw: " + rawResponse;
        }

        AnswerEvaluation eval = new AnswerEvaluation();
        eval.setId(UUID.randomUUID().toString());
        eval.setQuestionId(questionId);
        eval.setScore(score);
        eval.setFeedback(feedback);
        eval.setImprovement(improvement);

        return eval;
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
