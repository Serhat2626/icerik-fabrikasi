package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-028 Task 01: Execution environment.
 * Real FFmpeg allowed only in LOCAL/DEV/TEST.
 */
public enum IcerikFabrikasiFfmpegExecutionEnvironment {
    LOCAL,
    DEV,
    TEST,
    STAGING,
    PROD,
    UNKNOWN;

    public boolean isRealExecutionAllowed() {
        return this == LOCAL || this == DEV || this == TEST;
    }
}
