package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-029 Task 02: Internal generation trial service.
 * Validates full safety chain → produces simulated/attempted trial result.
 * No ProcessBuilder. Real execution delegated to FP-028 DevTestAdapter.
 */
public class IcerikFabrikasiInternalGenerationTrialService {

    private final IcerikFabrikasiFfmpegDevTestAdapter adapter;

    public IcerikFabrikasiInternalGenerationTrialService(IcerikFabrikasiFfmpegDevTestAdapter adapter) {
        this.adapter = adapter != null ? adapter : new IcerikFabrikasiFfmpegDevTestAdapter();
    }

    public IcerikFabrikasiInternalGenerationTrialService() {
        this(new IcerikFabrikasiFfmpegDevTestAdapter());
    }

    /**
     * Run internal generation trial with full FP-025/026/028 chain validation.
     */
    public IcerikFabrikasiInternalTrialResult runTrial(
            IcerikFabrikasiInternalSmokePathResult smokeResult,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Smoke path must have produced a candidate
        if (smokeResult == null || smokeResult.getStatus() != IcerikFabrikasiInternalSmokePathStatus.CANDIDATE_CREATED)
            return IcerikFabrikasiInternalTrialResult.denied(IcerikFabrikasiInternalTrialReasonCode.NOT_ATTEMPTED);

        // 2. FP-025 readiness
        if (readiness == null || readiness.getStatus() != IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY)
            return IcerikFabrikasiInternalTrialResult.denied(IcerikFabrikasiInternalTrialReasonCode.READINESS_NOT_MET);

        // 3. FP-026 review
        if (reviewState != IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP)
            return IcerikFabrikasiInternalTrialResult.denied(IcerikFabrikasiInternalTrialReasonCode.REVIEW_NOT_APPROVED);

        // 4. Plan bounds
        var plan = smokeResult.getPlan();
        if (plan == null || !plan.isBoundsValid())
            return IcerikFabrikasiInternalTrialResult.planInvalid();

        // 5. Delegate to FP-028 adapter (gate inside)
        var adapterResult = adapter.execute(binaryPolicy,
                "trial-job", null, null);

        if (!adapterResult.isAllowed())
            return IcerikFabrikasiInternalTrialResult.denied(IcerikFabrikasiInternalTrialReasonCode.GATE_DENIED_MODE);

        // 6. Create verification candidate
        var outputRef = adapterResult.getOutputRef();
        var candidate = new IcerikFabrikasiFfmpegVerificationCandidate(
                outputRef, "trial-job", plan.getFormat(),
                plan.getDurationSeconds(), plan.getFps(),
                plan.getWidth(), plan.getHeight(), plan.getEstimatedSizeMB());

        // 7. Determine execution type from gate mode
        var gateMode = adapterResult.getGateResult().getMode();
        if (gateMode == IcerikFabrikasiFfmpegRealExecutionMode.DRY_RUN
                || gateMode == IcerikFabrikasiFfmpegRealExecutionMode.SIMULATED)
            return IcerikFabrikasiInternalTrialResult.simulated(outputRef, candidate);

        // REAL_DEV_TEST_ONLY — trial attempted (no actual ProcessBuilder in this class)
        return IcerikFabrikasiInternalTrialResult.attempted(outputRef, candidate);
    }
}
