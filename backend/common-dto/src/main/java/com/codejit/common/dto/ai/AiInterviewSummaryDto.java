package com.codejit.common.dto.ai;

import java.time.LocalDateTime;

public class AiInterviewSummaryDto {
    private Long id;
    private String title;
    private String topic;
    private String roleTitle;
    private AiDifficulty difficulty;
    private AiInterviewMode mode;
    private AiInterviewStatus status;
    private Integer questionCount;
    private Integer answeredCount;
    private Integer durationMinutes;
    private Integer overallScore;
    private Integer focusIntegrityScore;
    private AiRecommendation recommendation;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public AiInterviewSummaryDto() {}

    public AiInterviewSummaryDto(
            Long id,
            String title,
            String topic,
            String roleTitle,
            AiDifficulty difficulty,
            AiInterviewMode mode,
            AiInterviewStatus status,
            Integer questionCount,
            Integer answeredCount,
            Integer durationMinutes,
            Integer overallScore,
            Integer focusIntegrityScore,
            AiRecommendation recommendation,
            LocalDateTime createdAt,
            LocalDateTime completedAt) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.roleTitle = roleTitle;
        this.difficulty = difficulty;
        this.mode = mode;
        this.status = status;
        this.questionCount = questionCount;
        this.answeredCount = answeredCount;
        this.durationMinutes = durationMinutes;
        this.overallScore = overallScore;
        this.focusIntegrityScore = focusIntegrityScore;
        this.recommendation = recommendation;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public AiDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(AiDifficulty difficulty) { this.difficulty = difficulty; }

    public AiInterviewMode getMode() { return mode; }
    public void setMode(AiInterviewMode mode) { this.mode = mode; }

    public AiInterviewStatus getStatus() { return status; }
    public void setStatus(AiInterviewStatus status) { this.status = status; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public Integer getAnsweredCount() { return answeredCount; }
    public void setAnsweredCount(Integer answeredCount) { this.answeredCount = answeredCount; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public Integer getFocusIntegrityScore() { return focusIntegrityScore; }
    public void setFocusIntegrityScore(Integer focusIntegrityScore) { this.focusIntegrityScore = focusIntegrityScore; }

    public AiRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(AiRecommendation recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String topic;
        private String roleTitle;
        private AiDifficulty difficulty;
        private AiInterviewMode mode;
        private AiInterviewStatus status;
        private Integer questionCount;
        private Integer answeredCount;
        private Integer durationMinutes;
        private Integer overallScore;
        private Integer focusIntegrityScore;
        private AiRecommendation recommendation;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder roleTitle(String roleTitle) { this.roleTitle = roleTitle; return this; }
        public Builder difficulty(AiDifficulty difficulty) { this.difficulty = difficulty; return this; }
        public Builder mode(AiInterviewMode mode) { this.mode = mode; return this; }
        public Builder status(AiInterviewStatus status) { this.status = status; return this; }
        public Builder questionCount(Integer questionCount) { this.questionCount = questionCount; return this; }
        public Builder answeredCount(Integer answeredCount) { this.answeredCount = answeredCount; return this; }
        public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder overallScore(Integer overallScore) { this.overallScore = overallScore; return this; }
        public Builder focusIntegrityScore(Integer focusIntegrityScore) { this.focusIntegrityScore = focusIntegrityScore; return this; }
        public Builder recommendation(AiRecommendation recommendation) { this.recommendation = recommendation; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public AiInterviewSummaryDto build() {
            return new AiInterviewSummaryDto(id, title, topic, roleTitle, difficulty, mode, status, questionCount, answeredCount, durationMinutes, overallScore, focusIntegrityScore, recommendation, createdAt, completedAt);
        }
    }
}
