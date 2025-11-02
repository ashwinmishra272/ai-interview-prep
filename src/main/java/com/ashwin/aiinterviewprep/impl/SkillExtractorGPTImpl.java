package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.ISkillExtractor;
import com.ashwin.aiinterviewprep.service.GPTClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Uses GPT to extract the most relevant skills shared between resume and job description.
 * This drives the rest of the interview plan.
 */
@Component
public class SkillExtractorGPTImpl implements ISkillExtractor {

    private final GPTClient gptClient;

    public SkillExtractorGPTImpl(GPTClient gptClient) {
        this.gptClient = gptClient;
    }

    @Override
    public List<String> extractSkills(String resumeText, String jdText) {
        String systemPrompt = """
                You are an expert AI career assistant.
                Your task is to read a resume and a job description, 
                and list the 5–10 most relevant professional or technical skills 
                that overlap between them.
                Return only a comma-separated list of skill names — no explanations.
                """;

        String userPrompt = String.format("""
                Job Description:
                %s

                Resume:
                %s

                List the overlapping skills:
                """, jdText, resumeText);

        try {
            String reply = gptClient.askChat(systemPrompt, userPrompt, 1);
            // Example reply: "Java, Spring Boot, REST APIs, Docker, AWS"
            String[] parts = reply.split("[,\\n]+");
            List<String> skills = new ArrayList<>();
            for (String p : parts) {
                String cleaned = p.trim();
                if (!cleaned.isEmpty()) {
                    skills.add(cleaned);
                }
            }
            return skills;
        } catch (Exception e) {
            e.printStackTrace();
            // fallback mock list if GPT fails
            return Arrays.asList("Java", "Spring Boot", "System Design", "Communication");
        }
    }
}
