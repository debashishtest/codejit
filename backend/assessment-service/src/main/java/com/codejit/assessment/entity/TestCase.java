package com.codejit.assessment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coding_question_id", nullable = false)
    private CodingQuestion codingQuestion;

    @Column(nullable = false)
    private int sequence;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String input;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    @Column(nullable = false)
    private boolean visible;

    public TestCase() {}

    public TestCase(Long id, CodingQuestion codingQuestion, int sequence, String input, String expectedOutput, boolean visible) {
        this.id = id;
        this.codingQuestion = codingQuestion;
        this.sequence = sequence;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.visible = visible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CodingQuestion getCodingQuestion() { return codingQuestion; }
    public void setCodingQuestion(CodingQuestion codingQuestion) { this.codingQuestion = codingQuestion; }

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
        private CodingQuestion codingQuestion;
        private int sequence;
        private String input;
        private String expectedOutput;
        private boolean visible;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder codingQuestion(CodingQuestion codingQuestion) { this.codingQuestion = codingQuestion; return this; }
        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder input(String input) { this.input = input; return this; }
        public Builder expectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; return this; }
        public Builder visible(boolean visible) { this.visible = visible; return this; }

        public TestCase build() {
            return new TestCase(id, codingQuestion, sequence, input, expectedOutput, visible);
        }
    }
}

