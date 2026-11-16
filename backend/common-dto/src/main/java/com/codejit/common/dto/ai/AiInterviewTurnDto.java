package com.codejit.common.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiInterviewTurnDto {
    private Long id;
    private Integer turnNumber;
    private boolean followUp;
    private Long parentTurnId;
    private String category;
    private String topicTag;
    private AiDifficulty difficulty;
    private String questionText;
    private List<String> revealedHints = new ArrayList<>();
    private Integer totalHintsAvailable;
    private String starterCode;
    private String codeLanguage;
    private List<String> expectedConcepts = new ArrayList<>();
    private String userAnswer;
    private String userCode;
    private String codeLanguageUsed;
    private String executionSummary;
    private Integer responseTimeSeconds;
    private AiTurnStatus status;
    private AiAnswerEvaluationDto evaluation;
    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;

    public AiInterviewTurnDto() {}

    public AiInterviewTurnDto(
            Long id,
            Integer turnNumber,
            boolean followUp,
            Long parentTurnId,
            String category,
            String topicTag,
            AiDifficulty difficulty,
            String questionText,
            List<String> revealedHints,
            Integer totalHintsAvailable,
            String starterCode,
            String codeLanguage,
            List<String> expectedConcepts,
            String userAnswer,
            String userCode,
            String codeLanguageUsed,
            String executionSummary,
            Integer responseTimeSeconds,
            AiTurnStatus status,
            AiAnswerEvaluationDto evaluation,
            LocalDateTime askedAt,
            LocalDateTime answeredAt) {
        this.id = id;
        this.turnNumber = turnNumber;
        this.followUp = followUp;
        this.parentTurnId = parentTurnId;
        this.category = category;
        this.topicTag = topicTag;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.revealedHints = revealedHints != null ? revealedHints : new ArrayList<>();
        this.totalHintsAvailable = totalHintsAvailable;
        this.starterCode = starterCode;
        this.codeLanguage = codeLanguage;
        this.expectedConcepts = expectedConcepts != null ? expectedConcepts : new ArrayList<>();
        this.userAnswer = userAnswer;
        this.userCode = userCode;
        this.codeLanguageUsed = codeLanguageUsed;
        this.executionSummary = executionSummary;
        this.responseTimeSeconds = responseTimeSeconds;
        this.status = status;
        this.evaluation = evaluation;
        this.askedAt = askedAt;
        this.answeredAt = answeredAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTurnNumber() { return turnNumber; }
    public void setTurnNumber(Integer turnNumber) { this.turnNumber = turnNumber; }

    public boolean isFollowUp() { return followUp; }
    public void setFollowUp(boolean followUp) { this.followUp = followUp; }

    public Long getParentTurnId() { return parentTurnId; }
    public void setParentTurnId(Long parentTurnId) { this.parentTurnId = parentTurnId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTopicTag() { return topicTag; }
    public void setTopicTag(String topicTag) { this.topicTag = topicTag; }

    public AiDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(AiDifficulty difficulty) { this.difficulty = difficulty; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getRevealedHints() { return revealedHints; }
    public void setRevealedHints(List<String> revealedHints) { this.revealedHints = revealedHints != null ? revealedHints : new ArrayList<>(); }

    public Integer getTotalHintsAvailable() { return totalHintsAvailable; }
    public void setTotalHintsAvailable(Integer totalHintsAvailable) { this.totalHintsAvailable = totalHintsAvailable; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public String getCodeLanguage() { return codeLanguage; }
    public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

    public List<String> getExpectedConcepts() { return expectedConcepts; }
    public void setExpectedConcepts(List<String> expectedConcepts) { this.expectedConcepts = expectedConcepts != null ? expectedConcepts : new ArrayList<>(); }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getCodeLanguageUsed() { return codeLanguageUsed; }
    public void setCodeLanguageUsed(String codeLanguageUsed) { this.codeLanguageUsed = codeLanguageUsed; }

    public String getExecutionSummary() { return executionSummary; }
    public void setExecutionSummary(String executionSummary) { this.executionSummary = executionSummary; }

    public Integer getResponseTimeSeconds() { return responseTimeSeconds; }
    public void setResponseTimeSeconds(Integer responseTimeSeconds) { this.responseTimeSeconds = responseTimeSeconds; }

    public AiTurnStatus getStatus() { return status; }
    public void setStatus(AiTurnStatus status) { this.status = status; }

    public AiAnswerEvaluationDto getEvaluation() { return evaluation; }
    public void setEvaluation(AiAnswerEvaluationDto evaluation) { this.evaluation = evaluation; }

    public LocalDateTime getAskedAt() { return askedAt; }
    public void setAskedAt(LocalDateTime askedAt) { this.askedAt = askedAt; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Integer turnNumber;
        private boolean followUp;
        private Long parentTurnId;
        private String category;
        private String topicTag;
        private AiDifficulty difficulty;
        private String questionText;
        private List<String> revealedHints = new ArrayList<>();
        private Integer totalHintsAvailable;
        private String starterCode;
        private String codeLanguage;
        private List<String> expectedConcepts = new ArrayList<>();
        private String userAnswer;
        private String userCode;
        private String codeLanguageUsed;
        private String executionSummary;
        private Integer responseTimeSeconds;
        private AiTurnStatus status;
        private AiAnswerEvaluationDto evaluation;
        private LocalDateTime askedAt;
        private LocalDateTime answeredAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder turnNumber(Integer turnNumber) { this.turnNumber = turnNumber; return this; }
        public Builder followUp(boolean followUp) { this.followUp = followUp; return this; }
        public Builder parentTurnId(Long parentTurnId) { this.parentTurnId = parentTurnId; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder topicTag(String topicTag) { this.topicTag = topicTag; return this; }
        public Builder difficulty(AiDifficulty difficulty) { this.difficulty = difficulty; return this; }
        public Builder questionText(String questionText) { this.questionText = questionText; return this; }
        public Builder revealedHints(List<String> revealedHints) { this.revealedHints = revealedHints; return this; }
        public Builder totalHintsAvailable(Integer totalHintsAvailable) { this.totalHintsAvailable = totalHintsAvailable; return this; }
        public Builder starterCode(String starterCode) { this.starterCode = starterCode; return this; }
        public Builder codeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; return this; }
        public Builder expectedConcepts(List<String> expectedConcepts) { this.expectedConcepts = expectedConcepts; return this; }
        public Builder userAnswer(String userAnswer) { this.userAnswer = userAnswer; return this; }
        public Builder userCode(String userCode) { this.userCode = userCode; return this; }
        public Builder codeLanguageUsed(String codeLanguageUsed) { this.codeLanguageUsed = codeLanguageUsed; return this; }
        public Builder executionSummary(String executionSummary) { this.executionSummary = executionSummary; return this; }
        public Builder responseTimeSeconds(Integer responseTimeSeconds) { this.responseTimeSeconds = responseTimeSeconds; return this; }
        public Builder status(AiTurnStatus status) { this.status = status; return this; }
        public Builder evaluation(AiAnswerEvaluationDto evaluation) { this.evaluation = evaluation; return this; }
        public Builder askedAt(LocalDateTime askedAt) { this.askedAt = askedAt; return this; }
        public Builder answeredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; return this; }

        public AiInterviewTurnDto build() {
            return new AiInterviewTurnDto(id, turnNumber, followUp, parentTurnId, category, topicTag, difficulty, questionText, revealedHints, totalHintsAvailable, starterCode, codeLanguage, expectedConcepts, userAnswer, userCode, codeLanguageUsed, executionSummary, responseTimeSeconds, status, evaluation, askedAt, answeredAt);
        }
    }
}
