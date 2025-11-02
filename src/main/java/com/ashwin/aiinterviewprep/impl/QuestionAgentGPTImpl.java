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
        // 1️⃣ Define behavior
        String systemPrompt = "You are an expert technical interviewer. " +
                "Generate short, clear, and contextually relevant interview questions. " +
                "Avoid multi-part or vague questions.";

        // 2️⃣ User-level prompt
        String userPrompt = String.format(
                "Generate one %s-level interview question focused on the skill: '%s'. " +
                "Keep it concise and assess practical understanding.",
                difficulty, skill
        );

        // 3️⃣ Call GPT
        String questionText = gptClient.askChat(systemPrompt, userPrompt, 1);

        // 4️⃣ Wrap in Question model
        return new Question(UUID.randomUUID().toString(), questionText, skill, difficulty);
    }
}
