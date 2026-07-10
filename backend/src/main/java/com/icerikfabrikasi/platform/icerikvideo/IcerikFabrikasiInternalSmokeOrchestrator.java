package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-029 Task 01: E2E smoke orchestrator.
 * Validates FP-025 readiness, FP-026 review, FP-027 dispatch, FP-028 gate.
 * No ProcessBuilder. Plan/candidate only.
 */
public class IcerikFabrikasiInternalSmokeOrchestrator {

    private final IcerikFabrikasiFfmpegExecutionGate ffmpegGate;

    public IcerikFabrikasiInternalSmokeOrchestrator(IcerikFabrikasiFfmpegExecutionGate gate) {
        this.ffmpegGate = gate != null ? gate : new IcerikFabrikasiFfmpegExecutionGate();
    }

    public IcerikFabrikasiInternalSmokeOrchestrator() {
        this(new IcerikFabrikasiFfmpegExecutionGate());
    }

    /**
     * Execute internal smoke path with full safety chain validation.
     */
    public IcerikFabrikasiInternalSmokePathResult execute(
            IcerikFabrikasiInternalSmokeRequest request,
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. FP-025 readiness
        if (readiness == null || readiness.getStatus() != IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY)
            return IcerikFabrikasiInternalSmokePathResult.denied(IcerikFabrikasiInternalSmokePathReasonCode.READINESS_NOT_MET);

        // 2. FP-025 safety chain
        if (safetyChain == null || !safetyChain.isConfigValid() || !safetyChain.isWorkspaceSafe()
                || !safetyChain.isExecutionBoundarySafe() || !safetyChain.isArtifactVerified())
            return IcerikFabrikasiInternalSmokePathResult.denied(IcerikFabrikasiInternalSmokePathReasonCode.READINESS_NOT_MET);

        // 3. FP-026 human review
        if (reviewState != IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP)
            return IcerikFabrikasiInternalSmokePathResult.denied(IcerikFabrikasiInternalSmokePathReasonCode.REVIEW_NOT_APPROVED);

        // 4. FP-028 FFmpeg gate
        var gateResult = ffmpegGate.evaluate(binaryPolicy,
                request.getJobRef(), request.getQueueItemRef(), request.getReviewRef());
        if (gateResult.getStatus() == IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED)
            return IcerikFabrikasiInternalSmokePathResult.denied(IcerikFabrikasiInternalSmokePathReasonCode.GATE_DENIED);

        // 5. Build plan
        var plan = new IcerikFabrikasiInternalSmokeRenderPlan(
                request.getFormat(), request.getDurationSeconds(), request.getFps(),
                request.getWidth(), request.getHeight(), 500,
                request.getSceneCount(), request.getAssetCount());
        if (!plan.isBoundsValid())
            return IcerikFabrikasiInternalSmokePathResult.denied(IcerikFabrikasiInternalSmokePathReasonCode.BOUNDS_INVALID);

        // 6. Create candidate with opaque output ref
        var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create();
        return IcerikFabrikasiInternalSmokePathResult.candidateCreated(outputRef.toOpaqueRef(), plan);
    }
}
