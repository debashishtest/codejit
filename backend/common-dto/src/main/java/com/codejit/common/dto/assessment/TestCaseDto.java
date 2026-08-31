package com.codejit.common.dto.assessment;

public class TestCaseDto {
    private Long id;
    private int sequence;
    private String input;
    private String expectedOutput;
    private boolean visible;

    public TestCaseDto() {}

    public TestCaseDto(Long id, int sequence, String input, String expectedOutput, boolean visible) {
        this.id = id;
        this.sequence = sequence;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.visible = visible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private int sequence;
        private String input;
        private String expectedOutput;
        private boolean visible;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder input(String input) { this.input = input; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder visible(boolean visible) { this.visible = visible; return this; }

        public TestCaseDto build() {
            return new TestCaseDto(id, sequence, input, expectedOutput, visible);
        }
    }
}

