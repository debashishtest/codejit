package com.codejit.interview.service.ai;

import com.codejit.common.dto.ai.*;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedEvaluation;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedFinalReport;
import com.codejit.interview.service.ai.AiGenerationModels.GeneratedQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("builtInAiInterviewEngine")
public class BuiltInAiInterviewEngine implements AiInterviewerEngine {

    public static final String PROVIDER_ID = "CODEJIT_AI_ENGINE";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> NON_ANSWERS = Set.of(
            "idk", "i dont know", "i don't know", "no idea", "pass", "skip", "none", "n/a", "na", "nothing", "unsure"
    );

    private static final List<String> REASONING_MARKERS = List.of(
            "because", "therefore", "trade-off", "tradeoff", "however", "whereas", "advantage", "disadvantage",
            "complexity", "latency", "throughput", "edge case", "bottleneck", "scale", "concurrent",
            "thread", "memory", "cache", "retry", "idempotent", "timeout", "for example", "e.g.", "o(1)", "o(n)", "o(log n)"
    );

    private static final Map<String, List<String>> CONCEPT_SYNONYMS = Map.ofEntries(
            Map.entry("hash map", List.of("hashmap", "dictionary", "hash table", "map", "o(1) lookup")),
            Map.entry("time complexity", List.of("o(n)", "o(1)", "o(log n)", "o(n log n)", "runtime", "big o")),
            Map.entry("space complexity", List.of("memory", "auxiliary space", "o(1) space", "o(n) space")),
            Map.entry("thread safety", List.of("synchronized", "lock", "reentrantlock", "atomic", "concurrent", "mutex", "volatile", "race condition")),
            Map.entry("cache", List.of("redis", "ttl", "eviction", "lru", "memoize", "in-memory", "write-through", "cache-aside")),
            Map.entry("rate limiting", List.of("token bucket", "sliding window", "leaky bucket", "throttle", "429", "quota")),
            Map.entry("idempotency", List.of("idempotent", "idempotency key", "deduplication", "duplicate", "exactly once", "retry")),
            Map.entry("database indexing", List.of("b-tree", "btree", "index", "composite index", "selectivity", "full table scan", "explain")),
            Map.entry("isolation", List.of("acid", "read committed", "repeatable read", "serializable", "phantom", "dirty read", "mvcc")),
            Map.entry("reconciliation", List.of("virtual dom", "diffing", "fiber", "key prop", "rerender", "memo")),
            Map.entry("closure", List.of("stale closure", "dependency array", "useeffect", "usecallback", "lexical scope", "ref")),
            Map.entry("gil", List.of("global interpreter lock", "asyncio", "multiprocessing", "event loop", "coroutine", "cpu-bound", "io-bound")),
            Map.entry("star", List.of("situation", "task", "action", "result", "outcome", "metric", "impact", "learned")),
            Map.entry("edge cases", List.of("null", "empty", "overflow", "negative", "boundary", "duplicate", "timeout", "zero"))
    );

    private record QuestionTemplate(
            String domainKey,
            String category,
            String topicTag,
            AiDifficulty difficulty,
            String questionText,
            List<String> hints,
            List<String> expectedConcepts,
            String referenceAnswer,
            String starterCode,
            String codeLanguage
    ) {}

    private static final List<QuestionTemplate> QUESTION_BANK = buildQuestionBank();

    @Override
    public GeneratedQuestion generateNextQuestion(AiInterviewSession session, List<AiInterviewTurn> previousTurns) {
        String targetCategory = pickTargetCategory(session.getMode(), previousTurns.size());
        Set<String> askedTexts = previousTurns.stream()
                .map(t -> normalize(t.getQuestionText()))
                .collect(Collectors.toSet());

        String topicLower = safeLower(session.getTopic());
        String focusLower = safeLower(session.getFocusAreas());
        AiDifficulty targetDiff = session.getDifficulty() != null ? session.getDifficulty() : AiDifficulty.MID;

        List<QuestionTemplate> candidates = QUESTION_BANK.stream()
                .filter(q -> !askedTexts.contains(normalize(q.questionText())))
                .sorted((a, b) -> Integer.compare(
                        scoreTemplateMatch(b, topicLower, focusLower, targetCategory, targetDiff),
                        scoreTemplateMatch(a, topicLower, focusLower, targetCategory, targetDiff)))
                .toList();

        if (!candidates.isEmpty()) {
            QuestionTemplate best = candidates.get(0);
            int bestScore = scoreTemplateMatch(best, topicLower, focusLower, targetCategory, targetDiff);
            if (bestScore >= 15) {
                return toGeneratedQuestion(best, topicLower);
            }
        }

        // Synthesize tailored question for custom/niche topics
        return synthesizeCustomQuestion(session, previousTurns, targetCategory, targetDiff);
    }

