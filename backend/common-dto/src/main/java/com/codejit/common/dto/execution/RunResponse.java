package com.codejit.common.dto.execution;

import java.util.ArrayList;
import java.util.List;

public class RunResponse {
    private List<TestResult> results = new ArrayList<>();
    private long totalRuntimeMillis;
    private boolean success;

    public RunResponse() {}

    public RunResponse(List<TestResult> results, long totalRuntimeMillis, boolean success) {
        this.results = results != null ? results : new ArrayList<>();
        this.totalRuntimeMillis = totalRuntimeMillis;
        this.success = success;
    }

    public List<TestResult> getResults() { return results; }
    public void setResults(List<TestResult> results) { this.results = results != null ? results : new ArrayList<>(); }

    public long getTotalRuntimeMillis() { return totalRuntimeMillis; }
    public void setTotalRuntimeMillis(long totalRuntimeMillis) { this.totalRuntimeMillis = totalRuntimeMillis; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<TestResult> results = new ArrayList<>();
        private long totalRuntimeMillis;
        private boolean success;

        public Builder results(List<TestResult> results) { this.results = results; return this; }
        public Builder totalRuntimeMillis(long totalRuntimeMillis) { this.totalRuntimeMillis = totalRuntimeMillis; return this; }
        public Builder success(boolean success) { this.success = success; return this; }

        public RunResponse build() {
            return new RunResponse(results, totalRuntimeMillis, success);
        }
    }
}

