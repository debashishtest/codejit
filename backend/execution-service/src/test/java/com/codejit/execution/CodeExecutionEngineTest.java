package com.codejit.execution;

import com.codejit.common.dto.execution.TestResult;
import com.codejit.execution.engine.CodeExecutionEngine;
import com.codejit.execution.engine.SandboxProcessExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeExecutionEngineTest {

    @Mock
    private SandboxProcessExecutor processExecutor;

    private CodeExecutionEngine executionEngine;

    @BeforeEach
    void setUp() {
        executionEngine = new CodeExecutionEngine(processExecutor);
    }

    @Test
    @DisplayName("Should route Java execution to SandboxProcessExecutor")
    void testExecuteJava() {
        TestResult mockResult = TestResult.builder()
                .sequence(0)
                .passed(true)
                .actualOutput("42")
                .expectedOutput("42")
                .runtimeMillis(35)
                .build();

        when(processExecutor.executeJava(anyString(), anyString(), anyString(), anyInt(), anyLong()))
                .thenReturn(mockResult);

        TestResult result = executionEngine.execute("java", "public class Main { public static void main(String[] a){ System.out.println(42); } }", "", "42", 0, 3000L);

        assertNotNull(result);
        assertTrue(result.isPassed());
        assertEquals("42", result.getActualOutput());
    }

    @Test
    @DisplayName("Should route Python execution to SandboxProcessExecutor")
    void testExecutePython() {
        TestResult mockResult = TestResult.builder()
                .sequence(0)
                .passed(true)
                .actualOutput("hello")
                .expectedOutput("hello")
                .runtimeMillis(20)
                .build();

        when(processExecutor.executePython(anyString(), anyString(), anyString(), anyInt(), anyLong()))
                .thenReturn(mockResult);

        TestResult result = executionEngine.execute("python", "print('hello')", "", "hello", 0, 3000L);

        assertNotNull(result);
        assertTrue(result.isPassed());
        assertEquals("hello", result.getActualOutput());
    }
}

