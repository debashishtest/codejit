package com.codejit.common.dto.assessment;

public class TestCaseRequest {
    private String input;
    private String expectedOutput;
    private boolean visible;
    private int sequence;

    public TestCaseRequest() {}

    public TestCaseRequest(String input, String expectedOutput, boolean visible, int sequence) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.visible = visible;
        this.sequence = sequence;
    }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String input;
        private String expectedOutput;
        private boolean visible;
        private int sequence;

        public Builder input(String input) { this.input = input; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder visible(boolean visible) { this.visible = visible; return this; }
        public Builder sequence(int sequence) { this.sequence = sequence; return this; }

        public TestCaseRequest build() {
            return new TestCaseRequest(input, expectedOutput, visible, sequence);
        }
    }
}

