package com.codejit.interview.service;

import com.codejit.common.dto.interview.*;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.ResourceNotFoundException;
import com.codejit.interview.entity.InterviewParticipant;
import com.codejit.interview.entity.InterviewRoom;
import com.codejit.interview.repository.InterviewParticipantRepository;
import com.codejit.interview.repository.InterviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisMessagePublisher redisPublisher;

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public InterviewService(
            InterviewRepository interviewRepository,
            InterviewParticipantRepository participantRepository,
            SimpMessagingTemplate messagingTemplate,
            RedisMessagePublisher redisPublisher) {
        this.interviewRepository = interviewRepository;
        this.participantRepository = participantRepository;
        this.messagingTemplate = messagingTemplate;
        this.redisPublisher = redisPublisher;
    }

    @Transactional
    public InterviewResponse createInterview(InterviewRequest request, String hostEmail, Long hostUserId) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Interview title is required");
        }

        String shareCode = generateShareCode();

        InterviewRoom room = InterviewRoom.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .assessmentId(request.getAssessmentId())
                .shareCode(shareCode)
                .scheduledStart(request.getScheduledStart() != null ? request.getScheduledStart() : LocalDateTime.now().toString())
                .scheduledEnd(request.getScheduledEnd() != null ? request.getScheduledEnd() : LocalDateTime.now().plusHours(1).toString())
                .status(InterviewStatus.SCHEDULED)
                .boardSnapshot("")
                .editorSnapshot("// Start collaborative coding session\npublic class Solution {\n    public static void main(String[] args) {\n        System.out.println(\"Hello CodeJIT\");\n    }\n}")
                .hostEmail(hostEmail)
                .build();

        InterviewParticipant host = InterviewParticipant.builder()
                .userId(hostUserId)
                .username(hostEmail != null ? hostEmail : "Host")
                .role("INTERVIEWER")
                .online(true)
                .build();

        room.addParticipant(host);
        InterviewRoom saved = interviewRepository.save(room);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(String hostEmail) {
        List<InterviewRoom> rooms = (hostEmail != null && !hostEmail.isBlank())
                ? interviewRepository.findByHostEmailOrderByCreatedAtDesc(hostEmail)
                : interviewRepository.findAll();

        return rooms.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long id) {
        InterviewRoom room = interviewRepository.findByIdWithParticipants(id)
                .or(() -> interviewRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with id: " + id));

        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewByShareCode(String shareCode) {
        InterviewRoom room = interviewRepository.findByShareCodeWithParticipants(shareCode.trim().toUpperCase())
                .or(() -> interviewRepository.findByShareCodeIgnoreCase(shareCode.trim().toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with code: " + shareCode));

        return toResponse(room);
    }

    @Transactional
    public InterviewResponse joinInterview(String shareCode, String userEmail, Long userId) {
        InterviewRoom room = interviewRepository.findByShareCodeIgnoreCase(shareCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with code: " + shareCode));

        String username = (userEmail != null && !userEmail.isBlank()) ? userEmail : "Candidate";
        boolean alreadyParticipant = participantRepository.findByInterviewRoomIdAndUsername(room.getId(), username).isPresent();

        if (!alreadyParticipant) {
            InterviewParticipant participant = InterviewParticipant.builder()
                    .userId(userId)
                    .username(username)
                    .role("CANDIDATE")
                    .online(true)
                    .build();
            room.addParticipant(participant);
            interviewRepository.save(room);
        }

        if (room.getStatus() == InterviewStatus.SCHEDULED) {
            room.setStatus(InterviewStatus.LIVE);
            interviewRepository.save(room);
        }

        return toResponse(room);
    }

    @Transactional
    public InterviewResponse startInterview(Long id) {
        InterviewRoom room = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with id: " + id));

        room.setStatus(InterviewStatus.LIVE);
        InterviewRoom saved = interviewRepository.save(room);
        return toResponse(saved);
    }

    @Transactional
    public InterviewResponse endInterview(Long id) {
        InterviewRoom room = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with id: " + id));

        room.setStatus(InterviewStatus.ENDED);
        InterviewRoom saved = interviewRepository.save(room);
        return toResponse(saved);
    }

    @Transactional
    public void processLiveEvent(Long roomId, LiveInterviewEvent event, String sender) {
        event.setSender(sender);
        event.setTimestamp(System.currentTimeMillis());

        if ("BOARD_UPDATED".equals(event.getType())) {
            interviewRepository.findById(roomId).ifPresent(room -> {
                room.setBoardSnapshot(event.getPayload());
                interviewRepository.save(room);
            });
        } else if ("EDITOR_UPDATED".equals(event.getType())) {
            interviewRepository.findById(roomId).ifPresent(room -> {
                room.setEditorSnapshot(event.getPayload());
                interviewRepository.save(room);
            });
        }

        messagingTemplate.convertAndSend("/topic/interviews/" + roomId, event);
        redisPublisher.publishEvent(roomId, event);
    }

    private String generateShareCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private InterviewResponse toResponse(InterviewRoom room) {
        List<InterviewParticipant> pList = (room.getParticipants() != null && !room.getParticipants().isEmpty())
                ? room.getParticipants()
                : participantRepository.findByInterviewRoomId(room.getId());

        List<InterviewParticipantDto> pDtos = pList.stream().map(p -> InterviewParticipantDto.builder()
                .userId(p.getUserId())
                .username(p.getUsername())
                .role(p.getRole())
                .online(p.isOnline())
                .joinedAt(p.getJoinedAt())
                .build()).collect(Collectors.toList());

        return InterviewResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .assessmentId(room.getAssessmentId())
                .shareCode(room.getShareCode())
                .scheduledStart(room.getScheduledStart())
                .scheduledEnd(room.getScheduledEnd())
                .status(room.getStatus())
                .currentQuestionId(room.getCurrentQuestionId())
                .boardSnapshot(room.getBoardSnapshot())
                .editorSnapshot(room.getEditorSnapshot())
                .participants(pDtos)
                .build();
    }
}

