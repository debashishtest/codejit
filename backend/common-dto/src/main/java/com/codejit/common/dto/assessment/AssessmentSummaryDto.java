package com.codejit.common.dto.assessment;

public class AssessmentSummaryDto {
    private Long id;
    private String title;
    private int durationMinutes;
    private AssessmentStatus status;
    private int questionCount;

    public AssessmentSummaryDto() {}

    public AssessmentSummaryDto(Long id, String title, int durationMinutes, AssessmentStatus status, int questionCount) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.questionCount = questionCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private int durationMinutes;
        private AssessmentStatus status;
        private int questionCount;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder status(AssessmentStatus status) { this.status = status; return this; }
        public Builder questionCount(int questionCount) { this.questionCount = questionCount; return this; }

        public AssessmentSummaryDto build() {
            return new AssessmentSummaryDto(id, title, durationMinutes, status, questionCount);
        }
    }
}

