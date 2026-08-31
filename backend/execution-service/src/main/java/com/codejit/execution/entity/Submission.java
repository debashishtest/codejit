package com.codejit.execution.entity;

import com.codejit.common.dto.execution.SubmissionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assessmentId;

    @Column(nullable = false)
    private Long questionId;

    private String candidateEmail;

    private Long candidateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(nullable = false)
    private int passedTests;

    @Column(nullable = false)
    private int totalTests;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    @Column(nullable = false)
    private String language;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubmissionTestCaseResult> results = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    public Submission() {}

    public Submission(Long id, Long assessmentId, Long questionId, String candidateEmail, Long candidateId, SubmissionStatus status, int passedTests, int totalTests, String sourceCode, String language, List<SubmissionTestCaseResult> results, LocalDateTime submittedAt, LocalDateTime completedAt) {
        this.id = id;
        this.assessmentId = assessmentId;
        this.questionId = questionId;
        this.candidateEmail = candidateEmail;
        this.candidateId = candidateId;
        this.status = status;
        this.passedTests = passedTests;
        this.totalTests = totalTests;
        this.sourceCode = sourceCode;
        this.language = language;
        this.results = results != null ? results : new ArrayList<>();
        this.submittedAt = submittedAt;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<SubmissionTestCaseResult> getResults() { return results; }
    public void setResults(List<SubmissionTestCaseResult> results) { this.results = results != null ? results : new ArrayList<>(); }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public void addResult(SubmissionTestCaseResult result) {
        results.add(result);
        result.setSubmission(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long assessmentId;
        private Long questionId;
        private String candidateEmail;
        private Long candidateId;
        private SubmissionStatus status;
        private int passedTests;
        private int totalTests;
        private String sourceCode;
        private String language;
        private List<SubmissionTestCaseResult> results = new ArrayList<>();
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder candidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; return this; }
        public Builder candidateId(Long candidateId) { this.candidateId = candidateId; return this; }
        public Builder status(SubmissionStatus status) { this.status = status; return this; }
        public Builder passedTests(int passedTests) { this.passedTests = passedTests; return this; }
        public Builder totalTests(int totalTests) { this.totalTests = totalTests; return this; }
        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder results(List<SubmissionTestCaseResult> results) { this.results = results; return this; }
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public Submission build() {
            return new Submission(id, assessmentId, questionId, candidateEmail, candidateId, status, passedTests, totalTests, sourceCode, language, results, submittedAt, completedAt);
        }
    }
}

