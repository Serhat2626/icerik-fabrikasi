package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-026 Task 03: Review action result wrapper.
 * Contains updated summary + safe reason. No raw exceptions.
 */
public final class IcerikFabrikasiInternalRenderReviewActionResult {
    private final boolean allowed;
    private final IcerikFabrikasiInternalRenderReviewSummary summary;
    private final IcerikFabrikasiInternalRenderReviewAction action;
    private final IcerikFabrikasiInternalRenderReviewReasonCode reasonCode;

    private IcerikFabrikasiInternalRenderReviewActionResult(boolean allowed,
            IcerikFabrikasiInternalRenderReviewSummary summary,
            IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewReasonCode reasonCode) {
        this.allowed = allowed;
        this.summary = summary;
        this.action = action;
        this.reasonCode = reasonCode;
    }

    public static IcerikFabrikasiInternalRenderReviewActionResult allowed(
            IcerikFabrikasiInternalRenderReviewSummary updated,
            IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewReasonCode reason) {
        return new IcerikFabrikasiInternalRenderReviewActionResult(true, updated, action, reason);
    }

    public static IcerikFabrikasiInternalRenderReviewActionResult denied(
            IcerikFabrikasiInternalRenderReviewSummary current,
            IcerikFabrikasiInternalRenderReviewAction action,
            IcerikFabrikasiInternalRenderReviewReasonCode reason) {
        return new IcerikFabrikasiInternalRenderReviewActionResult(false, current, action, reason);
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiInternalRenderReviewSummary getSummary() { return summary; }
    public IcerikFabrikasiInternalRenderReviewAction getAction() { return action; }
    public IcerikFabrikasiInternalRenderReviewReasonCode getReasonCode() { return reasonCode; }
}
