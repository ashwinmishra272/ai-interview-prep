package com.ashwin.aiinterviewprep.repository;

import com.ashwin.aiinterviewprep.model.InterviewQuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestionRecord, String> {
    List<InterviewQuestionRecord> findBySessionIdOrderByQuestionIndex(String sessionId);
    Optional<InterviewQuestionRecord> findBySessionIdAndQuestionIndex(String sessionId, int questionIndex);
}
