package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Queue transition result.
 */
public final class IcerikFabrikasiInternalRenderQueueTransitionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderQueueState fromState;
    private final IcerikFabrikasiInternalRenderQueueAction action;
    private final IcerikFabrikasiInternalRenderQueueState toState;
    private final IcerikFabrikasiInternalRenderQueueReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderQueueTransitionResult(boolean allowed,
            IcerikFabrikasiInternalRenderQueueState from, IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueState to, IcerikFabrikasiInternalRenderQueueReasonCode reason) {
        this.allowed = allowed; this.fromState = from; this.action = action;
        this.toState = to; this.reasonCode = reason;
    }

    public static IcerikFabrikasiInternalRenderQueueTransitionResult allowed(
            IcerikFabrikasiInternalRenderQueueState from, IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueState to, IcerikFabrikasiInternalRenderQueueReasonCode reason) {
        return new IcerikFabrikasiInternalRenderQueueTransitionResult(true, from, action, to, reason);
    }

    public static IcerikFabrikasiInternalRenderQueueTransitionResult denied(
            IcerikFabrikasiInternalRenderQueueState from, IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueReasonCode reason) {
        return new IcerikFabrikasiInternalRenderQueueTransitionResult(false, from, action, null, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderQueueState getFromState() { return fromState; }
    public IcerikFabrikasiInternalRenderQueueAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderQueueState getToState() { return toState; }
    public IcerikFabrikasiInternalRenderQueueReasonCode getReasonCode() { return reasonCode; }
}
