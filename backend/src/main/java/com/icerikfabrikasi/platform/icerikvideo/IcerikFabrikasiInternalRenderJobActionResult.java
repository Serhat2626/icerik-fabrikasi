package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-025 Task 03: Action result wrapper.
 * Contains updated summary + reason code. No raw exceptions.
 */
public final class IcerikFabrikasiInternalRenderJobActionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderJobSummary summary;
    private final IcerikFabrikasiInternalRenderJobAction action;
    private final IcerikFabrikasiInternalRenderJobReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderJobActionResult(boolean allowed,
            IcerikFabrikasiInternalRenderJobSummary summary,
            IcerikFabrikasiInternalRenderJobAction action,
            IcerikFabrikasiInternalRenderJobReasonCode reasonCode) {
        this.allowed = allowed;
        this.summary = summary;
        this.action = action;
        this.reasonCode = reasonCode;
    }

    public static IcerikFabrikasiInternalRenderJobActionResult allowed(
            IcerikFabrikasiInternalRenderJobSummary updated, IcerikFabrikasiInternalRenderJobAction action) {
        return new IcerikFabrikasiInternalRenderJobActionResult(
                true, updated, action, IcerikFabrikasiInternalRenderJobReasonCode.TRANSITION_ALLOWED);
    }

    public static IcerikFabrikasiInternalRenderJobActionResult denied(
            IcerikFabrikasiInternalRenderJobSummary current, IcerikFabrikasiInternalRenderJobAction action,
            IcerikFabrikasiInternalRenderJobReasonCode reason) {
        return new IcerikFabrikasiInternalRenderJobActionResult(false, current, action, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderJobSummary getSummary() { return summary; }
    public IcerikFabrikasiInternalRenderJobAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderJobReasonCode getReasonCode() { return reasonCode; }
}
