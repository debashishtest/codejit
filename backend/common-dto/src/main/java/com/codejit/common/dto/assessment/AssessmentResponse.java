package com.codejit.common.dto.assessment;

import java.util.ArrayList;
import java.util.List;

public class AssessmentResponse {
    private Long id;
    private String title;
    private String description;
    private String shareCode;
    private int durationMinutes;
    private String startTime;
    private String endTime;
    private AssessmentStatus status;
    private int questionCount;
    private List<CodingQuestionDto> questions = new ArrayList<>();

    public AssessmentResponse() {}

    public AssessmentResponse(Long id, String title, String description, String shareCode, int durationMinutes, String startTime, String endTime, AssessmentStatus status, int questionCount, List<CodingQuestionDto> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.shareCode = shareCode;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.questionCount = questionCount;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public List<CodingQuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<CodingQuestionDto> questions) { this.questions = questions != null ? questions : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String shareCode;
        private int durationMinutes;
        private String startTime;
        private String endTime;
        private AssessmentStatus status;
        private int questionCount;
        private List<CodingQuestionDto> questions = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shareCode(String shareCode) { this.shareCode = shareCode; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder startTime(String startTime) { this.startTime = startTime; return this; }
        public Builder endTime(String endTime) { this.endTime = endTime; return this; }
        public Builder status(AssessmentStatus status) { this.status = status; return this; }
        public Builder questionCount(int questionCount) { this.questionCount = questionCount; return this; }
        public Builder questions(List<CodingQuestionDto> questions) { this.questions = questions; return this; }

        public AssessmentResponse build() {
            return new AssessmentResponse(id, title, description, shareCode, durationMinutes, startTime, endTime, status, questionCount, questions);
        }
    }
}

