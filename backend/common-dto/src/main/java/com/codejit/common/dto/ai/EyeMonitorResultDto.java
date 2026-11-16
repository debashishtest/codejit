package com.codejit.common.dto.ai;

public class EyeMonitorResultDto {
    private boolean facePresent;
    private boolean multiplePeople;
    private boolean lookingAtScreen;
    private String gazeDirection;
    private boolean cheatingSuspected;
    private String alertReason;
    private AiMonitoringSummaryDto monitoring;

    public EyeMonitorResultDto() {}

    public EyeMonitorResultDto(
            boolean facePresent,
            boolean multiplePeople,
            boolean lookingAtScreen,
            String gazeDirection,
            boolean cheatingSuspected,
            String alertReason,
            AiMonitoringSummaryDto monitoring) {
        this.facePresent = facePresent;
        this.multiplePeople = multiplePeople;
        this.lookingAtScreen = lookingAtScreen;
        this.gazeDirection = gazeDirection;
        this.cheatingSuspected = cheatingSuspected;
        this.alertReason = alertReason;
        this.monitoring = monitoring;
    }

    public boolean isFacePresent() { return facePresent; }
    public void setFacePresent(boolean facePresent) { this.facePresent = facePresent; }

    public boolean isMultiplePeople() { return multiplePeople; }
    public void setMultiplePeople(boolean multiplePeople) { this.multiplePeople = multiplePeople; }

    public boolean isLookingAtScreen() { return lookingAtScreen; }
    public void setLookingAtScreen(boolean lookingAtScreen) { this.lookingAtScreen = lookingAtScreen; }

    public String getGazeDirection() { return gazeDirection; }
    public void setGazeDirection(String gazeDirection) { this.gazeDirection = gazeDirection; }

    public boolean isCheatingSuspected() { return cheatingSuspected; }
    public void setCheatingSuspected(boolean cheatingSuspected) { this.cheatingSuspected = cheatingSuspected; }

    public String getAlertReason() { return alertReason; }
    public void setAlertReason(String alertReason) { this.alertReason = alertReason; }

    public AiMonitoringSummaryDto getMonitoring() { return monitoring; }
    public void setMonitoring(AiMonitoringSummaryDto monitoring) { this.monitoring = monitoring; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean facePresent = true;
        private boolean multiplePeople = false;
        private boolean lookingAtScreen = true;
        private String gazeDirection = "CENTER";
        private boolean cheatingSuspected = false;
        private String alertReason;
        private AiMonitoringSummaryDto monitoring;

        public Builder facePresent(boolean facePresent) { this.facePresent = facePresent; return this; }
        public Builder multiplePeople(boolean multiplePeople) { this.multiplePeople = multiplePeople; return this; }
        public Builder lookingAtScreen(boolean lookingAtScreen) { this.lookingAtScreen = lookingAtScreen; return this; }
        public Builder gazeDirection(String gazeDirection) { this.gazeDirection = gazeDirection; return this; }
        public Builder cheatingSuspected(boolean cheatingSuspected) { this.cheatingSuspected = cheatingSuspected; return this; }
        public Builder alertReason(String alertReason) { this.alertReason = alertReason; return this; }
        public Builder monitoring(AiMonitoringSummaryDto monitoring) { this.monitoring = monitoring; return this; }

        public EyeMonitorResultDto build() {
            return new EyeMonitorResultDto(facePresent, multiplePeople, lookingAtScreen, gazeDirection, cheatingSuspected, alertReason, monitoring);
        }
    }
}
