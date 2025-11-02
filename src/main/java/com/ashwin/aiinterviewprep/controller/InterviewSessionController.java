package com.ashwin.aiinterviewprep.controller;

import com.ashwin.aiinterviewprep.service.InterviewSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interview")
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    @Autowired
    public InterviewSessionController(InterviewSessionService interviewSessionService) {
        this.interviewSessionService = interviewSessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startInterview(@RequestBody Map<String, String> payload) {
        String resumeText = payload.get("resumeText");
        String jdText = payload.get("jdText");

        String firstQuestion = interviewSessionService.startInterview(resumeText, jdText);
        return ResponseEntity.ok(Map.of("question", firstQuestion));
    }

    @PostMapping("/next")
    public ResponseEntity<Map<String, String>> nextQuestion(@RequestBody Map<String, String> payload) {
        String userAnswer = payload.get("answer");
        String nextQ = interviewSessionService.getNextQuestion(userAnswer);
        return ResponseEntity.ok(Map.of("question", nextQ));
    }
}
