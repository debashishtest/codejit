package com.codejit.interview.entity;

import com.codejit.common.dto.interview.InterviewStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_rooms")
public class InterviewRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long assessmentId;

    @Column(nullable = false, unique = true, length = 16)
    private String shareCode;

    private String scheduledStart;

    private String scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    private Long currentQuestionId;

    @Column(columnDefinition = "TEXT")
    private String boardSnapshot;

    @Column(columnDefinition = "TEXT")
    private String editorSnapshot;

    private String hostEmail;

    @OneToMany(mappedBy = "interviewRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterviewParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public InterviewRoom() {}

    public InterviewRoom(Long id, String title, String description, Long assessmentId, String shareCode, String scheduledStart, String scheduledEnd, InterviewStatus status, Long currentQuestionId, String boardSnapshot, String editorSnapshot, String hostEmail, List<InterviewParticipant> participants, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assessmentId = assessmentId;
        this.shareCode = shareCode;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.status = status != null ? status : InterviewStatus.SCHEDULED;
        this.currentQuestionId = currentQuestionId;
        this.boardSnapshot = boardSnapshot;
        this.editorSnapshot = editorSnapshot;
        this.hostEmail = hostEmail;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getHostEmail() { return hostEmail; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public List<InterviewParticipant> getParticipants() { return participants; }
    public void setParticipants(List<InterviewParticipant> participants) { this.participants = participants != null ? participants : new ArrayList<>(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addParticipant(InterviewParticipant participant) {
        participants.add(participant);
        participant.setInterviewRoom(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Long assessmentId;
        private String shareCode;
        private String scheduledStart;
        private String scheduledEnd;
        private InterviewStatus status = InterviewStatus.SCHEDULED;
        private Long currentQuestionId;
        private String boardSnapshot;
        private String editorSnapshot;
        private String hostEmail;
        private List<InterviewParticipant> participants = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

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
        public Builder hostEmail(String hostEmail) { this.hostEmail = hostEmail; return this; }
        public Builder participants(List<InterviewParticipant> participants) { this.participants = participants; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public InterviewRoom build() {
            return new InterviewRoom(id, title, description, assessmentId, shareCode, scheduledStart, scheduledEnd, status, currentQuestionId, boardSnapshot, editorSnapshot, hostEmail, participants, createdAt, updatedAt);
        }
    }
}

