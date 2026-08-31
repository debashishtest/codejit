package com.codejit.common.dto.assessment;

import java.util.ArrayList;
import java.util.List;

public class AssessmentRequest {
    private String title;
    private String description;
    private int durationMinutes;
    private String startTime;
    private String endTime;
    private List<CodingQuestionRequest> questions = new ArrayList<>();

    public AssessmentRequest() {}

    public AssessmentRequest(String title, String description, int durationMinutes, String startTime, String endTime, List<CodingQuestionRequest> questions) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public List<CodingQuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<CodingQuestionRequest> questions) { this.questions = questions != null ? questions : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String description;
        private int durationMinutes;
        private String startTime;
        private String endTime;
        private List<CodingQuestionRequest> questions = new ArrayList<>();

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder startTime(String startTime) { this.startTime = startTime; return this; }
        public Builder endTime(String endTime) { this.endTime = endTime; return this; }
        public Builder questions(List<CodingQuestionRequest> questions) { this.questions = questions; return this; }

        public AssessmentRequest build() {
            return new AssessmentRequest(title, description, durationMinutes, startTime, endTime, questions);
        }
    }
}

