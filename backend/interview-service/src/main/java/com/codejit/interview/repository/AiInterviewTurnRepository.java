package com.codejit.interview.repository;

import com.codejit.interview.entity.AiInterviewTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInterviewTurnRepository extends JpaRepository<AiInterviewTurn, Long> {
    List<AiInterviewTurn> findBySessionIdOrderByTurnNumberAsc(Long sessionId);
}
