package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.IQuestionAgent;
import com.ashwin.aiinterviewprep.model.Question;
import com.ashwin.aiinterviewprep.service.GPTClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QuestionAgentGPTImpl implements IQuestionAgent {

    private final GPTClient gptClient;

    public QuestionAgentGPTImpl(GPTClient gptClient) {
        this.gptClient = gptClient;
    }

    @Override
    public Question generateQuestion(String skill, String difficulty) {
        String systemPrompt = "You are an expert technical interviewer. " +
                "Generate short, clear, and contextually relevant interview questions. " +
                "Avoid multi-part or vague questions.";

        String userPrompt = String.format(
                "Generate one %s-level interview question focused on the skill: '%s'. " +
                "Keep it concise and assess practical understanding.",
                difficulty, skill
        );

        String questionText = gptClient.askChat(systemPrompt, userPrompt, 1);

        return new Question(UUID.randomUUID().toString(), skill, difficulty, questionText);
    }
}
