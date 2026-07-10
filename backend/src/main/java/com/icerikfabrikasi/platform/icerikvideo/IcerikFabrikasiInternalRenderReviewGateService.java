package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-026 Task 03: Review gate service.
 * Stateless. No persistence, no filesystem, no provider, no auto-approval.
 */
public class IcerikFabrikasiInternalRenderReviewGateService {

    private final IcerikFabrikasiInternalRenderReviewTransitionValidator transitionValidator;

    public IcerikFabrikasiInternalRenderReviewGateService() {
        this.transitionValidator = new IcerikFabrikasiInternalRenderReviewTransitionValidator();
    }

    public IcerikFabrikasiInternalRenderReviewGateService(IcerikFabrikasiInternalRenderReviewTransitionValidator validator) {
        this.transitionValidator = validator;
    }

    // ── Review Creation ──

    /**
     * Create initial review summary in REVIEW_NOT_REQUIRED state.
     */
    public IcerikFabrikasiInternalRenderReviewSummary createReview(String jobRef) {
        var reviewRef = IcerikFabrikasiInternalRenderReviewRef.create();
        return new IcerikFabrikasiInternalRenderReviewSummary(
                reviewRef.toOpaqueRef(), jobRef,
                IcerikFabrikasiInternalRenderReviewState.REVIEW_NOT_REQUIRED,
                IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_NOT_REQUIRED);
    }

    // ── Explicit Action Dispatcher ──

    /**
     * Apply explicit review action. No generic setStatus/updateStatus.
     */
    public IcerikFabrikasiInternalRenderReviewActionResult applyAction(
            IcerikFabrikasiInternalRenderReviewSummary current,
            IcerikFabrikasiInternalRenderReviewAction action) {

        if (current == null) {
            return IcerikFabrikasiInternalRenderReviewActionResult.denied(
                    null, action, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_REF_INVALID);
        }

        var transitionResult = transitionValidator.validate(current.getReviewState(), action);

        if (!transitionResult.isAllowed()) {
            return IcerikFabrikasiInternalRenderReviewActionResult.denied(
                    current, action, transitionResult.getReasonCode());
        }

        var updated = new IcerikFabrikasiInternalRenderReviewSummary(
                current.getReviewRef(), current.getJobRef(),
                transitionResult.getToState(), transitionResult.getReasonCode());

        return IcerikFabrikasiInternalRenderReviewActionResult.allowed(
                updated, action, transitionResult.getReasonCode());
    }

    // ── FP-025 Readiness Integration ──

    /**
     * Derive review readiness from FP-025 snapshots.
     */
    public IcerikFabrikasiInternalRenderReviewReadinessSnapshot reviewReadiness(
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain) {
        return IcerikFabrikasiInternalRenderReviewReadinessSnapshot.fromFP025(readiness, safetyChain);
    }
}
