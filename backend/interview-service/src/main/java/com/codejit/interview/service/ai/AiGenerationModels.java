package com.codejit.interview.service.ai;

import com.codejit.common.dto.ai.AiDifficulty;
import com.codejit.common.dto.ai.AiEvaluationVerdict;
import com.codejit.common.dto.ai.AiRecommendation;

import java.util.ArrayList;
import java.util.List;

public final class AiGenerationModels {

    private AiGenerationModels() {}

    public static class GeneratedQuestion {
        private String category;
        private String topicTag;
        private AiDifficulty difficulty;
        private String questionText;
        private List<String> hints = new ArrayList<>();
        private List<String> expectedConcepts = new ArrayList<>();
        private String referenceAnswer;
        private String starterCode;
        private String codeLanguage;
        private String providerUsed;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTopicTag() { return topicTag; }
        public void setTopicTag(String topicTag) { this.topicTag = topicTag; }

        public AiDifficulty getDifficulty() { return difficulty; }
        public void setDifficulty(AiDifficulty difficulty) { this.difficulty = difficulty; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public List<String> getHints() { return hints; }
        public void setHints(List<String> hints) { this.hints = hints != null ? hints : new ArrayList<>(); }

        public List<String> getExpectedConcepts() { return expectedConcepts; }
        public void setExpectedConcepts(List<String> expectedConcepts) { this.expectedConcepts = expectedConcepts != null ? expectedConcepts : new ArrayList<>(); }

        public String getReferenceAnswer() { return referenceAnswer; }
        public void setReferenceAnswer(String referenceAnswer) { this.referenceAnswer = referenceAnswer; }

        public String getStarterCode() { return starterCode; }
        public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

        public String getCodeLanguage() { return codeLanguage; }
        public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

        public String getProviderUsed() { return providerUsed; }
        public void setProviderUsed(String providerUsed) { this.providerUsed = providerUsed; }
    }

    public static class GeneratedEvaluation {
        private Integer score;
        private AiEvaluationVerdict verdict;
        private Integer technicalAccuracyScore;
        private Integer completenessScore;
        private Integer clarityScore;
        private Integer problemSolvingScore;
        private List<String> strengths = new ArrayList<>();
        private List<String> improvements = new ArrayList<>();
        private List<String> missingConcepts = new ArrayList<>();
        private String feedback;
        private String referenceAnswer;
        private String followUpQuestion;
        private String transcribedAnswer;
        private String providerUsed;

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public AiEvaluationVerdict getVerdict() { return verdict; }
        public void setVerdict(AiEvaluationVerdict verdict) { this.verdict = verdict; }

        public Integer getTechnicalAccuracyScore() { return technicalAccuracyScore; }
        public void setTechnicalAccuracyScore(Integer technicalAccuracyScore) { this.technicalAccuracyScore = technicalAccuracyScore; }

        public Integer getCompletenessScore() { return completenessScore; }
        public void setCompletenessScore(Integer completenessScore) { this.completenessScore = completenessScore; }

        public Integer getClarityScore() { return clarityScore; }
        public void setClarityScore(Integer clarityScore) { this.clarityScore = clarityScore; }

        public Integer getProblemSolvingScore() { return problemSolvingScore; }
        public void setProblemSolvingScore(Integer problemSolvingScore) { this.problemSolvingScore = problemSolvingScore; }

        public List<String> getStrengths() { return strengths; }
        public void setStrengths(List<String> strengths) { this.strengths = strengths != null ? strengths : new ArrayList<>(); }

        public List<String> getImprovements() { return improvements; }
        public void setImprovements(List<String> improvements) { this.improvements = improvements != null ? improvements : new ArrayList<>(); }

        public List<String> getMissingConcepts() { return missingConcepts; }
        public void setMissingConcepts(List<String> missingConcepts) { this.missingConcepts = missingConcepts != null ? missingConcepts : new ArrayList<>(); }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public String getReferenceAnswer() { return referenceAnswer; }
        public void setReferenceAnswer(String referenceAnswer) { this.referenceAnswer = referenceAnswer; }

        public String getFollowUpQuestion() { return followUpQuestion; }
        public void setFollowUpQuestion(String followUpQuestion) { this.followUpQuestion = followUpQuestion; }

        public String getTranscribedAnswer() { return transcribedAnswer; }
        public void setTranscribedAnswer(String transcribedAnswer) { this.transcribedAnswer = transcribedAnswer; }

        public String getProviderUsed() { return providerUsed; }
        public void setProviderUsed(String providerUsed) { this.providerUsed = providerUsed; }
    }

    public static class GeneratedEyeAnalysis {
        private boolean facePresent = true;
        private boolean multiplePeople = false;
        private boolean lookingAtScreen = true;
        private String gazeDirection = "CENTER";
        private boolean cheatingSuspected = false;
        private String alertReason;

        public boolean isFacePresent() { return facePresent; }
        public void setFacePresent(boolean facePresent) { this.facePresent = facePresent; }

        public boolean isMultiplePeople() { return multiplePeople; }
        public void setMultiplePeople(boolean multiplePeople) { this.multiplePeople = multiplePeople; }

        public boolean isLookingAtScreen() { return lookingAtScreen; }
        public void setLookingAtScreen(boolean lookingAtScreen) { this.lookingAtScreen = lookingAtScreen; }

        public String getGazeDirection() { return gazeDirection; }
        public void setGazeDirection(String gazeDirection) { this.gazeDirection = gazeDirection; }

        public boolean isCheatingSuspected() { return cheatingSuspected; }
        public void setCheatingSuspected(boolean cheatingSuspected) { this.cheatingSuspected = cheatingSuspected; }

        public String getAlertReason() { return alertReason; }
        public void setAlertReason(String alertReason) { this.alertReason = alertReason; }
    }

    public static class GeneratedFinalReport {
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
        private String providerUsed;

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

        public String getProviderUsed() { return providerUsed; }
        public void setProviderUsed(String providerUsed) { this.providerUsed = providerUsed; }
    }
}
