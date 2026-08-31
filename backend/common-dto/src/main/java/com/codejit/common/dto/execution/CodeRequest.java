package com.codejit.common.dto.execution;

import com.codejit.common.dto.assessment.TestCaseDto;

import java.util.ArrayList;
import java.util.List;

public class CodeRequest {
    private String sourceCode;
    private String language = "java";
    private String input;
    private String expectedOutput;
    private List<TestCaseDto> testCases = new ArrayList<>();

    public CodeRequest() {}

    public CodeRequest(String sourceCode, String language) {
        this.sourceCode = sourceCode;
        this.language = language != null ? language : "java";
        this.testCases = new ArrayList<>();
    }

    public CodeRequest(String sourceCode, String language, String input, String expectedOutput, List<TestCaseDto> testCases) {
        this.sourceCode = sourceCode;
        this.language = language != null ? language : "java";
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public List<TestCaseDto> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDto> testCases) { this.testCases = testCases != null ? testCases : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sourceCode;
        private String language = "java";
        private String input;
        private String expectedOutput;
        private List<TestCaseDto> testCases = new ArrayList<>();

        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder input(String input) { this.input = input; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder testCases(List<TestCaseDto> testCases) { this.testCases = testCases; return this; }

        public CodeRequest build() {
            return new CodeRequest(sourceCode, language, input, expectedOutput, testCases);
        }
    }
}
