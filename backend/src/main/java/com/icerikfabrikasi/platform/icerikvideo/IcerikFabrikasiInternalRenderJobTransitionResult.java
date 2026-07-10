package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-025 Task 02: Transition decision result.
 * Immutable, safe — no raw exception messages.
 */
public final class IcerikFabrikasiInternalRenderJobTransitionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderJobStatus fromState;
    private final IcerikFabrikasiInternalRenderJobAction action;
    private final IcerikFabrikasiInternalRenderJobStatus toState;
    private final IcerikFabrikasiInternalRenderJobReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderJobTransitionResult(boolean allowed,
            IcerikFabrikasiInternalRenderJobStatus fromState, IcerikFabrikasiInternalRenderJobAction action,
            IcerikFabrikasiInternalRenderJobStatus toState, IcerikFabrikasiInternalRenderJobReasonCode reasonCode) {
        this.allowed = allowed;
        this.fromState = fromState;
        this.action = action;
        this.toState = toState;
        this.reasonCode = reasonCode;
    }

    public static IcerikFabrikasiInternalRenderJobTransitionResult allowed(
            IcerikFabrikasiInternalRenderJobStatus from, IcerikFabrikasiInternalRenderJobAction action,
            IcerikFabrikasiInternalRenderJobStatus to) {
        return new IcerikFabrikasiInternalRenderJobTransitionResult(
                true, from, action, to, IcerikFabrikasiInternalRenderJobReasonCode.TRANSITION_ALLOWED);
    }

    public static IcerikFabrikasiInternalRenderJobTransitionResult denied(
            IcerikFabrikasiInternalRenderJobStatus from, IcerikFabrikasiInternalRenderJobAction action,
            IcerikFabrikasiInternalRenderJobReasonCode reason) {
        return new IcerikFabrikasiInternalRenderJobTransitionResult(false, from, action, null, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderJobStatus getFromState() { return fromState; }
    public IcerikFabrikasiInternalRenderJobAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderJobStatus getToState() { return toState; }
    public IcerikFabrikasiInternalRenderJobReasonCode getReasonCode() { return reasonCode; }
}
