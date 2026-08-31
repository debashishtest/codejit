package com.codejit.common.dto.interview;

public class LiveInterviewEvent {
    private String type;
    private Long questionId;
    private String payload;
    private String sender;
    private Long timestamp;

    public LiveInterviewEvent() {}

    public LiveInterviewEvent(String type, Long questionId, String payload, String sender, Long timestamp) {
        this.type = type;
        this.questionId = questionId;
        this.payload = payload;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String type;
        private Long questionId;
        private String payload;
        private String sender;
        private Long timestamp;

        public Builder type(String type) { this.type = type; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public LiveInterviewEvent build() {
            return new LiveInterviewEvent(type, questionId, payload, sender, timestamp);
        }
    }
}

