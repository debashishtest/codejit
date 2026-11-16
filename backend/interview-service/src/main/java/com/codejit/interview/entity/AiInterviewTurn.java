package com.codejit.interview.entity;

import com.codejit.common.dto.ai.AiDifficulty;
import com.codejit.common.dto.ai.AiEvaluationVerdict;
import com.codejit.common.dto.ai.AiTurnStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_interview_turns")
public class AiInterviewTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AiInterviewSession session;

    @Column(nullable = false)
    private Integer turnNumber;

    @Column(nullable = false)
    private boolean followUp = false;

    private Long parentTurnId;

    @Column(nullable = false)
    private String category;

    private String topicTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiDifficulty difficulty = AiDifficulty.MID;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String hintsJson;

    @Column(nullable = false)
    private Integer revealedHintsCount = 0;

    @Column(columnDefinition = "TEXT")
    private String expectedConceptsJson;

    @Column(columnDefinition = "TEXT")
    private String referenceAnswer;

    @Column(columnDefinition = "TEXT")
    private String starterCode;

    private String codeLanguage;

    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    @Column(columnDefinition = "TEXT")
    private String userCode;

    private String codeLanguageUsed;

    @Column(columnDefinition = "TEXT")
    private String executionSummary;

    private Integer responseTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiTurnStatus status = AiTurnStatus.ASKED;

    private Integer score;

    @Enumerated(EnumType.STRING)
    private AiEvaluationVerdict verdict;

    private Integer technicalAccuracyScore;

    private Integer completenessScore;

    private Integer clarityScore;

    private Integer problemSolvingScore;

    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(columnDefinition = "TEXT")
    private String improvementsJson;

    @Column(columnDefinition = "TEXT")
    private String missingConceptsJson;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String followUpQuestion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime askedAt;

    private LocalDateTime answeredAt;

    public AiInterviewTurn() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AiInterviewSession getSession() { return session; }
    public void setSession(AiInterviewSession session) { this.session = session; }

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

    public String getHintsJson() { return hintsJson; }
    public void setHintsJson(String hintsJson) { this.hintsJson = hintsJson; }

    public Integer getRevealedHintsCount() { return revealedHintsCount; }
    public void setRevealedHintsCount(Integer revealedHintsCount) { this.revealedHintsCount = revealedHintsCount; }

    public String getExpectedConceptsJson() { return expectedConceptsJson; }
    public void setExpectedConceptsJson(String expectedConceptsJson) { this.expectedConceptsJson = expectedConceptsJson; }

    public String getReferenceAnswer() { return referenceAnswer; }
    public void setReferenceAnswer(String referenceAnswer) { this.referenceAnswer = referenceAnswer; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public String getCodeLanguage() { return codeLanguage; }
    public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

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

    public String getStrengthsJson() { return strengthsJson; }
    public void setStrengthsJson(String strengthsJson) { this.strengthsJson = strengthsJson; }

    public String getImprovementsJson() { return improvementsJson; }
    public void setImprovementsJson(String improvementsJson) { this.improvementsJson = improvementsJson; }

    public String getMissingConceptsJson() { return missingConceptsJson; }
    public void setMissingConceptsJson(String missingConceptsJson) { this.missingConceptsJson = missingConceptsJson; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getFollowUpQuestion() { return followUpQuestion; }
    public void setFollowUpQuestion(String followUpQuestion) { this.followUpQuestion = followUpQuestion; }

    public LocalDateTime getAskedAt() { return askedAt; }
    public void setAskedAt(LocalDateTime askedAt) { this.askedAt = askedAt; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}