    @Override
    public GeneratedEvaluation evaluateAnswer(
            AiInterviewSession session,
            AiInterviewTurn turn,
            SubmitAiAnswerRequest answerRequest) {

        String answerText = answerRequest.getAnswerText() != null ? answerRequest.getAnswerText().trim() : "";
        String codeSnippet = answerRequest.getCodeSnippet() != null ? answerRequest.getCodeSnippet().trim() : "";
        String execSummary = answerRequest.getExecutionSummary() != null ? answerRequest.getExecutionSummary().trim() : "";
        String combined = (answerText + " " + codeSnippet).trim();
        String combinedLower = combined.toLowerCase(Locale.ROOT);

        List<String> expectedConcepts = parseList(turn.getExpectedConceptsJson());
        if (expectedConcepts.isEmpty()) {
            expectedConcepts = List.of("core mechanism", "trade-offs", "edge cases", "practical application");
        }

        if (isNonSubstantive(answerText, codeSnippet, turn.getStarterCode())) {
            return buildInsufficientEvaluation(turn, expectedConcepts);
        }

        List<String> matchedConcepts = new ArrayList<>();
        List<String> missingConcepts = new ArrayList<>();
        for (String concept : expectedConcepts) {
            if (matchesConcept(combinedLower, concept)) {
                matchedConcepts.add(concept);
            } else {
                missingConcepts.add(concept);
            }
        }

        double conceptRatio = expectedConcepts.isEmpty() ? 0.5 : (double) matchedConcepts.size() / expectedConcepts.size();
        int wordCount = countWords(answerText);
        long reasoningHits = REASONING_MARKERS.stream().filter(combinedLower::contains).count();
        boolean hasSubstantialCode = isSubstantialCode(codeSnippet, turn.getStarterCode());
        boolean isCodingTurn = "CODING".equalsIgnoreCase(turn.getCategory());

        int technicalAccuracy = clamp((int) Math.round(15 + conceptRatio * 75 + Math.min(reasoningHits, 4) * 2.5));
        int completeness = clamp((int) Math.round(12 + conceptRatio * 66 + Math.min(wordCount / 8.0, 18) + (hasSubstantialCode ? 6 : 0)));
        int clarity = clamp((int) Math.round(25 + conceptRatio * 30 + Math.min(wordCount / 6.0, 20) + Math.min(reasoningHits, 5) * 5));

        int problemSolvingBase = (int) Math.round(15 + conceptRatio * 60 + Math.min(reasoningHits, 5) * 4);
        if (isCodingTurn || hasSubstantialCode) {
            if (hasSubstantialCode) {
                problemSolvingBase += 14;
                if (hasControlFlow(codeSnippet)) {
                    problemSolvingBase += 6;
                }
            } else if (isCodingTurn) {
                problemSolvingBase -= 10;
            }
            if (!execSummary.isBlank()) {
                String execLower = execSummary.toLowerCase(Locale.ROOT);
                if ((execLower.contains("passed") || execLower.contains("success")) && !execLower.contains("failed")) {
                    problemSolvingBase += 10;
                    technicalAccuracy = clamp(technicalAccuracy + 8);
                } else if (execLower.contains("failed") || execLower.contains("error")) {
                    problemSolvingBase -= 6;
                }
            }
        }
        int problemSolving = clamp(problemSolvingBase);

        int overall = clamp((int) Math.round(
                technicalAccuracy * 0.35 +
                completeness * 0.25 +
                clarity * 0.20 +
                problemSolving * 0.20
        ));

        // Strict authenticity guards: zero concept match or very short answer cannot score high
        if (matchedConcepts.isEmpty() && !hasSubstantialCode) {
            overall = Math.min(overall, 18);
            technicalAccuracy = Math.min(technicalAccuracy, 15);
            completeness = Math.min(completeness, 15);
        } else if (wordCount < 15 && !hasSubstantialCode) {
            overall = Math.min(overall, 45);
            completeness = Math.min(completeness, 40);
        }

        AiEvaluationVerdict verdict = mapVerdict(overall);

        List<String> strengths = buildStrengths(matchedConcepts, reasoningHits, wordCount, hasSubstantialCode, execSummary);
        List<String> improvements = buildImprovements(missingConcepts, reasoningHits, wordCount, isCodingTurn, hasSubstantialCode, execSummary, answerRequest.getResponseTimeSeconds());

        String feedback = buildFeedbackParagraph(verdict, overall, matchedConcepts, missingConcepts, hasSubstantialCode, execSummary, answerRequest.getResponseTimeSeconds());
        String followUp = buildFollowUp(session, turn, overall, missingConcepts);

        GeneratedEvaluation eval = new GeneratedEvaluation();
        eval.setScore(overall);
        eval.setVerdict(verdict);
        eval.setTechnicalAccuracyScore(technicalAccuracy);
        eval.setCompletenessScore(completeness);
        eval.setClarityScore(clarity);
        eval.setProblemSolvingScore(problemSolving);
        eval.setStrengths(strengths);
        eval.setImprovements(improvements);
        eval.setMissingConcepts(missingConcepts);
        eval.setFeedback(feedback);
        eval.setReferenceAnswer(turn.getReferenceAnswer());
        eval.setFollowUpQuestion(followUp);
        eval.setProviderUsed(PROVIDER_ID);
        return eval;
    }

    @Override
    public GeneratedFinalReport generateFinalReport(
            AiInterviewSession session,
            List<AiInterviewTurn> allTurns,
            AiMonitoringSummaryDto monitoring) {

        List<AiInterviewTurn> evaluated = allTurns.stream()
                .filter(t -> t.getStatus() == AiTurnStatus.EVALUATED && t.getScore() != null)
                .toList();
        long skippedCount = allTurns.stream().filter(t -> t.getStatus() == AiTurnStatus.SKIPPED).count();

        int overall = evaluated.isEmpty() ? 0 : (int) Math.round(
                evaluated.stream().mapToInt(AiInterviewTurn::getScore).average().orElse(0)
        );
        // Penalize skipped questions moderately
        if (!allTurns.isEmpty() && skippedCount > 0) {
            int totalAttemptable = evaluated.size() + (int) skippedCount;
            if (totalAttemptable > 0) {
                overall = (int) Math.round(
                        evaluated.stream().mapToInt(AiInterviewTurn::getScore).sum() / (double) totalAttemptable
                );
            }
        }

        int techDepth = averageMetric(evaluated, AiInterviewTurn::getTechnicalAccuracyScore, overall);
        int problemSolving = averageMetric(evaluated, AiInterviewTurn::getProblemSolvingScore, overall);
        int communication = averageMetric(evaluated, AiInterviewTurn::getClarityScore, overall);
        int consistency = computeConsistency(evaluated);
        int focusScore = monitoring != null && monitoring.getFocusIntegrityScore() != null
                ? monitoring.getFocusIntegrityScore()
                : 100;

        AiRecommendation recommendation = mapRecommendation(overall, focusScore, evaluated.size());

        List<String> topStrengths = collectTopStrings(evaluated, true, List.of(
                "Completed the structured AI practice session on " + session.getTopic(),
                "Maintained active engagement through the assessment workflow"
        ));
        List<String> improvementAreas = collectTopStrings(evaluated, false, List.of(
                "Expand answers with concrete complexity analysis and failure-mode trade-offs"
        ));
        if (skippedCount > 0) {
            improvementAreas.add(0, "Skipped " + skippedCount + " question(s) — practice walking through partial intuition even when uncertain");
        }

        List<String> studyPlan = buildStudyPlan(session, evaluated, improvementAreas);

        String monitoringVerdict = buildMonitoringVerdict(monitoring, focusScore);
        String executiveSummary = buildExecutiveSummary(session, overall, recommendation, evaluated.size(), skippedCount, monitoring, focusScore);

        GeneratedFinalReport report = new GeneratedFinalReport();
        report.setOverallScore(overall);
        report.setRecommendation(recommendation);
        report.setExecutiveSummary(executiveSummary);
        report.setTechnicalDepthScore(techDepth);
        report.setProblemSolvingScore(problemSolving);
        report.setCommunicationScore(communication);
        report.setConsistencyScore(consistency);
        report.setIntegrityFocusScore(focusScore);
        report.setTopStrengths(topStrengths);
        report.setImprovementAreas(improvementAreas);
        report.setStudyPlan(studyPlan);
        report.setMonitoringVerdict(monitoringVerdict);
        report.setProviderUsed(PROVIDER_ID);
        return report;
    }

