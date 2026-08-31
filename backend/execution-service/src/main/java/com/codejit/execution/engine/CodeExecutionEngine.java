package com.codejit.execution.engine;

import com.codejit.common.dto.execution.TestResult;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionEngine {

    private final SandboxProcessExecutor processExecutor;

    public CodeExecutionEngine(SandboxProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public TestResult execute(String language, String sourceCode, String input, String expectedOutput, int sequence, long timeoutMillis) {
        String lang = (language != null) ? language.toLowerCase().trim() : "java";

        if ("python".equals(lang) || "py".equals(lang)) {
            return processExecutor.executePython(sourceCode, input, expectedOutput, sequence, timeoutMillis);
        } else {
            return processExecutor.executeJava(sourceCode, input, expectedOutput, sequence, timeoutMillis);
        }
    }
}

