package com.codejit.interview;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.controller.AiInterviewController;
import com.codejit.interview.service.AiInterviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiInterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiInterviewService aiInterviewService;

    @Test
    @DisplayName("GET /api/v1/ai-interviews should return user AI practice sessions")
    void testGetSessions() throws Exception {
        AiInterviewSummaryDto summary = AiInterviewSummaryDto.builder()
                .id(1L)
                .title("Java Backend Practice")
                .topic("Java & Spring Boot")
                .difficulty(AiDifficulty.MID)
                .mode(AiInterviewMode.MIXED)
                .status(AiInterviewStatus.COMPLETED)
                .overallScore(84)
                .focusIntegrityScore(95)
                .build();

        when(aiInterviewService.getSessions("dev@codejit.io")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/ai-interviews")
                        .header("X-User-Email", "dev@codejit.io"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].overallScore").value(84));
    }

    @Test
    @DisplayName("POST /api/v1/ai-interviews should create new AI interview session")
    void testCreateSession() throws Exception {
        CreateAiInterviewRequest request = CreateAiInterviewRequest.builder()
                .topic("System Design")
                .difficulty(AiDifficulty.SENIOR)
                .mode(AiInterviewMode.SYSTEM_DESIGN)
                .questionCount(4)
                .build();

        AiInterviewSessionDto created = AiInterviewSessionDto.builder()
                .id(10L)
                .title("System Design (SENIOR AI Practice)")
                .topic("System Design")
                .status(AiInterviewStatus.IN_PROGRESS)
                .turns(List.of(AiInterviewTurnDto.builder().id(100L).turnNumber(1).questionText("Design a rate limiter").build()))
                .build();

        when(aiInterviewService.createSession(any(), eq("dev@codejit.io"), eq(7L))).thenReturn(created);

        mockMvc.perform(post("/api/v1/ai-interviews")
                        .header("X-User-Email", "dev@codejit.io")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.turns[0].questionText").value("Design a rate limiter"));
    }

    @Test
    @DisplayName("POST /api/v1/ai-interviews/{id}/answer should evaluate answer and return updated session")
    void testSubmitAnswer() throws Exception {
        SubmitAiAnswerRequest req = SubmitAiAnswerRequest.builder()
                .turnId(100L)
                .answerText("Use Redis Lua token bucket for atomicity.")
                .responseTimeSeconds(40)
                .build();

        AiInterviewSessionDto updated = AiInterviewSessionDto.builder()
                .id(10L)
                .status(AiInterviewStatus.IN_PROGRESS)
                .turns(List.of(
                        AiInterviewTurnDto.builder()
                                .id(100L)
                                .status(AiTurnStatus.EVALUATED)
                                .evaluation(AiAnswerEvaluationDto.builder().score(78).verdict(AiEvaluationVerdict.STRONG).build())
                                .build()
                ))
                .build();

        when(aiInterviewService.submitAnswer(eq(10L), any(), eq("dev@codejit.io"))).thenReturn(updated);

        mockMvc.perform(post("/api/v1/ai-interviews/10/answer")
                        .header("X-User-Email", "dev@codejit.io")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turns[0].evaluation.score").value(78))
                .andExpect(jsonPath("$.turns[0].evaluation.verdict").value("STRONG"));
    }

    @Test
    @DisplayName("POST /api/v1/ai-interviews/{id}/monitor-events should update monitoring summary")
    void testRecordMonitorEvent() throws Exception {
        RecordMonitorEventRequest req = RecordMonitorEventRequest.builder()
                .eventType("TAB_SWITCH")
                .severity(AiMonitorSeverity.WARNING)
                .detail("User switched browser tab")
                .build();

        AiMonitoringSummaryDto summary = AiMonitoringSummaryDto.builder()
                .monitorStatus(AiMonitorStatus.OPTIMAL)
                .focusIntegrityScore(95)
                .tabSwitchCount(1)
                .build();

        when(aiInterviewService.recordMonitorEvent(eq(10L), any(), any())).thenReturn(summary);

        mockMvc.perform(post("/api/v1/ai-interviews/10/monitor-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.focusIntegrityScore").value(95))
                .andExpect(jsonPath("$.tabSwitchCount").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/ai-interviews/{id}/monitor-frame should detect eye/cheating alert")
    void testMonitorFrame() throws Exception {
        MonitorFrameRequest req = MonitorFrameRequest.builder()
                .imageBase64("dGVzdA==")
                .mimeType("image/jpeg")
                .localObservation("GAZE_AWAY")
                .build();

        EyeMonitorResultDto res = EyeMonitorResultDto.builder()
                .facePresent(true)
                .lookingAtScreen(false)
                .gazeDirection("LEFT")
                .cheatingSuspected(true)
                .alertReason("Candidate looking away from screen")
                .monitoring(AiMonitoringSummaryDto.builder()
                        .monitorStatus(AiMonitorStatus.ATTENTION_NEEDED)
                        .focusIntegrityScore(92)
                        .eyeOffScreenCount(1)
                        .cheatingAlertCount(1)
                        .build())
                .build();

        when(aiInterviewService.monitorFrame(eq(10L), any(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/ai-interviews/10/monitor-frame")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheatingSuspected").value(true))
                .andExpect(jsonPath("$.gazeDirection").value("LEFT"))
                .andExpect(jsonPath("$.monitoring.cheatingAlertCount").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/ai-interviews/{id}/complete should complete session and return final report")
    void testCompleteSession() throws Exception {
        AiInterviewSessionDto completed = AiInterviewSessionDto.builder()
                .id(10L)
                .status(AiInterviewStatus.COMPLETED)
                .overallScore(85)
                .finalReport(AiFinalReportDto.builder()
                        .overallScore(85)
                        .recommendation(AiRecommendation.STRONG_HIRE)
                        .executiveSummary("Strong performance")
                        .build())
                .build();

        when(aiInterviewService.completeSession(eq(10L), any())).thenReturn(completed);

        mockMvc.perform(post("/api/v1/ai-interviews/10/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.finalReport.recommendation").value("STRONG_HIRE"));
    }
}
