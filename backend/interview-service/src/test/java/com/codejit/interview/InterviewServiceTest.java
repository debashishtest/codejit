package com.codejit.interview;

import com.codejit.common.dto.interview.InterviewRequest;
import com.codejit.common.dto.interview.InterviewResponse;
import com.codejit.common.dto.interview.InterviewStatus;
import com.codejit.interview.entity.InterviewParticipant;
import com.codejit.interview.entity.InterviewRoom;
import com.codejit.interview.repository.InterviewParticipantRepository;
import com.codejit.interview.repository.InterviewRepository;
import com.codejit.interview.service.InterviewService;
import com.codejit.interview.service.RedisMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewParticipantRepository participantRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisMessagePublisher redisPublisher;

    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(
                interviewRepository,
                participantRepository,
                messagingTemplate,
                redisPublisher
        );
    }

    @Test
    @DisplayName("Should create interview room and add host participant")
    void testCreateInterview() {
        InterviewRequest request = InterviewRequest.builder()
                .title("Staff Engineer Screen")
                .description("System architecture deep dive")
                .build();

        InterviewRoom savedRoom = InterviewRoom.builder()
                .id(10L)
                .title("Staff Engineer Screen")
                .description("System architecture deep dive")
                .shareCode("ROOM1234")
                .status(InterviewStatus.SCHEDULED)
                .hostEmail("interviewer@codejit.io")
                .participants(List.of(
                        InterviewParticipant.builder()
                                .userId(1L)
                                .username("interviewer@codejit.io")
                                .role("INTERVIEWER")
                                .online(true)
                                .build()
                ))
                .build();

        when(interviewRepository.save(any(InterviewRoom.class))).thenReturn(savedRoom);

        InterviewResponse response = interviewService.createInterview(request, "interviewer@codejit.io", 1L);

        assertNotNull(response);
        assertEquals("ROOM1234", response.getShareCode());
        assertEquals("Staff Engineer Screen", response.getTitle());
        assertEquals(1, response.getParticipants().size());
    }

    @Test
    @DisplayName("Should allow candidate to join interview room")
    void testJoinInterview() {
        InterviewRoom room = InterviewRoom.builder()
                .id(10L)
                .title("Staff Engineer Screen")
                .shareCode("ROOM1234")
                .status(InterviewStatus.SCHEDULED)
                .build();

        when(interviewRepository.findByShareCodeIgnoreCase("ROOM1234")).thenReturn(Optional.of(room));
        when(participantRepository.findByInterviewRoomIdAndUsername(10L, "candidate@codejit.io")).thenReturn(Optional.empty());

        InterviewResponse response = interviewService.joinInterview("ROOM1234", "candidate@codejit.io", 2L);

        assertNotNull(response);
        assertEquals(InterviewStatus.LIVE, room.getStatus());
        verify(interviewRepository, atLeastOnce()).save(room);
    }
}

