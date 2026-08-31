package com.codejit.common.event;

import com.codejit.common.dto.assessment.TestCaseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionTaskEvent {
    private Long submissionId;
    private Long assessmentId;
    private Long questionId;
    private Long candidateId;
    private String candidateEmail;
    private String language;
    private String sourceCode;
    private List<TestCaseDto> testCases = new ArrayList<>();
    private LocalDateTime submittedAt;

    public SubmissionTaskEvent() {}

    public SubmissionTaskEvent(Long submissionId, Long assessmentId, Long questionId, Long candidateId, String candidateEmail, String language, String sourceCode, List<TestCaseDto> testCases, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.candidateId = candidateId;
        this.candidateEmail = candidateEmail;
        this.language = language;
        this.sourceCode = sourceCode;
        this.testCases = testCases != null ? testCases : new ArrayList<>();
        this.submittedAt = submittedAt;
    }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public List<TestCaseDto> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDto> testCases) { this.testCases = testCases != null ? testCases : new ArrayList<>(); }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long submissionId;
        private Long assessmentId;
        private Long questionId;
        private Long candidateId;
        private String candidateEmail;
        private String language;
        private String sourceCode;
        private List<TestCaseDto> testCases = new ArrayList<>();
        private LocalDateTime submittedAt;

        public Builder submissionId(Long submissionId) { this.submissionId = submissionId; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder candidateId(Long candidateId) { this.candidateId = candidateId; return this; }
        public Builder candidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder testCases(List<TestCaseDto> testCases) { this.testCases = testCases; return this; }
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }

        public SubmissionTaskEvent build() {
            return new SubmissionTaskEvent(submissionId, assessmentId, questionId, candidateId, candidateEmail, language, sourceCode, testCases, submittedAt);
        }
    }
}

