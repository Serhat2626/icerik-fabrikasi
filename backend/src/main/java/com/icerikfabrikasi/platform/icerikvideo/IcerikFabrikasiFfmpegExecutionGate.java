package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-028 Task 01: FFmpeg execution gate.
 * Determines whether real FFmpeg execution is allowed.
 * Default: DISABLED (fail-closed).
 */
public class IcerikFabrikasiFfmpegExecutionGate {

    private final IcerikFabrikasiFfmpegRealExecutionMode mode;
    private final IcerikFabrikasiFfmpegExecutionEnvironment environment;
    private final boolean internalDevTestEnabled;

    public IcerikFabrikasiFfmpegExecutionGate() {
        this(IcerikFabrikasiFfmpegRealExecutionMode.DISABLED,
                IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN, false);
    }

    public IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode mode,
            IcerikFabrikasiFfmpegExecutionEnvironment environment, boolean internalDevTestEnabled) {
        this.mode = mode != null ? mode : IcerikFabrikasiFfmpegRealExecutionMode.DISABLED;
        this.environment = environment != null ? environment : IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN;
        this.internalDevTestEnabled = internalDevTestEnabled;
    }

    /**
     * Evaluate whether execution should proceed and return a safe boundary result.
     */
    public IcerikFabrikasiFfmpegExecutionBoundaryResult evaluate(
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy,
            String jobRef, String queueRef, String reviewRef) {
        // 1. Mode disabled
        if (mode == IcerikFabrikasiFfmpegRealExecutionMode.DISABLED)
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.denied(
                    IcerikFabrikasiFfmpegBoundaryReasonCode.MODE_DISABLED, mode, environment);

        // 2. Binary unavailable
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.denied(
                    IcerikFabrikasiFfmpegBoundaryReasonCode.BINARY_UNAVAILABLE, mode, environment);

        // 3. DRY_RUN mode
        if (mode == IcerikFabrikasiFfmpegRealExecutionMode.DRY_RUN)
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.safe(
                    IcerikFabrikasiFfmpegBoundaryResultStatus.DRY_RUN_COMPLETE,
                    IcerikFabrikasiFfmpegBoundaryReasonCode.DRY_RUN_SAFE,
                    mode, environment, jobRef, queueRef, reviewRef);

        // 4. SIMULATED mode
        if (mode == IcerikFabrikasiFfmpegRealExecutionMode.SIMULATED)
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.safe(
                    IcerikFabrikasiFfmpegBoundaryResultStatus.SIMULATED_COMPLETE,
                    IcerikFabrikasiFfmpegBoundaryReasonCode.SIMULATED_SAFE,
                    mode, environment, jobRef, queueRef, reviewRef);

        // 5. REAL_DEV_TEST_ONLY — check environment
        if (!environment.isRealExecutionAllowed())
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.denied(
                    IcerikFabrikasiFfmpegBoundaryReasonCode.ENVIRONMENT_DENIED, mode, environment);

        // 6. Explicit internal flag required
        if (!internalDevTestEnabled)
            return IcerikFabrikasiFfmpegExecutionBoundaryResult.denied(
                    IcerikFabrikasiFfmpegBoundaryReasonCode.INTERNAL_FLAG_NOT_SET, mode, environment);

        // 7. All gates passed — real dev/test execution allowed
        return IcerikFabrikasiFfmpegExecutionBoundaryResult.safe(
                IcerikFabrikasiFfmpegBoundaryResultStatus.EXECUTED_INTERNAL,
                IcerikFabrikasiFfmpegBoundaryReasonCode.EXECUTED_INTERNAL_SAFE,
                mode, environment, jobRef, queueRef, reviewRef);
    }

    public IcerikFabrikasiFfmpegRealExecutionMode getMode() { return mode; }
    public IcerikFabrikasiFfmpegExecutionEnvironment getEnvironment() { return environment; }
    public boolean isInternalDevTestEnabled() { return internalDevTestEnabled; }
}
