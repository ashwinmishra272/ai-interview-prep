package com.ashwin.aiinterviewprep.agents;

import com.ashwin.aiinterviewprep.model.AnswerEvaluation;
import com.ashwin.aiinterviewprep.model.InterviewContext;
import com.ashwin.aiinterviewprep.model.Question;
import com.ashwin.aiinterviewprep.model.SkillPlan;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central orchestrator. Keeps an in-memory map of sessionId -> InterviewContext for demo.
 */
@Component
public class InterviewAgent {

    private final ISkillExtractor skillExtractor;
    private final IPlannerAgent planner;
    private final IQuestionAgent questionAgent;
    private final IEvaluatorAgent evaluator;

    // very simple in-memory session store (demo only)
    private final Map<String, InterviewContext> sessions = new ConcurrentHashMap<>();

    public InterviewAgent(ISkillExtractor skillExtractor,
                          IPlannerAgent planner,
                          IQuestionAgent questionAgent,
                          IEvaluatorAgent evaluator) {
        this.skillExtractor = skillExtractor;
        this.planner = planner;
        this.questionAgent = questionAgent;
        this.evaluator = evaluator;
    }

    /**
     * Create a session: extract skills, build a plan, generate first question.
     * Returns sessionId and first Question.
     */
    public Map<String, Object> startSession(String resumeText, String jdText) {
        String sessionId = UUID.randomUUID().toString();
        InterviewContext ctx = new InterviewContext();
        ctx.setResumeText(resumeText);
        ctx.setJobDescription(jdText);

        // Step 1: extract skills
        List<String> skills = skillExtractor.extractSkills(resumeText, jdText);
        ctx.setSkills(skills);

        // Step 2: plan
        List<SkillPlan> plan = planner.plan(skills);

        for (SkillPlan p : plan) {
            for (int i = 0; i < p.getQuestionsCount(); i++) {
                String difficulty = (i == 0) ? "medium" : "hard";
                Question q = questionAgent.generateQuestion(p.getSkill(), difficulty);
                ctx.getQuestions().add(q);
            }
        }

        // store session
        sessions.put(sessionId, ctx);

        Question first = ctx.getQuestions().isEmpty() ? null : ctx.getQuestions().get(0);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("question", first);
        return result;
    }

    /**
     * Submit answer for current question in session, evaluate and return next question (or null if done).
     */
    public Map<String, Object> submitAnswer(String sessionId, String answer) {
        InterviewContext ctx = sessions.get(sessionId);
        if (ctx == null) throw new IllegalArgumentException("Invalid sessionId");

        int idx = ctx.getQuestionIndex();
        if (idx >= ctx.getQuestions().size()) {
            ctx.setFinished(true);
            return Map.of("finished", true, "nextQuestion", null);
        }

        Question current = ctx.getQuestions().get(idx);
        AnswerEvaluation eval = evaluator.evaluate(current.getId(), current.getText(), answer);
        ctx.getEvaluations().add(eval);

        ctx.setQuestionIndex(idx + 1);

        Question next = (ctx.getQuestionIndex() < ctx.getQuestions().size()) ? ctx.getQuestions().get(ctx.getQuestionIndex()) : null;
        if (next == null) ctx.setFinished(true);

        return Map.of("finished", ctx.isFinished(), "nextQuestion", next, "evaluation", eval);
    }

    public InterviewContext getContext(String sessionId) {
        return sessions.get(sessionId);
    }
}
