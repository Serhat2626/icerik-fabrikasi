package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RP-003: Full scene asset plan result for a storyboard draft.
 */
public final class IcerikFabrikasiSceneAssetPlanResult {
    private final IcerikFabrikasiSceneAssetPlanStatus overallStatus;
    private final List<IcerikFabrikasiSceneAssetPlanIssueCode> overallIssueCodes;
    private final String opaquePlanRef;
    private final String opaqueDraftRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String plannerMode;
    private final List<IcerikFabrikasiSceneAssetPlanEntry> entries;
    private final int totalPlannedScenes;
    private final int blockedSceneCount;

    public IcerikFabrikasiSceneAssetPlanResult(
            IcerikFabrikasiSceneAssetPlanStatus overallStatus,
            List<IcerikFabrikasiSceneAssetPlanIssueCode> overallIssueCodes,
            String opaquePlanRef, String opaqueDraftRef,
            String safeIcerikFabrikasiTitleLabel, String plannerMode,
            List<IcerikFabrikasiSceneAssetPlanEntry> entries,
            int totalPlannedScenes, int blockedSceneCount) {
        this.overallStatus = overallStatus;
        this.overallIssueCodes = overallIssueCodes == null ? List.of() : List.copyOf(overallIssueCodes);
        this.opaquePlanRef = opaquePlanRef;
        this.opaqueDraftRef = opaqueDraftRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.plannerMode = plannerMode;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
        this.totalPlannedScenes = totalPlannedScenes;
        this.blockedSceneCount = blockedSceneCount;
    }

    public static IcerikFabrikasiSceneAssetPlanResult rejected(IcerikFabrikasiSceneAssetPlanIssueCode... codes) {
        return new IcerikFabrikasiSceneAssetPlanResult(
                IcerikFabrikasiSceneAssetPlanStatus.REJECTED_UNSAFE, List.of(codes),
                null, null, null, null, List.of(), 0, 0);
    }

    public static IcerikFabrikasiSceneAssetPlanResult failed(IcerikFabrikasiSceneAssetPlanIssueCode... codes) {
        return new IcerikFabrikasiSceneAssetPlanResult(
                IcerikFabrikasiSceneAssetPlanStatus.FAILED_SAFE, List.of(codes),
                null, null, null, null, List.of(), 0, 0);
    }

    public IcerikFabrikasiSceneAssetPlanStatus getOverallStatus() { return overallStatus; }
    public List<IcerikFabrikasiSceneAssetPlanIssueCode> getOverallIssueCodes() { return overallIssueCodes; }
    public String getOpaquePlanRef() { return opaquePlanRef; }
    public String getOpaqueDraftRef() { return opaqueDraftRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getPlannerMode() { return plannerMode; }
    public List<IcerikFabrikasiSceneAssetPlanEntry> getEntries() { return entries; }
    public int getTotalPlannedScenes() { return totalPlannedScenes; }
    public int getBlockedSceneCount() { return blockedSceneCount; }
}
