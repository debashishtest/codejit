package com.codejit.assessment.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_questions")
public class CodingQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(nullable = false)
    private int questionNumber;

    @Column(nullable = false)
    private String language;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String starterCode;

    @OneToMany(mappedBy = "codingQuestion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TestCase> testCases = new ArrayList<>();

    public CodingQuestion() {}

    public CodingQuestion(Long id, Assessment assessment, String question, int questionNumber, String language, String starterCode, List<TestCase> testCases) {
        this.id = id;
        this.assessment = assessment;
        this.question = question;
        this.questionNumber = questionNumber;
        this.language = language;
        this.starterCode = starterCode;
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases != null ? testCases : new ArrayList<>(); }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
        testCase.setCodingQuestion(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Assessment assessment;
        private String question;
        private int questionNumber;
        private String language;
        private String starterCode;
        private List<TestCase> testCases = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder assessment(Assessment assessment) { this.assessment = assessment; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder questionNumber(int questionNumber) { this.questionNumber = questionNumber; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder starterCode(String starterCode) { this.starterCode = starterCode; return this; }
        public Builder testCases(List<TestCase> testCases) { this.testCases = testCases; return this; }

        public CodingQuestion build() {
            return new CodingQuestion(id, assessment, question, questionNumber, language, starterCode, testCases);
        }
    }
}

