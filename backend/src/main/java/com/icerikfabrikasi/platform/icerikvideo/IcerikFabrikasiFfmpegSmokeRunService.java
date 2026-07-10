package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-030 Task 02: First real smoke run service.
 * Validates full chain → delegates to IcerikFabrikasiFfmpegLocalDevProcessRunner.
 * No ProcessBuilder in THIS class.
 */
public class IcerikFabrikasiFfmpegSmokeRunService {

    private final IcerikFabrikasiFfmpegLocalDevProcessRunner processRunner;

    public IcerikFabrikasiFfmpegSmokeRunService(IcerikFabrikasiFfmpegLocalDevProcessRunner processRunner) {
        this.processRunner = processRunner != null ? processRunner : new IcerikFabrikasiFfmpegLocalDevProcessRunner();
    }

    public IcerikFabrikasiFfmpegSmokeRunService() {
        this(new IcerikFabrikasiFfmpegLocalDevProcessRunner());
    }

    /**
     * Run first real smoke path with full chain validation.
     */
    public IcerikFabrikasiFfmpegSmokeRunResult run(
            IcerikFabrikasiFfmpegProcessRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. FP-025 readiness
        if (readiness == null || readiness.getStatus() != IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY)
            return IcerikFabrikasiFfmpegSmokeRunResult.denied(IcerikFabrikasiFfmpegSmokeRunStatus.READINESS_DENIED);

        // 2. FP-025 safety chain
        if (safetyChain == null || !safetyChain.isConfigValid() || !safetyChain.isWorkspaceSafe()
                || !safetyChain.isExecutionBoundarySafe() || !safetyChain.isArtifactVerified())
            return IcerikFabrikasiFfmpegSmokeRunResult.denied(IcerikFabrikasiFfmpegSmokeRunStatus.READINESS_DENIED);

        // 3. FP-026 review
        if (reviewState != IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP)
            return IcerikFabrikasiFfmpegSmokeRunResult.denied(IcerikFabrikasiFfmpegSmokeRunStatus.REVIEW_DENIED);

        // 4. Request validation
        if (request == null || !request.isValid())
            return IcerikFabrikasiFfmpegSmokeRunResult.denied(IcerikFabrikasiFfmpegSmokeRunStatus.PROCESS_DENIED);

        // 5. Binary check
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiFfmpegSmokeRunResult.skippedUnavailable();

        // 6. Process runner disabled?
        if (!processRunner.isProcessEnabled())
            return IcerikFabrikasiFfmpegSmokeRunResult.skippedDisabled();

        // 7. Delegate to process runner (gate inside)
        var processResult = processRunner.execute(request, binaryPolicy);

        // 8. Map process result
        return switch (processResult.getStatus()) {
            case PROCESS_COMPLETED_SAFE -> {
                var candidate = new IcerikFabrikasiFfmpegVerificationCandidate(
                        processResult.getOutputRef(), request.getJobRef(), request.getFormat(),
                        request.getDurationSeconds(), request.getFps(),
                        request.getWidth(), request.getHeight(), request.getMaxOutputSizeMB());
                yield IcerikFabrikasiFfmpegSmokeRunResult.completed(processResult.getOutputRef(),
                        candidate, processResult.getDurationMs());
            }
            case GATE_DENIED -> IcerikFabrikasiFfmpegSmokeRunResult.denied(IcerikFabrikasiFfmpegSmokeRunStatus.GATE_DENIED);
            case PROCESS_SKIPPED -> IcerikFabrikasiFfmpegSmokeRunResult.skippedDisabled();
            case PROCESS_TIMED_OUT, PROCESS_FAILED_SAFE -> IcerikFabrikasiFfmpegSmokeRunResult.failedSafe();
            default -> IcerikFabrikasiFfmpegSmokeRunResult.failedSafe();
        };
    }
}
