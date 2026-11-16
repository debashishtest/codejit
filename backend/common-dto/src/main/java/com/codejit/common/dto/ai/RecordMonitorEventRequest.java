package com.codejit.common.dto.ai;

public class RecordMonitorEventRequest {
    private String eventType;
    private AiMonitorSeverity severity = AiMonitorSeverity.INFO;
    private String detail;
    private Long turnId;
    private String metadata;

    public RecordMonitorEventRequest() {}

    public RecordMonitorEventRequest(String eventType, AiMonitorSeverity severity, String detail, Long turnId, String metadata) {
        this.eventType = eventType;
        this.severity = severity != null ? severity : AiMonitorSeverity.INFO;
        this.detail = detail;
        this.turnId = turnId;
        this.metadata = metadata;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public AiMonitorSeverity getSeverity() { return severity; }
    public void setSeverity(AiMonitorSeverity severity) { this.severity = severity; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Long getTurnId() { return turnId; }
    public void setTurnId(Long turnId) { this.turnId = turnId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String eventType;
        private AiMonitorSeverity severity = AiMonitorSeverity.INFO;
        private String detail;
        private Long turnId;
        private String metadata;

        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder severity(AiMonitorSeverity severity) { this.severity = severity; return this; }
        public Builder detail(String detail) { this.detail = detail; return this; }
        public Builder turnId(Long turnId) { this.turnId = turnId; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }

        public RecordMonitorEventRequest build() {
            return new RecordMonitorEventRequest(eventType, severity, detail, turnId, metadata);
        }
    }
}
