package com.codejit.common.dto.interview;

public class InterviewRequest {
    private String title;
    private String description;
    private Long assessmentId;
    private String scheduledStart;
    private String scheduledEnd;

    public InterviewRequest() {}

    public InterviewRequest(String title, String description, Long assessmentId, String scheduledStart, String scheduledEnd) {
        this.title = title;
        this.description = description;
        this.assessmentId = assessmentId;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; }

    public String getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String description;
        private Long assessmentId;
        private String scheduledStart;
        private String scheduledEnd;

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder scheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; return this; }
        public Builder scheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; return this; }

        public InterviewRequest build() {
            return new InterviewRequest(title, description, assessmentId, scheduledStart, scheduledEnd);
        }
    }
}

