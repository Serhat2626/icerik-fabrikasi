package com.icerikfabrikasi.platform.icerikvideo;

import java.util.Map;
import java.util.Set;

import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderJobStatus.*;
import static com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiInternalRenderJobAction.*;

/**
 * FP-025 Task 02: Transition validator.
 * Explicit matrix. Deny-by-default. No generic status update.
 */
public class IcerikFabrikasiInternalRenderJobTransitionValidator {

    // Explicit transition matrix: (from, action) -> to
    private static final Map<TransitionKey, IcerikFabrikasiInternalRenderJobStatus> ALLOWED = Map.ofEntries(
            // Normal forward flow
            Map.entry(key(DRAFT, VALIDATE_FOR_INTERNAL_RENDER), READY_FOR_INTERNAL_RENDER),
            Map.entry(key(READY_FOR_INTERNAL_RENDER, ENQUEUE_INTERNAL_RENDER), QUEUED_INTERNAL),
            Map.entry(key(QUEUED_INTERNAL, START_INTERNAL_RENDER), RUNNING_INTERNAL),
            Map.entry(key(RUNNING_INTERNAL, COMPLETE_INTERNAL_RENDER), SUCCEEDED_INTERNAL),
            Map.entry(key(RUNNING_INTERNAL, FAIL_INTERNAL_RENDER), FAILED_INTERNAL),
            // Cancel from any non-terminal active state
            Map.entry(key(DRAFT, CANCEL_INTERNAL_RENDER), CANCELLED_INTERNAL),
            Map.entry(key(READY_FOR_INTERNAL_RENDER, CANCEL_INTERNAL_RENDER), CANCELLED_INTERNAL),
            Map.entry(key(QUEUED_INTERNAL, CANCEL_INTERNAL_RENDER), CANCELLED_INTERNAL),
            Map.entry(key(RUNNING_INTERNAL, CANCEL_INTERNAL_RENDER), CANCELLED_INTERNAL),
            // Expire from any non-terminal state
            Map.entry(key(DRAFT, EXPIRE_INTERNAL_RENDER), EXPIRED_INTERNAL),
            Map.entry(key(READY_FOR_INTERNAL_RENDER, EXPIRE_INTERNAL_RENDER), EXPIRED_INTERNAL),
            Map.entry(key(QUEUED_INTERNAL, EXPIRE_INTERNAL_RENDER), EXPIRED_INTERNAL),
            Map.entry(key(RUNNING_INTERNAL, EXPIRE_INTERNAL_RENDER), EXPIRED_INTERNAL),
            Map.entry(key(SUCCEEDED_INTERNAL, EXPIRE_INTERNAL_RENDER), EXPIRED_INTERNAL)
    );

    /**
     * Validate a transition. Deny-by-default.
     */
    public IcerikFabrikasiInternalRenderJobTransitionResult validate(
            IcerikFabrikasiInternalRenderJobStatus currentState,
            IcerikFabrikasiInternalRenderJobAction action) {

        if (currentState == null) {
            return IcerikFabrikasiInternalRenderJobTransitionResult.denied(
                    null, action, IcerikFabrikasiInternalRenderJobReasonCode.UNKNOWN_CURRENT_STATE);
        }
        if (action == null) {
            return IcerikFabrikasiInternalRenderJobTransitionResult.denied(
                    currentState, null, IcerikFabrikasiInternalRenderJobReasonCode.UNKNOWN_ACTION);
        }

        // Terminal states deny all transitions except EXPIRE from SUCCEEDED
        if (currentState.isTerminal()) {
            return IcerikFabrikasiInternalRenderJobTransitionResult.denied(
                    currentState, action, IcerikFabrikasiInternalRenderJobReasonCode.TERMINAL_STATE);
        }

        var key = key(currentState, action);
        var target = ALLOWED.get(key);
        if (target != null) {
            return IcerikFabrikasiInternalRenderJobTransitionResult.allowed(currentState, action, target);
        }

        return IcerikFabrikasiInternalRenderJobTransitionResult.denied(
                currentState, action, IcerikFabrikasiInternalRenderJobReasonCode.TRANSITION_NOT_ALLOWED);
    }

    /**
     * Get all allowed transitions for reference/testing.
     */
    public Set<TransitionKey> getAllowedTransitions() {
        return ALLOWED.keySet();
    }

    public int getAllowedTransitionCount() {
        return ALLOWED.size();
    }

    // ── Internal key ──

    private static TransitionKey key(IcerikFabrikasiInternalRenderJobStatus from,
                                      IcerikFabrikasiInternalRenderJobAction action) {
        return new TransitionKey(from, action);
    }

    public record TransitionKey(IcerikFabrikasiInternalRenderJobStatus from,
                                 IcerikFabrikasiInternalRenderJobAction action) {}
}
