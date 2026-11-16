package com.codejit.common.dto.ai;

import java.util.ArrayList;
import java.util.List;

public class AiMonitoringSummaryDto {
    private AiMonitorStatus monitorStatus;
    private Integer focusIntegrityScore;
    private Integer tabSwitchCount;
    private Integer windowBlurCount;
    private Integer pasteEventCount;
    private Integer idleWarningCount;
    private Integer eyeOffScreenCount;
    private Integer cheatingAlertCount;
    private Integer questionsAnswered;
    private Integer questionsSkipped;
    private Integer totalPlannedQuestions;
    private Long elapsedSeconds;
    private Integer averageResponseSeconds;
    private Integer runningAverageScore;
    private String scoreTrend;
    private String liveObservation;
    private List<String> alerts = new ArrayList<>();

    public AiMonitoringSummaryDto() {}

    public AiMonitoringSummaryDto(
            AiMonitorStatus monitorStatus,
            Integer focusIntegrityScore,
            Integer tabSwitchCount,
            Integer windowBlurCount,
            Integer pasteEventCount,
            Integer idleWarningCount,
            Integer eyeOffScreenCount,
            Integer cheatingAlertCount,
            Integer questionsAnswered,
            Integer questionsSkipped,
            Integer totalPlannedQuestions,
            Long elapsedSeconds,
            Integer averageResponseSeconds,
            Integer runningAverageScore,
            String scoreTrend,
            String liveObservation,
            List<String> alerts) {
        this.monitorStatus = monitorStatus;
        this.focusIntegrityScore = focusIntegrityScore;
        this.tabSwitchCount = tabSwitchCount;
        this.windowBlurCount = windowBlurCount;
        this.pasteEventCount = pasteEventCount;
        this.idleWarningCount = idleWarningCount;
        this.eyeOffScreenCount = eyeOffScreenCount != null ? eyeOffScreenCount : 0;
        this.cheatingAlertCount = cheatingAlertCount != null ? cheatingAlertCount : 0;
        this.questionsAnswered = questionsAnswered;
        this.questionsSkipped = questionsSkipped;
        this.totalPlannedQuestions = totalPlannedQuestions;
        this.elapsedSeconds = elapsedSeconds;
        this.averageResponseSeconds = averageResponseSeconds;
        this.runningAverageScore = runningAverageScore;
        this.scoreTrend = scoreTrend;
        this.liveObservation = liveObservation;
        this.alerts = alerts != null ? alerts : new ArrayList<>();
    }

    public AiMonitorStatus getMonitorStatus() { return monitorStatus; }
    public void setMonitorStatus(AiMonitorStatus monitorStatus) { this.monitorStatus = monitorStatus; }

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

    public Integer getEyeOffScreenCount() { return eyeOffScreenCount; }
    public void setEyeOffScreenCount(Integer eyeOffScreenCount) { this.eyeOffScreenCount = eyeOffScreenCount; }

    public Integer getCheatingAlertCount() { return cheatingAlertCount; }
    public void setCheatingAlertCount(Integer cheatingAlertCount) { this.cheatingAlertCount = cheatingAlertCount; }

    public Integer getQuestionsAnswered() { return questionsAnswered; }
    public void setQuestionsAnswered(Integer questionsAnswered) { this.questionsAnswered = questionsAnswered; }

    public Integer getQuestionsSkipped() { return questionsSkipped; }
    public void setQuestionsSkipped(Integer questionsSkipped) { this.questionsSkipped = questionsSkipped; }

    public Integer getTotalPlannedQuestions() { return totalPlannedQuestions; }
    public void setTotalPlannedQuestions(Integer totalPlannedQuestions) { this.totalPlannedQuestions = totalPlannedQuestions; }

    public Long getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(Long elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }

    public Integer getAverageResponseSeconds() { return averageResponseSeconds; }
    public void setAverageResponseSeconds(Integer averageResponseSeconds) { this.averageResponseSeconds = averageResponseSeconds; }

    public Integer getRunningAverageScore() { return runningAverageScore; }
    public void setRunningAverageScore(Integer runningAverageScore) { this.runningAverageScore = runningAverageScore; }

    public String getScoreTrend() { return scoreTrend; }
    public void setScoreTrend(String scoreTrend) { this.scoreTrend = scoreTrend; }

    public String getLiveObservation() { return liveObservation; }
    public void setLiveObservation(String liveObservation) { this.liveObservation = liveObservation; }

    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts != null ? alerts : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AiMonitorStatus monitorStatus;
        private Integer focusIntegrityScore;
        private Integer tabSwitchCount;
        private Integer windowBlurCount;
        private Integer pasteEventCount;
        private Integer idleWarningCount;
        private Integer eyeOffScreenCount = 0;
        private Integer cheatingAlertCount = 0;
        private Integer questionsAnswered;
        private Integer questionsSkipped;
        private Integer totalPlannedQuestions;
        private Long elapsedSeconds;
        private Integer averageResponseSeconds;
        private Integer runningAverageScore;
        private String scoreTrend;
        private String liveObservation;
        private List<String> alerts = new ArrayList<>();

        public Builder monitorStatus(AiMonitorStatus monitorStatus) { this.monitorStatus = monitorStatus; return this; }
        public Builder focusIntegrityScore(Integer focusIntegrityScore) { this.focusIntegrityScore = focusIntegrityScore; return this; }
        public Builder tabSwitchCount(Integer tabSwitchCount) { this.tabSwitchCount = tabSwitchCount; return this; }
        public Builder windowBlurCount(Integer windowBlurCount) { this.windowBlurCount = windowBlurCount; return this; }
        public Builder pasteEventCount(Integer pasteEventCount) { this.pasteEventCount = pasteEventCount; return this; }
        public Builder idleWarningCount(Integer idleWarningCount) { this.idleWarningCount = idleWarningCount; return this; }
        public Builder eyeOffScreenCount(Integer eyeOffScreenCount) { this.eyeOffScreenCount = eyeOffScreenCount; return this; }
        public Builder cheatingAlertCount(Integer cheatingAlertCount) { this.cheatingAlertCount = cheatingAlertCount; return this; }
        public Builder questionsAnswered(Integer questionsAnswered) { this.questionsAnswered = questionsAnswered; return this; }
        public Builder questionsSkipped(Integer questionsSkipped) { this.questionsSkipped = questionsSkipped; return this; }
        public Builder totalPlannedQuestions(Integer totalPlannedQuestions) { this.totalPlannedQuestions = totalPlannedQuestions; return this; }
        public Builder elapsedSeconds(Long elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; return this; }
        public Builder averageResponseSeconds(Integer averageResponseSeconds) { this.averageResponseSeconds = averageResponseSeconds; return this; }
        public Builder runningAverageScore(Integer runningAverageScore) { this.runningAverageScore = runningAverageScore; return this; }
        public Builder scoreTrend(String scoreTrend) { this.scoreTrend = scoreTrend; return this; }
        public Builder liveObservation(String liveObservation) { this.liveObservation = liveObservation; return this; }
        public Builder alerts(List<String> alerts) { this.alerts = alerts; return this; }

        public AiMonitoringSummaryDto build() {
            return new AiMonitoringSummaryDto(monitorStatus, focusIntegrityScore, tabSwitchCount, windowBlurCount, pasteEventCount, idleWarningCount, eyeOffScreenCount, cheatingAlertCount, questionsAnswered, questionsSkipped, totalPlannedQuestions, elapsedSeconds, averageResponseSeconds, runningAverageScore, scoreTrend, liveObservation, alerts);
        }
    }
}
