package com.codejit.interview.repository;

import com.codejit.interview.entity.InterviewRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<InterviewRoom, Long> {
    Optional<InterviewRoom> findByShareCodeIgnoreCase(String shareCode);

    @Query("SELECT r FROM InterviewRoom r LEFT JOIN FETCH r.participants WHERE r.id = :id")
    Optional<InterviewRoom> findByIdWithParticipants(Long id);

    @Query("SELECT r FROM InterviewRoom r LEFT JOIN FETCH r.participants WHERE LOWER(r.shareCode) = LOWER(:shareCode)")
    Optional<InterviewRoom> findByShareCodeWithParticipants(String shareCode);

    List<InterviewRoom> findByHostEmailOrderByCreatedAtDesc(String hostEmail);
}