    @Override
    public AiGenerationModels.GeneratedEyeAnalysis analyzeMonitorFrame(
            AiInterviewSession session,
            MonitorFrameRequest frameRequest) {
        AiGenerationModels.GeneratedEyeAnalysis result = new AiGenerationModels.GeneratedEyeAnalysis();
        String obs = frameRequest != null && frameRequest.getLocalObservation() != null
                ? frameRequest.getLocalObservation().toUpperCase(Locale.ROOT)
                : "OK";
        if (obs.contains("FACE_MISSING") || obs.contains("CAMERA_COVERED")) {
            result.setFacePresent(false);
            result.setLookingAtScreen(false);
            result.setGazeDirection("AWAY");
            result.setCheatingSuspected(true);
            result.setAlertReason("Candidate face missing or camera obstructed.");
        } else if (obs.contains("MULTIPLE")) {
            result.setFacePresent(true);
            result.setMultiplePeople(true);
            result.setLookingAtScreen(false);
            result.setGazeDirection("AWAY");
            result.setCheatingSuspected(true);
            result.setAlertReason("Multiple faces detected in camera frame.");
        } else if (obs.contains("GAZE_AWAY") || obs.contains("LOOKING_AWAY")) {
            result.setFacePresent(true);
            result.setLookingAtScreen(false);
            result.setGazeDirection("AWAY");
            result.setCheatingSuspected(true);
            result.setAlertReason("Candidate eyes looking away from interview screen.");
        }
        return result;
    }

    private GeneratedQuestion toGeneratedQuestion(QuestionTemplate template, String topicLower) {
        GeneratedQuestion q = new GeneratedQuestion();
        q.setCategory(template.category());
        q.setTopicTag(template.topicTag());
        q.setDifficulty(template.difficulty());
        q.setQuestionText(template.questionText());
        q.setHints(template.hints());
        q.setExpectedConcepts(template.expectedConcepts());
        q.setReferenceAnswer(template.referenceAnswer());

        String lang = template.codeLanguage();
        String starter = template.starterCode();
        if ("CODING".equalsIgnoreCase(template.category()) && topicLower.contains("python") && !"python".equalsIgnoreCase(lang)) {
            lang = "python";
            starter = "# Write your solution here\ndef solve():\n    pass\n";
        }
        q.setCodeLanguage(lang);
        q.setStarterCode(starter);
        q.setProviderUsed(PROVIDER_ID);
        return q;
    }

    private GeneratedQuestion synthesizeCustomQuestion(
            AiInterviewSession session,
            List<AiInterviewTurn> previousTurns,
            String category,
            AiDifficulty difficulty) {

        int qIndex = previousTurns.size() + 1;
        String topic = session.getTopic();
        String role = (session.getRoleTitle() != null && !session.getRoleTitle().isBlank())
                ? session.getRoleTitle()
                : "Software Engineer";
        String focus = (session.getFocusAreas() != null && !session.getFocusAreas().isBlank())
                ? session.getFocusAreas()
                : topic;

        GeneratedQuestion q = new GeneratedQuestion();
        q.setCategory(category);
        q.setTopicTag(topic);
        q.setDifficulty(difficulty);
        q.setProviderUsed(PROVIDER_ID);

        if ("CODING".equalsIgnoreCase(category)) {
            boolean usePython = safeLower(topic).contains("python");
            q.setCodeLanguage(usePython ? "python" : "java");
            q.setStarterCode(usePython
                    ? "# Implement a thread-safe or bounded cache / processor for " + topic + "\nclass Solution:\n    def process(self, items):\n        # TODO: implement\n        return []\n"
                    : "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Implement and test your solution for: " + topic + "\n        System.out.println(\"ready\");\n    }\n}\n");
            q.setQuestionText("Coding Exercise (Q" + qIndex + " — " + difficulty + " " + role + "): Implement a clean, production-grade algorithm or utility relevant to " + topic + " (focus: " + focus + ") that processes an input stream while deduplicating items and preserving recency order. Explain the time and space complexity and handle empty/edge inputs.");
            q.setHints(List.of(
                    "Combine a hash-based lookup structure with an ordered structure to achieve O(1) operations.",
                    "Explicitly guard against null/empty inputs and bounded capacity overflow."
            ));
            q.setExpectedConcepts(List.of("hash map", "time complexity", "space complexity", "edge cases", "clean code"));
            q.setReferenceAnswer("Use a HashMap paired with a LinkedHashSet/doubly-linked list for O(1) membership checks and recency updates. Validate null/empty inputs up front, state O(1) amortized time per operation and O(N) space complexity, and verify boundary behavior with tests.");
        } else if ("SYSTEM_DESIGN".equalsIgnoreCase(category)) {
            q.setQuestionText("System Design (Q" + qIndex + " — " + difficulty + "): Design a scalable, fault-tolerant service centered on " + topic + " (focus: " + focus + ") capable of handling high concurrency. Cover API contracts, data storage, caching, bottleneck mitigation, and observability.");
            q.setHints(List.of(
                    "Start by clarifying read/write throughput, latency SLOs, and consistency requirements.",
                    "Address single points of failure, caching invalidation, and backpressure under traffic spikes."
            ));
            q.setExpectedConcepts(List.of("api contract", "storage", "cache", "scalability", "fault tolerance", "observability"));
            q.setReferenceAnswer("Define clear SLAs and idempotent APIs, partition storage by access key, introduce cache-aside with TTL and jitter to prevent stampedes, decouple bursty writes via queues, and instrument p95/p99 latency, error rates, and saturation alerts.");
        } else if ("BEHAVIORAL".equalsIgnoreCase(category)) {
            q.setQuestionText("Behavioral Scenario (Q" + qIndex + "): Tell me about a challenging engineering decision or production incident involving " + topic + ". Using the STAR framework, walk through the context, trade-offs you evaluated, actions you drove, and measurable outcome.");
            q.setHints(List.of(
                    "Structure your response as Situation, Task, Action, and Result.",
                    "Highlight concrete metrics, trade-offs considered, and what you changed afterward."
            ));
            q.setExpectedConcepts(List.of("star", "trade-offs", "ownership", "root cause", "measurable impact"));
            q.setReferenceAnswer("Frame the Situation and business impact clearly, explain your personal Task, detail the technical and cross-team Actions taken (including alternatives rejected), and conclude with quantified Results and systemic prevention measures.");
        } else {
            String angle = switch (qIndex % 3) {
                case 1 -> "core architecture, internal mechanics, and key trade-offs";
                case 2 -> "failure modes, debugging strategy, and performance optimization";
                default -> "security, concurrency, and testing best practices";
            };
            q.setQuestionText("Technical Deep-Dive (Q" + qIndex + " — " + difficulty + "): In the context of " + topic + " (focus: " + focus + "), explain " + angle + ". Compare at least two approaches and discuss when you would choose one over the other in production.");
            q.setHints(List.of(
                    "Ground your explanation in concrete production constraints like latency, memory, or consistency.",
                    "Mention common pitfalls and how you test or monitor for them."
            ));
            q.setExpectedConcepts(List.of("architecture", "trade-offs", "performance", "edge cases", "testing"));
            q.setReferenceAnswer("A strong answer clearly explains the underlying mechanics of " + topic + ", compares competing approaches across latency, maintainability, and resource usage, and identifies concrete failure modes, observability signals, and testing strategies.");
        }
        return q;
    }

