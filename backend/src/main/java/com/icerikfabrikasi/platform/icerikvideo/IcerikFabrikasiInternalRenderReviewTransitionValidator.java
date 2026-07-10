package com.icerikfabrikasi.platform.icerikvideo;

import java.util.Map;
import java.util.Set;

import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderReviewState.*;
import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderReviewAction.*;

/**
 * FP-026 Task 02: Review transition validator.
 * Explicit matrix. Deny-by-default. No auto-approve. No generic status update.
 */
public class IcerikFabrikasiInternalRenderReviewTransitionValidator {

    private record TransitionKey(IcerikFabrikasiInternalRenderReviewState from,
                                  IcerikFabrikasiInternalRenderReviewAction action) {}

    private record TransitionTarget(IcerikFabrikasiInternalRenderReviewState to,
                                     IcerikFabrikasiInternalRenderReviewReasonCode reason) {}

    private static final Map<TransitionKey, TransitionTarget> ALLOWED = Map.ofEntries(
            // Require review
            Map.entry(new TransitionKey(REVIEW_NOT_REQUIRED, REQUIRE_HUMAN_REVIEW),
                    new TransitionTarget(REVIEW_REQUIRED, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_REQUIRED)),
            // Mark ready
            Map.entry(new TransitionKey(REVIEW_REQUIRED, MARK_READY_FOR_REVIEW),
                    new TransitionTarget(READY_FOR_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.READY_FOR_REVIEW)),
            // Start review
            Map.entry(new TransitionKey(READY_FOR_REVIEW, START_HUMAN_REVIEW),
                    new TransitionTarget(IN_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_STARTED)),
            // Approve (internal only)
            Map.entry(new TransitionKey(IN_REVIEW, APPROVE_FOR_INTERNAL_NEXT_STEP),
                    new TransitionTarget(APPROVED_FOR_INTERNAL_NEXT_STEP, IcerikFabrikasiInternalRenderReviewReasonCode.APPROVED_INTERNAL_ONLY)),
            // Reject
            Map.entry(new TransitionKey(IN_REVIEW, REJECT_HUMAN_REVIEW),
                    new TransitionTarget(REJECTED_BY_HUMAN_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.REJECTED_BY_REVIEWER)),
            // Block for safety from multiple states
            Map.entry(new TransitionKey(REVIEW_REQUIRED, BLOCK_FOR_SAFETY),
                    new TransitionTarget(BLOCKED_BY_SAFETY, IcerikFabrikasiInternalRenderReviewReasonCode.BLOCKED_BY_SAFETY)),
            Map.entry(new TransitionKey(READY_FOR_REVIEW, BLOCK_FOR_SAFETY),
                    new TransitionTarget(BLOCKED_BY_SAFETY, IcerikFabrikasiInternalRenderReviewReasonCode.BLOCKED_BY_SAFETY)),
            Map.entry(new TransitionKey(IN_REVIEW, BLOCK_FOR_SAFETY),
                    new TransitionTarget(BLOCKED_BY_SAFETY, IcerikFabrikasiInternalRenderReviewReasonCode.BLOCKED_BY_SAFETY)),
            // Expire from multiple states
            Map.entry(new TransitionKey(REVIEW_REQUIRED, EXPIRE_HUMAN_REVIEW),
                    new TransitionTarget(EXPIRED_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_EXPIRED)),
            Map.entry(new TransitionKey(READY_FOR_REVIEW, EXPIRE_HUMAN_REVIEW),
                    new TransitionTarget(EXPIRED_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_EXPIRED)),
            Map.entry(new TransitionKey(IN_REVIEW, EXPIRE_HUMAN_REVIEW),
                    new TransitionTarget(EXPIRED_REVIEW, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_EXPIRED))
    );

    /**
     * Validate a review transition. Deny-by-default.
     */
    public IcerikFabrikasiInternalRenderReviewTransitionResult validate(
            IcerikFabrikasiInternalRenderReviewState currentState,
            IcerikFabrikasiInternalRenderReviewAction action) {

        if (currentState == null) {
            return IcerikFabrikasiInternalRenderReviewTransitionResult.denied(
                    null, action, IcerikFabrikasiInternalRenderReviewReasonCode.REVIEW_REF_INVALID);
        }
        if (action == null) {
            return IcerikFabrikasiInternalRenderReviewTransitionResult.denied(
                    currentState, null, IcerikFabrikasiInternalRenderReviewReasonCode.TRANSITION_NOT_ALLOWED);
        }

        // Terminal states deny all transitions
        if (currentState.isTerminal()) {
            return IcerikFabrikasiInternalRenderReviewTransitionResult.denied(
                    currentState, action, IcerikFabrikasiInternalRenderReviewReasonCode.TERMINAL_STATE);
        }

        var key = new TransitionKey(currentState, action);
        var target = ALLOWED.get(key);
        if (target != null) {
            return IcerikFabrikasiInternalRenderReviewTransitionResult.allowed(
                    currentState, action, target.to(), target.reason());
        }

        return IcerikFabrikasiInternalRenderReviewTransitionResult.denied(
                currentState, action, IcerikFabrikasiInternalRenderReviewReasonCode.TRANSITION_NOT_ALLOWED);
    }

    public int getAllowedTransitionCount() {
        return ALLOWED.size();
    }

    public Set<TransitionKey> getAllowedTransitions() {
        return ALLOWED.keySet();
    }
}
