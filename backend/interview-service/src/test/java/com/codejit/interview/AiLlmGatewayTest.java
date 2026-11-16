package com.codejit.interview;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedQuestion;
import com.codejit.interview.service.ai.AiLlmGateway;
import com.codejit.interview.service.ai.BuiltInAiInterviewEngine;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiLlmGatewayTest {

    @Test
    @DisplayName("Should use built-in engine seamlessly when no external API key is configured")
    void testFallbackWhenNoApiKey() {
        BuiltInAiInterviewEngine fallback = new BuiltInAiInterviewEngine();
        AiLlmGateway gateway = new AiLlmGateway(fallback, "auto", "", "", "", 5);

        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("System Design");
        session.setDifficulty(AiDifficulty.SENIOR);
        session.setMode(AiInterviewMode.SYSTEM_DESIGN);

        GeneratedQuestion q = gateway.generateNextQuestion(session, List.of());
        assertNotNull(q);
        assertEquals(BuiltInAiInterviewEngine.PROVIDER_ID, q.getProviderUsed());
        assertFalse(q.getQuestionText().isBlank());
    }

    @Test
    @DisplayName("Should fall back gracefully to built-in engine if external LLM endpoint fails")
    void testFallbackWhenExternalEndpointFails() {
        BuiltInAiInterviewEngine fallback = new BuiltInAiInterviewEngine();
        AiLlmGateway gateway = new AiLlmGateway(
                fallback, "openai", "sk-test-invalid", "gpt-4o-mini", "http://127.0.0.1:1", 5);

        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Java & Spring Boot");
        session.setDifficulty(AiDifficulty.MID);
        session.setMode(AiInterviewMode.TECHNICAL_QA);

        GeneratedQuestion q = gateway.generateNextQuestion(session, List.of());
        assertNotNull(q);
        assertEquals(BuiltInAiInterviewEngine.PROVIDER_ID, q.getProviderUsed());
    }

    @Test
    @DisplayName("Should detect Gemini provider for AIza keys and unwrap array-wrapped or fenced JSON")
    void testGeminiDetectionAndJsonUnwrap() throws Exception {
        BuiltInAiInterviewEngine fallback = new BuiltInAiInterviewEngine();
        AiLlmGateway gateway = new AiLlmGateway(fallback, "auto", "AIzaDummyTestKey123", "", "", 10);

        Method resolve = AiLlmGateway.class.getDeclaredMethod("resolveProvider");
        resolve.setAccessible(true);
        assertEquals("gemini", resolve.invoke(gateway));

        Method parse = AiLlmGateway.class.getDeclaredMethod("parseJsonContent", String.class);
        parse.setAccessible(true);
        JsonNode unwrapped = (JsonNode) parse.invoke(gateway, "```json\n[{\"questionText\":\"Explain MVCC\"}]\n```");
        assertNotNull(unwrapped);
        assertEquals("Explain MVCC", unwrapped.path("questionText").asText());
    }

    @Test
    @DisplayName("Should fail strictly at 5/100 when candidate gives empty, filler, or question-parroting answer even with Gemini configured")
    void testStrictGateRejectsParrotedOrFillerAnswers() {
        BuiltInAiInterviewEngine fallback = new BuiltInAiInterviewEngine();
        AiLlmGateway gateway = new AiLlmGateway(fallback, "gemini", "AIzaDummyTestKey123", "", "", 10);

        AiInterviewSession session = new AiInterviewSession();
        session.setTopic("Java");

        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setQuestionText("Explain how ConcurrentHashMap achieves thread safety in Java 8");
        turn.setReferenceAnswer("Uses CAS and synchronized bin heads.");

        var fillerEval = gateway.evaluateAnswer(session, turn, SubmitAiAnswerRequest.builder().answerText("uh idk").build());
        assertEquals(5, fillerEval.getScore());
        assertEquals(AiEvaluationVerdict.INSUFFICIENT, fillerEval.getVerdict());

        var parrotEval = gateway.evaluateAnswer(session, turn, SubmitAiAnswerRequest.builder()
                .answerText("ConcurrentHashMap achieves thread safety in Java 8").build());
        assertEquals(5, parrotEval.getScore());
        assertEquals(AiEvaluationVerdict.INSUFFICIENT, parrotEval.getVerdict());
    }
}