    private int scoreTemplateMatch(
            QuestionTemplate q,
            String topicLower,
            String focusLower,
            String targetCategory,
            AiDifficulty targetDiff) {
        int score = 0;
        if (q.category().equalsIgnoreCase(targetCategory)) {
            score += 40;
        }
        if (q.difficulty() == targetDiff) {
            score += 25;
        }
        String qDomain = q.domainKey().toLowerCase(Locale.ROOT);
        String qTag = q.topicTag().toLowerCase(Locale.ROOT);
        String qText = q.questionText().toLowerCase(Locale.ROOT);

        if (!topicLower.isBlank()) {
            for (String token : topicLower.split("[\\s,;/&]+")) {
                if (token.length() < 2) continue;
                if (qDomain.contains(token) || qTag.contains(token)) {
                    score += 35;
                } else if (qText.contains(token)) {
                    score += 15;
                }
            }
            if ((topicLower.contains("dsa") || topicLower.contains("algorithm") || topicLower.contains("coding"))
                    && "dsa".equals(qDomain)) {
                score += 35;
            }
            if ((topicLower.contains("frontend") || topicLower.contains("react") || topicLower.contains("typescript") || topicLower.contains("javascript"))
                    && "frontend".equals(qDomain)) {
                score += 35;
            }
            if ((topicLower.contains("java") || topicLower.contains("spring") || topicLower.contains("backend"))
                    && "java".equals(qDomain)) {
                score += 30;
            }
            if ((topicLower.contains("system") || topicLower.contains("architecture") || topicLower.contains("distributed"))
                    && "system-design".equals(qDomain)) {
                score += 35;
            }
            if ((topicLower.contains("database") || topicLower.contains("sql") || topicLower.contains("postgres"))
                    && "database".equals(qDomain)) {
                score += 35;
            }
            if (topicLower.contains("python") && "python".equals(qDomain)) {
                score += 35;
            }
            if ((topicLower.contains("behavioral") || topicLower.contains("leadership")) && "behavioral".equals(qDomain)) {
                score += 40;
            }
        }

        if (!focusLower.isBlank()) {
            for (String token : focusLower.split("[\\s,;/]+")) {
                if (token.length() >= 3 && (qText.contains(token) || qTag.contains(token))) {
                    score += 12;
                }
            }
        }
        return score;
    }

    private String pickTargetCategory(AiInterviewMode mode, int alreadyAskedCount) {
        if (mode == null || mode == AiInterviewMode.MIXED) {
            return switch (alreadyAskedCount % 4) {
                case 0 -> "CONCEPTUAL";
                case 1 -> "CODING";
                case 2 -> "SYSTEM_DESIGN";
                default -> "BEHAVIORAL";
            };
        }
        return switch (mode) {
            case CODING -> "CODING";
            case SYSTEM_DESIGN -> "SYSTEM_DESIGN";
            case BEHAVIORAL -> "BEHAVIORAL";
            case TECHNICAL_QA -> "CONCEPTUAL";
            default -> "CONCEPTUAL";
        };
    }

    private GeneratedEvaluation buildInsufficientEvaluation(AiInterviewTurn turn, List<String> expectedConcepts) {
        GeneratedEvaluation eval = new GeneratedEvaluation();
        eval.setScore(5);
        eval.setVerdict(AiEvaluationVerdict.INSUFFICIENT);
        eval.setTechnicalAccuracyScore(5);
        eval.setCompletenessScore(5);
        eval.setClarityScore(10);
        eval.setProblemSolvingScore(5);
        eval.setStrengths(List.of("None demonstrated — answer was empty or lacked technical content"));
        eval.setImprovements(List.of(
                "Provide a structured explanation covering core principles, trade-offs, and concrete examples",
                "Walk through your reasoning step by step even when unsure of the complete solution"
        ));
        eval.setMissingConcepts(expectedConcepts);
        eval.setFeedback("Your response was too brief or lacked technical detail to demonstrate understanding of "
                + (turn.getTopicTag() != null ? turn.getTopicTag() : "the question")
                + ". Try breaking the problem down out loud, naming key components, and discussing edge cases.");
        eval.setReferenceAnswer(turn.getReferenceAnswer());
        eval.setFollowUpQuestion(null);
        eval.setProviderUsed(PROVIDER_ID);
        return eval;
    }

    private boolean isNonSubstantive(String answerText, String codeSnippet, String starterCode) {
        String normalized = normalize(answerText);
        boolean hasCode = isSubstantialCode(codeSnippet, starterCode);
        if (hasCode) {
            return false;
        }
        if (normalized.isBlank() || NON_ANSWERS.contains(normalized)) {
            return true;
        }
        return countWords(answerText) < 4;
    }

    private boolean isSubstantialCode(String codeSnippet, String starterCode) {
        if (codeSnippet == null || codeSnippet.isBlank()) {
            return false;
        }
        String normCode = normalize(codeSnippet);
        String normStarter = normalize(starterCode);
        if (normCode.equals(normStarter)) {
            return false;
        }
        return codeSnippet.trim().length() >= 30;
    }

    private boolean hasControlFlow(String code) {
        String lower = code.toLowerCase(Locale.ROOT);
        return lower.contains("for ") || lower.contains("for(") || lower.contains("while")
                || lower.contains("if ") || lower.contains("if(") || lower.contains("return ")
                || lower.contains("def ") || lower.contains("map") || lower.contains("set");
    }

