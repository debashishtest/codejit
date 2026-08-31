package com.codejit.common.dto.execution;

import java.util.ArrayList;
import java.util.List;

public class SubmissionResponse {
    private Long id;
    private Long assessmentId;
    private Long questionId;
    private SubmissionStatus status;
    private int passedTests;
    private int totalTests;
    private String submittedAt;
    private List<TestResult> results = new ArrayList<>();

    public SubmissionResponse() {}

    public SubmissionResponse(Long id, Long assessmentId, Long questionId, SubmissionStatus status, int passedTests, int totalTests, String submittedAt, List<TestResult> results) {
        this.id = id;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.status = status;
        this.passedTests = passedTests;
        this.totalTests = totalTests;
        this.submittedAt = submittedAt;
        this.results = results != null ? results : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public List<TestResult> getResults() { return results; }
    public void setResults(List<TestResult> results) { this.results = results != null ? results : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long assessmentId;
        private Long questionId;
        private SubmissionStatus status;
        private int passedTests;
        private int totalTests;
        private String submittedAt;
        private List<TestResult> results = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder status(SubmissionStatus status) { this.status = status; return this; }
        public Builder passedTests(int passedTests) { this.passedTests = passedTests; return this; }
        public Builder totalTests(int totalTests) { this.totalTests = totalTests; return this; }
        public Builder submittedAt(String submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder results(List<TestResult> results) { this.results = results; return this; }

        public SubmissionResponse build() {
            return new SubmissionResponse(id, assessmentId, questionId, status, passedTests, totalTests, submittedAt, results);
        }
    }
}

