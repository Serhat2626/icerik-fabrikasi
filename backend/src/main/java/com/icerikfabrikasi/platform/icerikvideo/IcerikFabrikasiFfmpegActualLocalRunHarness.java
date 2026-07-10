package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-031 Task 02: Actual local smoke run harness.
 * Wraps ManualExecutionService with real-local-run status mapping.
 * No ProcessBuilder in THIS class.
 */
public class IcerikFabrikasiFfmpegActualLocalRunHarness {

    private final IcerikFabrikasiFfmpegManualExecutionService manualService;

    public IcerikFabrikasiFfmpegActualLocalRunHarness(IcerikFabrikasiFfmpegManualExecutionService service) {
        this.manualService = service != null ? service : new IcerikFabrikasiFfmpegManualExecutionService();
    }

    public IcerikFabrikasiFfmpegActualLocalRunHarness() {
        this(new IcerikFabrikasiFfmpegManualExecutionService());
    }

    /**
     * Attempt actual local smoke run. Maps result to real-local-run statuses.
     */
    public IcerikFabrikasiFfmpegManualExecutionResult attempt(
            IcerikFabrikasiFfmpegManualExecutionRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy,
            IcerikFabrikasiFfmpegExecutionEnvironment environment) {

        // Delegate to manual execution service
        var result = manualService.execute(request, readiness, safetyChain,
                reviewState, binaryPolicy, environment);

        // Map to real-local-run statuses
        return switch (result.getStatus()) {
            case EXECUTION_COMPLETED_SAFE, VERIFICATION_PASSED ->
                IcerikFabrikasiFfmpegManualExecutionResult.denied(
                        IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_SUCCEEDED,
                        IcerikFabrikasiFfmpegManualExecutionReasonCode.REAL_LOCAL_SUCCEEDED_SAFE);
            case EXECUTION_FAILED_SAFE ->
                IcerikFabrikasiFfmpegManualExecutionResult.denied(
                        IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_FAILED_SAFE,
                        IcerikFabrikasiFfmpegManualExecutionReasonCode.REAL_LOCAL_FAILED_SAFE);
            case BINARY_UNAVAILABLE ->
                IcerikFabrikasiFfmpegManualExecutionResult.denied(
                        IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_SKIPPED_UNAVAILABLE,
                        IcerikFabrikasiFfmpegManualExecutionReasonCode.REAL_LOCAL_SKIPPED_BINARY_UNAVAILABLE);
            case EXECUTION_SKIPPED_FAKE ->
                IcerikFabrikasiFfmpegManualExecutionResult.denied(
                        IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_ATTEMPTED,
                        IcerikFabrikasiFfmpegManualExecutionReasonCode.REAL_LOCAL_ATTEMPTED_SAFE);
            default -> result; // pass through denials unchanged
        };
    }
}
