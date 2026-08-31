package com.codejit.common.event;

import com.codejit.common.dto.execution.SubmissionStatus;
import com.codejit.common.dto.execution.TestResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionResultEvent {
    private Long submissionId;
    private Long assessmentId;
    private Long questionId;
    private Long candidateId;
    private SubmissionStatus status;
    private int passedTests;
    private int totalTests;
    private List<TestResult> results = new ArrayList<>();
    private LocalDateTime completedAt;

    public SubmissionResultEvent() {}

    public SubmissionResultEvent(Long submissionId, Long assessmentId, Long questionId, Long candidateId, SubmissionStatus status, int passedTests, int totalTests, List<TestResult> results, LocalDateTime completedAt) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.candidateId = candidateId;
        this.status = status;
        this.passedTests = passedTests;
        this.totalTests = totalTests;
        this.results = results != null ? results : new ArrayList<>();
        this.completedAt = completedAt;
    }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public List<TestResult> getResults() { return results; }
    public void setResults(List<TestResult> results) { this.results = results != null ? results : new ArrayList<>(); }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long submissionId;
        private Long assessmentId;
        private Long questionId;
        private Long candidateId;
        private SubmissionStatus status;
        private int passedTests;
        private int totalTests;
        private List<TestResult> results = new ArrayList<>();
        private LocalDateTime completedAt;

        public Builder submissionId(Long submissionId) { this.submissionId = submissionId; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder candidateId(Long candidateId) { this.candidateId = candidateId; return this; }
        public Builder status(SubmissionStatus status) { this.status = status; return this; }
        public Builder passedTests(int passedTests) { this.passedTests = passedTests; return this; }
        public Builder totalTests(int totalTests) { this.totalTests = totalTests; return this; }
        public Builder results(List<TestResult> results) { this.results = results; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public SubmissionResultEvent build() {
            return new SubmissionResultEvent(submissionId, assessmentId, questionId, candidateId, status, passedTests, totalTests, results, completedAt);
        }
    }
}

