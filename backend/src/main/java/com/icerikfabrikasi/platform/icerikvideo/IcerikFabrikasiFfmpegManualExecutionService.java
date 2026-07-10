package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-031 Task 01: Local-only manual execution service.
 * Validates full gate chain, delegates to IcerikFabrikasiFfmpegLocalDevProcessRunner.
 * No ProcessBuilder in THIS class.
 */
public class IcerikFabrikasiFfmpegManualExecutionService {

    private final IcerikFabrikasiFfmpegLocalDevProcessRunner processRunner;

    public IcerikFabrikasiFfmpegManualExecutionService(IcerikFabrikasiFfmpegLocalDevProcessRunner runner) {
        this.processRunner = runner != null ? runner : new IcerikFabrikasiFfmpegLocalDevProcessRunner();
    }

    public IcerikFabrikasiFfmpegManualExecutionService() {
        this(new IcerikFabrikasiFfmpegLocalDevProcessRunner());
    }

    public IcerikFabrikasiFfmpegManualExecutionResult execute(
            IcerikFabrikasiFfmpegManualExecutionRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy,
            IcerikFabrikasiFfmpegExecutionEnvironment environment) {

        // 1. Policy check
        if (request == null || request.getPolicy() != IcerikFabrikasiFfmpegManualExecutionPolicy.LOCAL_ONLY_MANUAL)
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.POLICY_DISABLED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.POLICY_NOT_LOCAL_MANUAL);

        // 2. Manual confirmation
        if (!request.isManualExecutionConfirmed())
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.MANUAL_CONFIRMATION_MISSING,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.MANUAL_NOT_CONFIRMED);

        // 3. Process enabled
        if (!request.isProcessEnabled())
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.PROCESS_DISABLED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.PROCESS_NOT_ENABLED);

        // 4. Environment must be LOCAL
        if (environment == null || environment != IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL)
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.ENVIRONMENT_NOT_LOCAL);

        // 5. Binary
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.BINARY_UNAVAILABLE,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.BINARY_NOT_AVAILABLE);

        // 6. FP-025 readiness
        if (readiness == null || readiness.getStatus() != IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY)
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.READINESS_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.READINESS_NOT_MET);

        // 7. FP-025 safety chain
        if (safetyChain == null || !safetyChain.isConfigValid() || !safetyChain.isWorkspaceSafe()
                || !safetyChain.isExecutionBoundarySafe() || !safetyChain.isArtifactVerified())
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.READINESS_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.READINESS_NOT_MET);

        // 8. FP-026 review
        if (reviewState != IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP)
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.REVIEW_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.REVIEW_NOT_APPROVED);

        // 9. Format validation
        if (!request.isFormatValid())
            return IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.GATE_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.GATE_DENIED_MODE);

        // 10. Runner disabled → fake skip
        if (!processRunner.isProcessEnabled())
            return IcerikFabrikasiFfmpegManualExecutionResult.skipped();

        // 11. Delegate to process runner
        var processReq = new IcerikFabrikasiFfmpegProcessRequest(request.getJobRef(), request.getFormat(),
                request.getTimeoutSeconds(), request.getDurationSeconds(), request.getFps(),
                request.getWidth(), request.getHeight(), request.getMaxOutputSizeMB());
        var processResult = processRunner.execute(processReq, binaryPolicy);

        // 12. Map result
        return switch (processResult.getStatus()) {
            case PROCESS_COMPLETED_SAFE -> {
                var outputRef = processResult.getOutputRef();
                var metadata = new IcerikFabrikasiFfmpegFixtureMetadata(outputRef, request.getFormat(),
                        request.getDurationSeconds(), request.getFps(),
                        request.getWidth(), request.getHeight(), request.getMaxOutputSizeMB());
                if (metadata.isBoundsValid())
                    yield IcerikFabrikasiFfmpegManualExecutionResult.verificationPassed(outputRef, metadata);
                else
                    yield IcerikFabrikasiFfmpegManualExecutionResult.verificationFailed();
            }
            case GATE_DENIED -> IcerikFabrikasiFfmpegManualExecutionResult.denied(
                    IcerikFabrikasiFfmpegManualExecutionStatus.GATE_DENIED,
                    IcerikFabrikasiFfmpegManualExecutionReasonCode.GATE_DENIED_MODE);
            case PROCESS_SKIPPED -> IcerikFabrikasiFfmpegManualExecutionResult.skipped();
            default -> IcerikFabrikasiFfmpegManualExecutionResult.failedSafe();
        };
    }
}
