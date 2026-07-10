package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-023 Task 02: Render adapter result.
 * No binary, no file path, no URL, no secrets.
 */
public final class IcerikFabrikasiRenderAdapterResult {
    private final boolean success;
    private final IcerikFabrikasiRenderAdapterMode adapterMode;
    private final IcerikFabrikasiRenderAdapterFailureCode failureCode;
    private final String sanitizedMessage;
    private final IcerikFabrikasiRenderAdapterTraceReference traceReference;
    private final IcerikFabrikasiRenderAdapterSafetyEnvelope safetyEnvelope;
    private final long durationMs;

    public IcerikFabrikasiRenderAdapterResult(boolean success, IcerikFabrikasiRenderAdapterMode adapterMode,
                                     IcerikFabrikasiRenderAdapterFailureCode failureCode,
                                     String sanitizedMessage,
                                     IcerikFabrikasiRenderAdapterTraceReference traceReference,
                                     IcerikFabrikasiRenderAdapterSafetyEnvelope safetyEnvelope,
                                     long durationMs) {
        this.success = success;
        this.adapterMode = adapterMode;
        this.failureCode = failureCode;
        this.sanitizedMessage = sanitizedMessage;
        this.traceReference = traceReference;
        this.safetyEnvelope = safetyEnvelope;
        this.durationMs = durationMs;
    }

    public boolean isSuccess() { return success; }
    public IcerikFabrikasiRenderAdapterMode getAdapterMode() { return adapterMode; }
    public IcerikFabrikasiRenderAdapterFailureCode getFailureCode() { return failureCode; }
    public String getSanitizedMessage() { return sanitizedMessage; }
    public IcerikFabrikasiRenderAdapterTraceReference getTraceReference() { return traceReference; }
    public IcerikFabrikasiRenderAdapterSafetyEnvelope getSafetyEnvelope() { return safetyEnvelope; }
    public long getDurationMs() { return durationMs; }
}
