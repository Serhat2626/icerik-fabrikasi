package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-028 Task 01: FFmpeg binary policy.
 * Opaque label only — no raw path exposure.
 */
public enum IcerikFabrikasiFfmpegBinaryPolicy {
    SYSTEM_FFMPEG_LOCAL,
    TEST_FIXTURE_FFMPEG,
    DISABLED_BINARY;

    public boolean isAvailable() {
        return this != DISABLED_BINARY;
    }
}
