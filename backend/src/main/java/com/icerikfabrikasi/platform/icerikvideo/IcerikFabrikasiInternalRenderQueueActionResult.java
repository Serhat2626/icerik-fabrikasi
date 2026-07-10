package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Queue action result wrapper.
 */
public final class IcerikFabrikasiInternalRenderQueueActionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderQueueSummary summary;
    private final IcerikFabrikasiInternalRenderQueueAction action;
    private final IcerikFabrikasiInternalRenderQueueReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderQueueActionResult(boolean allowed,
            IcerikFabrikasiInternalRenderQueueSummary summary,
            IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueReasonCode reasonCode) {
        this.allowed = allowed; this.summary = summary;
        this.action = action; this.reasonCode = reasonCode;
    }

    public static IcerikFabrikasiInternalRenderQueueActionResult allowed(
            IcerikFabrikasiInternalRenderQueueSummary updated,
            IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueReasonCode reason) {
        return new IcerikFabrikasiInternalRenderQueueActionResult(true, updated, action, reason);
    }

    public static IcerikFabrikasiInternalRenderQueueActionResult denied(
            IcerikFabrikasiInternalRenderQueueSummary current,
            IcerikFabrikasiInternalRenderQueueAction action,
            IcerikFabrikasiInternalRenderQueueReasonCode reason) {
        return new IcerikFabrikasiInternalRenderQueueActionResult(false, current, action, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderQueueSummary getSummary() { return summary; }
    public IcerikFabrikasiInternalRenderQueueAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderQueueReasonCode getReasonCode() { return reasonCode; }
}
