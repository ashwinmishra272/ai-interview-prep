package com.ashwin.aiinterviewprep.repository;

import com.ashwin.aiinterviewprep.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, String> {
    List<InterviewSession> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
