package com.codejit.interview.repository;

import com.codejit.interview.entity.InterviewParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, Long> {
    List<InterviewParticipant> findByInterviewRoomId(Long roomId);
    Optional<InterviewParticipant> findByInterviewRoomIdAndUsername(Long roomId, String username);
}

