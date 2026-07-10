package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-032: Safe local-only FFmpeg availability checker.
 * Does NOT use ProcessBuilder directly.
 * Delegates to IcerikFabrikasiFfmpegLocalDevProcessRunner (the ONLY allowed ProcessBuilder class).
 */
public class IcerikFabrikasiFfmpegLocalAvailabilityChecker {

    private final IcerikFabrikasiFfmpegExecutionEnvironment environment;
    private final boolean checkEnabled;
    private final IcerikFabrikasiFfmpegLocalDevProcessRunner runner;

    public IcerikFabrikasiFfmpegLocalAvailabilityChecker(IcerikFabrikasiFfmpegExecutionEnvironment environment,
            boolean checkEnabled, IcerikFabrikasiFfmpegLocalDevProcessRunner runner) {
        this.environment = environment != null ? environment : IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN;
        this.checkEnabled = checkEnabled;
        this.runner = runner;
    }

    public IcerikFabrikasiFfmpegLocalAvailabilityChecker(IcerikFabrikasiFfmpegExecutionEnvironment environment, boolean checkEnabled) {
        this(environment, checkEnabled, null);
    }

    public IcerikFabrikasiFfmpegLocalAvailabilityChecker() {
        this(IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN, false, null);
    }

    /**
     * Check FFmpeg availability safely. No path/output exposure.
     * Delegates probe to the allowed runner boundary class.
     */
    public IcerikFabrikasiFfmpegLocalAvailabilityStatus check() {
        if (!checkEnabled)
            return IcerikFabrikasiFfmpegLocalAvailabilityStatus.DISABLED;

        if (environment != IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL)
            return IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNSUPPORTED_ENVIRONMENT;

        // Delegate to runner for probe — no direct ProcessBuilder here
        if (runner == null)
            return IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNAVAILABLE;

        var probeReq = new IcerikFabrikasiFfmpegProcessRequest("probe", "mp4", 10, 1, 1, 1, 1, 1);
        var result = runner.execute(probeReq, IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);

        return switch (result.getStatus()) {
            case PROCESS_COMPLETED_SAFE -> IcerikFabrikasiFfmpegLocalAvailabilityStatus.AVAILABLE;
            case PROCESS_SKIPPED -> IcerikFabrikasiFfmpegLocalAvailabilityStatus.DISABLED;
            case GATE_DENIED -> IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNSUPPORTED_ENVIRONMENT;
            default -> IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNAVAILABLE;
        };
    }

    public boolean isCheckEnabled() { return checkEnabled; }
}
