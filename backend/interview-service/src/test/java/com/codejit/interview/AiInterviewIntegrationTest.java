package com.codejit.interview;

import com.codejit.common.dto.ai.*;
import com.codejit.common.exception.ResourceNotFoundException;
import com.codejit.interview.service.AiInterviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AiInterviewIntegrationTest {

    @Autowired
    private AiInterviewService aiInterviewService;

    @Test
    @DisplayName("Full AI practice interview lifecycle: create, ask, hint, answer/evaluate, monitor, and complete")
    void testCompleteAiInterviewLifecycle() {
        String candidate = "candidate@codejit.io";

        CreateAiInterviewRequest createReq = CreateAiInterviewRequest.builder()
                .title("Java Backend AI Practice")
                .topic("Java & Spring Boot")
                .roleTitle("Backend Engineer")
                .difficulty(AiDifficulty.MID)
                .mode(AiInterviewMode.TECHNICAL_QA)
                .questionCount(2)
                .durationMinutes(20)
                .enableFollowUps(false)
                .strictMonitoring(true)
                .build();

        AiInterviewSessionDto created = aiInterviewService.createSession(createReq, candidate, 42L);
        assertNotNull(created.getId());
        assertEquals(AiInterviewStatus.IN_PROGRESS, created.getStatus());
        assertEquals(1, created.getTurns().size());
        assertEquals(AiTurnStatus.ASKED, created.getTurns().get(0).getStatus());
        assertTrue(created.getTurns().get(0).getExpectedConcepts().isEmpty(), "Expected concepts must not leak before answering");
        assertEquals(100, created.getMonitoring().getFocusIntegrityScore());

        // Request hint on Q1
        AiInterviewSessionDto afterHint = aiInterviewService.requestHint(created.getId(), created.getTurns().get(0).getId(), candidate);
        assertEquals(1, afterHint.getTurns().get(0).getRevealedHints().size());

        // Record monitoring events (tab switch + paste)
        AiMonitoringSummaryDto monitorAfterTab = aiInterviewService.recordMonitorEvent(
                created.getId(),
                RecordMonitorEventRequest.builder()
                        .eventType("TAB_SWITCH")
                        .severity(AiMonitorSeverity.WARNING)
                        .detail("Switched tab briefly")
                        .build(),
                candidate
        );
        assertEquals(1, monitorAfterTab.getTabSwitchCount());
        assertEquals(94, monitorAfterTab.getFocusIntegrityScore());

        AiMonitoringSummaryDto monitorAfterPaste = aiInterviewService.recordMonitorEvent(
                created.getId(),
                RecordMonitorEventRequest.builder()
                        .eventType("PASTE_DETECTED")
                        .severity(AiMonitorSeverity.WARNING)
                        .detail("Pasted 120 characters")
                        .build(),
                candidate
        );
        assertEquals(1, monitorAfterPaste.getPasteEventCount());
        assertEquals(86, monitorAfterPaste.getFocusIntegrityScore());

        EyeMonitorResultDto eyeAlert = aiInterviewService.monitorFrame(
                created.getId(),
                MonitorFrameRequest.builder()
                        .imageBase64("")
                        .localObservation("GAZE_AWAY")
                        .turnId(created.getTurns().get(0).getId())
                        .build(),
                candidate
        );
        assertTrue(eyeAlert.isCheatingSuspected());
        assertEquals(1, eyeAlert.getMonitoring().getEyeOffScreenCount());
        assertEquals(78, eyeAlert.getMonitoring().getFocusIntegrityScore());

        // Answer Q1 -> AI evaluates Q1 and generates Q2
        AiInterviewTurnDto q1Turn = afterHint.getTurns().get(0);
        String q1Answer = q1Turn.getQuestionText().contains("CompletableFuture")
                ? "CompletableFuture supports non-blocking callbacks like thenCompose and exception handling, whereas Future.get() blocks. Because default async methods use ForkJoinPool.commonPool, blocking I/O there causes thread pool starvation so we pass a bounded Executor."
                : "Spring AOP uses a proxy around beans, so self-invocation via `this` bypasses @Transactional. REQUIRES_NEW suspends the outer transaction and borrows a second DB connection from the connection pool, risking exhaustion under isolation and load.";

        AiInterviewSessionDto afterQ1 = aiInterviewService.submitAnswer(
                created.getId(),
                SubmitAiAnswerRequest.builder()
                        .turnId(q1Turn.getId())
                        .answerText(q1Answer)
                        .responseTimeSeconds(55)
                        .build(),
                candidate
        );
        assertEquals(AiInterviewStatus.IN_PROGRESS, afterQ1.getStatus());
        assertEquals(2, afterQ1.getTurns().size());
        assertEquals(AiTurnStatus.EVALUATED, afterQ1.getTurns().get(0).getStatus());
        assertNotNull(afterQ1.getTurns().get(0).getEvaluation());
        assertTrue(afterQ1.getTurns().get(0).getEvaluation().getScore() >= 70,
                "Expected Q1 score >= 70, got " + afterQ1.getTurns().get(0).getEvaluation().getScore());
        assertEquals(AiTurnStatus.ASKED, afterQ1.getTurns().get(1).getStatus());

        // Answer Q2 -> AI evaluates Q2 and automatically completes session + generates final report
        AiInterviewTurnDto q2Turn = afterQ1.getTurns().get(1);
        String q2Answer = q2Turn.getQuestionText().contains("CompletableFuture")
                ? "CompletableFuture supports non-blocking callbacks like thenCompose and exception handling, whereas Future.get() blocks. Because default async methods use ForkJoinPool.commonPool, blocking I/O there causes thread pool starvation so we pass a bounded Executor."
                : "Spring AOP uses a proxy around beans, so self-invocation via `this` bypasses @Transactional. REQUIRES_NEW suspends the outer transaction and borrows a second DB connection from the connection pool, risking exhaustion under isolation and load.";

        AiInterviewSessionDto completed = aiInterviewService.submitAnswer(
                created.getId(),
                SubmitAiAnswerRequest.builder()
                        .turnId(q2Turn.getId())
                        .answerText(q2Answer)
                        .responseTimeSeconds(60)
                        .build(),
                candidate
        );

        assertEquals(AiInterviewStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getFinalReport());
        assertNotNull(completed.getOverallScore());
        assertEquals(78, completed.getMonitoring().getFocusIntegrityScore());
        assertFalse(completed.getMonitorEvents().isEmpty());

        // Listing sessions for this candidate includes the completed session
        List<AiInterviewSummaryDto> list = aiInterviewService.getSessions(candidate);
        assertEquals(1, list.size());
        assertEquals(completed.getId(), list.get(0).getId());
        assertEquals(AiInterviewStatus.COMPLETED, list.get(0).getStatus());

        // Another user cannot access this session
        assertThrows(ResourceNotFoundException.class,
                () -> aiInterviewService.getSession(created.getId(), "other@codejit.io"));
    }

    @Test
    @DisplayName("Should ask follow-up question on partial answer and allow early completion with report")
    void testFollowUpAndEarlyCompletion() {
        String candidate = "followup@codejit.io";

        AiInterviewSessionDto created = aiInterviewService.createSession(
                CreateAiInterviewRequest.builder()
                        .topic("System Design")
                        .difficulty(AiDifficulty.SENIOR)
                        .mode(AiInterviewMode.SYSTEM_DESIGN)
                        .questionCount(3)
                        .enableFollowUps(true)
                        .strictMonitoring(true)
                        .build(),
                candidate,
                99L
        );

        AiInterviewTurnDto firstTurn = created.getTurns().get(0);
        AiInterviewSessionDto afterPartial = aiInterviewService.submitAnswer(
                created.getId(),
                SubmitAiAnswerRequest.builder()
                        .turnId(firstTurn.getId())
                        .answerText("We can use a centralized Redis cache with a token bucket rate limiting algorithm across gateway nodes because it keeps request counters shared.")
                        .responseTimeSeconds(35)
                        .build(),
                candidate
        );

        assertEquals(2, afterPartial.getTurns().size());
        assertTrue(afterPartial.getTurns().get(1).isFollowUp(), "Second turn should be an AI follow-up question");
        assertEquals("FOLLOW_UP", afterPartial.getTurns().get(1).getCategory());

        AiInterviewSessionDto endedEarly = aiInterviewService.completeSession(created.getId(), candidate);
        assertEquals(AiInterviewStatus.COMPLETED, endedEarly.getStatus());
        assertNotNull(endedEarly.getFinalReport());
        assertNotNull(endedEarly.getFinalReport().getMonitoringVerdict());
    }
}
