package com.codejit.interview.repository;

import com.codejit.interview.entity.AiMonitorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMonitorEventRepository extends JpaRepository<AiMonitorEvent, Long> {
    List<AiMonitorEvent> findBySessionIdOrderByOccurredAtDescIdDesc(Long sessionId);
}
