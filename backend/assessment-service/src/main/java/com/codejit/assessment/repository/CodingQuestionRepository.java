package com.codejit.assessment.repository;

import com.codejit.assessment.entity.CodingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, Long> {
    List<CodingQuestion> findByAssessmentIdOrderByQuestionNumberAsc(Long assessmentId);

    @Query("SELECT q FROM CodingQuestion q LEFT JOIN FETCH q.testCases WHERE q.id = :id")
    Optional<CodingQuestion> findByIdWithTestCases(Long id);
}

