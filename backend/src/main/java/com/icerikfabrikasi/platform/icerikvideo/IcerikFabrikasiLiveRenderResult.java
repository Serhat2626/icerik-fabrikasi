package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * LIVE-004: Render execution result.
 */
public final class IcerikFabrikasiLiveRenderResult {
    private final IcerikFabrikasiLiveRenderStatus status;
    private final IcerikFabrikasiLiveRenderMode mode;
    private final List<IcerikFabrikasiLiveRenderIssueCode> issues;
    private final String opaqueRenderJobRef;
    private final String opaqueStorageRef;
    private final boolean killSwitchActive;
    private final boolean operatorGatePassed;
    private final boolean artifactsReady;
    private final IcerikFabrikasiLiveRenderArtifact renderArtifact;
    private final int inputSceneCount;
    private final List<String> blockedReasons;
    private final String sanitizedRenderSummary;
    private final String validationSummary;

    public IcerikFabrikasiLiveRenderResult(
            IcerikFabrikasiLiveRenderStatus status, IcerikFabrikasiLiveRenderMode mode,
            List<IcerikFabrikasiLiveRenderIssueCode> issues,
            String opaqueRenderJobRef, String opaqueStorageRef,
            boolean killSwitchActive, boolean operatorGatePassed, boolean artifactsReady,
            IcerikFabrikasiLiveRenderArtifact renderArtifact, int inputSceneCount,
            List<String> blockedReasons, String sanitizedRenderSummary, String validationSummary) {
        this.status = status;
        this.mode = mode;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaqueRenderJobRef = opaqueRenderJobRef;
        this.opaqueStorageRef = opaqueStorageRef;
        this.killSwitchActive = killSwitchActive;
        this.operatorGatePassed = operatorGatePassed;
        this.artifactsReady = artifactsReady;
        this.renderArtifact = renderArtifact;
        this.inputSceneCount = inputSceneCount;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.sanitizedRenderSummary = sanitizedRenderSummary;
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiLiveRenderStatus getStatus() { return status; }
    public IcerikFabrikasiLiveRenderMode getMode() { return mode; }
    public List<IcerikFabrikasiLiveRenderIssueCode> getIssues() { return issues; }
    public String getOpaqueRenderJobRef() { return opaqueRenderJobRef; }
    public String getOpaqueStorageRef() { return opaqueStorageRef; }
    public boolean isKillSwitchActive() { return killSwitchActive; }
    public boolean isOperatorGatePassed() { return operatorGatePassed; }
    public boolean isArtifactsReady() { return artifactsReady; }
    public IcerikFabrikasiLiveRenderArtifact getRenderArtifact() { return renderArtifact; }
    public int getInputSceneCount() { return inputSceneCount; }
    public List<String> getBlockedReasons() { return blockedReasons; }
    public String getSanitizedRenderSummary() { return sanitizedRenderSummary; }
    public String getValidationSummary() { return validationSummary; }
}
