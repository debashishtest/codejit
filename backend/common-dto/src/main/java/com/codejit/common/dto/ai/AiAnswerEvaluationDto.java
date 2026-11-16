package com.codejit.common.dto.ai;

import java.util.ArrayList;
import java.util.List;

public class AiAnswerEvaluationDto {
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

    public AiAnswerEvaluationDto() {}

    public AiAnswerEvaluationDto(
            Integer score,
            AiEvaluationVerdict verdict,
            Integer technicalAccuracyScore,
            Integer completenessScore,
            Integer clarityScore,
            Integer problemSolvingScore,
            List<String> strengths,
            List<String> improvements,
            List<String> missingConcepts,
            String feedback,
            String referenceAnswer,
            String followUpQuestion) {
        this.score = score;
        this.verdict = verdict;
        this.technicalAccuracyScore = technicalAccuracyScore;
        this.completenessScore = completenessScore;
        this.clarityScore = clarityScore;
        this.problemSolvingScore = problemSolvingScore;
        this.strengths = strengths != null ? strengths : new ArrayList<>();
        this.improvements = improvements != null ? improvements : new ArrayList<>();
        this.missingConcepts = missingConcepts != null ? missingConcepts : new ArrayList<>();
        this.feedback = feedback;
        this.referenceAnswer = referenceAnswer;
        this.followUpQuestion = followUpQuestion;
    }

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
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

        public Builder score(Integer score) { this.score = score; return this; }
        public Builder verdict(AiEvaluationVerdict verdict) { this.verdict = verdict; return this; }
        public Builder technicalAccuracyScore(Integer technicalAccuracyScore) { this.technicalAccuracyScore = technicalAccuracyScore; return this; }
        public Builder completenessScore(Integer completenessScore) { this.completenessScore = completenessScore; return this; }
        public Builder clarityScore(Integer clarityScore) { this.clarityScore = clarityScore; return this; }
        public Builder problemSolvingScore(Integer problemSolvingScore) { this.problemSolvingScore = problemSolvingScore; return this; }
        public Builder strengths(List<String> strengths) { this.strengths = strengths; return this; }
        public Builder improvements(List<String> improvements) { this.improvements = improvements; return this; }
        public Builder missingConcepts(List<String> missingConcepts) { this.missingConcepts = missingConcepts; return this; }
        public Builder feedback(String feedback) { this.feedback = feedback; return this; }
        public Builder referenceAnswer(String referenceAnswer) { this.referenceAnswer = referenceAnswer; return this; }
        public Builder followUpQuestion(String followUpQuestion) { this.followUpQuestion = followUpQuestion; return this; }

        public AiAnswerEvaluationDto build() {
            return new AiAnswerEvaluationDto(score, verdict, technicalAccuracyScore, completenessScore, clarityScore, problemSolvingScore, strengths, improvements, missingConcepts, feedback, referenceAnswer, followUpQuestion);
        }
    }
}
