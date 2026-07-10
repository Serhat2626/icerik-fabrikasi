package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Retry snapshot.
 */
public final class IcerikFabrikasiInternalRenderRetrySnapshot {
    private final int currentAttempt;
    private final int maxAttempts;
    private final int retryDelaySeconds;
    private final boolean retriable;
    private final IcerikFabrikasiInternalRenderRetryReasonCode reasonCode;

    public IcerikFabrikasiInternalRenderRetrySnapshot(int currentAttempt, IcerikFabrikasiInternalRenderRetryPolicy policy,
            IcerikFabrikasiInternalRenderRetryReasonCode reasonCode) {
        this.currentAttempt = Math.max(0, Math.min(5, currentAttempt));
        this.maxAttempts = policy.getMaxAttempts();
        this.retryDelaySeconds = policy.getRetryDelaySeconds();
        this.retriable = currentAttempt < maxAttempts
                && reasonCode != IcerikFabrikasiInternalRenderRetryReasonCode.NOT_RETRIABLE_PERMANENT
                && reasonCode != IcerikFabrikasiInternalRenderRetryReasonCode.MAX_RETRIES_EXHAUSTED
                && reasonCode != IcerikFabrikasiInternalRenderRetryReasonCode.CANCELLED_NO_RETRY
                && reasonCode != IcerikFabrikasiInternalRenderRetryReasonCode.EXPIRED_NO_RETRY;
        this.reasonCode = reasonCode;
    }

    public int getCurrentAttempt() { return currentAttempt; }
    public int getMaxAttempts() { return maxAttempts; }
    public int getRetryDelaySeconds() { return retryDelaySeconds; }
    public boolean isRetriable() { return retriable; }
    public IcerikFabrikasiInternalRenderRetryReasonCode getReasonCode() { return reasonCode; }
}
