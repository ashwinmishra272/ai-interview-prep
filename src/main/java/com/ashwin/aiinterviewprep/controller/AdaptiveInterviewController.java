package com.ashwin.aiinterviewprep.controller;

import com.ashwin.aiinterviewprep.agents.InterviewAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public endpoints for adaptive interview sessions.
 * For demo purposes we allow CORS from anywhere; tighten it for production.
 */
@RestController
@RequestMapping("/adaptive")
@CrossOrigin(origins = "*") // allow frontend access during dev
public class AdaptiveInterviewController {

    private final InterviewAgent interviewAgent;

    public AdaptiveInterviewController(InterviewAgent interviewAgent) {
        this.interviewAgent = interviewAgent;
    }

    /**
     * Start a new interview session.
     * Expects JSON body with "resumeText" and "jdText".
     *
     * Example body:
     * {
     *   "resumeText": "Java developer with 5 years of experience...",
     *   "jdText": "Looking for a Spring Boot backend engineer..."
     * }
     *
     * Response:
     * {
     *   "sessionId": "...",
     *   "question": { "id": "...", "text": "..." }
     * }
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSession(@RequestBody Map<String, String> body) {
        String resume = body.getOrDefault("resumeText", "");
        String jd = body.getOrDefault("jdText", "");
        Map<String, Object> result = interviewAgent.startSession(resume, jd);
        return ResponseEntity.ok(result);
    }

    /**
     * Submit an answer and get next question.
     *
     * Example body:
     * {
     *   "sessionId": "...",
     *   "answer": "I used Kafka for event-driven microservices..."
     * }
     *
     * Response:
     * {
     *   "finished": false,
     *   "nextQuestion": { "id": "...", "text": "..." },
     *   "evaluation": {
     *       "score": 8,
     *       "feedback": "Good practical example, mention message retention next time."
     *   }
     * }
     */
    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        String answer = body.get("answer");
        Map<String, Object> result = interviewAgent.submitAnswer(sessionId, answer);
        return ResponseEntity.ok(result);
    }

    /**
     * Get the current interview context (debug/demo).
     */
    @GetMapping("/context/{sessionId}")
    public ResponseEntity<Object> getContext(@PathVariable String sessionId) {
        return ResponseEntity.ok(interviewAgent.getContext(sessionId));
    }
}
