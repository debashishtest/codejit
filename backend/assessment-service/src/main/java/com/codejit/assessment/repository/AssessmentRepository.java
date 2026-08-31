package com.codejit.assessment.repository;

import com.codejit.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByShareCodeIgnoreCase(String shareCode);

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.questions WHERE a.id = :id")
    Optional<Assessment> findByIdWithQuestions(Long id);

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.questions WHERE LOWER(a.shareCode) = LOWER(:shareCode)")
    Optional<Assessment> findByShareCodeWithQuestions(String shareCode);

    List<Assessment> findByCreatorEmailOrderByCreatedAtDesc(String creatorEmail);
}

