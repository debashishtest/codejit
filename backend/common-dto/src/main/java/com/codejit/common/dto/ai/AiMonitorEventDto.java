package com.codejit.common.dto.ai;

import java.time.LocalDateTime;

public class AiMonitorEventDto {
    private Long id;
    private String eventType;
    private AiMonitorSeverity severity;
    private String detail;
    private Long turnId;
    private Integer focusScoreImpact;
    private LocalDateTime occurredAt;

    public AiMonitorEventDto() {}

    public AiMonitorEventDto(
            Long id,
            String eventType,
            AiMonitorSeverity severity,
            String detail,
            Long turnId,
            Integer focusScoreImpact,
            LocalDateTime occurredAt) {
        this.id = id;
        this.eventType = eventType;
        this.severity = severity;
        this.detail = detail;
        this.turnId = turnId;
        this.focusScoreImpact = focusScoreImpact;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String eventType;
        private AiMonitorSeverity severity;
        private String detail;
        private Long turnId;
        private Integer focusScoreImpact;
        private LocalDateTime occurredAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder severity(AiMonitorSeverity severity) { this.severity = severity; return this; }
        public Builder detail(String detail) { this.detail = detail; return this; }
        public Builder turnId(Long turnId) { this.turnId = turnId; return this; }
        public Builder focusScoreImpact(Integer focusScoreImpact) { this.focusScoreImpact = focusScoreImpact; return this; }
        public Builder occurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; return this; }

        public AiMonitorEventDto build() {
            return new AiMonitorEventDto(id, eventType, severity, detail, turnId, focusScoreImpact, occurredAt);
        }
    }
}
