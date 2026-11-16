package com.codejit.interview.service.ai;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEvaluation;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEyeAnalysis;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedFinalReport;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Component
@Primary
public class AiLlmGateway implements AiInterviewerEngine {

    private static final Logger log = LoggerFactory.getLogger(AiLlmGateway.class);

    private static final Set<String> NON_ANSWERS = Set.of(
            "idk", "i dont know", "i don't know", "don't know", "dont know", "no idea",
            "pass", "skip", "none", "n/a", "na", "nothing", "unsure", "um", "uh", "hmm", "hello", "test", "testing"
    );

    private final BuiltInAiInterviewEngine fallbackEngine;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutSeconds;

    public AiLlmGateway(
            @Qualifier("builtInAiInterviewEngine") BuiltInAiInterviewEngine fallbackEngine,
            @Value("${ai.interview.provider:auto}") String provider,
            @Value("${ai.interview.api-key:}") String apiKey,
            @Value("${ai.interview.model:}") String model,
            @Value("${ai.interview.base-url:}") String baseUrl,
            @Value("${ai.interview.timeout-seconds:45}") int timeoutSeconds) {
        this.fallbackEngine = fallbackEngine;
        this.objectMapper = new ObjectMapper();
        this.provider = provider != null ? provider.trim().toLowerCase(Locale.ROOT) : "auto";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null ? model.trim() : "";
        this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
        this.timeoutSeconds = Math.max(5, timeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
    }

    @Override
    public GeneratedQuestion generateNextQuestion(AiInterviewSession session, List<AiInterviewTurn> previousTurns) {
        String activeProvider = resolveProvider();
        if (activeProvider == null) {
            return fallbackEngine.generateNextQuestion(session, previousTurns);
        }

        try {
            String systemPrompt = "You are a senior technical interviewer conducting a spoken live interview on CodeJIT. "
                    + "Generate one concise, non-repeating oral interview question that the candidate will answer verbally. "
                    + "Return ONLY a single JSON object (not an array) with fields: "
                    + "category (CONCEPTUAL|CODING|SYSTEM_DESIGN|BEHAVIORAL), topicTag (string), questionText (string), "
                    + "hints (array of 2 concise strings), expectedConcepts (array of 4-6 short concept keywords), "
                    + "referenceAnswer (concise model answer string), starterCode (null), codeLanguage (null).";

            StringBuilder history = new StringBuilder();
            for (AiInterviewTurn t : previousTurns) {
                history.append("- Q").append(t.getTurnNumber()).append(" [").append(t.getCategory()).append("]: ")
                        .append(t.getQuestionText());
                if (t.getScore() != null) {
                    history.append(" (Score: ").append(t.getScore()).append("/100)");
                }
                history.append("\n");
            }

            String userPrompt = String.format(
                    "Topic: %s\nTarget Role: %s\nDifficulty: %s\nMode: %s\nFocus Areas: %s\nQuestion #%d of %d.\nPrevious Turns:\n%s\nGenerate the next distinct question.",
                    session.getTopic(),
                    session.getRoleTitle() != null ? session.getRoleTitle() : "Software Engineer",
                    session.getDifficulty(),
                    session.getMode(),
                    session.getFocusAreas() != null ? session.getFocusAreas() : "General",
                    previousTurns.size() + 1,
                    session.getQuestionCount(),
                    history.isEmpty() ? "(None yet — this is Question 1)" : history
            );

            JsonNode json = invokeJsonLlm(activeProvider, systemPrompt, userPrompt);
            if (json != null && json.hasNonNull("questionText")) {
                GeneratedQuestion q = new GeneratedQuestion();
                q.setCategory(normalizeCategory(json.path("category").asText("CONCEPTUAL")));
                q.setTopicTag(json.path("topicTag").asText(session.getTopic()));
                q.setDifficulty(session.getDifficulty());
                q.setQuestionText(json.path("questionText").asText());
                q.setHints(readStringArray(json.get("hints")));
                q.setExpectedConcepts(readStringArray(json.get("expectedConcepts")));
                q.setReferenceAnswer(json.path("referenceAnswer").asText(""));
                q.setStarterCode(readNullableText(json.get("starterCode")));
                q.setCodeLanguage(readNullableText(json.get("codeLanguage")));
                q.setProviderUsed(activeProvider.toUpperCase(Locale.ROOT));
                return q;
            }
        } catch (Exception ex) {
            log.warn("External AI question generation failed ({}), falling back to built-in engine: {}", activeProvider, ex.getMessage());
        }
        return fallbackEngine.generateNextQuestion(session, previousTurns);
    }

    @Override
    public GeneratedEvaluation evaluateAnswer(
            AiInterviewSession session,
            AiInterviewTurn turn,
            SubmitAiAnswerRequest answerRequest) {
        String activeProvider = resolveProvider();

        // Transcribe audio via Gemini multimodal if audio was recorded and transcript is empty/too short
        String spokenText = answerRequest.getAnswerText() != null ? answerRequest.getAnswerText().trim() : "";
        if ("gemini".equals(activeProvider)
                && answerRequest.getAudioBase64() != null
                && !answerRequest.getAudioBase64().isBlank()
                && countWords(spokenText) < 4) {
            String transcribed = transcribeAudioWithGemini(
                    answerRequest.getAudioBase64(),
                    answerRequest.getAudioMimeType());
            if (transcribed != null && !transcribed.isBlank()) {
                spokenText = transcribed.trim();
                answerRequest.setAnswerText(spokenText);
            }
        }

        // Deterministic pre-check gate: empty, filler, "idk", <4 words, or parroted question MUST fail strictly
        if (isNonSubstantiveOrEcho(spokenText, turn.getQuestionText(), answerRequest.getCodeSnippet())) {
            GeneratedEvaluation strictFail = buildStrictZeroEvaluation(turn, spokenText, activeProvider);
            strictFail.setTranscribedAnswer(spokenText);
            return strictFail;
        }

        if (activeProvider == null) {
            GeneratedEvaluation fallback = fallbackEngine.evaluateAnswer(session, turn, answerRequest);
            fallback.setTranscribedAnswer(spokenText);
            return fallback;
        }

        try {
            String systemPrompt = "You are a strict, realistic technical interviewer grading a live test. "
                    + "Grade AUTHENTICALLY with ZERO score inflation. Do NOT give polite 60-80 scores to vague, generic, incomplete, or incorrect answers. "
                    + "Strict calibration rubric: "
                    + "0-15 (INSUFFICIENT): Wrong, off-topic, generic buzzwords unrelated to the prompt, evasive ('I don't know'), or parroting the question. "
                    + "16-39 (NEEDS_IMPROVEMENT): Mentions a surface keyword or fragment with major gaps, misconceptions, or missing core mechanism. "
                    + "40-64 (ADEQUATE): Correct high-level idea, but lacks technical depth, trade-offs, edge cases, or concurrency/complexity analysis. "
                    + "65-84 (STRONG): Accurate, clear, well-reasoned with minor omissions. "
                    + "85-100 (EXCELLENT): Production-grade depth, exact mechanisms, trade-offs, complexity, and edge cases. "
                    + "Return ONLY a single JSON object (not an array) with fields: "
                    + "score (int 0-100), verdict (EXCELLENT|STRONG|ADEQUATE|NEEDS_IMPROVEMENT|INSUFFICIENT), "
                    + "technicalAccuracyScore (int 0-100), completenessScore (int 0-100), clarityScore (int 0-100), problemSolvingScore (int 0-100), "
                    + "strengths (array of strings; empty or honest if none), improvements (array of strings), missingConcepts (array of strings), "
                    + "feedback (blunt, constructive string), referenceAnswer (string), followUpQuestion (nullable string; only if followUpsEnabled=true and score is 30-78).";

            String userPrompt = String.format(
                    "Topic: %s | Difficulty: %s | Follow-ups enabled: %s | Is already follow-up: %s\n"
                            + "Question: %s\nExpected Concepts: %s\nReference Guide: %s\n\n"
                            + "Candidate Spoken Answer Transcript: %s\nResponse Time: %ss",
                    session.getTopic(),
                    session.getDifficulty(),
                    session.isEnableFollowUps(),
                    turn.isFollowUp(),
                    turn.getQuestionText(),
                    turn.getExpectedConceptsJson(),
                    turn.getReferenceAnswer(),
                    spokenText,
                    answerRequest.getResponseTimeSeconds()
            );

            JsonNode json = invokeJsonLlm(activeProvider, systemPrompt, userPrompt);
            if (json != null && json.has("score")) {
                GeneratedEvaluation eval = new GeneratedEvaluation();
                int wordCount = countWords(spokenText);
                int score = clamp(json.path("score").asInt(0));
                int tech = clamp(json.path("technicalAccuracyScore").asInt(score));
                int comp = clamp(json.path("completenessScore").asInt(score));
                int clarity = clamp(json.path("clarityScore").asInt(score));
                int prob = clamp(json.path("problemSolvingScore").asInt(score));

                // Guard against LLM over-scoring ultra-short 1-line spoken fragments
                if (wordCount < 12 && score > 42) {
                    score = 42;
                    comp = Math.min(comp, 35);
                }

                eval.setScore(score);
                eval.setVerdict(mapStrictVerdict(score));
                eval.setTechnicalAccuracyScore(tech);
                eval.setCompletenessScore(comp);
                eval.setClarityScore(clarity);
                eval.setProblemSolvingScore(prob);
                eval.setStrengths(readStringArray(json.get("strengths")));
                eval.setImprovements(readStringArray(json.get("improvements")));
                eval.setMissingConcepts(readStringArray(json.get("missingConcepts")));
                eval.setFeedback(json.path("feedback").asText(""));
                eval.setReferenceAnswer(json.path("referenceAnswer").asText(turn.getReferenceAnswer()));
                if (session.isEnableFollowUps() && !turn.isFollowUp()) {
                    eval.setFollowUpQuestion(readNullableText(json.get("followUpQuestion")));
                }
                eval.setTranscribedAnswer(spokenText);
                eval.setProviderUsed(activeProvider.toUpperCase(Locale.ROOT));
                return eval;
            }
        } catch (Exception ex) {
            log.warn("External AI evaluation failed ({}), falling back to built-in engine: {}", activeProvider, ex.getMessage());
        }
        GeneratedEvaluation fallback = fallbackEngine.evaluateAnswer(session, turn, answerRequest);
        fallback.setTranscribedAnswer(spokenText);
        return fallback;
    }

    @Override
    public GeneratedFinalReport generateFinalReport(
            AiInterviewSession session,
            List<AiInterviewTurn> allTurns,
            AiMonitoringSummaryDto monitoringSummary) {

        int trueTurnAvg = computeTrueTurnAverage(allTurns);
        int focusScore = monitoringSummary != null && monitoringSummary.getFocusIntegrityScore() != null
                ? monitoringSummary.getFocusIntegrityScore()
                : 100;
        int cheatingCount = monitoringSummary != null && monitoringSummary.getCheatingAlertCount() != null
                ? monitoringSummary.getCheatingAlertCount()
                : 0;

        String activeProvider = resolveProvider();
        if (activeProvider == null) {
            GeneratedFinalReport fallback = fallbackEngine.generateFinalReport(session, allTurns, monitoringSummary);
            reconcileAuthenticReport(fallback, trueTurnAvg, focusScore, cheatingCount, allTurns);
            return fallback;
        }

        try {
            String systemPrompt = "You are a strict hiring committee panelist summarizing a real monitored oral technical test. "
                    + "Base scores strictly on the candidate's actual per-question scores and monitoring integrity (NO inflation). "
                    + "Return ONLY a single JSON object (not an array) with fields: overallScore (int 0-100), recommendation (STRONG_HIRE|HIRE|BORDERLINE|NEEDS_PRACTICE), "
                    + "executiveSummary (string), technicalDepthScore (int 0-100), problemSolvingScore (int 0-100), communicationScore (int 0-100), "
                    + "consistencyScore (int 0-100), integrityFocusScore (int 0-100), topStrengths (array of strings), "
                    + "improvementAreas (array of strings), studyPlan (array of strings), monitoringVerdict (string).";

            StringBuilder turnsText = new StringBuilder();
            for (AiInterviewTurn t : allTurns) {
                turnsText.append(String.format("Turn %d [%s, status=%s, score=%s]: %s | Answer: %s\n",
                        t.getTurnNumber(), t.getTopicTag(), t.getStatus(), t.getScore(), t.getQuestionText(),
                        t.getUserAnswer() != null ? t.getUserAnswer() : "(none)"));
            }

            String userPrompt = String.format(
                    "Session: %s (%s, %s)\nTrue Arithmetic Average Across Turns: %d/100\n"
                            + "Monitoring Summary: focus=%d/100, status=%s, cheatingAlerts=%d, eyeOffScreen=%d, tabSwitches=%d, windowBlurs=%d, idleAlerts=%d\nTurns:\n%s",
                    session.getTopic(), session.getDifficulty(), session.getMode(), trueTurnAvg,
                    focusScore, monitoringSummary != null ? monitoringSummary.getMonitorStatus() : "OPTIMAL",
                    cheatingCount,
                    monitoringSummary != null && monitoringSummary.getEyeOffScreenCount() != null ? monitoringSummary.getEyeOffScreenCount() : 0,
                    monitoringSummary != null && monitoringSummary.getTabSwitchCount() != null ? monitoringSummary.getTabSwitchCount() : 0,
                    monitoringSummary != null && monitoringSummary.getWindowBlurCount() != null ? monitoringSummary.getWindowBlurCount() : 0,
                    monitoringSummary != null && monitoringSummary.getIdleWarningCount() != null ? monitoringSummary.getIdleWarningCount() : 0,
                    turnsText
            );

            JsonNode json = invokeJsonLlm(activeProvider, systemPrompt, userPrompt);
            if (json != null && json.has("overallScore")) {
                GeneratedFinalReport report = new GeneratedFinalReport();
                int score = clamp(json.path("overallScore").asInt(trueTurnAvg));
                report.setOverallScore(score);
                report.setRecommendation(parseRecommendation(json.path("recommendation").asText(""), score));
                report.setExecutiveSummary(json.path("executiveSummary").asText(""));
                report.setTechnicalDepthScore(clamp(json.path("technicalDepthScore").asInt(score)));
                report.setProblemSolvingScore(clamp(json.path("problemSolvingScore").asInt(score)));
                report.setCommunicationScore(clamp(json.path("communicationScore").asInt(score)));
                report.setConsistencyScore(clamp(json.path("consistencyScore").asInt(score)));
                report.setIntegrityFocusScore(focusScore);
                report.setTopStrengths(readStringArray(json.get("topStrengths")));
                report.setImprovementAreas(readStringArray(json.get("improvementAreas")));
                report.setStudyPlan(readStringArray(json.get("studyPlan")));
                report.setMonitoringVerdict(json.path("monitoringVerdict").asText(""));
                report.setProviderUsed(activeProvider.toUpperCase(Locale.ROOT));
                reconcileAuthenticReport(report, trueTurnAvg, focusScore, cheatingCount, allTurns);
                return report;
            }
        } catch (Exception ex) {
            log.warn("External AI report generation failed ({}), falling back to built-in engine: {}", activeProvider, ex.getMessage());
        }
        GeneratedFinalReport fallback = fallbackEngine.generateFinalReport(session, allTurns, monitoringSummary);
        reconcileAuthenticReport(fallback, trueTurnAvg, focusScore, cheatingCount, allTurns);
        return fallback;
    }

    @Override
    public GeneratedEyeAnalysis analyzeMonitorFrame(AiInterviewSession session, MonitorFrameRequest frameRequest) {
        String activeProvider = resolveProvider();
        if (!"gemini".equals(activeProvider)
                || frameRequest == null
                || frameRequest.getImageBase64() == null
                || frameRequest.getImageBase64().isBlank()) {
            return fallbackEngine.analyzeMonitorFrame(session, frameRequest);
        }

        try {
            String geminiModel = model.isEmpty() ? "gemini-3.1-flash-lite" : model;
            String endpoint = (baseUrl.isEmpty() ? "https://generativelanguage.googleapis.com" : baseUrl)
                    + "/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

            String prompt = "You are a strict AI exam proctor analyzing a webcam frame during a live oral interview. "
                    + "Check if the candidate's face and eyes are visible and focused on the screen. "
                    + "Ignore brief natural micro-blinks, but set cheatingSuspected=true and lookingAtScreen=false if: "
                    + "(1) no face or camera is covered/black, (2) more than 1 person is visible, "
                    + "(3) candidate's eyes/head are clearly looking away, sideways at another monitor, or down at a phone/notes, "
                    + "or (4) a phone, book, or cheat-sheet is visible. "
                    + "Local CV observation: " + (frameRequest.getLocalObservation() != null ? frameRequest.getLocalObservation() : "none") + ". "
                    + "Return ONLY a single JSON object (not an array): "
                    + "{facePresent: boolean, multiplePeople: boolean, lookingAtScreen: boolean, "
                    + "gazeDirection: \"CENTER\"|\"LEFT\"|\"RIGHT\"|\"DOWN\"|\"UP\"|\"AWAY\", "
                    + "cheatingSuspected: boolean, alertReason: string|null}.";

            String cleanBase64 = stripDataUrlPrefix(frameRequest.getImageBase64());
            String mimeType = frameRequest.getMimeType() != null && !frameRequest.getMimeType().isBlank()
                    ? frameRequest.getMimeType()
                    : "image/jpeg";

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inlineData", Map.of("mimeType", mimeType, "data", cleanBase64))
                            )
                    )),
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.1)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(resp.body());
                String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
                JsonNode json = parseJsonContent(text);
                if (json != null && json.has("facePresent")) {
                    GeneratedEyeAnalysis analysis = new GeneratedEyeAnalysis();
                    analysis.setFacePresent(json.path("facePresent").asBoolean(true));
                    analysis.setMultiplePeople(json.path("multiplePeople").asBoolean(false));
                    analysis.setLookingAtScreen(json.path("lookingAtScreen").asBoolean(true));
                    analysis.setGazeDirection(json.path("gazeDirection").asText("CENTER").toUpperCase(Locale.ROOT));
                    analysis.setCheatingSuspected(json.path("cheatingSuspected").asBoolean(false));
                    analysis.setAlertReason(readNullableText(json.get("alertReason")));
                    return analysis;
                }
            }
        } catch (Exception ex) {
            log.warn("Gemini Vision monitor frame check failed, falling back to local observation: {}", ex.getMessage());
        }
        return fallbackEngine.analyzeMonitorFrame(session, frameRequest);
    }

    private String transcribeAudioWithGemini(String audioBase64, String audioMimeType) {
        try {
            String geminiModel = model.isEmpty() ? "gemini-3.1-flash-lite" : model;
            String endpoint = (baseUrl.isEmpty() ? "https://generativelanguage.googleapis.com" : baseUrl)
                    + "/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

            String cleanBase64 = stripDataUrlPrefix(audioBase64);
            String mimeType = (audioMimeType != null && !audioMimeType.isBlank())
                    ? audioMimeType.split(";")[0].trim()
                    : "audio/webm";

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(
                                    Map.of("text", "Transcribe the candidate's spoken answer verbatim. Do not invent or improve anything. If silent or unintelligible, return empty string. Return ONLY JSON: {\"transcript\": string}"),
                                    Map.of("inlineData", Map.of("mimeType", mimeType, "data", cleanBase64))
                            )
                    )),
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.0)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(resp.body());
                String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
                JsonNode json = parseJsonContent(text);
                if (json != null && json.has("transcript")) {
                    return json.path("transcript").asText("").trim();
                }
            }
        } catch (Exception ex) {
            log.warn("Gemini audio transcription failed: {}", ex.getMessage());
        }
        return null;
    }

    private void reconcileAuthenticReport(
            GeneratedFinalReport report,
            int trueTurnAvg,
            int focusScore,
            int cheatingCount,
            List<AiInterviewTurn> allTurns) {
        long evaluatedCount = allTurns.stream().filter(t -> t.getStatus() == AiTurnStatus.EVALUATED).count();
        int adjustedScore = Math.min(report.getOverallScore() != null ? report.getOverallScore() : trueTurnAvg, trueTurnAvg);

        if (cheatingCount >= 3 || focusScore < 55) {
            adjustedScore = Math.max(0, adjustedScore - 15);
        } else if (cheatingCount >= 1 || focusScore < 80) {
            adjustedScore = Math.max(0, adjustedScore - 5);
        }

        report.setOverallScore(clamp(adjustedScore));
        report.setIntegrityFocusScore(clamp(focusScore));

        AiRecommendation strictRec;
        if (evaluatedCount == 0 || adjustedScore < 50 || cheatingCount >= 3 || focusScore < 50) {
            strictRec = AiRecommendation.NEEDS_PRACTICE;
        } else if (adjustedScore < 70 || cheatingCount >= 2 || focusScore < 70) {
            strictRec = AiRecommendation.BORDERLINE;
        } else if (adjustedScore >= 85 && focusScore >= 85 && cheatingCount == 0) {
            strictRec = AiRecommendation.STRONG_HIRE;
        } else {
            strictRec = AiRecommendation.HIRE;
        }
        report.setRecommendation(strictRec);

        if (cheatingCount > 0) {
            String prefix = "INTEGRITY WARNING (" + cheatingCount + " cheating/eye-monitor alert(s), Focus " + focusScore + "/100): ";
            if (report.getMonitoringVerdict() == null || !report.getMonitoringVerdict().contains("INTEGRITY WARNING")) {
                report.setMonitoringVerdict(prefix + (report.getMonitoringVerdict() != null ? report.getMonitoringVerdict() : ""));
            }
        }
    }

    private int computeTrueTurnAverage(List<AiInterviewTurn> allTurns) {
        List<AiInterviewTurn> finished = allTurns.stream()
                .filter(t -> t.getStatus() == AiTurnStatus.EVALUATED || t.getStatus() == AiTurnStatus.SKIPPED)
                .toList();
        if (finished.isEmpty()) return 0;
        double sum = finished.stream().mapToInt(t -> t.getScore() != null ? t.getScore() : 0).sum();
        return clamp((int) Math.round(sum / finished.size()));
    }

    private boolean isNonSubstantiveOrEcho(String spokenText, String questionText, String codeSnippet) {
        if (codeSnippet != null && codeSnippet.trim().length() >= 30) {
            return false;
        }
        if (spokenText == null || spokenText.isBlank()) {
            return true;
        }
        String norm = spokenText.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s']", " ").replaceAll("\\s+", " ").trim();
        if (norm.isBlank() || NON_ANSWERS.contains(norm) || countWords(norm) < 4) {
            return true;
        }
        // Detect repeating the question verbatim without meaningful additional explanation
        if (questionText != null && !questionText.isBlank()) {
            Set<String> qWords = new HashSet<>(Arrays.asList(questionText.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").trim().split("\\s+")));
            String[] aWords = norm.split("\\s+");
            long nonQuestionWords = Arrays.stream(aWords).filter(w -> w.length() >= 3 && !qWords.contains(w)).count();
            if (aWords.length >= 4 && nonQuestionWords <= 2) {
                return true;
            }
        }
        return false;
    }

    private GeneratedEvaluation buildStrictZeroEvaluation(AiInterviewTurn turn, String spokenText, String activeProvider) {
        GeneratedEvaluation eval = new GeneratedEvaluation();
        eval.setScore(5);
        eval.setVerdict(AiEvaluationVerdict.INSUFFICIENT);
        eval.setTechnicalAccuracyScore(0);
        eval.setCompletenessScore(0);
        eval.setClarityScore(spokenText != null && !spokenText.isBlank() ? 10 : 0);
        eval.setProblemSolvingScore(0);
        eval.setStrengths(List.of("None demonstrated — response lacked technical substance."));
        eval.setImprovements(List.of(
                "Answer the specific question directly with concrete mechanisms, reasoning, and examples.",
                "Avoid filler phrases or repeating the prompt without explaining how or why."
        ));
        eval.setMissingConcepts(List.of("Core mechanism", "Technical explanation", "Trade-offs & edge cases"));
        eval.setFeedback("Insufficient answer (5/100). Your spoken response did not provide a substantive technical explanation for this question.");
        eval.setReferenceAnswer(turn.getReferenceAnswer());
        eval.setFollowUpQuestion(null);
        eval.setProviderUsed(activeProvider != null ? activeProvider.toUpperCase(Locale.ROOT) : BuiltInAiInterviewEngine.PROVIDER_ID);
        return eval;
    }

    private AiEvaluationVerdict mapStrictVerdict(int score) {
        if (score >= 85) return AiEvaluationVerdict.EXCELLENT;
        if (score >= 65) return AiEvaluationVerdict.STRONG;
        if (score >= 40) return AiEvaluationVerdict.ADEQUATE;
        if (score >= 16) return AiEvaluationVerdict.NEEDS_IMPROVEMENT;
        return AiEvaluationVerdict.INSUFFICIENT;
    }

    String resolveProvider() {
        if ("builtin".equals(provider) || apiKey.isEmpty()) {
            return null;
        }
        if ("gemini".equals(provider) || ("auto".equals(provider) && apiKey.startsWith("AIza"))) {
            return "gemini";
        }
        if ("openai".equals(provider) || "auto".equals(provider)) {
            return "openai";
        }
        return null;
    }

    private JsonNode invokeJsonLlm(String activeProvider, String systemPrompt, String userPrompt) throws Exception {
        if ("gemini".equals(activeProvider)) {
            String geminiModel = model.isEmpty() ? "gemini-3.1-flash-lite" : model;
            String endpoint = (baseUrl.isEmpty() ? "https://generativelanguage.googleapis.com" : baseUrl)
                    + "/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

            Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.3)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IllegalStateException("Gemini HTTP " + resp.statusCode());
            }
            JsonNode root = objectMapper.readTree(resp.body());
            String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            return parseJsonContent(text);
        }

        String openaiModel = model.isEmpty() ? "gpt-4o-mini" : model;
        String endpoint = (baseUrl.isEmpty() ? "https://api.openai.com/v1" : baseUrl) + "/chat/completions";
        Map<String, Object> body = Map.of(
                "model", openaiModel,
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("OpenAI HTTP " + resp.statusCode());
        }
        JsonNode root = objectMapper.readTree(resp.body());
        String text = root.path("choices").path(0).path("message").path("content").asText("");
        return parseJsonContent(text);
    }

    JsonNode parseJsonContent(String raw) throws Exception {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        JsonNode parsed = objectMapper.readTree(cleaned);
        if (parsed != null && parsed.isArray() && !parsed.isEmpty()) {
            return parsed.get(0);
        }
        return parsed;
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        return objectMapper.convertValue(node, new TypeReference<>() {});
    }

    private String readNullableText(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String text = node.asText("").trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "none".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    private String normalizeCategory(String raw) {
        String upper = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (upper.contains("CODING")) return "CODING";
        if (upper.contains("SYSTEM") || upper.contains("DESIGN") || upper.contains("ARCH")) return "SYSTEM_DESIGN";
        if (upper.contains("BEHAVIOR")) return "BEHAVIORAL";
        return "CONCEPTUAL";
    }

    private String stripDataUrlPrefix(String raw) {
        if (raw == null) return "";
        int commaIdx = raw.indexOf(',');
        if (raw.startsWith("data:") && commaIdx > 0) {
            return raw.substring(commaIdx + 1);
        }
        return raw;
    }

    private AiRecommendation parseRecommendation(String val, int score) {
        try {
            return AiRecommendation.valueOf(val.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            if (score >= 85) return AiRecommendation.STRONG_HIRE;
            if (score >= 70) return AiRecommendation.HIRE;
            if (score >= 50) return AiRecommendation.BORDERLINE;
            return AiRecommendation.NEEDS_PRACTICE;
        }
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
