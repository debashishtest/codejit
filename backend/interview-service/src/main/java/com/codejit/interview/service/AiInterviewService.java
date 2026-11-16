package com.codejit.interview.service;

import com.codejit.common.dto.ai.*;
import com.codejit.common.exception.BadRequestException;
import com.codejit.common.exception.ResourceNotFoundException;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;
import com.codejit.interview.entity.AiMonitorEvent;
import com.codejit.interview.repository.AiInterviewSessionRepository;
import com.codejit.interview.repository.AiMonitorEventRepository;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEvaluation;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEyeAnalysis;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedFinalReport;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedQuestion;
import com.codejit.interview.service.ai.AiInterviewerEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class AiInterviewService {

    private static final String DEFAULT_CANDIDATE_EMAIL = "practice@codejit.local";

    private final AiInterviewSessionRepository sessionRepository;
    private final AiMonitorEventRepository monitorEventRepository;
    private final AiInterviewerEngine aiEngine;
    private final ObjectMapper objectMapper;

    public AiInterviewService(
            AiInterviewSessionRepository sessionRepository,
            AiMonitorEventRepository monitorEventRepository,
            AiInterviewerEngine aiEngine) {
        this.sessionRepository = sessionRepository;
        this.monitorEventRepository = monitorEventRepository;
        this.aiEngine = aiEngine;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public AiInterviewSessionDto createSession(CreateAiInterviewRequest request, String userEmail, Long userId) {
        if (request == null || request.getTopic() == null || request.getTopic().isBlank()) {
            throw new BadRequestException("Interview topic is required");
        }

        String topic = request.getTopic().trim();
        AiDifficulty difficulty = request.getDifficulty() != null ? request.getDifficulty() : AiDifficulty.MID;
        AiInterviewMode mode = request.getMode() != null ? request.getMode() : AiInterviewMode.MIXED;
        int questionCount = clamp(request.getQuestionCount() != null ? request.getQuestionCount() : 5, 1, 10);
        int durationMinutes = clamp(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30, 5, 120);
        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle().trim()
                : topic + " (" + difficulty.name() + " AI Practice)";
        String email = resolveCandidateEmail(userEmail);

        AiInterviewSession session = new AiInterviewSession();
        session.setTitle(truncate(title, 250));
        session.setTopic(truncate(topic, 250));
        session.setRoleTitle(truncate(request.getRoleTitle() != null && !request.getRoleTitle().isBlank()
                ? request.getRoleTitle().trim()
                : "Software Engineer", 250));
        session.setDifficulty(difficulty);
        session.setMode(mode);
        session.setStatus(AiInterviewStatus.IN_PROGRESS);
        session.setQuestionCount(questionCount);
        session.setDurationMinutes(durationMinutes);
        session.setFocusAreas(request.getFocusAreas() != null ? request.getFocusAreas().trim() : null);
        session.setEnableFollowUps(!Boolean.FALSE.equals(request.getEnableFollowUps()));
        session.setStrictMonitoring(!Boolean.FALSE.equals(request.getStrictMonitoring()));
        session.setCandidateEmail(truncate(email, 250));
        session.setCandidateUserId(userId);
        session.setCurrentTurnIndex(0);
        session.setStartedAt(LocalDateTime.now());

        GeneratedQuestion q1 = aiEngine.generateNextQuestion(session, List.of());
        session.setAiProviderUsed(truncate(q1.getProviderUsed(), 100));

        AiInterviewTurn firstTurn = buildTurnEntity(1, false, null, q1);
        session.addTurn(firstTurn);

        AiMonitorEvent startEvent = new AiMonitorEvent();
        startEvent.setEventType("SESSION_STARTED");
        startEvent.setSeverity(AiMonitorSeverity.INFO);
        startEvent.setDetail("AI practice interview created and live session monitoring started.");
        startEvent.setFocusScoreImpact(0);
        session.addMonitorEvent(startEvent);

        AiInterviewSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AiInterviewSummaryDto> getSessions(String userEmail) {
        String email = resolveCandidateEmail(userEmail);
        List<AiInterviewSession> sessions = sessionRepository.findByCandidateEmailOrderByCreatedAtDesc(email);
        return sessions.stream().map(this::toSummaryDto).toList();
    }

    @Transactional(readOnly = true)
    public AiInterviewSessionDto getSession(Long id, String userEmail) {
        AiInterviewSession session = loadOwnedSession(id, userEmail);
        return toSessionDto(session);
    }

    @Transactional
    public AiInterviewSessionDto submitAnswer(Long sessionId, SubmitAiAnswerRequest request, String userEmail) {
        AiInterviewSession session = loadOwnedSession(sessionId, userEmail);
        if (session.getStatus() != AiInterviewStatus.IN_PROGRESS) {
            throw new BadRequestException("Interview session is already completed");
        }
        if (request == null) {
            throw new BadRequestException("Answer payload is required");
        }

        AiInterviewTurn activeTurn = resolveActiveTurn(session, request.getTurnId());
        boolean skipped = Boolean.TRUE.equals(request.getSkipped());

        String answerText = request.getAnswerText() != null ? request.getAnswerText().trim() : "";
        String codeSnippet = request.getCodeSnippet() != null ? request.getCodeSnippet().trim() : "";
        boolean hasAudio = request.getAudioBase64() != null && !request.getAudioBase64().isBlank();
        if (!skipped && answerText.isBlank() && codeSnippet.isBlank() && !hasAudio) {
            throw new BadRequestException("Please speak an answer before submitting");
        }

        activeTurn.setUserAnswer(answerText);
        activeTurn.setUserCode(codeSnippet.isBlank() ? null : codeSnippet);
        activeTurn.setCodeLanguageUsed(request.getCodeLanguage());
        activeTurn.setExecutionSummary(request.getExecutionSummary());
        activeTurn.setResponseTimeSeconds(request.getResponseTimeSeconds());
        activeTurn.setAnsweredAt(LocalDateTime.now());

        GeneratedEvaluation evaluation = null;
        if (skipped) {
            activeTurn.setStatus(AiTurnStatus.SKIPPED);
            activeTurn.setScore(0);
            activeTurn.setVerdict(AiEvaluationVerdict.INSUFFICIENT);
            activeTurn.setFeedback("Question skipped. Review the reference answer in your final report.");
            recordInternalEvent(session, "QUESTION_SKIPPED", AiMonitorSeverity.INFO,
                    "Skipped turn #" + activeTurn.getTurnNumber() + " (" + activeTurn.getTopicTag() + ")", activeTurn.getId(), 0);
        } else {
            evaluation = aiEngine.evaluateAnswer(session, activeTurn, request);
            if (evaluation.getTranscribedAnswer() != null && !evaluation.getTranscribedAnswer().isBlank()) {
                activeTurn.setUserAnswer(evaluation.getTranscribedAnswer());
            }
            if (evaluation.getProviderUsed() != null) {
                session.setAiProviderUsed(evaluation.getProviderUsed());
            }
            activeTurn.setStatus(AiTurnStatus.EVALUATED);
            activeTurn.setScore(evaluation.getScore());
            activeTurn.setVerdict(evaluation.getVerdict());
            activeTurn.setTechnicalAccuracyScore(evaluation.getTechnicalAccuracyScore());
            activeTurn.setCompletenessScore(evaluation.getCompletenessScore());
            activeTurn.setClarityScore(evaluation.getClarityScore());
            activeTurn.setProblemSolvingScore(evaluation.getProblemSolvingScore());
            activeTurn.setStrengthsJson(toJson(evaluation.getStrengths()));
            activeTurn.setImprovementsJson(toJson(evaluation.getImprovements()));
            activeTurn.setMissingConceptsJson(toJson(evaluation.getMissingConcepts()));
            activeTurn.setFeedback(evaluation.getFeedback());
            activeTurn.setFollowUpQuestion(evaluation.getFollowUpQuestion());

            boolean rushed = request.getResponseTimeSeconds() != null && request.getResponseTimeSeconds() < 6
                    && answerText.length() < 25 && codeSnippet.isBlank();
            recordInternalEvent(session, "ANSWER_EVALUATED",
                    rushed ? AiMonitorSeverity.WARNING : AiMonitorSeverity.INFO,
                    "Evaluated turn #" + activeTurn.getTurnNumber() + " with score " + evaluation.getScore() + "/100"
                            + (rushed ? " (rushed response pacing)" : ""),
                    activeTurn.getId(), 0);
        }

        long mainQuestionsAsked = session.getTurns().stream().filter(t -> !t.isFollowUp()).count();
        long followUpsAsked = session.getTurns().stream().filter(AiInterviewTurn::isFollowUp).count();

        boolean shouldAskFollowUp = !skipped
                && session.isEnableFollowUps()
                && !activeTurn.isFollowUp()
                && followUpsAsked < 2
                && evaluation != null
                && evaluation.getFollowUpQuestion() != null
                && !evaluation.getFollowUpQuestion().isBlank();

        int nextTurnNumber = session.getTurns().size() + 1;

        if (shouldAskFollowUp) {
            GeneratedQuestion followUpQ = new GeneratedQuestion();
            followUpQ.setCategory("FOLLOW_UP");
            followUpQ.setTopicTag(activeTurn.getTopicTag());
            followUpQ.setDifficulty(activeTurn.getDifficulty());
            followUpQ.setQuestionText(evaluation.getFollowUpQuestion());
            followUpQ.setHints(List.of("Focus specifically on edge cases, failure modes, and operational trade-offs."));
            followUpQ.setExpectedConcepts(parseList(activeTurn.getMissingConceptsJson()));
            followUpQ.setReferenceAnswer(activeTurn.getReferenceAnswer());

            AiInterviewTurn followUpTurn = buildTurnEntity(nextTurnNumber, true, activeTurn.getId(), followUpQ);
            session.addTurn(followUpTurn);
            session.setCurrentTurnIndex(nextTurnNumber - 1);
            recordInternalEvent(session, "AI_FOLLOW_UP_ASKED", AiMonitorSeverity.INFO,
                    "AI asked follow-up probing question #" + nextTurnNumber, followUpTurn.getId(), 0);
        } else if (mainQuestionsAsked < session.getQuestionCount()) {
            GeneratedQuestion nextQ = aiEngine.generateNextQuestion(session, session.getTurns());
            if (nextQ.getProviderUsed() != null) {
                session.setAiProviderUsed(nextQ.getProviderUsed());
            }
            AiInterviewTurn nextTurn = buildTurnEntity(nextTurnNumber, false, null, nextQ);
            session.addTurn(nextTurn);
            session.setCurrentTurnIndex(nextTurnNumber - 1);
            recordInternalEvent(session, "AI_QUESTION_ASKED", AiMonitorSeverity.INFO,
                    "AI asked question #" + (mainQuestionsAsked + 1) + " of " + session.getQuestionCount(), nextTurn.getId(), 0);
        } else {
            finalizeSession(session);
        }

        AiInterviewSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    @Transactional
    public AiInterviewSessionDto requestHint(Long sessionId, Long turnId, String userEmail) {
        AiInterviewSession session = loadOwnedSession(sessionId, userEmail);
        if (session.getStatus() != AiInterviewStatus.IN_PROGRESS) {
            throw new BadRequestException("Interview session is already completed");
        }
        AiInterviewTurn activeTurn = resolveActiveTurn(session, turnId);
        List<String> allHints = parseList(activeTurn.getHintsJson());
        int current = activeTurn.getRevealedHintsCount() != null ? activeTurn.getRevealedHintsCount() : 0;
        if (current < allHints.size()) {
            activeTurn.setRevealedHintsCount(current + 1);
            recordInternalEvent(session, "HINT_REQUESTED", AiMonitorSeverity.INFO,
                    "Candidate requested hint #" + (current + 1) + " for turn #" + activeTurn.getTurnNumber(),
                    activeTurn.getId(), 0);
            sessionRepository.save(session);
        }
        return toSessionDto(session);
    }

    @Transactional
    public AiMonitoringSummaryDto recordMonitorEvent(Long sessionId, RecordMonitorEventRequest request, String userEmail) {
        AiInterviewSession session = loadOwnedSession(sessionId, userEmail);
        if (request == null || request.getEventType() == null || request.getEventType().isBlank()) {
            throw new BadRequestException("Monitor eventType is required");
        }

        if (session.getStatus() == AiInterviewStatus.IN_PROGRESS) {
            String type = request.getEventType().trim().toUpperCase(Locale.ROOT);
            AiMonitorSeverity severity = request.getSeverity() != null ? request.getSeverity() : AiMonitorSeverity.INFO;
            int impact = 0;

            switch (type) {
                case "TAB_SWITCH" -> {
                    session.setTabSwitchCount(session.getTabSwitchCount() + 1);
                    if (session.isStrictMonitoring() || session.getTabSwitchCount() >= 2) {
                        session.setCheatingAlertCount(session.getCheatingAlertCount() + 1);
                    }
                    impact = session.isStrictMonitoring() ? -6 : -3;
                    severity = AiMonitorSeverity.ALERT;
                }
                case "WINDOW_BLUR" -> {
                    session.setWindowBlurCount(session.getWindowBlurCount() + 1);
                    impact = session.isStrictMonitoring() ? -4 : -2;
                    severity = AiMonitorSeverity.WARNING;
                }
                case "PASTE_DETECTED" -> {
                    session.setPasteEventCount(session.getPasteEventCount() + 1);
                    session.setCheatingAlertCount(session.getCheatingAlertCount() + 1);
                    impact = session.isStrictMonitoring() ? -8 : -4;
                    severity = AiMonitorSeverity.ALERT;
                }
                case "IDLE_DETECTED" -> {
                    session.setIdleWarningCount(session.getIdleWarningCount() + 1);
                    impact = session.isStrictMonitoring() ? -2 : 0;
                    severity = AiMonitorSeverity.WARNING;
                }
                case "EYE_GAZE_AWAY", "EYE_CHEATING_SUSPECTED", "FACE_MISSING" -> {
                    session.setEyeOffScreenCount(session.getEyeOffScreenCount() + 1);
                    session.setCheatingAlertCount(session.getCheatingAlertCount() + 1);
                    impact = session.isStrictMonitoring() ? -8 : -4;
                    severity = AiMonitorSeverity.ALERT;
                }
                case "MULTIPLE_FACES", "CAMERA_COVERED", "CAMERA_BLOCKED" -> {
                    session.setCheatingAlertCount(session.getCheatingAlertCount() + 1);
                    impact = session.isStrictMonitoring() ? -12 : -6;
                    severity = AiMonitorSeverity.ALERT;
                }
                default -> {
                    // Observational telemetry (FOCUS_REGAINED, CAMERA_ENABLED, VOICE_USED)
                }
            }

            int updatedFocus = clamp(session.getFocusIntegrityScore() + impact, 0, 100);
            session.setFocusIntegrityScore(updatedFocus);

            AiMonitorEvent event = new AiMonitorEvent();
            event.setEventType(type);
            event.setSeverity(severity);
            event.setDetail(request.getDetail() != null && !request.getDetail().isBlank()
                    ? request.getDetail().trim()
                    : "Monitored event: " + type);
            event.setTurnId(request.getTurnId());
            event.setFocusScoreImpact(impact);
            event.setMetadata(request.getMetadata());
            session.addMonitorEvent(event);

            sessionRepository.save(session);
        }

        return buildMonitoringSummary(session);
    }

    @Transactional
    public EyeMonitorResultDto monitorFrame(Long sessionId, MonitorFrameRequest request, String userEmail) {
        AiInterviewSession session = loadOwnedSession(sessionId, userEmail);
        if (session.getStatus() != AiInterviewStatus.IN_PROGRESS) {
            return EyeMonitorResultDto.builder()
                    .facePresent(true)
                    .lookingAtScreen(true)
                    .gazeDirection("CENTER")
                    .cheatingSuspected(false)
                    .monitoring(buildMonitoringSummary(session))
                    .build();
        }

        GeneratedEyeAnalysis analysis = aiEngine.analyzeMonitorFrame(session, request);
        if (analysis.isCheatingSuspected() || !analysis.isFacePresent() || analysis.isMultiplePeople() || !analysis.isLookingAtScreen()) {
            String eventType = analysis.isMultiplePeople()
                    ? "MULTIPLE_FACES"
                    : (!analysis.isFacePresent() ? "FACE_MISSING" : "EYE_CHEATING_SUSPECTED");
            String detail = analysis.getAlertReason() != null && !analysis.getAlertReason().isBlank()
                    ? analysis.getAlertReason()
                    : "AI Eye Monitor detected off-screen gaze or suspicious behavior (" + analysis.getGazeDirection() + ")";

            RecordMonitorEventRequest eventReq = RecordMonitorEventRequest.builder()
                    .eventType(eventType)
                    .severity(AiMonitorSeverity.ALERT)
                    .detail(detail)
                    .turnId(request != null ? request.getTurnId() : null)
                    .build();
            AiMonitoringSummaryDto summary = recordMonitorEvent(sessionId, eventReq, userEmail);

            return EyeMonitorResultDto.builder()
                    .facePresent(analysis.isFacePresent())
                    .multiplePeople(analysis.isMultiplePeople())
                    .lookingAtScreen(analysis.isLookingAtScreen())
                    .gazeDirection(analysis.getGazeDirection())
                    .cheatingSuspected(true)
                    .alertReason(detail)
                    .monitoring(summary)
                    .build();
        }

        return EyeMonitorResultDto.builder()
                .facePresent(analysis.isFacePresent())
                .multiplePeople(analysis.isMultiplePeople())
                .lookingAtScreen(analysis.isLookingAtScreen())
                .gazeDirection(analysis.getGazeDirection())
                .cheatingSuspected(false)
                .monitoring(buildMonitoringSummary(session))
                .build();
    }

    @Transactional
    public AiInterviewSessionDto completeSession(Long sessionId, String userEmail) {
        AiInterviewSession session = loadOwnedSession(sessionId, userEmail);
        if (session.getStatus() == AiInterviewStatus.IN_PROGRESS) {
            finalizeSession(session);
            session = sessionRepository.save(session);
        }
        return toSessionDto(session);
    }

    private void finalizeSession(AiInterviewSession session) {
        session.setStatus(AiInterviewStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());

        AiMonitoringSummaryDto monitoring = buildMonitoringSummary(session);
        GeneratedFinalReport generatedReport = aiEngine.generateFinalReport(session, session.getTurns(), monitoring);
        if (generatedReport.getProviderUsed() != null) {
            session.setAiProviderUsed(generatedReport.getProviderUsed());
        }

        AiFinalReportDto reportDto = AiFinalReportDto.builder()
                .overallScore(generatedReport.getOverallScore())
                .recommendation(generatedReport.getRecommendation())
                .executiveSummary(generatedReport.getExecutiveSummary())
                .technicalDepthScore(generatedReport.getTechnicalDepthScore())
                .problemSolvingScore(generatedReport.getProblemSolvingScore())
                .communicationScore(generatedReport.getCommunicationScore())
                .consistencyScore(generatedReport.getConsistencyScore())
                .integrityFocusScore(generatedReport.getIntegrityFocusScore())
                .topStrengths(generatedReport.getTopStrengths())
                .improvementAreas(generatedReport.getImprovementAreas())
                .studyPlan(generatedReport.getStudyPlan())
                .monitoringVerdict(generatedReport.getMonitoringVerdict())
                .generatedAt(LocalDateTime.now())
                .build();

        session.setOverallScore(reportDto.getOverallScore());
        session.setRecommendation(reportDto.getRecommendation());
        session.setFinalReportJson(toJson(reportDto));

        recordInternalEvent(session, "INTERVIEW_COMPLETED", AiMonitorSeverity.INFO,
                "Complete interview evaluated and monitored. Overall score: " + reportDto.getOverallScore()
                        + "/100, Focus score: " + session.getFocusIntegrityScore() + "/100.",
                null, 0);
    }

    private AiInterviewSession loadOwnedSession(Long id, String userEmail) {
        AiInterviewSession session = sessionRepository.findByIdWithTurns(id)
                .or(() -> sessionRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("AI interview session not found with id: " + id));

        if (userEmail != null && !userEmail.isBlank()
                && !session.getCandidateEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new ResourceNotFoundException("AI interview session not found with id: " + id);
        }
        return session;
    }

    private AiInterviewTurn resolveActiveTurn(AiInterviewSession session, Long turnId) {
        List<AiInterviewTurn> turns = session.getTurns();
        if (turnId != null) {
            return turns.stream()
                    .filter(t -> Objects.equals(t.getId(), turnId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Interview turn not found with id: " + turnId));
        }
        return turns.stream()
                .filter(t -> t.getStatus() == AiTurnStatus.ASKED)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No active unanswered question remaining in this interview"));
    }

    private AiInterviewTurn buildTurnEntity(int turnNumber, boolean isFollowUp, Long parentTurnId, GeneratedQuestion q) {
        AiInterviewTurn turn = new AiInterviewTurn();
        turn.setTurnNumber(turnNumber);
        turn.setFollowUp(isFollowUp);
        turn.setParentTurnId(parentTurnId);
        turn.setCategory(truncate(q.getCategory() != null ? q.getCategory() : "CONCEPTUAL", 250));
        turn.setTopicTag(truncate(q.getTopicTag(), 250));
        turn.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : AiDifficulty.MID);
        turn.setQuestionText(q.getQuestionText());
        turn.setHintsJson(toJson(q.getHints()));
        turn.setRevealedHintsCount(0);
        turn.setExpectedConceptsJson(toJson(q.getExpectedConcepts()));
        turn.setReferenceAnswer(q.getReferenceAnswer());
        turn.setStarterCode(q.getStarterCode());
        turn.setCodeLanguage(truncate(q.getCodeLanguage(), 100));
        turn.setStatus(AiTurnStatus.ASKED);
        return turn;
    }

    private void recordInternalEvent(
            AiInterviewSession session,
            String type,
            AiMonitorSeverity severity,
            String detail,
            Long turnId,
            int impact) {
        AiMonitorEvent event = new AiMonitorEvent();
        event.setEventType(type);
        event.setSeverity(severity);
        event.setDetail(detail);
        event.setTurnId(turnId);
        event.setFocusScoreImpact(impact);
        session.addMonitorEvent(event);
    }

    private AiMonitoringSummaryDto buildMonitoringSummary(AiInterviewSession session) {
        List<AiInterviewTurn> turns = session.getTurns();
        int answered = (int) turns.stream().filter(t -> t.getStatus() == AiTurnStatus.EVALUATED).count();
        int skipped = (int) turns.stream().filter(t -> t.getStatus() == AiTurnStatus.SKIPPED).count();

        List<Integer> scores = turns.stream()
                .filter(t -> t.getStatus() == AiTurnStatus.EVALUATED && t.getScore() != null)
                .map(AiInterviewTurn::getScore)
                .toList();

        int runningAvg = scores.isEmpty() ? 0 : (int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(0));
        String trend = computeTrend(scores);

        int avgResponseSeconds = (int) Math.round(turns.stream()
                .map(AiInterviewTurn::getResponseTimeSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));

        LocalDateTime endRef = session.getCompletedAt() != null ? session.getCompletedAt() : LocalDateTime.now();
        long elapsedSeconds = session.getStartedAt() != null
                ? Math.max(0, Duration.between(session.getStartedAt(), endRef).getSeconds())
                : 0;

        int focusScore = session.getFocusIntegrityScore() != null ? session.getFocusIntegrityScore() : 100;
        AiMonitorStatus monitorStatus;
        if (focusScore < 65 || session.getCheatingAlertCount() >= 3 || session.getTabSwitchCount() >= 4) {
            monitorStatus = AiMonitorStatus.FLAGGED;
        } else if (focusScore < 85 || session.getCheatingAlertCount() >= 1 || session.getEyeOffScreenCount() >= 1 || session.getTabSwitchCount() >= 1 || session.getIdleWarningCount() >= 2) {
            monitorStatus = AiMonitorStatus.ATTENTION_NEEDED;
        } else {
            monitorStatus = AiMonitorStatus.OPTIMAL;
        }

        List<String> alerts = new ArrayList<>();
        if (session.getCheatingAlertCount() > 0) {
            alerts.add(session.getCheatingAlertCount() + " cheating / integrity alert(s) triggered");
        }
        if (session.getEyeOffScreenCount() > 0) {
            alerts.add(session.getEyeOffScreenCount() + " eye gaze off-screen / face missing event(s)");
        }
        if (session.getTabSwitchCount() > 0) {
            alerts.add(session.getTabSwitchCount() + " tab switch(es) recorded during session");
        }
        if (session.getWindowBlurCount() > 0) {
            alerts.add(session.getWindowBlurCount() + " window focus loss event(s) detected");
        }
        if (session.getPasteEventCount() > 0) {
            alerts.add(session.getPasteEventCount() + " unauthorized paste action(s) flagged");
        }
        if (session.getIdleWarningCount() > 0) {
            alerts.add(session.getIdleWarningCount() + " prolonged inactivity warning(s)");
        }

        String observation;
        if (session.getCheatingAlertCount() > 0 && monitorStatus == AiMonitorStatus.FLAGGED) {
            observation = "ALERT: Repeated cheating / eye-off-screen signals detected (" + session.getCheatingAlertCount() + " alerts). Keep eyes on screen at all times.";
        } else if (answered == 0) {
            observation = "AI Eye & Voice Monitor active — observing eye gaze, presence, and spoken answers.";
        } else {
            observation = String.format("Tracking %d evaluated turn(s) — running average %d/100 (%s), focus integrity %d/100.",
                    answered, runningAvg, trend.toLowerCase(Locale.ROOT), focusScore);
        }

        return AiMonitoringSummaryDto.builder()
                .monitorStatus(monitorStatus)
                .focusIntegrityScore(focusScore)
                .tabSwitchCount(session.getTabSwitchCount())
                .windowBlurCount(session.getWindowBlurCount())
                .pasteEventCount(session.getPasteEventCount())
                .idleWarningCount(session.getIdleWarningCount())
                .eyeOffScreenCount(session.getEyeOffScreenCount())
                .cheatingAlertCount(session.getCheatingAlertCount())
                .questionsAnswered(answered)
                .questionsSkipped(skipped)
                .totalPlannedQuestions(session.getQuestionCount())
                .elapsedSeconds(elapsedSeconds)
                .averageResponseSeconds(avgResponseSeconds)
                .runningAverageScore(runningAvg)
                .scoreTrend(trend)
                .liveObservation(observation)
                .alerts(alerts)
                .build();
    }

    private String computeTrend(List<Integer> scores) {
        if (scores.size() < 2) {
            return "INSUFFICIENT_DATA";
        }
        int mid = scores.size() / 2;
        double firstHalf = scores.subList(0, mid).stream().mapToInt(Integer::intValue).average().orElse(0);
        double secondHalf = scores.subList(mid, scores.size()).stream().mapToInt(Integer::intValue).average().orElse(0);
        double diff = secondHalf - firstHalf;
        if (diff >= 5.0) return "IMPROVING";
        if (diff <= -5.0) return "DECLINING";
        return "STEADY";
    }

    private AiInterviewSessionDto toSessionDto(AiInterviewSession session) {
        boolean isSessionCompleted = session.getStatus() == AiInterviewStatus.COMPLETED;
        List<AiInterviewTurnDto> turnDtos = session.getTurns().stream()
                .map(t -> toTurnDto(t, isSessionCompleted))
                .toList();

        List<AiMonitorEvent> events = session.getId() != null
                ? monitorEventRepository.findBySessionIdOrderByOccurredAtDescIdDesc(session.getId())
                : session.getMonitorEvents();
        if (events.isEmpty() && !session.getMonitorEvents().isEmpty()) {
            events = session.getMonitorEvents();
        }
        List<AiMonitorEventDto> eventDtos = events.stream()
                .map(e -> AiMonitorEventDto.builder()
                        .id(e.getId())
                        .eventType(e.getEventType())
                        .severity(e.getSeverity())
                        .detail(e.getDetail())
                        .turnId(e.getTurnId())
                        .focusScoreImpact(e.getFocusScoreImpact())
                        .occurredAt(e.getOccurredAt())
                        .build())
                .toList();

        AiFinalReportDto finalReport = parseFinalReport(session.getFinalReportJson());

        return AiInterviewSessionDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .topic(session.getTopic())
                .roleTitle(session.getRoleTitle())
                .difficulty(session.getDifficulty())
                .mode(session.getMode())
                .status(session.getStatus())
                .questionCount(session.getQuestionCount())
                .durationMinutes(session.getDurationMinutes())
                .focusAreas(session.getFocusAreas())
                .enableFollowUps(session.isEnableFollowUps())
                .strictMonitoring(session.isStrictMonitoring())
                .candidateEmail(session.getCandidateEmail())
                .currentTurnIndex(session.getCurrentTurnIndex())
                .overallScore(session.getOverallScore())
                .recommendation(session.getRecommendation())
                .aiProviderUsed(session.getAiProviderUsed())
                .monitoring(buildMonitoringSummary(session))
                .finalReport(finalReport)
                .turns(turnDtos)
                .monitorEvents(eventDtos)
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    private AiInterviewTurnDto toTurnDto(AiInterviewTurn t, boolean sessionCompleted) {
        List<String> allHints = parseList(t.getHintsJson());
        int revealedCount = Math.min(allHints.size(), t.getRevealedHintsCount() != null ? t.getRevealedHintsCount() : 0);
        List<String> revealedHints = allHints.subList(0, revealedCount);

        boolean revealKey = sessionCompleted || t.getStatus() != AiTurnStatus.ASKED;

        AiAnswerEvaluationDto evalDto = null;
        if (t.getStatus() == AiTurnStatus.EVALUATED || t.getStatus() == AiTurnStatus.SKIPPED) {
            evalDto = AiAnswerEvaluationDto.builder()
                    .score(t.getScore())
                    .verdict(t.getVerdict())
                    .technicalAccuracyScore(t.getTechnicalAccuracyScore())
                    .completenessScore(t.getCompletenessScore())
                    .clarityScore(t.getClarityScore())
                    .problemSolvingScore(t.getProblemSolvingScore())
                    .strengths(parseList(t.getStrengthsJson()))
                    .improvements(parseList(t.getImprovementsJson()))
                    .missingConcepts(parseList(t.getMissingConceptsJson()))
                    .feedback(t.getFeedback())
                    .referenceAnswer(t.getReferenceAnswer())
                    .followUpQuestion(t.getFollowUpQuestion())
                    .build();
        }

        return AiInterviewTurnDto.builder()
                .id(t.getId())
                .turnNumber(t.getTurnNumber())
                .followUp(t.isFollowUp())
                .parentTurnId(t.getParentTurnId())
                .category(t.getCategory())
                .topicTag(t.getTopicTag())
                .difficulty(t.getDifficulty())
                .questionText(t.getQuestionText())
                .revealedHints(new ArrayList<>(revealedHints))
                .totalHintsAvailable(allHints.size())
                .starterCode(t.getStarterCode())
                .codeLanguage(t.getCodeLanguage())
                .expectedConcepts(revealKey ? parseList(t.getExpectedConceptsJson()) : List.of())
                .userAnswer(t.getUserAnswer())
                .userCode(t.getUserCode())
                .codeLanguageUsed(t.getCodeLanguageUsed())
                .executionSummary(t.getExecutionSummary())
                .responseTimeSeconds(t.getResponseTimeSeconds())
                .status(t.getStatus())
                .evaluation(evalDto)
                .askedAt(t.getAskedAt())
                .answeredAt(t.getAnsweredAt())
                .build();
    }

    private AiInterviewSummaryDto toSummaryDto(AiInterviewSession session) {
        int answered = (int) session.getTurns().stream()
                .filter(t -> t.getStatus() == AiTurnStatus.EVALUATED || t.getStatus() == AiTurnStatus.SKIPPED)
                .count();
        return AiInterviewSummaryDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .topic(session.getTopic())
                .roleTitle(session.getRoleTitle())
                .difficulty(session.getDifficulty())
                .mode(session.getMode())
                .status(session.getStatus())
                .questionCount(session.getQuestionCount())
                .answeredCount(answered)
                .durationMinutes(session.getDurationMinutes())
                .overallScore(session.getOverallScore())
                .focusIntegrityScore(session.getFocusIntegrityScore())
                .recommendation(session.getRecommendation())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .build();
    }

    private String resolveCandidateEmail(String userEmail) {
        return (userEmail != null && !userEmail.isBlank()) ? userEmail.trim().toLowerCase(Locale.ROOT) : DEFAULT_CANDIDATE_EMAIL;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private AiFinalReportDto parseFinalReport(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, AiFinalReportDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) return null;
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
