package com.ashwin.aiinterviewprep.agents;

import com.ashwin.aiinterviewprep.model.AnswerEvaluation;

/**
 * Evaluate an answer for a question and return score+feedback.
 */
public interface IEvaluatorAgent {
    AnswerEvaluation evaluate(String questionId, String questionText, String answer);
}
