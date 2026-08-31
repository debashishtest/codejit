package com.codejit.common.dto.assessment;

import java.util.ArrayList;
import java.util.List;

public class CodingQuestionRequest {
    private String question;
    private int questionNumber;
    private String language;
    private String starterCode;
    private List<TestCaseRequest> testCases = new ArrayList<>();

    public CodingQuestionRequest() {}

    public CodingQuestionRequest(String question, int questionNumber, String language, String starterCode, List<TestCaseRequest> testCases) {
        this.question = question;
        this.questionNumber = questionNumber;
        this.language = language;
        this.starterCode = starterCode;
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public List<TestCaseRequest> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseRequest> testCases) { this.testCases = testCases != null ? testCases : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String question;
        private int questionNumber;
        private String language;
        private String starterCode;
        private List<TestCaseRequest> testCases = new ArrayList<>();

        public Builder question(String question) { this.question = question; return this; }
        public Builder questionNumber(int questionNumber) { this.questionNumber = questionNumber; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder starterCode(String starterCode) { this.starterCode = starterCode; return this; }
        public Builder testCases(List<TestCaseRequest> testCases) { this.testCases = testCases; return this; }

        public CodingQuestionRequest build() {
            return new CodingQuestionRequest(question, questionNumber, language, starterCode, testCases);
        }
    }
}

