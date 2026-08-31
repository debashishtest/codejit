package com.codejit.common.dto.execution;

public class TestResult {
    private int sequence;
    private boolean passed;
    private String actualOutput;
    private String expectedOutput;
    private String errorMessage;
    private long runtimeMillis;

    public TestResult() {}

    public TestResult(int sequence, boolean passed, String actualOutput, String expectedOutput, String errorMessage, long runtimeMillis) {
        this.sequence = sequence;
        this.passed = passed;
        this.actualOutput = actualOutput;
        this.expectedOutput = expectedOutput;
        this.errorMessage = errorMessage;
        this.runtimeMillis = runtimeMillis;
    }

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
        private int sequence;
        private boolean passed;
        private String actualOutput;
        private String expectedOutput;
        private String errorMessage;
        private long runtimeMillis;

        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder passed(boolean passed) { this.passed = passed; return this; }
        public Builder actualOutput(String actualOutput) { this.actualOutput = actualOutput; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder runtimeMillis(long runtimeMillis) { this.runtimeMillis = runtimeMillis; return this; }

        public TestResult build() {
            return new TestResult(sequence, passed, actualOutput, expectedOutput, errorMessage, runtimeMillis);
        }
    }
}

