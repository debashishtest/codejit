package com.codejit.interview;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEvaluation;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedFinalReport;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedQuestion;
import com.codejit.interview.service.ai.BuiltInAiInterviewEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInAiInterviewEngineTest {

    private BuiltInAiInterviewEngine engine;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        engine = new BuiltInAiInterviewEngine();
    }

    @Test
    @DisplayName("Should generate distinct non-repeating questions across turns")
    void testGenerateNonRepeatingQuestions() {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Java & Spring Boot");
        session.setDifficulty(AiDifficulty.SENIOR);
        session.setMode(AiInterviewMode.MIXED);
        session.setQuestionCount(5);

        List<AiInterviewTurn> history = new ArrayList<>();
        Set<String> seenQuestions = new HashSet<>();

        for (int i = 1; i <= 5; i++) {
            GeneratedQuestion q = engine.generateNextQuestion(session, history);
            assertNotNull(q);
            assertNotNull(q.getQuestionText());
            assertFalse(q.getQuestionText().isBlank());
            assertTrue(seenQuestions.add(q.getQuestionText()), "Question should not repeat on turn " + i);

            AiInterviewTurn turn = new AiInterviewTurn();
            turn.setTurnNumber(i);
            turn.setCategory(q.getCategory());
            turn.setQuestionText(q.getQuestionText());
            history.add(turn);
        }
    }

    @Test
    @DisplayName("Should synthesize custom topic coding question with Python starter code when requested")
    void testCustomTopicPythonCodingQuestion() {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Python FastAPI Async Workers");
        session.setDifficulty(AiDifficulty.MID);
        session.setMode(AiInterviewMode.CODING);

        GeneratedQuestion q = engine.generateNextQuestion(session, List.of());

        assertEquals("CODING", q.getCategory());
        assertEquals("python", q.getCodeLanguage());
        assertNotNull(q.getStarterCode());
        assertFalse(q.getExpectedConcepts().isEmpty());
    }

    @Test
    @DisplayName("Should evaluate empty/non-substantive answers as INSUFFICIENT")
    void testEvaluateInsufficientAnswer() throws Exception {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Java");
        session.setEnableFollowUps(true);

        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setCategory("CONCEPTUAL");
        turn.setTopicTag("Java Concurrency");
        turn.setQuestionText("Explain CompletableFuture vs Future");
        turn.setExpectedConceptsJson(mapper.writeValueAsString(List.of("non-blocking", "thread pool", "forkjoinpool")));
        turn.setReferenceAnswer("Use a dedicated executor for blocking I/O.");

        SubmitAiAnswerRequest req = SubmitAiAnswerRequest.builder()
                .answerText("idk")
                .build();

        GeneratedEvaluation eval = engine.evaluateAnswer(session, turn, req);

        assertEquals(AiEvaluationVerdict.INSUFFICIENT, eval.getVerdict());
        assertTrue(eval.getScore() <= 15);
        assertFalse(eval.getMissingConcepts().isEmpty());
    }

    @Test
    @DisplayName("Should strictly penalize off-topic rambling answers with zero concept coverage")
    void testEvaluateOffTopicAnswerStrictly() throws Exception {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Java");

        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setCategory("CONCEPTUAL");
        turn.setTopicTag("Spring AOP Proxies");
        turn.setQuestionText("Explain why self-invocation bypasses @Transactional");
        turn.setExpectedConceptsJson(mapper.writeValueAsString(List.of("proxy", "self-invocation", "propagation", "isolation")));
        turn.setReferenceAnswer("Spring AOP uses dynamic proxies.");

        SubmitAiAnswerRequest req = SubmitAiAnswerRequest.builder()
                .answerText("I really enjoy building web apps with React and CSS flexbox because the UI looks clean and responsive for users.")
                .build();

        GeneratedEvaluation eval = engine.evaluateAnswer(session, turn, req);

        assertEquals(AiEvaluationVerdict.INSUFFICIENT, eval.getVerdict());
        assertTrue(eval.getScore() <= 20, "Off-topic answer must score <= 20, got " + eval.getScore());
    }

    @Test
    @DisplayName("Should reward strong technical answer covering expected concepts and reasoning")
    void testEvaluateStrongAnswer() throws Exception {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("DSA");
        session.setEnableFollowUps(true);

        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setCategory("CODING");
        turn.setTopicTag("LRU Cache Design");
        turn.setQuestionText("Design an LRU Cache");
        turn.setExpectedConceptsJson(mapper.writeValueAsString(List.of("hash map", "doubly linked list", "time complexity", "space complexity", "edge cases")));
        turn.setStarterCode("class LRUCache {}");
        turn.setReferenceAnswer("Combine HashMap and doubly linked list.");

        SubmitAiAnswerRequest req = SubmitAiAnswerRequest.builder()
                .answerText("Because we need O(1) lookup and O(1) eviction, I combine a hash map with a doubly linked list. "
                        + "Time complexity is O(1) for get and put, and space complexity is O(capacity). "
                        + "Edge cases include capacity <= 0 and updating an existing key without evicting.")
                .codeSnippet("class LRUCache { private Map<Integer, Node> map = new HashMap<>(); public int get(int k) { if (!map.containsKey(k)) return -1; return map.get(k).val; } }")
                .codeLanguage("java")
                .executionSummary("All 2 test cases passed in 14ms")
                .responseTimeSeconds(95)
                .build();

        GeneratedEvaluation eval = engine.evaluateAnswer(session, turn, req);

        assertTrue(eval.getScore() >= 75, "Expected strong score >= 75, got " + eval.getScore());
        assertTrue(eval.getVerdict() == AiEvaluationVerdict.EXCELLENT || eval.getVerdict() == AiEvaluationVerdict.STRONG);
        assertTrue(eval.getMissingConcepts().isEmpty());
    }

    @Test
    @DisplayName("Should ask targeted follow-up for partial answer when follow-ups are enabled")
    void testPartialAnswerFollowUp() throws Exception {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("System Design");
        session.setEnableFollowUps(true);

        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setFollowUp(false);
        turn.setCategory("SYSTEM_DESIGN");
        turn.setTopicTag("Distributed Rate Limiter");
        turn.setQuestionText("Design a distributed rate limiter");
        turn.setExpectedConceptsJson(mapper.writeValueAsString(List.of("rate limiting", "cache", "atomicity", "fault tolerance")));

        SubmitAiAnswerRequest req = SubmitAiAnswerRequest.builder()
                .answerText("We can use Redis as a centralized cache to store request counters per client using a sliding window rate limiting algorithm.")
                .responseTimeSeconds(45)
                .build();

        GeneratedEvaluation eval = engine.evaluateAnswer(session, turn, req);

        assertTrue(eval.getScore() >= 38 && eval.getScore() <= 80, "Expected partial score in follow-up range, got " + eval.getScore());
        assertNotNull(eval.getFollowUpQuestion(), "Should generate a follow-up question for missing concepts");
    }

    @Test
    @DisplayName("Should generate holistic final report including monitoring integrity")
    void testGenerateFinalReport() throws Exception {
        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Backend Engineering");
        session.setDifficulty(AiDifficulty.SENIOR);
        session.setMode(AiInterviewMode.MIXED);

        AiInterviewTurn t1 = new AiInterviewTurn();
        t1.setStatus(AiTurnStatus.EVALUATED);
        t1.setScore(82);
        t1.setTechnicalAccuracyScore(85);
        t1.setProblemSolvingScore(80);
        t1.setClarityScore(82);
        t1.setStrengthsJson(mapper.writeValueAsString(List.of("Strong concurrency explanation")));
        t1.setImprovementsJson(mapper.writeValueAsString(List.of("Add timeout discussion")));

        AiInterviewTurn t2 = new AiInterviewTurn();
        t2.setStatus(AiTurnStatus.SKIPPED);
        t2.setScore(0);

        AiMonitoringSummaryDto monitoring = AiMonitoringSummaryDto.builder()
                .monitorStatus(AiMonitorStatus.ATTENTION_NEEDED)
                .focusIntegrityScore(84)
                .tabSwitchCount(2)
                .windowBlurCount(1)
                .pasteEventCount(0)
                .idleWarningCount(0)
                .averageResponseSeconds(50)
                .scoreTrend("STEADY")
                .build();

        GeneratedFinalReport report = engine.generateFinalReport(session, List.of(t1, t2), monitoring);

        assertEquals(41, report.getOverallScore());
        assertEquals(84, report.getIntegrityFocusScore());
        assertNotNull(report.getExecutiveSummary());
        assertNotNull(report.getMonitoringVerdict());
        assertFalse(report.getStudyPlan().isEmpty());
    }
}
