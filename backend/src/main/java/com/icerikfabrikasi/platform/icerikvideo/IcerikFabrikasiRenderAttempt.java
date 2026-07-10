package com.icerikfabrikasi.platform.icerikvideo;

import java.time.Instant;
import java.util.UUID;

public final class IcerikFabrikasiRenderAttempt {
    private final UUID attemptId;
    private final int attemptNo;
    private final Instant startedAt;
    private final Instant completedAt;
    private final IcerikFabrikasiRenderJobStatus status;
    private final IcerikFabrikasiRenderFailureCode failureCode;
    private final long durationMs;
    private final String rendererAlias;

    public IcerikFabrikasiRenderAttempt(UUID attemptId, int attemptNo, Instant startedAt,
                               Instant completedAt, IcerikFabrikasiRenderJobStatus status,
                               IcerikFabrikasiRenderFailureCode failureCode, long durationMs,
                               String rendererAlias) {
        this.attemptId = attemptId;
        this.attemptNo = attemptNo;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.failureCode = failureCode;
        this.durationMs = durationMs;
        this.rendererAlias = rendererAlias;
    }

    public UUID getAttemptId() { return attemptId; }
    public int getAttemptNo() { return attemptNo; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public IcerikFabrikasiRenderJobStatus getStatus() { return status; }
    public IcerikFabrikasiRenderFailureCode getFailureCode() { return failureCode; }
    public long getDurationMs() { return durationMs; }
    public String getRendererAlias() { return rendererAlias; }
}
