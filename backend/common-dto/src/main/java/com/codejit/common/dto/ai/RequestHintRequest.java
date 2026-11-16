package com.codejit.common.dto.ai;

public class RequestHintRequest {
    private Long turnId;

    public RequestHintRequest() {}

    public RequestHintRequest(Long turnId) {
        this.turnId = turnId;
    }

    public Long getTurnId() { return turnId; }
    public void setTurnId(Long turnId) { this.turnId = turnId; }
}
