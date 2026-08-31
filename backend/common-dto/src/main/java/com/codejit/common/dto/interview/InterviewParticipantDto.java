package com.codejit.common.dto.interview;

import java.time.LocalDateTime;

public class InterviewParticipantDto {
    private Long userId;
    private String username;
    private String role;
    private boolean online;
    private LocalDateTime joinedAt;

    public InterviewParticipantDto() {}

    public InterviewParticipantDto(Long userId, String username, String role, boolean online, LocalDateTime joinedAt) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.online = online;
        this.joinedAt = joinedAt;
    }

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
        private Long userId;
        private String username;
        private String role;
        private boolean online;
        private LocalDateTime joinedAt;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder online(boolean online) { this.online = online; return this; }
        public Builder joinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; return this; }

        public InterviewParticipantDto build() {
            return new InterviewParticipantDto(userId, username, role, online, joinedAt);
        }
    }
}

