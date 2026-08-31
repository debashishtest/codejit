package com.codejit.common.dto.execution;

public class CodeRequest {
    private String sourceCode;
    private String language = "java";

    public CodeRequest() {}

    public CodeRequest(String sourceCode, String language) {
        this.sourceCode = sourceCode;
        this.language = language != null ? language : "java";
    }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sourceCode;
        private String language = "java";

        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder language(String language) { this.language = language; return this; }

        public CodeRequest build() {
            return new CodeRequest(sourceCode, language);
        }
    }
}