    private boolean matchesConcept(String textLower, String concept) {
        String cLower = concept.toLowerCase(Locale.ROOT).trim();
        if (textLower.contains(cLower)) {
            return true;
        }
        List<String> synonyms = CONCEPT_SYNONYMS.get(cLower);
        if (synonyms != null && synonyms.stream().anyMatch(textLower::contains)) {
            return true;
        }
        String[] words = cLower.split("[\\s/-]+");
        if (words.length > 1) {
            long matchedWords = Arrays.stream(words)
                    .filter(w -> w.length() >= 3 && textLower.contains(w))
                    .count();
            return matchedWords >= Math.max(1, words.length - 1);
        }
        return cLower.length() >= 4 && textLower.contains(cLower.substring(0, Math.min(cLower.length(), 5)));
    }

    private List<String> buildStrengths(
            List<String> matchedConcepts,
            long reasoningHits,
            int wordCount,
            boolean hasCode,
            String execSummary) {
        List<String> strengths = new ArrayList<>();
        if (!matchedConcepts.isEmpty()) {
            strengths.add("Covered key rubric concepts: " + String.join(", ", matchedConcepts));
        }
        if (reasoningHits >= 2) {
            strengths.add("Demonstrated analytical depth with explicit trade-off and reasoning signals");
        }
        if (wordCount >= 45) {
            strengths.add("Provided a well-developed, structured explanation");
        }
        if (hasCode) {
            strengths.add("Submitted a concrete code implementation alongside explanation");
        }
        if (!execSummary.isBlank() && execSummary.toLowerCase(Locale.ROOT).contains("passed")
                && !execSummary.toLowerCase(Locale.ROOT).contains("failed")) {
            strengths.add("Verified code solution successfully against sandbox execution checks");
        }
        if (strengths.isEmpty()) {
            strengths.add("Addressed the prompt directly with relevant terminology");
        }
        return strengths;
    }

    private List<String> buildImprovements(
            List<String> missingConcepts,
            long reasoningHits,
            int wordCount,
            boolean isCodingTurn,
            boolean hasCode,
            String execSummary,
            Integer responseTimeSeconds) {
        List<String> improvements = new ArrayList<>();
        if (!missingConcepts.isEmpty()) {
            improvements.add("Address missing core concepts: " + String.join(", ", missingConcepts));
        }
        if (reasoningHits == 0) {
            improvements.add("Explain *why* you chose an approach, including complexity or operational trade-offs");
        }
        if (wordCount < 25 && !hasCode) {
            improvements.add("Elaborate further with a concrete example or edge-case walkthrough");
        }
        if (isCodingTurn && !hasCode) {
            improvements.add("Include working code in the code editor and test it against edge cases");
        }
        if (!execSummary.isBlank() && (execSummary.toLowerCase(Locale.ROOT).contains("failed") || execSummary.toLowerCase(Locale.ROOT).contains("error"))) {
            improvements.add("Resolve the failing sandbox test execution errors before finalizing");
        }
        if (responseTimeSeconds != null && responseTimeSeconds < 8 && wordCount < 20) {
            improvements.add("Slow down slightly to verify completeness before submitting");
        }
        if (improvements.isEmpty()) {
            improvements.add("Consider quantifying production metrics or rare failure modes to push from strong to staff-level");
        }
        return improvements;
    }

    private String buildFeedbackParagraph(
            AiEvaluationVerdict verdict,
            int score,
            List<String> matched,
            List<String> missing,
            boolean hasCode,
            String execSummary,
            Integer responseTimeSeconds) {
        StringBuilder sb = new StringBuilder();
        switch (verdict) {
            case EXCELLENT -> sb.append("Excellent answer (").append(score).append("/100). ");
            case STRONG -> sb.append("Strong answer (").append(score).append("/100). ");
            case ADEQUATE -> sb.append("Solid baseline answer (").append(score).append("/100), with room for deeper precision. ");
            case NEEDS_IMPROVEMENT -> sb.append("Partial answer (").append(score).append("/100) — key architectural or algorithmic pieces are missing. ");
            default -> sb.append("Insufficient answer (").append(score).append("/100). ");
        }
        if (!matched.isEmpty()) {
            sb.append("You accurately touched on ").append(String.join(", ", matched)).append(". ");
        }
        if (!missing.isEmpty()) {
            sb.append("To strengthen this further, explicitly discuss ").append(String.join(", ", missing)).append(". ");
        }
        if (hasCode && !execSummary.isBlank()) {
            sb.append("Sandbox check noted: ").append(execSummary).append(". ");
        }
        if (responseTimeSeconds != null && responseTimeSeconds > 0) {
            sb.append("(Response time: ").append(responseTimeSeconds).append("s)");
        }
        return sb.toString().trim();
    }

    private String buildFollowUp(
            AiInterviewSession session,
            AiInterviewTurn turn,
            int score,
            List<String> missingConcepts) {
        if (!session.isEnableFollowUps() || turn.isFollowUp()) {
            return null;
        }
        if (score >= 32 && score <= 78 && !missingConcepts.isEmpty()) {
            String targetConcept = missingConcepts.get(0);
            return "Follow-up on " + (turn.getTopicTag() != null ? turn.getTopicTag() : session.getTopic())
                    + ": You covered the main flow, though we didn't examine \"" + targetConcept
                    + "\". How would you address " + targetConcept + " and potential edge cases in production?";
        }
        return null;
    }

    private AiEvaluationVerdict mapVerdict(int score) {
        if (score >= 85) return AiEvaluationVerdict.EXCELLENT;
        if (score >= 72) return AiEvaluationVerdict.STRONG;
        if (score >= 55) return AiEvaluationVerdict.ADEQUATE;
        if (score >= 35) return AiEvaluationVerdict.NEEDS_IMPROVEMENT;
        return AiEvaluationVerdict.INSUFFICIENT;
    }

    private AiRecommendation mapRecommendation(int overallScore, int focusScore, int evaluatedCount) {
        if (evaluatedCount == 0) {
            return AiRecommendation.NEEDS_PRACTICE;
        }
        int effective = focusScore < 60 ? Math.max(0, overallScore - 8) : overallScore;
        if (effective >= 84 && focusScore >= 80) return AiRecommendation.STRONG_HIRE;
        if (effective >= 70) return AiRecommendation.HIRE;
        if (effective >= 52) return AiRecommendation.BORDERLINE;
        return AiRecommendation.NEEDS_PRACTICE;
    }

    private int averageMetric(List<AiInterviewTurn> turns, java.util.function.Function<AiInterviewTurn, Integer> getter, int fallback) {
        if (turns.isEmpty()) return fallback;
        OptionalDouble avg = turns.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        return avg.isPresent() ? (int) Math.round(avg.getAsDouble()) : fallback;
    }

