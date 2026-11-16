package com.codejit.common.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiInterviewSessionDto {
    private Long id;
    private String title;
    private String topic;
    private String roleTitle;
    private AiDifficulty difficulty;
    private AiInterviewMode mode;
    private AiInterviewStatus status;
    private Integer questionCount;
    private Integer durationMinutes;
    private String focusAreas;
    private boolean enableFollowUps;
    private boolean strictMonitoring;
    private String candidateEmail;
    private Integer currentTurnIndex;
    private Integer overallScore;
    private AiRecommendation recommendation;
    private String aiProviderUsed;
    private AiMonitoringSummaryDto monitoring;
    private AiFinalReportDto finalReport;
    private List<AiInterviewTurnDto> turns = new ArrayList<>();
    private List<AiMonitorEventDto> monitorEvents = new ArrayList<>();
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public AiInterviewSessionDto() {}

    public AiInterviewSessionDto(
            Long id,
            String title,
            String topic,
            String roleTitle,
            AiDifficulty difficulty,
            AiInterviewMode mode,
            AiInterviewStatus status,
            Integer questionCount,
            Integer durationMinutes,
            String focusAreas,
            boolean enableFollowUps,
            boolean strictMonitoring,
            String candidateEmail,
            Integer currentTurnIndex,
            Integer overallScore,
            AiRecommendation recommendation,
            String aiProviderUsed,
            AiMonitoringSummaryDto monitoring,
            AiFinalReportDto finalReport,
            List<AiInterviewTurnDto> turns,
            List<AiMonitorEventDto> monitorEvents,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.roleTitle = roleTitle;
        this.difficulty = difficulty;
        this.mode = mode;
        this.status = status;
        this.questionCount = questionCount;
        this.durationMinutes = durationMinutes;
        this.focusAreas = focusAreas;
        this.enableFollowUps = enableFollowUps;
        this.strictMonitoring = strictMonitoring;
        this.candidateEmail = candidateEmail;
        this.currentTurnIndex = currentTurnIndex;
        this.overallScore = overallScore;
        this.recommendation = recommendation;
        this.aiProviderUsed = aiProviderUsed;
        this.monitoring = monitoring;
        this.finalReport = finalReport;
        this.turns = turns != null ? turns : new ArrayList<>();
        this.monitorEvents = monitorEvents != null ? monitorEvents : new ArrayList<>();
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
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

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getFocusAreas() { return focusAreas; }
    public void setFocusAreas(String focusAreas) { this.focusAreas = focusAreas; }

    public boolean isEnableFollowUps() { return enableFollowUps; }
    public void setEnableFollowUps(boolean enableFollowUps) { this.enableFollowUps = enableFollowUps; }

    public boolean isStrictMonitoring() { return strictMonitoring; }
    public void setStrictMonitoring(boolean strictMonitoring) { this.strictMonitoring = strictMonitoring; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public Integer getCurrentTurnIndex() { return currentTurnIndex; }
    public void setCurrentTurnIndex(Integer currentTurnIndex) { this.currentTurnIndex = currentTurnIndex; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public AiRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(AiRecommendation recommendation) { this.recommendation = recommendation; }

    public String getAiProviderUsed() { return aiProviderUsed; }
    public void setAiProviderUsed(String aiProviderUsed) { this.aiProviderUsed = aiProviderUsed; }

    public AiMonitoringSummaryDto getMonitoring() { return monitoring; }
    public void setMonitoring(AiMonitoringSummaryDto monitoring) { this.monitoring = monitoring; }

    public AiFinalReportDto getFinalReport() { return finalReport; }
    public void setFinalReport(AiFinalReportDto finalReport) { this.finalReport = finalReport; }

    public List<AiInterviewTurnDto> getTurns() { return turns; }
    public void setTurns(List<AiInterviewTurnDto> turns) { this.turns = turns != null ? turns : new ArrayList<>(); }

    public List<AiMonitorEventDto> getMonitorEvents() { return monitorEvents; }
    public void setMonitorEvents(List<AiMonitorEventDto> monitorEvents) { this.monitorEvents = monitorEvents != null ? monitorEvents : new ArrayList<>(); }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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
        private Integer durationMinutes;
        private String focusAreas;
        private boolean enableFollowUps;
        private boolean strictMonitoring;
        private String candidateEmail;
        private Integer currentTurnIndex;
        private Integer overallScore;
        private AiRecommendation recommendation;
        private String aiProviderUsed;
        private AiMonitoringSummaryDto monitoring;
        private AiFinalReportDto finalReport;
        private List<AiInterviewTurnDto> turns = new ArrayList<>();
        private List<AiMonitorEventDto> monitorEvents = new ArrayList<>();
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder roleTitle(String roleTitle) { this.roleTitle = roleTitle; return this; }
        public Builder difficulty(AiDifficulty difficulty) { this.difficulty = difficulty; return this; }
        public Builder mode(AiInterviewMode mode) { this.mode = mode; return this; }
        public Builder status(AiInterviewStatus status) { this.status = status; return this; }
        public Builder questionCount(Integer questionCount) { this.questionCount = questionCount; return this; }
        public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder focusAreas(String focusAreas) { this.focusAreas = focusAreas; return this; }
        public Builder enableFollowUps(boolean enableFollowUps) { this.enableFollowUps = enableFollowUps; return this; }
        public Builder strictMonitoring(boolean strictMonitoring) { this.strictMonitoring = strictMonitoring; return this; }
        public Builder candidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; return this; }
        public Builder currentTurnIndex(Integer currentTurnIndex) { this.currentTurnIndex = currentTurnIndex; return this; }
        public Builder overallScore(Integer overallScore) { this.overallScore = overallScore; return this; }
        public Builder recommendation(AiRecommendation recommendation) { this.recommendation = recommendation; return this; }
        public Builder aiProviderUsed(String aiProviderUsed) { this.aiProviderUsed = aiProviderUsed; return this; }
        public Builder monitoring(AiMonitoringSummaryDto monitoring) { this.monitoring = monitoring; return this; }
        public Builder finalReport(AiFinalReportDto finalReport) { this.finalReport = finalReport; return this; }
        public Builder turns(List<AiInterviewTurnDto> turns) { this.turns = turns; return this; }
        public Builder monitorEvents(List<AiMonitorEventDto> monitorEvents) { this.monitorEvents = monitorEvents; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AiInterviewSessionDto build() {
            return new AiInterviewSessionDto(id, title, topic, roleTitle, difficulty, mode, status, questionCount, durationMinutes, focusAreas, enableFollowUps, strictMonitoring, candidateEmail, currentTurnIndex, overallScore, recommendation, aiProviderUsed, monitoring, finalReport, turns, monitorEvents, startedAt, completedAt, createdAt);
        }
    }
}
