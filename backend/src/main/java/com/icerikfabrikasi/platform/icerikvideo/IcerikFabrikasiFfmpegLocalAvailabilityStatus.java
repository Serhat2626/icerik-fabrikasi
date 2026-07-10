package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-032: Safe local-only FFmpeg availability status.
 * Does NOT expose executable path.
 */
public enum IcerikFabrikasiFfmpegLocalAvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    DISABLED,
    UNSUPPORTED_ENVIRONMENT
}
