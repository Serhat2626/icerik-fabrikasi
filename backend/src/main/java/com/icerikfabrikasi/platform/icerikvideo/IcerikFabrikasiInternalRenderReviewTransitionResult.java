package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-026 Task 02: Review transition result.
 * Immutable, safe — no raw exception messages.
 */
public final class IcerikFabrikasiInternalRenderReviewTransitionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderReviewState fromState;
    private final IcerikFabrikasiInternalRenderReviewAction action;
    private final IcerikFabrikasiInternalRenderReviewState toState;
    private final IcerikFabrikasiInternalRenderReviewReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderReviewTransitionResult(boolean allowed,
            IcerikFabrikasiInternalRenderReviewState fromState, IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewState toState, IcerikFabrikasiInternalRenderReviewReasonCode reasonCode) {
        this.allowed = allowed;
        this.fromState = fromState;
        this.action = action;
        this.toState = toState;
        this.reasonCode = reasonCode;
    }

    public static IcerikFabrikasiInternalRenderReviewTransitionResult allowed(
            IcerikFabrikasiInternalRenderReviewState from, IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewState to, IcerikFabrikasiInternalRenderReviewReasonCode reason) {
        return new IcerikFabrikasiInternalRenderReviewTransitionResult(true, from, action, to, reason);
    }

    public static IcerikFabrikasiInternalRenderReviewTransitionResult denied(
            IcerikFabrikasiInternalRenderReviewState from, IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewReasonCode reason) {
        return new IcerikFabrikasiInternalRenderReviewTransitionResult(false, from, action, null, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderReviewState getFromState() { return fromState; }
    public IcerikFabrikasiInternalRenderReviewAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderReviewState getToState() { return toState; }
    public IcerikFabrikasiInternalRenderReviewReasonCode getReasonCode() { return reasonCode; }
}
