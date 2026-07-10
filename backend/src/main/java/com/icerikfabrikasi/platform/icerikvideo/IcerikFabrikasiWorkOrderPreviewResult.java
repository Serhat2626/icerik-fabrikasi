package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * MP-002: Gated work order preview result — non-executable metadata.
 */
public final class IcerikFabrikasiWorkOrderPreviewResult {
    private final IcerikFabrikasiWorkOrderPreviewStatus status;
    private final List<IcerikFabrikasiWorkOrderPreviewIssueCode> blockedReasons;
    private final String opaqueWorkOrderRef;
    private final String opaqueEnablementRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String operatorSummary;
    private final List<IcerikFabrikasiProductionChecklistItem> checklistItems;
    private final int totalItems;
    private final int completedItems;
    private final int blockedItems;
    private final List<String> workOrderStepLabels;

    public IcerikFabrikasiWorkOrderPreviewResult(
            IcerikFabrikasiWorkOrderPreviewStatus status,
            List<IcerikFabrikasiWorkOrderPreviewIssueCode> blockedReasons,
            String opaqueWorkOrderRef, String opaqueEnablementRef,
            String safeIcerikFabrikasiTitleLabel, String operatorSummary,
            List<IcerikFabrikasiProductionChecklistItem> checklistItems,
            int totalItems, int completedItems, int blockedItems,
            List<String> workOrderStepLabels) {
        this.status = status;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.opaqueWorkOrderRef = opaqueWorkOrderRef;
        this.opaqueEnablementRef = opaqueEnablementRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.operatorSummary = operatorSummary;
        this.checklistItems = checklistItems == null ? List.of() : List.copyOf(checklistItems);
        this.totalItems = totalItems;
        this.completedItems = completedItems;
        this.blockedItems = blockedItems;
        this.workOrderStepLabels = workOrderStepLabels == null ? List.of() : List.copyOf(workOrderStepLabels);
    }

    public IcerikFabrikasiWorkOrderPreviewStatus getStatus() { return status; }
    public List<IcerikFabrikasiWorkOrderPreviewIssueCode> getBlockedReasons() { return blockedReasons; }
    public String getOpaqueWorkOrderRef() { return opaqueWorkOrderRef; }
    public String getOpaqueEnablementRef() { return opaqueEnablementRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getOperatorSummary() { return operatorSummary; }
    public List<IcerikFabrikasiProductionChecklistItem> getChecklistItems() { return checklistItems; }
    public int getTotalItems() { return totalItems; }
    public int getCompletedItems() { return completedItems; }
    public int getBlockedItems() { return blockedItems; }
    public List<String> getWorkOrderStepLabels() { return workOrderStepLabels; }
}