    private int computeConsistency(List<AiInterviewTurn> turns) {
        if (turns.size() <= 1) return turns.isEmpty() ? 0 : 85;
        double mean = turns.stream().mapToInt(AiInterviewTurn::getScore).average().orElse(0);
        double variance = turns.stream()
                .mapToDouble(t -> Math.pow(t.getScore() - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        return clamp((int) Math.round(100 - stdDev * 1.8));
    }

    private List<String> collectTopStrings(List<AiInterviewTurn> turns, boolean strengthsMode, List<String> fallback) {
        List<String> result = new ArrayList<>();
        for (AiInterviewTurn t : turns) {
            String json = strengthsMode ? t.getStrengthsJson() : t.getImprovementsJson();
            for (String item : parseList(json)) {
                if (!result.contains(item)) {
                    result.add(item);
                }
                if (result.size() >= 4) return result;
            }
        }
        return result.isEmpty() ? new ArrayList<>(fallback) : result;
    }

    private List<String> buildStudyPlan(AiInterviewSession session, List<AiInterviewTurn> evaluated, List<String> improvementAreas) {
        List<String> plan = new ArrayList<>();
        evaluated.stream()
                .filter(t -> t.getScore() != null && t.getScore() < 75)
                .sorted(Comparator.comparingInt(AiInterviewTurn::getScore))
                .limit(2)
                .forEach(t -> plan.add("Review " + (t.getTopicTag() != null ? t.getTopicTag() : session.getTopic())
                        + ": practice articulating missing concepts (" + String.join(", ", parseList(t.getMissingConceptsJson())) + ")."));

        if (!improvementAreas.isEmpty()) {
            plan.add("Run another " + session.getDifficulty() + " timed mock session focusing on: " + improvementAreas.get(0));
        }
        plan.add("Practice explaining time/space complexity and failure modes aloud within 2–3 minutes per question.");
        return plan;
    }

    private String buildMonitoringVerdict(AiMonitoringSummaryDto monitoring, int focusScore) {
        if (monitoring == null) {
            return "Session completed with standard monitoring active (Focus Score: " + focusScore + "/100).";
        }
        return String.format(
                "AI Monitor recorded Focus & Integrity Score %d/100 (%s). Tab switches: %d, Window blurs: %d, Large paste events: %d, Idle alerts: %d, Avg response pace: %ds/question.",
                focusScore,
                monitoring.getMonitorStatus(),
                monitoring.getTabSwitchCount(),
                monitoring.getWindowBlurCount(),
                monitoring.getPasteEventCount(),
                monitoring.getIdleWarningCount(),
                monitoring.getAverageResponseSeconds()
        );
    }

    private String buildExecutiveSummary(
            AiInterviewSession session,
            int overall,
            AiRecommendation rec,
            int answeredCount,
            long skippedCount,
            AiMonitoringSummaryDto monitoring,
            int focusScore) {
        return String.format(
                "Completed %s (%s / %s) with an overall AI evaluation score of %d/100 (%s). Evaluated %d answered turn(s)%s while maintaining a %d/100 focus integrity score (%s).",
                session.getTopic(),
                session.getDifficulty(),
                session.getMode(),
                overall,
                rec,
                answeredCount,
                skippedCount > 0 ? " and " + skippedCount + " skipped" : "",
                focusScore,
                monitoring != null && monitoring.getScoreTrend() != null ? "trajectory: " + monitoring.getScoreTrend() : "monitored"
        );
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static List<QuestionTemplate> buildQuestionBank() {
        return List.of(
                // Java & Spring Boot
                new QuestionTemplate(
                        "java", "CONCEPTUAL", "Java Concurrency & CompletableFuture", AiDifficulty.MID,
                        "How does CompletableFuture differ from a standard Future in Java, and how do you prevent thread pool starvation when composing async I/O calls in Spring Boot?",
                        List.of("Think about non-blocking callbacks vs blocking .get(), and ForkJoinPool.commonPool()."),
                        List.of("non-blocking", "thread pool", "forkjoinpool", "exception handling", "executor"),
                        "CompletableFuture supports non-blocking chaining (thenApply, thenCompose, exceptionally) unlike Future.get(). Because default async methods share ForkJoinPool.commonPool(), blocking I/O there can starve CPU tasks, so supply a dedicated bounded Executor with timeouts and exception handlers.",
                        null, null
                ),
                new QuestionTemplate(
                        "java", "CONCEPTUAL", "Spring Transactions & AOP Proxies", AiDifficulty.SENIOR,
                        "Why can self-invocation bypass @Transactional in Spring Boot, and how do propagation levels like REQUIRES_NEW interact with connection pools?",
                        List.of("Consider JDK dynamic proxies / CGLIB and when the proxy intercepts calls."),
                        List.of("proxy", "self-invocation", "propagation", "connection pool", "isolation"),
                        "Spring @Transactional relies on AOP proxies wrapping the bean; calling an internal method via `this` bypasses the proxy so no transaction boundary starts. REQUIRES_NEW suspends the outer transaction and borrows a second DB connection, which can exhaust HikariCP pools under high concurrency if nested deeply.",
                        null, null
                ),
                new QuestionTemplate(
                        "java", "CODING", "Thread-Safe Token Bucket Rate Limiter", AiDifficulty.SENIOR,
                        "Implement a thread-safe in-memory Token Bucket rate limiter in Java supporting `allowRequest(String clientId)` with configurable capacity and refill rate per second. Explain synchronization and memory eviction.",
                        List.of("Track per-client tokens and lastRefillTimestamp lazily on each request; use ConcurrentHashMap and per-bucket lock or atomic state."),
                        List.of("thread safety", "token bucket", "time complexity", "edge cases", "eviction"),
                        "Store a Bucket per client in a ConcurrentHashMap, synchronize or use AtomicReference/Lock per bucket to compute elapsed refill (`elapsedNanos * rate`) without background timers, cap at maxCapacity, and expire idle client entries to prevent unbounded memory growth.",
                        "import java.util.concurrent.*;\n\npublic class Main {\n    static class TokenBucketLimiter {\n        // TODO: store buckets and implement allowRequest(String clientId)\n        public boolean allowRequest(String clientId) {\n            return true;\n        }\n    }\n    public static void main(String[] args) {\n        TokenBucketLimiter limiter = new TokenBucketLimiter();\n        System.out.println(limiter.allowRequest(\"user-1\"));\n    }\n}\n",
                        "java"
                ),

                // DSA & Algorithms
                new QuestionTemplate(
                        "dsa", "CODING", "LRU Cache Design", AiDifficulty.MID,
                        "Design and implement an LRU (Least Recently Used) Cache with O(1) average time complexity for both `get(key)` and `put(key, value)`. Discuss edge cases such as capacity <= 0 and key updates.",
                        List.of("Combine a HashMap for O(1) lookup with a doubly linked list (or LinkedHashMap with accessOrder=true) for O(1) eviction ordering."),
                        List.of("hash map", "doubly linked list", "time complexity", "space complexity", "edge cases"),
                        "Pair a HashMap<K, Node> with a doubly-linked list using dummy head/tail sentinels. On get or put update, move the node to the head in O(1); when exceeding capacity, evict the tail node and remove its key from the map. Time complexity O(1), space O(capacity).",
                        "import java.util.*;\n\npublic class Main {\n    static class LRUCache {\n        // TODO: implement O(1) get and put\n        public LRUCache(int capacity) {}\n        public int get(int key) { return -1; }\n        public void put(int key, int value) {}\n    }\n    public static void main(String[] args) {\n        LRUCache cache = new LRUCache(2);\n        cache.put(1, 10);\n        System.out.println(cache.get(1));\n    }\n}\n",
                        "java"
                ),
                new QuestionTemplate(
                        "dsa", "CODING", "Longest Substring Without Repeating Characters", AiDifficulty.MID,
                        "Given a string `s`, write a function to find the length of the longest substring without repeating characters in O(n) time. Explain your sliding window invariant and space trade-off.",
                        List.of("Maintain two pointers [left, right] and a map/array storing the last seen index of each character."),
                        List.of("sliding window", "hash map", "time complexity", "space complexity", "edge cases"),
                        "Maintain a sliding window [left, right] and a lastIndex map. When `s[right]` was seen at idx >= left, jump `left = idx + 1` in O(1) rather than shrinking step-by-step. Update max length `right - left + 1`. Time O(n), space O(min(n, charset)). Handle empty strings safely.",
                        "import java.util.*;\n\npublic class Main {\n    public static int lengthOfLongestSubstring(String s) {\n        // TODO: implement sliding window\n        return 0;\n    }\n    public static void main(String[] args) {\n        System.out.println(lengthOfLongestSubstring(\"abcabcbb\"));\n    }\n}\n",
                        "java"
                ),
                new QuestionTemplate(
                        "dsa", "CONCEPTUAL", "Graph Traversal & Cycle Detection", AiDifficulty.MID,
                        "Compare BFS vs DFS for shortest path and cycle detection in both undirected and directed graphs. What changes when edges have positive or negative weights?",
                        List.of("Think about 3-color cycle detection (unvisited/visiting/visited) for directed graphs and Dijkstra/Bellman-Ford for weighted graphs."),
                        List.of("bfs", "dfs", "directed", "time complexity", "dijkstra"),
                        "BFS finds unweighted shortest paths in O(V+E); DFS detects cycles in directed graphs using a 3-state recursion stack (visiting vs visited). For positive edge weights use Dijkstra (O((V+E) log V)); for negative weights use Bellman-Ford.",
                        null, null
                ),

                // Frontend React & TypeScript
                new QuestionTemplate(
                        "frontend", "CONCEPTUAL", "React Reconciliation & Rendering Performance", AiDifficulty.MID,
                        "How does React's reconciliation algorithm decide what to re-render, why are unstable `key` props (like array indices) dangerous in dynamic lists, and when should you use `useMemo`/`React.memo`?",
                        List.of("Consider Fiber tree diffing, component identity via keys, and memoization overhead vs actual render cost."),
                        List.of("reconciliation", "key", "memo", "state", "performance"),
                        "React diffs Fiber trees by element type and `key`. Using array index as a key on mutable/reordered lists causes mismatched component state and DOM bugs. Apply React.memo/useMemo only when profiling confirms expensive subtrees or referential identity is needed for memoized children.",
                        null, null
                ),
                new QuestionTemplate(
                        "frontend", "CONCEPTUAL", "React Hooks & Stale Closures", AiDifficulty.SENIOR,
                        "What causes stale closures in `useEffect`, `setInterval`, or WebSocket event handlers in React, and how do you solve them cleanly without re-subscribing on every render?",
                        List.of("Think about functional state updates, `useRef` latest-callback pattern, and `useEffectEvent`."),
                        List.of("closure", "dependency array", "ref", "functional update", "cleanup"),
                        "Closures capture state/props from the render in which an effect ran. Leaving dependencies out creates stale reads; adding them tears down sockets/timers repeatedly. Solve via functional state updates (`setState(prev => ...)`), storing latest values/handlers in `useRef`, and always returning cleanup functions.",
                        null, null
                ),
                new QuestionTemplate(
                        "frontend", "CODING", "Debounced Async Search with Race Condition Guard", AiDifficulty.SENIOR,
                        "Explain and implement a debounced async search utility (or TypeScript/Java pseudo-implementation) that cancels or ignores stale out-of-order responses when the user types rapidly.",
                        List.of("Use a timer + AbortController or monotonic request sequence ID to discard out-of-order completions."),
                        List.of("debounce", "race condition", "abort", "cleanup", "edge cases"),
                        "Clear any pending debounce timer on new input, issue requests with AbortController (or track an incrementing requestId), ignore responses where requestId !== latestId or signal.aborted is true, and clean up timers on unmount.",
                        "// Pseudocode or Java/Python equivalent for debounced request ordering\n// Describe or implement sequence guard / AbortController logic\n",
                        "java"
                ),

                // Python & Backend
                new QuestionTemplate(
                        "python", "CONCEPTUAL", "Python GIL, Asyncio, and Multiprocessing", AiDifficulty.MID,
                        "Explain how Python's Global Interpreter Lock (GIL) impacts multithreading, and when you should choose `threading`, `asyncio`, or `multiprocessing` in a production backend.",
                        List.of("Distinguish CPU-bound bytecode execution from I/O-bound waiting where the GIL is released."),
                        List.of("gil", "asyncio", "multiprocessing", "io-bound", "cpu-bound"),
                        "The GIL allows only one thread to execute Python bytecode at a time per process. Use `asyncio` or `threading` for I/O-bound workloads (HTTP, DB calls) because the GIL is released during I/O; use `multiprocessing` or C-extensions for CPU-bound parallelism.",
                        null, null
                ),
                new QuestionTemplate(
                        "python", "CODING", "Top-K Frequent Events Stream", AiDifficulty.MID,
                        "Write a Python function `top_k_frequent(events, k)` that returns the `k` most frequent items in better than O(n log n) time when k << n. Handle ties and invalid k gracefully.",
                        List.of("Count frequencies with Counter/dict, then use a min-heap of size k (`heapq.nlargest` or bucket sort)."),
                        List.of("hash map", "heap", "time complexity", "space complexity", "edge cases"),
                        "Count frequencies in O(n) via a dictionary/Counter, then maintain a size-k min-heap in O(n log k) or use bucket sort by frequency for O(n) time and O(n) space. Guard against k <= 0 or empty input.",
                        "from collections import Counter\nimport heapq\n\ndef top_k_frequent(events, k):\n    # TODO: return top k frequent items\n    return []\n\nif __name__ == '__main__':\n    print(top_k_frequent(['a', 'b', 'a', 'c', 'a', 'b'], 2))\n",
                        "python"
                ),

                // System Design
                new QuestionTemplate(
                        "system-design", "SYSTEM_DESIGN", "Distributed Rate Limiter Architecture", AiDifficulty.SENIOR,
                        "Design a distributed API rate limiter across a fleet of stateless API gateway nodes handling 100k req/sec. Discuss algorithm choice, Redis atomicity, failure modes, and hot keys.",
                        List.of("Compare token bucket vs sliding window log/counter, Redis Lua scripts for atomicity, and fail-open vs fail-closed."),
                        List.of("rate limiting", "cache", "atomicity", "fault tolerance", "latency"),
                        "Use Redis with atomic Lua scripts (e.g., Token Bucket or Sliding Window Counter) to avoid read-modify-write race conditions across gateways. Keep local fallback token budgets or fail-open on Redis outages depending on endpoint criticality, and shard Redis keys to avoid single-node hot spots.",
                        null, null
                ),
                new QuestionTemplate(
                        "system-design", "SYSTEM_DESIGN", "Idempotent Payment & Outbox Architecture", AiDifficulty.SENIOR,
                        "Design an idempotent payment/order processing pipeline so network retries never double-charge users and downstream Kafka events remain consistent with database commits.",
                        List.of("Think about Idempotency-Key headers, unique DB constraints, and the Transactional Outbox pattern."),
                        List.of("idempotency", "isolation", "outbox", "retry", "dead letter"),
                        "Require client-supplied `Idempotency-Key` stored with a unique constraint and status/response payload inside the DB transaction. Write domain state + an `outbox_events` row atomically in one transaction, then relay to Kafka via CDC/poller with at-least-once delivery and idempotent consumers.",
                        null, null
                ),
                new QuestionTemplate(
                        "system-design", "SYSTEM_DESIGN", "Cache Stampede & Eviction Strategy", AiDifficulty.MID,
                        "When a high-traffic cached key expires simultaneously for thousands of concurrent requests (cache stampede), how does it impact the database and how do you mitigate it?",
                        List.of("Consider single-flight / request coalescing, probabilistic early expiration, and jittered TTLs."),
                        List.of("cache", "ttl", "stampede", "singleflight", "fallback"),
                        "A thundering herd hits the origin DB simultaneously. Mitigate with request coalescing (singleflight/distributed mutex so one worker recomputes), randomized TTL jitter, stale-while-revalidate serving, and circuit breakers.",
                        null, null
                ),

                // Database & SQL
                new QuestionTemplate(
                        "database", "CONCEPTUAL", "SQL Indexing & Query Optimization", AiDifficulty.MID,
                        "How does a composite B-Tree index on `(tenant_id, status, created_at)` serve queries, when is the index skipped, and how do you diagnose and fix an N+1 query problem?",
                        List.of("Apply the leftmost-prefix rule, check cardinality/selectivity, and use EXPLAIN ANALYZE + batch/join fetching."),
                        List.of("database indexing", "leftmost prefix", "explain", "n+1", "selectivity"),
                        "Composite B-Tree indexes follow the leftmost-prefix rule: filtering on `tenant_id` and `status` uses the index, whereas filtering only on `created_at` usually skips it. Diagnose N+1 via SQL logging/EXPLAIN and fix with JOIN FETCH, IN-clause batching, or DataLoader.",
                        null, null
                ),
                new QuestionTemplate(
                        "database", "CONCEPTUAL", "ACID Isolation Levels & Concurrency Anomalies", AiDifficulty.SENIOR,
                        "Contrast READ COMMITTED, REPEATABLE READ, and SERIALIZABLE isolation levels in PostgreSQL. Which anomalies does each prevent, and how does MVCC prevent readers from blocking writers?",
                        List.of("Explain dirty reads, non-repeatable reads, phantom reads, write skew, and MVCC snapshot tuples."),
                        List.of("isolation", "mvcc", "phantom", "write skew", "deadlock"),
                        "MVCC maintains tuple row versions with transaction IDs so readers view consistent snapshots without locking writers. READ COMMITTED sees committed data per statement (allows non-repeatable reads); REPEATABLE READ freezes snapshot at transaction start (still susceptible to write skew in some engines); SERIALIZABLE detects serialization conflicts via SSI.",
                        null, null
                ),

                // Behavioral
                new QuestionTemplate(
                        "behavioral", "BEHAVIORAL", "Production Incident Ownership", AiDifficulty.MID,
                        "Describe a high-severity production bug or outage you helped resolve. Using STAR, explain how you triaged root cause under pressure, communicated with stakeholders, and prevented recurrence.",
                        List.of("Highlight clear triage metrics, blameless root cause analysis, and concrete guardrails added afterward."),
                        List.of("star", "root cause", "communication", "rollback", "prevention"),
                        "Cover the Situation (service impact & SLO breach), Task (your triage ownership), Action (stabilization/rollback first, logs/metrics correlation to isolate root cause, stakeholder updates), and Result (MTTR, postmortem action items such as canary alerts or automated tests).",
                        null, null
                ),
                new QuestionTemplate(
                        "behavioral", "BEHAVIORAL", "Technical Disagreement & Trade-offs", AiDifficulty.SENIOR,
                        "Tell me about a time you disagreed with a teammate or manager on an architectural choice or deadline trade-off. How did you evaluate the options and reach alignment?",
                        List.of("Show data-driven objectivity, active listening, and commitment after decision."),
                        List.of("star", "trade-offs", "data", "alignment", "outcome"),
                        "Demonstrate empathy by accurately framing the other viewpoint, bringing benchmarks/prototypes or risk matrices to compare options objectively, aligning on business constraints, and documenting the agreed Architecture Decision Record (ADR).",
                        null, null
                )
        );
    }
}
