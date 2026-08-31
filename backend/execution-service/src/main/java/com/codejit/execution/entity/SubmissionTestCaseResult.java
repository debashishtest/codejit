package com.codejit.execution.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_test_results")
public class SubmissionTestCaseResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String actualOutput;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private long runtimeMillis;

    public SubmissionTestCaseResult() {}

    public SubmissionTestCaseResult(Long id, Submission submission, int sequence, boolean passed, String actualOutput, String expectedOutput, String errorMessage, long runtimeMillis) {
        this.id = id;
        this.submission = submission;
        this.sequence = sequence;
        this.passed = passed;
        this.actualOutput = actualOutput;
        this.expectedOutput = expectedOutput;
        this.errorMessage = errorMessage;
        this.runtimeMillis = runtimeMillis;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Submission getSubmission() { return submission; }
    public void setSubmission(Submission submission) { this.submission = submission; }

    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getRuntimeMillis() { return runtimeMillis; }
    public void setRuntimeMillis(long runtimeMillis) { this.runtimeMillis = runtimeMillis; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Submission submission;
        private int sequence;
        private boolean passed;
        private String actualOutput;
        private String expectedOutput;
        private String errorMessage;
        private long runtimeMillis;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder submission(Submission submission) { this.submission = submission; return this; }
        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder passed(boolean passed) { this.passed = passed; return this; }
        public Builder actualOutput(String actualOutput) { this.actualOutput = actualOutput; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder runtimeMillis(long runtimeMillis) { this.runtimeMillis = runtimeMillis; return this; }

        public SubmissionTestCaseResult build() {
            return new SubmissionTestCaseResult(id, submission, sequence, passed, actualOutput, expectedOutput, errorMessage, runtimeMillis);
        }
    }
}

