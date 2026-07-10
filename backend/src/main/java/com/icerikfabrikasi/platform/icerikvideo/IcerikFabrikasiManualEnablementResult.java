package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * MP-001: Manual enablement candidate result.
 * Non-executable safe summary for operator review.
 */
public final class IcerikFabrikasiManualEnablementResult {
    private final IcerikFabrikasiManualEnablementStatus status;
    private final List<IcerikFabrikasiManualEnablementIssueCode> blockedReasons;
    private final String opaqueReadinessRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String operatorSummary;
    private final List<String> safetyPrerequisites;
    private final List<String> manualStepLabels;
    private final boolean copyrightSafeIntentPresent;
    private final boolean allScenesPlanned;
    private final boolean timelineComplete;
    private final boolean noBlockedScenes;

    public IcerikFabrikasiManualEnablementResult(
            IcerikFabrikasiManualEnablementStatus status,
            List<IcerikFabrikasiManualEnablementIssueCode> blockedReasons,
            String opaqueReadinessRef, String safeIcerikFabrikasiTitleLabel,
            String operatorSummary,
            List<String> safetyPrerequisites,
            List<String> manualStepLabels,
            boolean copyrightSafeIntentPresent, boolean allScenesPlanned,
            boolean timelineComplete, boolean noBlockedScenes) {
        this.status = status;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.opaqueReadinessRef = opaqueReadinessRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.operatorSummary = operatorSummary;
        this.safetyPrerequisites = safetyPrerequisites == null ? List.of() : List.copyOf(safetyPrerequisites);
        this.manualStepLabels = manualStepLabels == null ? List.of() : List.copyOf(manualStepLabels);
        this.copyrightSafeIntentPresent = copyrightSafeIntentPresent;
        this.allScenesPlanned = allScenesPlanned;
        this.timelineComplete = timelineComplete;
        this.noBlockedScenes = noBlockedScenes;
    }

    public IcerikFabrikasiManualEnablementStatus getStatus() { return status; }
    public List<IcerikFabrikasiManualEnablementIssueCode> getBlockedReasons() { return blockedReasons; }
    public String getOpaqueReadinessRef() { return opaqueReadinessRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getOperatorSummary() { return operatorSummary; }
    public List<String> getSafetyPrerequisites() { return safetyPrerequisites; }
    public List<String> getManualStepLabels() { return manualStepLabels; }
    public boolean isCopyrightSafeIntentPresent() { return copyrightSafeIntentPresent; }
    public boolean isAllScenesPlanned() { return allScenesPlanned; }
    public boolean isTimelineComplete() { return timelineComplete; }
    public boolean isNoBlockedScenes() { return noBlockedScenes; }
}
