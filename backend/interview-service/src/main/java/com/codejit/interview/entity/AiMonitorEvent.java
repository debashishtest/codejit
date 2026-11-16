package com.codejit.interview.entity;

import com.codejit.common.dto.ai.AiMonitorSeverity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_monitor_events")
public class AiMonitorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AiInterviewSession session;

    @Column(nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiMonitorSeverity severity = AiMonitorSeverity.INFO;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private Long turnId;

    @Column(nullable = false)
    private Integer focusScoreImpact = 0;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    public AiMonitorEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AiInterviewSession getSession() { return session; }
    public void setSession(AiInterviewSession session) { this.session = session; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public AiMonitorSeverity getSeverity() { return severity; }
    public void setSeverity(AiMonitorSeverity severity) { this.severity = severity; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Long getTurnId() { return turnId; }
    public void setTurnId(Long turnId) { this.turnId = turnId; }

    public Integer getFocusScoreImpact() { return focusScoreImpact; }
    public void setFocusScoreImpact(Integer focusScoreImpact) { this.focusScoreImpact = focusScoreImpact; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
