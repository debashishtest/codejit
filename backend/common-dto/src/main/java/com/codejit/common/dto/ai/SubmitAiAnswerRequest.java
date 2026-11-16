package com.codejit.common.dto.ai;

public class SubmitAiAnswerRequest {
    private Long turnId;
    private String answerText;
    private String audioBase64;
    private String audioMimeType;
    private String codeSnippet;
    private String codeLanguage;
    private String executionSummary;
    private Integer responseTimeSeconds;
    private Boolean skipped = Boolean.FALSE;

    public SubmitAiAnswerRequest() {}

    public SubmitAiAnswerRequest(
            Long turnId,
            String answerText,
            String audioBase64,
            String audioMimeType,
            String codeSnippet,
            String codeLanguage,
            String executionSummary,
            Integer responseTimeSeconds,
            Boolean skipped) {
        this.turnId = turnId;
        this.answerText = answerText;
        this.audioBase64 = audioBase64;
        this.audioMimeType = audioMimeType;
        this.codeSnippet = codeSnippet;
        this.codeLanguage = codeLanguage;
        this.executionSummary = executionSummary;
        this.responseTimeSeconds = responseTimeSeconds;
        this.skipped = skipped != null ? skipped : Boolean.FALSE;
    }

    public Long getTurnId() { return turnId; }
    public void setTurnId(Long turnId) { this.turnId = turnId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }

    public String getAudioMimeType() { return audioMimeType; }
    public void setAudioMimeType(String audioMimeType) { this.audioMimeType = audioMimeType; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getCodeLanguage() { return codeLanguage; }
    public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

    public String getExecutionSummary() { return executionSummary; }
    public void setExecutionSummary(String executionSummary) { this.executionSummary = executionSummary; }

    public Integer getResponseTimeSeconds() { return responseTimeSeconds; }
    public void setResponseTimeSeconds(Integer responseTimeSeconds) { this.responseTimeSeconds = responseTimeSeconds; }

    public Boolean getSkipped() { return skipped; }
    public void setSkipped(Boolean skipped) { this.skipped = skipped; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long turnId;
        private String answerText;
        private String audioBase64;
        private String audioMimeType;
        private String codeSnippet;
        private String codeLanguage;
        private String executionSummary;
        private Integer responseTimeSeconds;
        private Boolean skipped = Boolean.FALSE;

        public Builder turnId(Long turnId) { this.turnId = turnId; return this; }
        public Builder answerText(String answerText) { this.answerText = answerText; return this; }
        public Builder audioBase64(String audioBase64) { this.audioBase64 = audioBase64; return this; }
        public Builder audioMimeType(String audioMimeType) { this.audioMimeType = audioMimeType; return this; }
        public Builder codeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; return this; }
        public Builder codeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; return this; }
        public Builder executionSummary(String executionSummary) { this.executionSummary = executionSummary; return this; }
        public Builder responseTimeSeconds(Integer responseTimeSeconds) { this.responseTimeSeconds = responseTimeSeconds; return this; }
        public Builder skipped(Boolean skipped) { this.skipped = skipped; return this; }

        public SubmitAiAnswerRequest build() {
            return new SubmitAiAnswerRequest(turnId, answerText, audioBase64, audioMimeType, codeSnippet, codeLanguage, executionSummary, responseTimeSeconds, skipped);
        }
    }
}
