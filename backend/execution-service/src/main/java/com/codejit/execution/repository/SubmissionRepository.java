package com.codejit.execution.repository;

import com.codejit.execution.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.results WHERE s.id = :id")
    Optional<Submission> findByIdWithResults(Long id);

    List<Submission> findByAssessmentIdAndCandidateEmail(Long assessmentId, String candidateEmail);
}

