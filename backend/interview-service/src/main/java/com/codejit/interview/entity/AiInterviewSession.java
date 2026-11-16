package com.codejit.interview.entity;

import com.codejit.common.dto.ai.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_interview_sessions")
public class AiInterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String topic;

    private String roleTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiDifficulty difficulty = AiDifficulty.MID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiInterviewMode mode = AiInterviewMode.MIXED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiInterviewStatus status = AiInterviewStatus.IN_PROGRESS;

    @Column(nullable = false)
    private Integer questionCount = 5;

    @Column(nullable = false)
    private Integer durationMinutes = 30;

    @Column(columnDefinition = "TEXT")
    private String focusAreas;

    @Column(nullable = false)
    private boolean enableFollowUps = true;

    @Column(nullable = false)
    private boolean strictMonitoring = true;

    @Column(nullable = false)
    private String candidateEmail;

    private Long candidateUserId;

    @Column(nullable = false)
    private Integer currentTurnIndex = 0;

    @Column(nullable = false)
    private Integer focusIntegrityScore = 100;

    @Column(nullable = false)
    private Integer tabSwitchCount = 0;

    @Column(nullable = false)
    private Integer windowBlurCount = 0;

    @Column(nullable = false)
    private Integer pasteEventCount = 0;

    @Column(nullable = false)
    private Integer idleWarningCount = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer eyeOffScreenCount = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer cheatingAlertCount = 0;

    private Integer overallScore;

    @Enumerated(EnumType.STRING)
    private AiRecommendation recommendation;

    @Column(columnDefinition = "TEXT")
    private String finalReportJson;

    private String aiProviderUsed;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("turnNumber ASC")
    private List<AiInterviewTurn> turns = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("occurredAt DESC")
    private List<AiMonitorEvent> monitorEvents = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public AiInterviewSession() {}

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

    public Long getCandidateUserId() { return candidateUserId; }
    public void setCandidateUserId(Long candidateUserId) { this.candidateUserId = candidateUserId; }

    public Integer getCurrentTurnIndex() { return currentTurnIndex; }
    public void setCurrentTurnIndex(Integer currentTurnIndex) { this.currentTurnIndex = currentTurnIndex; }

    public Integer getFocusIntegrityScore() { return focusIntegrityScore; }
    public void setFocusIntegrityScore(Integer focusIntegrityScore) { this.focusIntegrityScore = focusIntegrityScore; }

    public Integer getTabSwitchCount() { return tabSwitchCount; }
    public void setTabSwitchCount(Integer tabSwitchCount) { this.tabSwitchCount = tabSwitchCount; }

    public Integer getWindowBlurCount() { return windowBlurCount; }
    public void setWindowBlurCount(Integer windowBlurCount) { this.windowBlurCount = windowBlurCount; }

    public Integer getPasteEventCount() { return pasteEventCount; }
    public void setPasteEventCount(Integer pasteEventCount) { this.pasteEventCount = pasteEventCount; }

    public Integer getIdleWarningCount() { return idleWarningCount; }
    public void setIdleWarningCount(Integer idleWarningCount) { this.idleWarningCount = idleWarningCount; }

    public Integer getEyeOffScreenCount() { return eyeOffScreenCount != null ? eyeOffScreenCount : 0; }
    public void setEyeOffScreenCount(Integer eyeOffScreenCount) { this.eyeOffScreenCount = eyeOffScreenCount != null ? eyeOffScreenCount : 0; }

    public Integer getCheatingAlertCount() { return cheatingAlertCount != null ? cheatingAlertCount : 0; }
    public void setCheatingAlertCount(Integer cheatingAlertCount) { this.cheatingAlertCount = cheatingAlertCount != null ? cheatingAlertCount : 0; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public AiRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(AiRecommendation recommendation) { this.recommendation = recommendation; }

    public String getFinalReportJson() { return finalReportJson; }
    public void setFinalReportJson(String finalReportJson) { this.finalReportJson = finalReportJson; }

    public String getAiProviderUsed() { return aiProviderUsed; }
    public void setAiProviderUsed(String aiProviderUsed) { this.aiProviderUsed = aiProviderUsed; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<AiInterviewTurn> getTurns() { return turns; }
    public void setTurns(List<AiInterviewTurn> turns) { this.turns = turns != null ? turns : new ArrayList<>(); }

    public List<AiMonitorEvent> getMonitorEvents() { return monitorEvents; }
    public void setMonitorEvents(List<AiMonitorEvent> monitorEvents) { this.monitorEvents = monitorEvents != null ? monitorEvents : new ArrayList<>(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addTurn(AiInterviewTurn turn) {
        turns.add(turn);
        turn.setSession(this);
    }

    public void addMonitorEvent(AiMonitorEvent event) {
        monitorEvents.add(event);
        event.setSession(this);
    }
}
