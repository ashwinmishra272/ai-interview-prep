package com.ashwin.aiinterviewprep.repository;

import com.ashwin.aiinterviewprep.model.InterviewEvaluationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluationRecord, String> {
    List<InterviewEvaluationRecord> findBySessionId(String sessionId);
    List<InterviewEvaluationRecord> findBySessionIdIn(List<String> sessionIds);
}
