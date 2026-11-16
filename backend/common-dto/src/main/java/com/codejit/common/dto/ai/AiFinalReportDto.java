package com.codejit.common.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiFinalReportDto {
    private Integer overallScore;
    private AiRecommendation recommendation;
    private String executiveSummary;
    private Integer technicalDepthScore;
    private Integer problemSolvingScore;
    private Integer communicationScore;
    private Integer consistencyScore;
    private Integer integrityFocusScore;
    private List<String> topStrengths = new ArrayList<>();
    private List<String> improvementAreas = new ArrayList<>();
    private List<String> studyPlan = new ArrayList<>();
    private String monitoringVerdict;
    private LocalDateTime generatedAt;

    public AiFinalReportDto() {}

    public AiFinalReportDto(
            Integer overallScore,
            AiRecommendation recommendation,
            String executiveSummary,
            Integer technicalDepthScore,
            Integer problemSolvingScore,
            Integer communicationScore,
            Integer consistencyScore,
            Integer integrityFocusScore,
            List<String> topStrengths,
            List<String> improvementAreas,
            List<String> studyPlan,
            String monitoringVerdict,
            LocalDateTime generatedAt) {
        this.overallScore = overallScore;
        this.recommendation = recommendation;
        this.executiveSummary = executiveSummary;
        this.technicalDepthScore = technicalDepthScore;
        this.problemSolvingScore = problemSolvingScore;
        this.communicationScore = communicationScore;
        this.consistencyScore = consistencyScore;
        this.integrityFocusScore = integrityFocusScore;
        this.topStrengths = topStrengths != null ? topStrengths : new ArrayList<>();
        this.improvementAreas = improvementAreas != null ? improvementAreas : new ArrayList<>();
        this.studyPlan = studyPlan != null ? studyPlan : new ArrayList<>();
        this.monitoringVerdict = monitoringVerdict;
        this.generatedAt = generatedAt;
    }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public AiRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(AiRecommendation recommendation) { this.recommendation = recommendation; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public Integer getTechnicalDepthScore() { return technicalDepthScore; }
    public void setTechnicalDepthScore(Integer technicalDepthScore) { this.technicalDepthScore = technicalDepthScore; }

    public Integer getProblemSolvingScore() { return problemSolvingScore; }
    public void setProblemSolvingScore(Integer problemSolvingScore) { this.problemSolvingScore = problemSolvingScore; }

    public Integer getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(Integer communicationScore) { this.communicationScore = communicationScore; }

    public Integer getConsistencyScore() { return consistencyScore; }
    public void setConsistencyScore(Integer consistencyScore) { this.consistencyScore = consistencyScore; }

    public Integer getIntegrityFocusScore() { return integrityFocusScore; }
    public void setIntegrityFocusScore(Integer integrityFocusScore) { this.integrityFocusScore = integrityFocusScore; }

    public List<String> getTopStrengths() { return topStrengths; }
    public void setTopStrengths(List<String> topStrengths) { this.topStrengths = topStrengths != null ? topStrengths : new ArrayList<>(); }

    public List<String> getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas != null ? improvementAreas : new ArrayList<>(); }

    public List<String> getStudyPlan() { return studyPlan; }
    public void setStudyPlan(List<String> studyPlan) { this.studyPlan = studyPlan != null ? studyPlan : new ArrayList<>(); }

    public String getMonitoringVerdict() { return monitoringVerdict; }
    public void setMonitoringVerdict(String monitoringVerdict) { this.monitoringVerdict = monitoringVerdict; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer overallScore;
        private AiRecommendation recommendation;
        private String executiveSummary;
        private Integer technicalDepthScore;
        private Integer problemSolvingScore;
        private Integer communicationScore;
        private Integer consistencyScore;
        private Integer integrityFocusScore;
        private List<String> topStrengths = new ArrayList<>();
        private List<String> improvementAreas = new ArrayList<>();
        private List<String> studyPlan = new ArrayList<>();
        private String monitoringVerdict;
        private LocalDateTime generatedAt;

        public Builder overallScore(Integer overallScore) { this.overallScore = overallScore; return this; }
        public Builder recommendation(AiRecommendation recommendation) { this.recommendation = recommendation; return this; }
        public Builder executiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; return this; }
        public Builder technicalDepthScore(Integer technicalDepthScore) { this.technicalDepthScore = technicalDepthScore; return this; }
        public Builder problemSolvingScore(Integer problemSolvingScore) { this.problemSolvingScore = problemSolvingScore; return this; }
        public Builder communicationScore(Integer communicationScore) { this.communicationScore = communicationScore; return this; }
        public Builder consistencyScore(Integer consistencyScore) { this.consistencyScore = consistencyScore; return this; }
        public Builder integrityFocusScore(Integer integrityFocusScore) { this.integrityFocusScore = integrityFocusScore; return this; }
        public Builder topStrengths(List<String> topStrengths) { this.topStrengths = topStrengths; return this; }
        public Builder improvementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas; return this; }
        public Builder studyPlan(List<String> studyPlan) { this.studyPlan = studyPlan; return this; }
        public Builder monitoringVerdict(String monitoringVerdict) { this.monitoringVerdict = monitoringVerdict; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }

        public AiFinalReportDto build() {
            return new AiFinalReportDto(overallScore, recommendation, executiveSummary, technicalDepthScore, problemSolvingScore, communicationScore, consistencyScore, integrityFocusScore, topStrengths, improvementAreas, studyPlan, monitoringVerdict, generatedAt);
        }
    }
}
