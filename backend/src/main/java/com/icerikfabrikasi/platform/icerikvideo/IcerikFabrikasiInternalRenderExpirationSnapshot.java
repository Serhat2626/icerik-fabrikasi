package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Expiration snapshot.
 */
public final class IcerikFabrikasiInternalRenderExpirationSnapshot {
    private final boolean expired;
    private final IcerikFabrikasiInternalRenderExpirationReasonCode reasonCode;
    private final int maxQueueDurationSeconds;
    private final int maxRetryWaitDurationSeconds;

    public IcerikFabrikasiInternalRenderExpirationSnapshot(boolean expired,
            IcerikFabrikasiInternalRenderExpirationReasonCode reasonCode,
            IcerikFabrikasiInternalRenderExpirationPolicy policy) {
        this.expired = expired;
        this.reasonCode = reasonCode;
        this.maxQueueDurationSeconds = policy.getMaxQueueDurationSeconds();
        this.maxRetryWaitDurationSeconds = policy.getMaxRetryWaitDurationSeconds();
    }

    public boolean isExpired() { return expired; }
    public IcerikFabrikasiInternalRenderExpirationReasonCode getReasonCode() { return reasonCode; }
    public int getMaxQueueDurationSeconds() { return maxQueueDurationSeconds; }
    public int getMaxRetryWaitDurationSeconds() { return maxRetryWaitDurationSeconds; }
}
