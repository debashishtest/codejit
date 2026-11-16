package com.codejit.interview.repository;

import com.codejit.interview.entity.AiInterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiInterviewSessionRepository extends JpaRepository<AiInterviewSession, Long> {
    List<AiInterviewSession> findByCandidateEmailOrderByCreatedAtDesc(String candidateEmail);

    @Query("SELECT DISTINCT s FROM AiInterviewSession s LEFT JOIN FETCH s.turns WHERE s.id = :id")
    Optional<AiInterviewSession> findByIdWithTurns(Long id);
}
