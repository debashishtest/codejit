package com.codejit.common.dto.ai;

public class MonitorFrameRequest {
    private String imageBase64;
    private String mimeType = "image/jpeg";
    private Long turnId;
    private String localObservation;

    public MonitorFrameRequest() {}

    public MonitorFrameRequest(String imageBase64, String mimeType, Long turnId, String localObservation) {
        this.imageBase64 = imageBase64;
        this.mimeType = mimeType != null ? mimeType : "image/jpeg";
        this.turnId = turnId;
        this.localObservation = localObservation;
    }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getTurnId() { return turnId; }
    public void setTurnId(Long turnId) { this.turnId = turnId; }

    public String getLocalObservation() { return localObservation; }
    public void setLocalObservation(String localObservation) { this.localObservation = localObservation; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String imageBase64;
        private String mimeType = "image/jpeg";
        private Long turnId;
        private String localObservation;

        public Builder imageBase64(String imageBase64) { this.imageBase64 = imageBase64; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder turnId(Long turnId) { this.turnId = turnId; return this; }
        public Builder localObservation(String localObservation) { this.localObservation = localObservation; return this; }

        public MonitorFrameRequest build() {
            return new MonitorFrameRequest(imageBase64, mimeType, turnId, localObservation);
        }
    }
}
