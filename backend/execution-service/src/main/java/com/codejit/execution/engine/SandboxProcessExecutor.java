package com.codejit.execution.engine;

import com.codejit.common.dto.execution.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Component
public class SandboxProcessExecutor {

    private static final Logger log = LoggerFactory.getLogger(SandboxProcessExecutor.class);

    public TestResult executeJava(String sourceCode, String input, String expectedOutput, int sequence, long timeoutMillis) {
        Path tempDir = null;
        long startTime = System.currentTimeMillis();
        try {
            tempDir = Files.createTempDirectory("codejit_sandbox_");
            Path sourceFile = tempDir.resolve("Main.java");

            String codeToCompile = sourceCode;
            if (!codeToCompile.contains("class Main") && !codeToCompile.contains("class Solution")) {
                codeToCompile = "public class Main {\n" + codeToCompile + "\n}";
            }

            Files.writeString(sourceFile, codeToCompile, StandardCharsets.UTF_8);

            // Step 1: Compile with javac
            ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
            compilePb.directory(tempDir.toFile());
            compilePb.redirectErrorStream(true);

            Process compileProcess = compilePb.start();
            boolean compiled = compileProcess.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!compiled) {
                compileProcess.destroyForcibly();
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Compilation Timed Out")
                        .actualOutput("")
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(System.currentTimeMillis() - startTime)
                        .build();
            }

            if (compileProcess.exitValue() != 0) {
                String compileError = readStream(compileProcess.getInputStream());
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Compilation Error: " + compileError)
                        .actualOutput("")
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Step 2: Execute with java
            long execStart = System.currentTimeMillis();
            ProcessBuilder runPb = new ProcessBuilder("java", "-Xmx128m", "-Xms32m", "Main");
            runPb.directory(tempDir.toFile());

            Process runProcess = runPb.start();

            if (input != null && !input.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(input);
                    writer.flush();
                }
            } else {
                runProcess.getOutputStream().close();
            }

            boolean finished = runProcess.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            long runtime = System.currentTimeMillis() - execStart;

            if (!finished) {
                runProcess.destroyForcibly();
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Time Limit Exceeded")
                        .actualOutput("")
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(runtime)
                        .build();
            }

            String actualOutput = readStream(runProcess.getInputStream()).trim();
            String errorOutput = readStream(runProcess.getErrorStream()).trim();

            if (runProcess.exitValue() != 0 && !errorOutput.isEmpty()) {
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Runtime Error: " + errorOutput)
                        .actualOutput(actualOutput)
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(runtime)
                        .build();
            }

            String cleanExpected = expectedOutput != null ? expectedOutput.trim() : "";
            boolean passed = actualOutput.equals(cleanExpected);

            return TestResult.builder()
                    .sequence(sequence)
                    .passed(passed)
                    .actualOutput(actualOutput)
                    .expectedOutput(cleanExpected)
                    .errorMessage(passed ? null : "Output mismatch")
                    .runtimeMillis(runtime)
                    .build();

        } catch (Exception e) {
            log.error("Execution exception in sandbox: {}", e.getMessage(), e);
            return TestResult.builder()
                    .sequence(sequence)
                    .passed(false)
                    .errorMessage("Execution failed: " + e.getMessage())
                    .actualOutput("")
                    .expectedOutput(expectedOutput)
                    .runtimeMillis(System.currentTimeMillis() - startTime)
                    .build();
        } finally {
            if (tempDir != null) {
                cleanupTempDir(tempDir);
            }
        }
    }

    public TestResult executePython(String sourceCode, String input, String expectedOutput, int sequence, long timeoutMillis) {
        Path tempDir = null;
        long startTime = System.currentTimeMillis();
        try {
            tempDir = Files.createTempDirectory("codejit_py_sandbox_");
            Path scriptFile = tempDir.resolve("solution.py");
            Files.writeString(scriptFile, sourceCode, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder("python3", "solution.py");
            pb.directory(tempDir.toFile());

            Process process = pb.start();

            if (input != null && !input.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(input);
                    writer.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            long runtime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Time Limit Exceeded")
                        .actualOutput("")
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(runtime)
                        .build();
            }

            String actualOutput = readStream(process.getInputStream()).trim();
            String errorOutput = readStream(process.getErrorStream()).trim();

            if (process.exitValue() != 0) {
                return TestResult.builder()
                        .sequence(sequence)
                        .passed(false)
                        .errorMessage("Runtime Error: " + errorOutput)
                        .actualOutput(actualOutput)
                        .expectedOutput(expectedOutput)
                        .runtimeMillis(runtime)
                        .build();
            }

            String cleanExpected = expectedOutput != null ? expectedOutput.trim() : "";
            boolean passed = actualOutput.equals(cleanExpected);

            return TestResult.builder()
                    .sequence(sequence)
                    .passed(passed)
                    .actualOutput(actualOutput)
                    .expectedOutput(cleanExpected)
                    .runtimeMillis(runtime)
                    .build();

        } catch (Exception e) {
            return TestResult.builder()
                    .sequence(sequence)
                    .passed(false)
                    .errorMessage("Execution failed: " + e.getMessage())
                    .actualOutput("")
                    .expectedOutput(expectedOutput)
                    .runtimeMillis(System.currentTimeMillis() - startTime)
                    .build();
        } finally {
            if (tempDir != null) {
                cleanupTempDir(tempDir);
            }
        }
    }

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private void cleanupTempDir(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {
        }
    }
}

