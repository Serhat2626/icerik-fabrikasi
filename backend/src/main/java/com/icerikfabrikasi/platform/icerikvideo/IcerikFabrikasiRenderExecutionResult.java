package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-022 Task 03: Render execution result.
 * No binary data. No file path. No public URL.
 */
public final class IcerikFabrikasiRenderExecutionResult {
    private final boolean success;
    private final long durationMs;
    private final IcerikFabrikasiRenderFailureCode failureCode;
    private final String failureMessage;
    private final IcerikFabrikasiRenderArtifact artifact;
    private final String safeTraceRef;

    public IcerikFabrikasiRenderExecutionResult(boolean success, long durationMs,
                                       IcerikFabrikasiRenderFailureCode failureCode,
                                       String failureMessage,
                                       IcerikFabrikasiRenderArtifact artifact,
                                       String safeTraceRef) {
        this.success = success;
        this.durationMs = durationMs;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.artifact = artifact;
        this.safeTraceRef = safeTraceRef;
    }

    public boolean isSuccess() { return success; }
    public long getDurationMs() { return durationMs; }
    public IcerikFabrikasiRenderFailureCode getFailureCode() { return failureCode; }
    public String getFailureMessage() { return failureMessage; }
    public IcerikFabrikasiRenderArtifact getArtifact() { return artifact; }
    public String getSafeTraceRef() { return safeTraceRef; }
}
