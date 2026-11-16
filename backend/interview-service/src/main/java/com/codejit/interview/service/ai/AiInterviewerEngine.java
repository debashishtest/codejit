package com.codejit.interview.service.ai;

import com.codejit.common.dto.ai.AiMonitoringSummaryDto;
import com.codejit.common.dto.ai.MonitorFrameRequest;
import com.codejit.common.dto.ai.SubmitAiAnswerRequest;
import com.codejit.interview.entity.AiInterviewSession;
import com.codejit.interview.entity.AiInterviewTurn;

import java.util.List;

public interface AiInterviewerEngine {

    AiGenerationModels.GeneratedQuestion generateNextQuestion(
            AiInterviewSession session,
            List<AiInterviewTurn> previousTurns);

    AiGenerationModels.GeneratedEvaluation evaluateAnswer(
            AiInterviewSession session,
            AiInterviewTurn turn,
            SubmitAiAnswerRequest answerRequest);

    AiGenerationModels.GeneratedFinalReport generateFinalReport(
            AiInterviewSession session,
            List<AiInterviewTurn> allTurns,
            AiMonitoringSummaryDto monitoringSummary);

    AiGenerationModels.GeneratedEyeAnalysis analyzeMonitorFrame(
            AiInterviewSession session,
            MonitorFrameRequest frameRequest);
}
