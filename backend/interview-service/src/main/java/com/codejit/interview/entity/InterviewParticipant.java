package com.codejit.interview.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_participants")
public class InterviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_room_id", nullable = false)
    private InterviewRoom interviewRoom;

    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean online;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public InterviewParticipant() {}

    public InterviewParticipant(Long id, InterviewRoom interviewRoom, Long userId, String username, String role, boolean online, LocalDateTime joinedAt) {
        this.id = id;
        this.interviewRoom = interviewRoom;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.online = online;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InterviewRoom getInterviewRoom() { return interviewRoom; }
    public void setInterviewRoom(InterviewRoom interviewRoom) { this.interviewRoom = interviewRoom; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private InterviewRoom interviewRoom;
        private Long userId;
        private String username;
        private String role;
        private boolean online;
        private LocalDateTime joinedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder interviewRoom(InterviewRoom interviewRoom) { this.interviewRoom = interviewRoom; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder online(boolean online) { this.online = online; return this; }
        public Builder joinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; return this; }

        public InterviewParticipant build() {
            return new InterviewParticipant(id, interviewRoom, userId, username, role, online, joinedAt);
        }
    }
}

