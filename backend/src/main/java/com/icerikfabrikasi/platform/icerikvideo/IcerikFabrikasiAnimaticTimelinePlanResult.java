package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RP-004: Animatic timeline plan result.
 */
public final class IcerikFabrikasiAnimaticTimelinePlanResult {
    private final IcerikFabrikasiAnimaticTimelinePlanStatus status;
    private final List<IcerikFabrikasiAnimaticTimelinePlanIssueCode> issueCodes;
    private final String opaqueTimelineRef;
    private final String opaquePlanRef;
    private final String opaqueDraftRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String plannerMode;
    private final List<IcerikFabrikasiAnimaticTimelineItem> items;
    private final int totalItems;
    private final int totalDurationMs;

    public IcerikFabrikasiAnimaticTimelinePlanResult(
            IcerikFabrikasiAnimaticTimelinePlanStatus status,
            List<IcerikFabrikasiAnimaticTimelinePlanIssueCode> issueCodes,
            String opaqueTimelineRef, String opaquePlanRef, String opaqueDraftRef,
            String safeIcerikFabrikasiTitleLabel, String plannerMode,
            List<IcerikFabrikasiAnimaticTimelineItem> items, int totalItems, int totalDurationMs) {
        this.status = status;
        this.issueCodes = issueCodes == null ? List.of() : List.copyOf(issueCodes);
        this.opaqueTimelineRef = opaqueTimelineRef;
        this.opaquePlanRef = opaquePlanRef;
        this.opaqueDraftRef = opaqueDraftRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.plannerMode = plannerMode;
        this.items = items == null ? List.of() : List.copyOf(items);
        this.totalItems = totalItems;
        this.totalDurationMs = totalDurationMs;
    }

    public static IcerikFabrikasiAnimaticTimelinePlanResult rejected(IcerikFabrikasiAnimaticTimelinePlanIssueCode... codes) {
        return new IcerikFabrikasiAnimaticTimelinePlanResult(
                IcerikFabrikasiAnimaticTimelinePlanStatus.REJECTED_UNSAFE, List.of(codes),
                null, null, null, null, null, List.of(), 0, 0);
    }

    public static IcerikFabrikasiAnimaticTimelinePlanResult failed(IcerikFabrikasiAnimaticTimelinePlanIssueCode... codes) {
        return new IcerikFabrikasiAnimaticTimelinePlanResult(
                IcerikFabrikasiAnimaticTimelinePlanStatus.FAILED_SAFE, List.of(codes),
                null, null, null, null, null, List.of(), 0, 0);
    }

    public IcerikFabrikasiAnimaticTimelinePlanStatus getStatus() { return status; }
    public List<IcerikFabrikasiAnimaticTimelinePlanIssueCode> getIssueCodes() { return issueCodes; }
    public String getOpaqueTimelineRef() { return opaqueTimelineRef; }
    public String getOpaquePlanRef() { return opaquePlanRef; }
    public String getOpaqueDraftRef() { return opaqueDraftRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getPlannerMode() { return plannerMode; }
    public List<IcerikFabrikasiAnimaticTimelineItem> getItems() { return items; }
    public int getTotalItems() { return totalItems; }
    public int getTotalDurationMs() { return totalDurationMs; }
}
