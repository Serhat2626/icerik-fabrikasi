package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Bounded retry policy value object.
 */
public final class IcerikFabrikasiInternalRenderRetryPolicy {
    private final int maxAttempts;
    private final int retryDelaySeconds;

    public IcerikFabrikasiInternalRenderRetryPolicy(int maxAttempts, int retryDelaySeconds) {
        this.maxAttempts = Math.max(0, Math.min(5, maxAttempts));
        this.retryDelaySeconds = Math.max(30, Math.min(600, retryDelaySeconds));
    }

    public static IcerikFabrikasiInternalRenderRetryPolicy defaults() {
        return new IcerikFabrikasiInternalRenderRetryPolicy(3, 60);
    }

    public int getMaxAttempts() { return maxAttempts; }
    public int getRetryDelaySeconds() { return retryDelaySeconds; }
}
