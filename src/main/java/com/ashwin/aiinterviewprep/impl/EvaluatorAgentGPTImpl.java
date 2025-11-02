package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.IEvaluatorAgent;
import com.ashwin.aiinterviewprep.model.AnswerEvaluation;
import com.ashwin.aiinterviewprep.service.GPTClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Uses GPT to evaluate an interview answer with feedback and score.
 */
@Component
public class EvaluatorAgentGPTImpl implements IEvaluatorAgent {

    private final GPTClient gptClient;

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

        // Try to extract JSON from GPT response (safe fallback if it adds text)
        String jsonPart = extractJson(rawResponse);

        // Parse JSON manually (safe, no dependency)
        int score = 0;
        String feedback = "";
        String improvement = "";

        try {
            if (jsonPart.contains("\"score\"")) {
                score = Integer.parseInt(jsonPart.replaceAll(".*\"score\"\\s*:\\s*(\\d+).*", "$1"));
            }
            if (jsonPart.contains("\"feedback\"")) {
                feedback = jsonPart.replaceAll(".*\"feedback\"\\s*:\\s*\"(.*?)\".*", "$1");
            }
            if (jsonPart.contains("\"improvement\"")) {
                improvement = jsonPart.replaceAll(".*\"improvement\"\\s*:\\s*\"(.*?)\".*", "$1");
            }
        } catch (Exception e) {
            feedback = "Evaluation parsing failed: " + rawResponse;
        }

        AnswerEvaluation eval = new AnswerEvaluation();
        eval.setId(UUID.randomUUID().toString());
        eval.setQuestionId(questionId);
        eval.setScore(score);
        eval.setFeedback(feedback);
        eval.setImprovement(improvement);

        return eval;
    }

    /**
     * Extracts JSON from text if GPT adds extra notes or markdown.
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
