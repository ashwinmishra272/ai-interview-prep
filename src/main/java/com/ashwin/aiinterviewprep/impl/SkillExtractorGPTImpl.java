package com.ashwin.aiinterviewprep.impl;

import com.ashwin.aiinterviewprep.agents.ISkillExtractor;
import com.ashwin.aiinterviewprep.service.GPTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
public class SkillExtractorGPTImpl implements ISkillExtractor {

    private static final Logger log = LoggerFactory.getLogger(SkillExtractorGPTImpl.class);

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
            log.warn("Skill extraction failed, using fallback list", e);
            return Arrays.asList("Java", "Spring Boot", "System Design", "Communication");
        }
    }
}
