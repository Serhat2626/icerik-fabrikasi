package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-005: Image prompt package result — aggregates scene prompts + manual approval gate.
 */
public final class IcerikFabrikasiImagePromptPackageResult {
    private final IcerikFabrikasiImagePromptPackageStatus status;
    private final List<IcerikFabrikasiImagePromptPackageIssueCode> issues;
    private final String opaquePromptPackageRef;
    private final String opaqueExecutionPackageRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final List<IcerikFabrikasiSceneImagePrompt> scenePrompts;
    private final int totalScenes;
    private final int blockedScenes;
    private final boolean manualApprovalGranted;
    private final String approvalGateLabel;
    private final String validationSummary;

    public IcerikFabrikasiImagePromptPackageResult(
            IcerikFabrikasiImagePromptPackageStatus status, List<IcerikFabrikasiImagePromptPackageIssueCode> issues,
            String opaquePromptPackageRef, String opaqueExecutionPackageRef,
            String safeIcerikFabrikasiTitleLabel, List<IcerikFabrikasiSceneImagePrompt> scenePrompts,
            int totalScenes, int blockedScenes, boolean manualApprovalGranted,
            String approvalGateLabel, String validationSummary) {
        this.status = status;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaquePromptPackageRef = opaquePromptPackageRef;
        this.opaqueExecutionPackageRef = opaqueExecutionPackageRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.scenePrompts = scenePrompts == null ? List.of() : List.copyOf(scenePrompts);
        this.totalScenes = totalScenes;
        this.blockedScenes = blockedScenes;
        this.manualApprovalGranted = manualApprovalGranted;
        this.approvalGateLabel = approvalGateLabel;
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiImagePromptPackageStatus getStatus() { return status; }
    public List<IcerikFabrikasiImagePromptPackageIssueCode> getIssues() { return issues; }
    public String getOpaquePromptPackageRef() { return opaquePromptPackageRef; }
    public String getOpaqueExecutionPackageRef() { return opaqueExecutionPackageRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public List<IcerikFabrikasiSceneImagePrompt> getScenePrompts() { return scenePrompts; }
    public int getTotalScenes() { return totalScenes; }
    public int getBlockedScenes() { return blockedScenes; }
    public boolean isManualApprovalGranted() { return manualApprovalGranted; }
    public String getApprovalGateLabel() { return approvalGateLabel; }
    public String getValidationSummary() { return validationSummary; }
}
