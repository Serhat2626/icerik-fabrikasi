package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-027 Task 02: Internal render queue service.
 * Stateless. No DB, no filesystem, no provider, no auto-approval.
 */
public class IcerikFabrikasiInternalRenderQueueService {

    private final IcerikFabrikasiInternalRenderQueueTransitionValidator transitionValidator;

    public IcerikFabrikasiInternalRenderQueueService() {
        this.transitionValidator = new IcerikFabrikasiInternalRenderQueueTransitionValidator();
    }

    public IcerikFabrikasiInternalRenderQueueService(IcerikFabrikasiInternalRenderQueueTransitionValidator validator) {
        this.transitionValidator = validator;
    }

    /**
     * Create initial queue summary in NOT_QUEUED state.
     */
    public IcerikFabrikasiInternalRenderQueueSummary createQueueItem(String jobRef, String reviewRef) {
        var ref = IcerikFabrikasiInternalRenderQueueItemRef.create();
        return new IcerikFabrikasiInternalRenderQueueSummary(
                ref.toOpaqueRef(), jobRef, reviewRef,
                IcerikFabrikasiInternalRenderQueueState.NOT_QUEUED,
                IcerikFabrikasiInternalRenderQueueReasonCode.NOT_QUEUED,
                0, 3, false, false);
    }

    /**
     * Apply explicit queue action. No generic setStatus/updateStatus.
     */
    public IcerikFabrikasiInternalRenderQueueActionResult applyAction(
            IcerikFabrikasiInternalRenderQueueSummary current, IcerikFabrikasiInternalRenderQueueAction action) {
        if (current == null)
            return IcerikFabrikasiInternalRenderQueueActionResult.denied(null, action,
                    IcerikFabrikasiInternalRenderQueueReasonCode.QUEUE_REF_INVALID);

        var result = transitionValidator.validate(current.getQueueState(), action);
        if (!result.isAllowed())
            return IcerikFabrikasiInternalRenderQueueActionResult.denied(current, action, result.getReasonCode());

        int attempt = current.getAttemptNumber();
        if (action == IcerikFabrikasiInternalRenderQueueAction.MARK_RETRY_WAIT) attempt++;

        var updated = new IcerikFabrikasiInternalRenderQueueSummary(
                current.getQueueItemRef(), current.getJobRef(), current.getReviewRef(),
                result.getToState(), result.getReasonCode(),
                attempt, current.getMaxAttempts(),
                attempt < current.getMaxAttempts()
                        && result.getToState() != IcerikFabrikasiInternalRenderQueueState.FAILED_INTERNAL,
                result.getToState() == IcerikFabrikasiInternalRenderQueueState.EXPIRED_INTERNAL);
        return IcerikFabrikasiInternalRenderQueueActionResult.allowed(updated, action, result.getReasonCode());
    }

    /**
     * Check dispatch readiness using FP-025 + FP-026 snapshots.
     */
    public boolean isDispatchReady(
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain,
            IcerikFabrikasiInternalRenderReviewState reviewState) {
        boolean jobReady = readiness != null
                && readiness.getStatus() == IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY;
        boolean chainSafe = safetyChain != null
                && safetyChain.isConfigValid()
                && safetyChain.isWorkspaceSafe()
                && safetyChain.isExecutionBoundarySafe()
                && safetyChain.isArtifactVerified();
        boolean reviewApproved = reviewState == IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP;
        return jobReady && chainSafe && reviewApproved;
    }
}
