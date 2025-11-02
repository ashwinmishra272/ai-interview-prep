package com.ashwin.aiinterviewprep.agents;

import com.ashwin.aiinterviewprep.model.Question;

public interface IQuestionAgent {
    /**
     * Generate a question for given skill and difficulty.
     */
    Question generateQuestion(String skill, String difficulty);
}
