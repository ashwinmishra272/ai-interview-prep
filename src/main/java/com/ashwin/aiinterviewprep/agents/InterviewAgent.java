package com.ashwin.aiinterviewprep.agents;

import com.ashwin.aiinterviewprep.model.*;
import com.ashwin.aiinterviewprep.repository.InterviewEvaluationRepository;
import com.ashwin.aiinterviewprep.repository.InterviewQuestionRepository;
import com.ashwin.aiinterviewprep.repository.InterviewSessionRepository;
import com.ashwin.aiinterviewprep.service.GPTClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class InterviewAgent {

    private static final Logger log = LoggerFactory.getLogger(InterviewAgent.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ISkillExtractor skillExtractor;
    private final IPlannerAgent planner;
    private final IQuestionAgent questionAgent;
    private final IEvaluatorAgent evaluator;
    private final GPTClient gptClient;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewEvaluationRepository evaluationRepository;

    public InterviewAgent(ISkillExtractor skillExtractor,
                          IPlannerAgent planner,
                          IQuestionAgent questionAgent,
                          IEvaluatorAgent evaluator,
                          GPTClient gptClient,
                          InterviewSessionRepository sessionRepository,
                          InterviewQuestionRepository questionRepository,
                          InterviewEvaluationRepository evaluationRepository) {
        this.skillExtractor = skillExtractor;
        this.planner = planner;
        this.questionAgent = questionAgent;
        this.evaluator = evaluator;
        this.gptClient = gptClient;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public Map<String, Object> startSession(String resumeText, String jdText, String userEmail) {
        String sessionId = UUID.randomUUID().toString();

        List<String> skills = skillExtractor.extractSkills(resumeText, jdText);
        List<SkillPlan> plan = planner.plan(skills);

        String skillPlanStr = plan.stream()
                .map(p -> p.getSkill() + ":" + p.getQuestionsCount())
                .collect(Collectors.joining("|"));

        InterviewQuestionRecord firstRecord = null;
        if (!plan.isEmpty()) {
            Question q = questionAgent.generateQuestion(plan.getFirst().getSkill(), "medium");
            firstRecord = new InterviewQuestionRecord();
            firstRecord.setId(q.getId());
            firstRecord.setSessionId(sessionId);
            firstRecord.setQuestionIndex(0);
            firstRecord.setSkill(q.getSkill());
            firstRecord.setDifficulty(q.getDifficulty());
            firstRecord.setText(q.getText());
        }

        InterviewSession session = new InterviewSession();
        session.setId(sessionId);
        session.setUserEmail(userEmail);
        session.setResumeText(resumeText);
        session.setJobDescription(jdText);
        session.setSkills(String.join(",", skills));
        session.setSkillPlan(skillPlanStr);
        session.setCurrentDifficulty("medium");
        session.setQuestionIndex(0);
        session.setFinished(false);

        sessionRepository.save(session);
        if (firstRecord != null) questionRepository.save(firstRecord);

        Question first = firstRecord != null ? toQuestion(firstRecord) : null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("question", first);
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswer(String sessionId, String answer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId: " + sessionId));

        int idx = session.getQuestionIndex();
        int totalQuestions = getTotalQuestions(session.getSkillPlan());

        if (session.isFinished() || idx >= totalQuestions) {
            session.setFinished(true);
            sessionRepository.save(session);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("finished", true);
            result.put("nextQuestion", null);
            return result;
        }

        InterviewQuestionRecord current = questionRepository
                .findBySessionIdAndQuestionIndex(sessionId, idx)
                .orElseThrow(() -> new IllegalStateException("Question not found for index: " + idx));

        AnswerEvaluation eval = evaluator.evaluate(current.getId(), current.getText(), answer);

        InterviewEvaluationRecord evalRecord = new InterviewEvaluationRecord();
        evalRecord.setId(eval.getId());
        evalRecord.setSessionId(sessionId);
        evalRecord.setQuestionId(eval.getQuestionId());
        evalRecord.setScore(eval.getScore());
        evalRecord.setFeedback(eval.getFeedback());
        evalRecord.setImprovement(eval.getImprovement());
        evaluationRepository.save(evalRecord);

        int nextIdx = idx + 1;
        session.setQuestionIndex(nextIdx);

        Question next = null;
        if (nextIdx < totalQuestions) {
            String nextDifficulty = adaptDifficulty(eval.getScore());
            String nextSkill = getSkillForIndex(session.getSkillPlan(), nextIdx);
            session.setCurrentDifficulty(nextDifficulty);

            Question generated = questionAgent.generateQuestion(nextSkill, nextDifficulty);
            InterviewQuestionRecord nextRecord = new InterviewQuestionRecord();
            nextRecord.setId(generated.getId());
            nextRecord.setSessionId(sessionId);
            nextRecord.setQuestionIndex(nextIdx);
            nextRecord.setSkill(generated.getSkill());
            nextRecord.setDifficulty(generated.getDifficulty());
            nextRecord.setText(generated.getText());
            questionRepository.save(nextRecord);

            next = toQuestion(nextRecord);
        } else {
            session.setFinished(true);
        }

        sessionRepository.save(session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("finished", session.isFinished());
        result.put("evaluation", eval);
        result.put("nextQuestion", next);
        return result;
    }

    private String adaptDifficulty(int score) {
        if (score >= 8) return "hard";
        if (score >= 5) return "medium";
        return "easy";
    }

    private int getTotalQuestions(String skillPlan) {
        if (skillPlan == null || skillPlan.isBlank()) return 0;
        int total = 0;
        for (String part : skillPlan.split("\\|")) {
            String[] kv = part.split(":");
            if (kv.length == 2) total += Integer.parseInt(kv[1].trim());
        }
        return total;
    }

    private String getSkillForIndex(String skillPlan, int index) {
        if (skillPlan == null || skillPlan.isBlank()) return "General";
        int cursor = 0;
        String lastSkill = "General";
        for (String part : skillPlan.split("\\|")) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                lastSkill = kv[0].trim();
                int count = Integer.parseInt(kv[1].trim());
                if (index < cursor + count) return lastSkill;
                cursor += count;
            }
        }
        return lastSkill;
    }

    public InterviewContext getContext(String sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return null;

        List<InterviewQuestionRecord> qRecords =
                questionRepository.findBySessionIdOrderByQuestionIndex(sessionId);
        List<InterviewEvaluationRecord> eRecords =
                evaluationRepository.findBySessionId(sessionId);

        InterviewContext ctx = new InterviewContext();
        ctx.setResumeText(session.getResumeText());
        ctx.setJobDescription(session.getJobDescription());
        ctx.setSkills(session.getSkills() == null ? List.of()
                : Arrays.asList(session.getSkills().split(",")));
        ctx.setQuestionIndex(session.getQuestionIndex());
        ctx.setFinished(session.isFinished());

        List<Question> questions = new ArrayList<>();
        for (InterviewQuestionRecord r : qRecords) {
            questions.add(toQuestion(r));
        }
        ctx.setQuestions(questions);

        List<AnswerEvaluation> evaluations = new ArrayList<>();
        for (InterviewEvaluationRecord r : eRecords) {
            AnswerEvaluation ae = new AnswerEvaluation();
            ae.setId(r.getId());
            ae.setQuestionId(r.getQuestionId());
            ae.setScore(r.getScore());
            ae.setFeedback(r.getFeedback());
            ae.setImprovement(r.getImprovement());
            evaluations.add(ae);
        }
        ctx.setEvaluations(evaluations);

        return ctx;
    }


    public void streamEvaluation(String sessionId, String answer, SseEmitter emitter) {
        try {
            InterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId: " + sessionId));

            int idx = session.getQuestionIndex();
            int totalQuestions = getTotalQuestions(session.getSkillPlan());

            if (session.isFinished() || idx >= totalQuestions) {
                session.setFinished(true);
                sessionRepository.save(session);
                emitter.send(SseEmitter.event().name("done")
                        .data("{\"finished\":true,\"evaluation\":null,\"nextQuestion\":null}"));
                emitter.complete();
                return;
            }

            InterviewQuestionRecord current = questionRepository
                    .findBySessionIdAndQuestionIndex(sessionId, idx)
                    .orElseThrow(() -> new IllegalStateException("Question not found at index: " + idx));

            String systemPrompt = """
                    You are an expert technical interviewer.
                    Evaluate the candidate's answer fairly and concisely.
                    Output structured JSON:
                    {
                      "score": integer (1-10),
                      "feedback": "2-3 sentence comment on the answer quality",
                      "improvement": "one concrete suggestion to improve"
                    }
                    """;

            String userPrompt = String.format("""
                    Question: "%s"
                    Candidate Answer: "%s"
                    """, current.getText(), answer);

            int nextIdx = idx + 1;
            String nextSkill = nextIdx < totalQuestions ? getSkillForIndex(session.getSkillPlan(), nextIdx) : null;
            CompletableFuture<Question> nextQuestionFuture = (nextSkill != null)
                    ? CompletableFuture.supplyAsync(() -> questionAgent.generateQuestion(nextSkill, "medium"))
                    : CompletableFuture.completedFuture(null);

            StringBuilder fullResponse = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Throwable> streamError = new AtomicReference<>();

            gptClient.askChatStream(systemPrompt, userPrompt, 1.0)
                    .subscribe(
                            token -> {
                                fullResponse.append(token);
                                try {
                                    emitter.send(SseEmitter.event().name("token").data(token));
                                } catch (IOException e) {
                                    log.warn("Failed to send token to client", e);
                                }
                            },
                            error -> {
                                streamError.set(error);
                                latch.countDown();
                            },
                            latch::countDown
                    );

            boolean completed = latch.await(90, TimeUnit.SECONDS);
            if (!completed || streamError.get() != null) {
                nextQuestionFuture.cancel(true);
                emitter.completeWithError(
                        streamError.get() != null ? streamError.get()
                                : new RuntimeException("GPT stream timed out"));
                return;
            }

            String raw = fullResponse.toString();
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            String jsonPart = (start != -1 && end > start) ? raw.substring(start, end + 1) : raw;

            int score = 0;
            String feedback = raw;
            String improvement = "";
            try {
                var node = objectMapper.readTree(jsonPart);
                score = node.path("score").asInt(0);
                feedback = node.path("feedback").asText(raw);
                improvement = node.path("improvement").asText("");
            } catch (Exception e) {
                log.warn("Failed to parse streamed evaluation JSON", e);
            }

            AnswerEvaluation eval = new AnswerEvaluation();
            eval.setId(UUID.randomUUID().toString());
            eval.setQuestionId(current.getId());
            eval.setScore(score);
            eval.setFeedback(feedback);
            eval.setImprovement(improvement);

            InterviewEvaluationRecord evalRecord = new InterviewEvaluationRecord();
            evalRecord.setId(eval.getId());
            evalRecord.setSessionId(sessionId);
            evalRecord.setQuestionId(current.getId());
            evalRecord.setScore(score);
            evalRecord.setFeedback(feedback);
            evalRecord.setImprovement(improvement);
            evaluationRepository.save(evalRecord);

            session.setQuestionIndex(nextIdx);

            Question next = null;
            if (nextIdx < totalQuestions) {
                String actualDifficulty = adaptDifficulty(score);
                session.setCurrentDifficulty(actualDifficulty);

                try {
                    Question generated = nextQuestionFuture.get(30, TimeUnit.SECONDS);
                    InterviewQuestionRecord nextRecord = new InterviewQuestionRecord();
                    nextRecord.setId(generated.getId());
                    nextRecord.setSessionId(sessionId);
                    nextRecord.setQuestionIndex(nextIdx);
                    nextRecord.setSkill(generated.getSkill());
                    nextRecord.setDifficulty(generated.getDifficulty());
                    nextRecord.setText(generated.getText());
                    questionRepository.save(nextRecord);
                    next = toQuestion(nextRecord);
                } catch (Exception e) {
                    log.warn("Next question generation failed, marking session finished", e);
                    session.setFinished(true);
                }
            } else {
                session.setFinished(true);
            }
            sessionRepository.save(session);

            Map<String, Object> donePayload = new LinkedHashMap<>();
            donePayload.put("finished", session.isFinished());
            donePayload.put("evaluation", eval);
            donePayload.put("nextQuestion", next);
            emitter.send(SseEmitter.event().name("done").data(objectMapper.writeValueAsString(donePayload)));
            emitter.complete();

        } catch (Exception e) {
            log.error("Error in streamEvaluation", e);
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    public List<SessionListItem> getSessions(String userEmail) {
        List<InterviewSession> sessions = sessionRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        List<String> sessionIds = sessions.stream().map(InterviewSession::getId).collect(Collectors.toList());

        Map<String, List<InterviewEvaluationRecord>> evalsBySession = evaluationRepository
                .findBySessionIdIn(sessionIds).stream()
                .collect(Collectors.groupingBy(InterviewEvaluationRecord::getSessionId));

        return sessions.stream().map(s -> {
            List<InterviewEvaluationRecord> evals = evalsBySession.getOrDefault(s.getId(), List.of());
            double avg = evals.stream().mapToInt(InterviewEvaluationRecord::getScore).average().orElse(0.0);
            int total = getTotalQuestions(s.getSkillPlan());
            List<String> skills = s.getSkills() == null ? List.of()
                    : Arrays.asList(s.getSkills().split(","));
            return new SessionListItem(s.getId(), s.getCreatedAt(), skills, total,
                    evals.size(), Math.round(avg * 10.0) / 10.0, s.isFinished());
        }).collect(Collectors.toList());
    }

    public SessionSummary getSessionSummary(String sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<InterviewQuestionRecord> questions =
                questionRepository.findBySessionIdOrderByQuestionIndex(sessionId);
        List<InterviewEvaluationRecord> evals = evaluationRepository.findBySessionId(sessionId);

        Map<String, InterviewQuestionRecord> questionMap = questions.stream()
                .collect(Collectors.toMap(InterviewQuestionRecord::getId, q -> q));

        Map<String, List<Integer>> scoresBySkill = new HashMap<>();
        Map<String, List<Integer>> scoresByDifficulty = new HashMap<>();
        for (InterviewEvaluationRecord e : evals) {
            InterviewQuestionRecord q = questionMap.get(e.getQuestionId());
            if (q != null) {
                scoresBySkill.computeIfAbsent(q.getSkill(), k -> new ArrayList<>()).add(e.getScore());
                scoresByDifficulty.computeIfAbsent(q.getDifficulty(), k -> new ArrayList<>()).add(e.getScore());
            }
        }

        Map<String, Double> scoreBySkill = new LinkedHashMap<>();
        scoresBySkill.forEach((skill, scores) -> {
            double avg = scores.stream().mapToInt(i -> i).average().orElse(0.0);
            scoreBySkill.put(skill, Math.round(avg * 10.0) / 10.0);
        });

        Map<String, Double> scoreByDifficulty = new LinkedHashMap<>();
        scoresByDifficulty.forEach((diff, scores) -> {
            double avg = scores.stream().mapToInt(i -> i).average().orElse(0.0);
            scoreByDifficulty.put(diff, Math.round(avg * 10.0) / 10.0);
        });

        List<String> weakSkills = scoreBySkill.entrySet().stream()
                .filter(e -> e.getValue() < 5).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> strongSkills = scoreBySkill.entrySet().stream()
                .filter(e -> e.getValue() >= 8).map(Map.Entry::getKey).collect(Collectors.toList());

        double avgScore = evals.stream().mapToInt(InterviewEvaluationRecord::getScore).average().orElse(0.0);
        List<String> skills = session.getSkills() == null ? List.of()
                : Arrays.asList(session.getSkills().split(","));

        return new SessionSummary(sessionId, session.getCreatedAt(), skills,
                getTotalQuestions(session.getSkillPlan()), evals.size(),
                Math.round(avgScore * 10.0) / 10.0, session.isFinished(),
                scoreBySkill, scoreByDifficulty, weakSkills, strongSkills);
    }

    private Question toQuestion(InterviewQuestionRecord r) {
        return new Question(r.getId(), r.getSkill(), r.getDifficulty(), r.getText());
    }
}
