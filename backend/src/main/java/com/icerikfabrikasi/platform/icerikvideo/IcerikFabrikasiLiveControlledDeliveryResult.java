package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * LIVE-005: Controlled delivery candidate.
 * Safe admin-facing metadata only. No storage key/URL/path/base64/binary.
 */
public final class IcerikFabrikasiLiveControlledDeliveryResult {
    private final IcerikFabrikasiLiveDeliveryStatus status;
    private final List<IcerikFabrikasiLiveDeliveryIssueCode> issues;
    private final String opaqueDeliveryRef;
    private final String opaqueRenderRef;
    private final IcerikFabrikasiLiveReviewDecision reviewDecision;
    private final List<IcerikFabrikasiLiveReviewChecklistItem> reviewChecklist;
    private final String renderArtifactLabel;
    private final String formatHint;
    private final String resolutionHint;
    private final String durationLabel;
    private final String sizeLabel;
    private final boolean exportEligible;
    private final String exportEligibilityNote;
    private final List<String> blockedReasons;
    private final String validationSummary;

    public IcerikFabrikasiLiveControlledDeliveryResult(
            IcerikFabrikasiLiveDeliveryStatus status, List<IcerikFabrikasiLiveDeliveryIssueCode> issues,
            String opaqueDeliveryRef, String opaqueRenderRef,
            IcerikFabrikasiLiveReviewDecision reviewDecision,
            List<IcerikFabrikasiLiveReviewChecklistItem> reviewChecklist,
            String renderArtifactLabel, String formatHint, String resolutionHint,
            String durationLabel, String sizeLabel,
            boolean exportEligible, String exportEligibilityNote,
            List<String> blockedReasons, String validationSummary) {
        this.status = status;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaqueDeliveryRef = opaqueDeliveryRef;
        this.opaqueRenderRef = opaqueRenderRef;
        this.reviewDecision = reviewDecision;
        this.reviewChecklist = reviewChecklist == null ? List.of() : List.copyOf(reviewChecklist);
        this.renderArtifactLabel = renderArtifactLabel;
        this.formatHint = formatHint;
        this.resolutionHint = resolutionHint;
        this.durationLabel = durationLabel;
        this.sizeLabel = sizeLabel;
        this.exportEligible = exportEligible;
        this.exportEligibilityNote = exportEligibilityNote;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiLiveDeliveryStatus getStatus() { return status; }
    public List<IcerikFabrikasiLiveDeliveryIssueCode> getIssues() { return issues; }
    public String getOpaqueDeliveryRef() { return opaqueDeliveryRef; }
    public String getOpaqueRenderRef() { return opaqueRenderRef; }
    public IcerikFabrikasiLiveReviewDecision getReviewDecision() { return reviewDecision; }
    public List<IcerikFabrikasiLiveReviewChecklistItem> getReviewChecklist() { return reviewChecklist; }
    public String getRenderArtifactLabel() { return renderArtifactLabel; }
    public String getFormatHint() { return formatHint; }
    public String getResolutionHint() { return resolutionHint; }
    public String getDurationLabel() { return durationLabel; }
    public String getSizeLabel() { return sizeLabel; }
    public boolean isExportEligible() { return exportEligible; }
    public String getExportEligibilityNote() { return exportEligibilityNote; }
    public List<String> getBlockedReasons() { return blockedReasons; }
    public String getValidationSummary() { return validationSummary; }
}
