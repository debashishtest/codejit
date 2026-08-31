package com.codejit.assessment.repository;

import com.codejit.assessment.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByCodingQuestionIdOrderBySequenceAsc(Long codingQuestionId);
    List<TestCase> findByCodingQuestionIdAndVisibleTrueOrderBySequenceAsc(Long codingQuestionId);
}

