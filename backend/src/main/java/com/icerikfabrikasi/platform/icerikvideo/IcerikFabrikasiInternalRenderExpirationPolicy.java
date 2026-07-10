package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Bounded expiration policy value object.
 */
public final class IcerikFabrikasiInternalRenderExpirationPolicy {
    private final int maxQueueDurationSeconds;
    private final int maxRetryWaitDurationSeconds;

    public IcerikFabrikasiInternalRenderExpirationPolicy(int maxQueueDurationSeconds, int maxRetryWaitDurationSeconds) {
        this.maxQueueDurationSeconds = Math.max(60, Math.min(7200, maxQueueDurationSeconds));
        this.maxRetryWaitDurationSeconds = Math.max(60, Math.min(3600, maxRetryWaitDurationSeconds));
    }

    public static IcerikFabrikasiInternalRenderExpirationPolicy defaults() {
        return new IcerikFabrikasiInternalRenderExpirationPolicy(3600, 1800);
    }

    public int getMaxQueueDurationSeconds() { return maxQueueDurationSeconds; }
    public int getMaxRetryWaitDurationSeconds() { return maxRetryWaitDurationSeconds; }
}
