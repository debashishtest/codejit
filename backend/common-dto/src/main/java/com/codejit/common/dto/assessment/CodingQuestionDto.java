package com.codejit.common.dto.assessment;

import java.util.ArrayList;
import java.util.List;

public class CodingQuestionDto {
    private Long id;
    private String question;
    private int questionNumber;
    private String language;
    private String starterCode;
    private List<TestCaseDto> visibleTestCases = new ArrayList<>();
    private List<TestCaseDto> testCases = new ArrayList<>();

    public CodingQuestionDto() {}

    public CodingQuestionDto(Long id, String question, int questionNumber, String language, String starterCode, List<TestCaseDto> visibleTestCases, List<TestCaseDto> testCases) {
        this.id = id;
        this.question = question;
        this.questionNumber = questionNumber;
        this.language = language;
        this.starterCode = starterCode;
        this.visibleTestCases = visibleTestCases != null ? visibleTestCases : new ArrayList<>();
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public List<TestCaseDto> getVisibleTestCases() { return visibleTestCases; }
    public void setVisibleTestCases(List<TestCaseDto> visibleTestCases) { this.visibleTestCases = visibleTestCases != null ? visibleTestCases : new ArrayList<>(); }

    public List<TestCaseDto> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDto> testCases) { this.testCases = testCases != null ? testCases : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String question;
        private int questionNumber;
        private String language;
        private String starterCode;
        private List<TestCaseDto> visibleTestCases = new ArrayList<>();
        private List<TestCaseDto> testCases = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder questionNumber(int questionNumber) { this.questionNumber = questionNumber; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder starterCode(String starterCode) { this.starterCode = starterCode; return this; }
        public Builder visibleTestCases(List<TestCaseDto> visibleTestCases) { this.visibleTestCases = visibleTestCases; return this; }
        public Builder testCases(List<TestCaseDto> testCases) { this.testCases = testCases; return this; }

        public CodingQuestionDto build() {
            return new CodingQuestionDto(id, question, questionNumber, language, starterCode, visibleTestCases, testCases);
        }
    }
}

