package com.codejit.common.dto.interview;

import java.util.ArrayList;
import java.util.List;

public class InterviewResponse {
    private Long id;
    private String title;
    private String description;
    private Long assessmentId;
    private String shareCode;
    private String scheduledStart;
    private String scheduledEnd;
    private InterviewStatus status;
    private Long currentQuestionId;
    private String boardSnapshot;
    private String editorSnapshot;
    private List<InterviewParticipantDto> participants = new ArrayList<>();

    public InterviewResponse() {}

    public InterviewResponse(Long id, String title, String description, Long assessmentId, String shareCode, String scheduledStart, String scheduledEnd, InterviewStatus status, Long currentQuestionId, String boardSnapshot, String editorSnapshot, List<InterviewParticipantDto> participants) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assessmentId = assessmentId;
        this.shareCode = shareCode;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.status = status;
        this.currentQuestionId = currentQuestionId;
        this.boardSnapshot = boardSnapshot;
        this.editorSnapshot = editorSnapshot;
        this.participants = participants != null ? participants : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }

    public String getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; }

    public String getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

    public Long getCurrentQuestionId() { return currentQuestionId; }
    public void setCurrentQuestionId(Long currentQuestionId) { this.currentQuestionId = currentQuestionId; }

    public String getBoardSnapshot() { return boardSnapshot; }
    public void setBoardSnapshot(String boardSnapshot) { this.boardSnapshot = boardSnapshot; }

    public String getEditorSnapshot() { return editorSnapshot; }
    public void setEditorSnapshot(String editorSnapshot) { this.editorSnapshot = editorSnapshot; }

    public List<InterviewParticipantDto> getParticipants() { return participants; }
    public void setParticipants(List<InterviewParticipantDto> participants) { this.participants = participants != null ? participants : new ArrayList<>(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Long assessmentId;
        private String shareCode;
        private String scheduledStart;
        private String scheduledEnd;
        private InterviewStatus status;
        private Long currentQuestionId;
        private String boardSnapshot;
        private String editorSnapshot;
        private List<InterviewParticipantDto> participants = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder shareCode(String shareCode) { this.shareCode = shareCode; return this; }
        public Builder scheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; return this; }
        public Builder scheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; return this; }
        public Builder status(InterviewStatus status) { this.status = status; return this; }
        public Builder currentQuestionId(Long currentQuestionId) { this.currentQuestionId = currentQuestionId; return this; }
        public Builder boardSnapshot(String boardSnapshot) { this.boardSnapshot = boardSnapshot; return this; }
        public Builder editorSnapshot(String editorSnapshot) { this.editorSnapshot = editorSnapshot; return this; }
        public Builder participants(List<InterviewParticipantDto> participants) { this.participants = participants; return this; }

        public InterviewResponse build() {
            return new InterviewResponse(id, title, description, assessmentId, shareCode, scheduledStart, scheduledEnd, status, currentQuestionId, boardSnapshot, editorSnapshot, participants);
        }
    }
}

