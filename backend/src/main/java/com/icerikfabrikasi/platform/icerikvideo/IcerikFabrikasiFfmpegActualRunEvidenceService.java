package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-032: Actual local smoke execution evidence service.
 * Coordinates availability check → manual execution → evidence result.
 * No ProcessBuilder in THIS class (delegates to checker and runner).
 */
public class IcerikFabrikasiFfmpegActualRunEvidenceService {

    private final IcerikFabrikasiFfmpegLocalAvailabilityChecker checker;
    private final IcerikFabrikasiFfmpegManualExecutionService manualService;

    public IcerikFabrikasiFfmpegActualRunEvidenceService(
            IcerikFabrikasiFfmpegLocalAvailabilityChecker checker,
            IcerikFabrikasiFfmpegManualExecutionService manualService) {
        this.checker = checker != null ? checker : new IcerikFabrikasiFfmpegLocalAvailabilityChecker();
        this.manualService = manualService != null ? manualService : new IcerikFabrikasiFfmpegManualExecutionService();
    }

    public IcerikFabrikasiFfmpegActualRunEvidenceService() {
        this(new IcerikFabrikasiFfmpegLocalAvailabilityChecker(), new IcerikFabrikasiFfmpegManualExecutionService());
    }

    /**
     * Attempt actual local smoke run with evidence.
     */
    public IcerikFabrikasiFfmpegActualRunEvidenceResult attempt(
            IcerikFabrikasiFfmpegManualExecutionRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy,
            IcerikFabrikasiFfmpegExecutionEnvironment environment) {

        // 1. Check availability
        var availability = checker.check();
        return switch (availability) {
            case DISABLED -> IcerikFabrikasiFfmpegActualRunEvidenceResult.disabled();
            case UNSUPPORTED_ENVIRONMENT -> IcerikFabrikasiFfmpegActualRunEvidenceResult.unsupportedEnv();
            case UNAVAILABLE -> IcerikFabrikasiFfmpegActualRunEvidenceResult.unavailable();
            case AVAILABLE -> executeWithEvidence(request, readiness, safetyChain,
                    reviewState, binaryPolicy, environment);
        };
    }

    private IcerikFabrikasiFfmpegActualRunEvidenceResult executeWithEvidence(
            IcerikFabrikasiFfmpegManualExecutionRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy,
            IcerikFabrikasiFfmpegExecutionEnvironment environment) {

        var result = manualService.execute(request, readiness, safetyChain,
                reviewState, binaryPolicy, environment);

        return switch (result.getStatus()) {
            case EXECUTION_COMPLETED_SAFE, VERIFICATION_PASSED -> {
                var metadata = result.getFixtureMetadata();
                yield IcerikFabrikasiFfmpegActualRunEvidenceResult.succeeded(
                        result.getOutputRef(), metadata, 0);
            }
            case EXECUTION_SKIPPED_FAKE ->
                IcerikFabrikasiFfmpegActualRunEvidenceResult.disabled();
            case EXECUTION_FAILED_SAFE ->
                IcerikFabrikasiFfmpegActualRunEvidenceResult.failedSafe();
            case BINARY_UNAVAILABLE ->
                IcerikFabrikasiFfmpegActualRunEvidenceResult.unavailable();
            case GATE_DENIED, ENVIRONMENT_DENIED, READINESS_DENIED, REVIEW_DENIED ->
                IcerikFabrikasiFfmpegActualRunEvidenceResult.gateDenied();
            default -> IcerikFabrikasiFfmpegActualRunEvidenceResult.gateDenied();
        };
    }
}
