package com.codejit.common.dto.ai;

public class CreateAiInterviewRequest {
    private String title;
    private String topic;
    private String roleTitle;
    private AiDifficulty difficulty = AiDifficulty.MID;
    private AiInterviewMode mode = AiInterviewMode.MIXED;
    private Integer questionCount = 5;
    private Integer durationMinutes = 30;
    private String focusAreas;
    private Boolean enableFollowUps = Boolean.TRUE;
    private Boolean strictMonitoring = Boolean.TRUE;

    public CreateAiInterviewRequest() {}

    public CreateAiInterviewRequest(
            String title,
            String topic,
            String roleTitle,
            AiDifficulty difficulty,
            AiInterviewMode mode,
            Integer questionCount,
            Integer durationMinutes,
            String focusAreas,
            Boolean enableFollowUps,
            Boolean strictMonitoring) {
        this.title = title;
        this.topic = topic;
        this.roleTitle = roleTitle;
        this.difficulty = difficulty != null ? difficulty : AiDifficulty.MID;
        this.mode = mode != null ? mode : AiInterviewMode.MIXED;
        this.questionCount = questionCount != null ? questionCount : 5;
        this.durationMinutes = durationMinutes != null ? durationMinutes : 30;
        this.focusAreas = focusAreas;
        this.enableFollowUps = enableFollowUps != null ? enableFollowUps : Boolean.TRUE;
        this.strictMonitoring = strictMonitoring != null ? strictMonitoring : Boolean.TRUE;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public AiDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(AiDifficulty difficulty) { this.difficulty = difficulty; }

    public AiInterviewMode getMode() { return mode; }
    public void setMode(AiInterviewMode mode) { this.mode = mode; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getFocusAreas() { return focusAreas; }
    public void setFocusAreas(String focusAreas) { this.focusAreas = focusAreas; }

    public Boolean getEnableFollowUps() { return enableFollowUps; }
    public void setEnableFollowUps(Boolean enableFollowUps) { this.enableFollowUps = enableFollowUps; }

    public Boolean getStrictMonitoring() { return strictMonitoring; }
    public void setStrictMonitoring(Boolean strictMonitoring) { this.strictMonitoring = strictMonitoring; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String topic;
        private String roleTitle;
        private AiDifficulty difficulty = AiDifficulty.MID;
        private AiInterviewMode mode = AiInterviewMode.MIXED;
        private Integer questionCount = 5;
        private Integer durationMinutes = 30;
        private String focusAreas;
        private Boolean enableFollowUps = Boolean.TRUE;
        private Boolean strictMonitoring = Boolean.TRUE;

        public Builder title(String title) { this.title = title; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder roleTitle(String roleTitle) { this.roleTitle = roleTitle; return this; }
        public Builder difficulty(AiDifficulty difficulty) { this.difficulty = difficulty; return this; }
        public Builder mode(AiInterviewMode mode) { this.mode = mode; return this; }
        public Builder questionCount(Integer questionCount) { this.questionCount = questionCount; return this; }
        public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder focusAreas(String focusAreas) { this.focusAreas = focusAreas; return this; }
        public Builder enableFollowUps(Boolean enableFollowUps) { this.enableFollowUps = enableFollowUps; return this; }
        public Builder strictMonitoring(Boolean strictMonitoring) { this.strictMonitoring = strictMonitoring; return this; }

        public CreateAiInterviewRequest build() {
            return new CreateAiInterviewRequest(title, topic, roleTitle, difficulty, mode, questionCount, durationMinutes, focusAreas, enableFollowUps, strictMonitoring);
        }
    }
}
