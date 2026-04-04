package com.ashwin.aiinterviewprep.controller;

import com.ashwin.aiinterviewprep.agents.InterviewAgent;
import com.ashwin.aiinterviewprep.dto.StartSessionRequest;
import com.ashwin.aiinterviewprep.dto.SubmitAnswerRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/adaptive")
public class AdaptiveInterviewController {

    private final InterviewAgent interviewAgent;

    public AdaptiveInterviewController(InterviewAgent interviewAgent) {
        this.interviewAgent = interviewAgent;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @Valid @RequestBody StartSessionRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        Map<String, Object> result = interviewAgent.startSession(
                request.getResumeText(), request.getJdText(), userEmail);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request) {
        Map<String, Object> result = interviewAgent.submitAnswer(
                request.getSessionId(), request.getAnswer());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/context/{sessionId}")
    public ResponseEntity<Object> getContext(@PathVariable String sessionId) {
        Object ctx = interviewAgent.getContext(sessionId);
        if (ctx == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ctx);
    }

    /**
     * SSE endpoint: streams GPT evaluation tokens as 'token' events,
     * then sends a final 'done' event with the structured result.
     */
    @PostMapping(value = "/stream/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        CompletableFuture.runAsync(() -> interviewAgent.streamEvaluation(
                request.getSessionId(), request.getAnswer(), emitter));
        return emitter;
    }

    @GetMapping("/sessions")
    public ResponseEntity<Object> getSessions(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(interviewAgent.getSessions(authentication.getName()));
    }

    @GetMapping("/sessions/{sessionId}/summary")
    public ResponseEntity<Object> getSessionSummary(@PathVariable String sessionId) {
        try {
            return ResponseEntity.ok(interviewAgent.getSessionSummary(sessionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
