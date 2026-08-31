package com.codejit.interview.controller;

import com.codejit.common.dto.interview.LiveInterviewEvent;
import com.codejit.interview.service.InterviewService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LiveInterviewSocketController {

    private final InterviewService interviewService;

    public LiveInterviewSocketController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @MessageMapping("/interviews/{id}/event")
    public void handleLiveEvent(
            @DestinationVariable Long id,
            @Payload LiveInterviewEvent event,
            Principal principal) {
        String sender = (principal != null && principal.getName() != null)
                ? principal.getName()
                : (event.getSender() != null ? event.getSender() : "Participant");

        interviewService.processLiveEvent(id, event, sender);
    }
}

