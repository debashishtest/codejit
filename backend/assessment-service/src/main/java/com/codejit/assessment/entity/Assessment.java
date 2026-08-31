package com.codejit.assessment.entity;

import com.codejit.common.dto.assessment.AssessmentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 16)
    private String shareCode;

    @Column(nullable = false)
    private int durationMinutes;

    private String startTime;

    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    private String creatorEmail;

    private Long creatorId;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CodingQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Assessment() {}

    public Assessment(Long id, String title, String description, String shareCode, int durationMinutes, String startTime, String endTime, AssessmentStatus status, String creatorEmail, Long creatorId, List<CodingQuestion> questions, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.shareCode = shareCode;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : AssessmentStatus.DRAFT;
        this.creatorEmail = creatorEmail;
        this.creatorId = creatorId;
        this.questions = questions != null ? questions : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getCreatorEmail() { return creatorEmail; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public List<CodingQuestion> getQuestions() { return questions; }
    public void setQuestions(List<CodingQuestion> questions) { this.questions = questions != null ? questions : new ArrayList<>(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addQuestion(CodingQuestion question) {
        questions.add(question);
        question.setAssessment(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String shareCode;
        private int durationMinutes;
        private String startTime;
        private String endTime;
        private AssessmentStatus status = AssessmentStatus.DRAFT;
        private String creatorEmail;
        private Long creatorId;
        private List<CodingQuestion> questions = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder shareCode(String shareCode) { this.shareCode = shareCode; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder startTime(String startTime) { this.startTime = startTime; return this; }
        public Builder endTime(String endTime) { this.endTime = endTime; return this; }
        public Builder status(AssessmentStatus status) { this.status = status; return this; }
        public Builder creatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; return this; }
        public Builder creatorId(Long creatorId) { this.creatorId = creatorId; return this; }
        public Builder questions(List<CodingQuestion> questions) { this.questions = questions; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Assessment build() {
            return new Assessment(id, title, description, shareCode, durationMinutes, startTime, endTime, status, creatorEmail, creatorId, questions, createdAt, updatedAt);
        }
    }
}

