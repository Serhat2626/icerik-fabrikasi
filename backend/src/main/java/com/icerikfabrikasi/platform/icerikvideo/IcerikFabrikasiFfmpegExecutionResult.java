package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-024 Task 04: FFmpeg execution result.
 * Safe metadata only — no file paths, no binary, no storage keys.
 */
public final class IcerikFabrikasiFfmpegExecutionResult {
    private final boolean success;
    private final IcerikFabrikasiFfmpegExecutionMode executionMode;
    private final IcerikFabrikasiFfmpegExecutionFailureCode failureCode;
    private final String safeTraceRef;
    private final String opaqueArtifactRef;
    private final IcerikFabrikasiFfmpegExecutionSafetyEnvelope safetyEnvelope;
    private final String safeMessage;
    private final long durationMs;

    private IcerikFabrikasiFfmpegExecutionResult(boolean success,
                                        IcerikFabrikasiFfmpegExecutionMode executionMode,
                                        IcerikFabrikasiFfmpegExecutionFailureCode failureCode,
                                        String safeTraceRef,
                                        String opaqueArtifactRef,
                                        IcerikFabrikasiFfmpegExecutionSafetyEnvelope safetyEnvelope,
                                        String safeMessage,
                                        long durationMs) {
        this.success = success;
        this.executionMode = executionMode;
        this.failureCode = failureCode;
        this.safeTraceRef = safeTraceRef;
        this.opaqueArtifactRef = opaqueArtifactRef;
        this.safetyEnvelope = safetyEnvelope;
        this.safeMessage = safeMessage;
        this.durationMs = durationMs;
    }

    public static IcerikFabrikasiFfmpegExecutionResult disabled() {
        return new IcerikFabrikasiFfmpegExecutionResult(false,
                IcerikFabrikasiFfmpegExecutionMode.DISABLED,
                IcerikFabrikasiFfmpegExecutionFailureCode.EXECUTION_DISABLED,
                "ffmpeg-disabled-trace:" + UUID.randomUUID(),
                null,
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.DISABLED),
                "FFmpeg execution disabled — using stub",
                0);
    }

    public static IcerikFabrikasiFfmpegExecutionResult configMissing() {
        return new IcerikFabrikasiFfmpegExecutionResult(false,
                IcerikFabrikasiFfmpegExecutionMode.CONFIG_MISSING,
                IcerikFabrikasiFfmpegExecutionFailureCode.CONFIG_MISSING,
                "ffmpeg-config-missing-trace:" + UUID.randomUUID(),
                null,
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.CONFIG_MISSING),
                "FFmpeg config missing",
                0);
    }

    public static IcerikFabrikasiFfmpegExecutionResult configInvalid(List<String> notes) {
        return new IcerikFabrikasiFfmpegExecutionResult(false,
                IcerikFabrikasiFfmpegExecutionMode.CONFIG_INVALID,
                IcerikFabrikasiFfmpegExecutionFailureCode.CONFIG_INVALID,
                "ffmpeg-config-invalid-trace:" + UUID.randomUUID(),
                null,
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.CONFIG_INVALID, notes),
                "FFmpeg config invalid",
                0);
    }

    public static IcerikFabrikasiFfmpegExecutionResult dryRunSuccess(List<String> notes, long durationMs) {
        return new IcerikFabrikasiFfmpegExecutionResult(true,
                IcerikFabrikasiFfmpegExecutionMode.DRY_RUN,
                null,
                "ffmpeg-dryrun-trace:" + UUID.randomUUID(),
                "internal-dryrun-artifact:" + UUID.randomUUID(),
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.DRY_RUN, notes),
                "Dry-run validation passed — no real execution",
                durationMs);
    }

    public static IcerikFabrikasiFfmpegExecutionResult dryRunFailed(IcerikFabrikasiFfmpegExecutionFailureCode code,
                                                           String message, List<String> notes) {
        return new IcerikFabrikasiFfmpegExecutionResult(false,
                IcerikFabrikasiFfmpegExecutionMode.DRY_RUN,
                code,
                "ffmpeg-dryrun-failed-trace:" + UUID.randomUUID(),
                null,
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.DRY_RUN, notes),
                message,
                0);
    }

    public static IcerikFabrikasiFfmpegExecutionResult simulatedSuccess(long durationMs) {
        return new IcerikFabrikasiFfmpegExecutionResult(true,
                IcerikFabrikasiFfmpegExecutionMode.SIMULATED_SUCCESS,
                null,
                "ffmpeg-simulated-trace:" + UUID.randomUUID(),
                "internal-simulated-artifact:" + UUID.randomUUID(),
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.SIMULATED_SUCCESS),
                "Simulated execution completed — no real process",
                durationMs);
    }

    public static IcerikFabrikasiFfmpegExecutionResult failedSafe(IcerikFabrikasiFfmpegExecutionFailureCode code,
                                                         String message) {
        return new IcerikFabrikasiFfmpegExecutionResult(false,
                IcerikFabrikasiFfmpegExecutionMode.FAILED_SAFE,
                code,
                "ffmpeg-failed-safe-trace:" + UUID.randomUUID(),
                null,
                new IcerikFabrikasiFfmpegExecutionSafetyEnvelope(IcerikFabrikasiFfmpegExecutionMode.FAILED_SAFE),
                message,
                0);
    }

    public boolean isSuccess() { return success; }
    public IcerikFabrikasiFfmpegExecutionMode getExecutionMode() { return executionMode; }
    public IcerikFabrikasiFfmpegExecutionFailureCode getFailureCode() { return failureCode; }
    public String getSafeTraceRef() { return safeTraceRef; }
    public String getOpaqueArtifactRef() { return opaqueArtifactRef; }
    public IcerikFabrikasiFfmpegExecutionSafetyEnvelope getSafetyEnvelope() { return safetyEnvelope; }
    public String getSafeMessage() { return safeMessage; }
    public long getDurationMs() { return durationMs; }
}
