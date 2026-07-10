package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-038: Multi-scene render job plan. Metadata-only.
 */
public final class IcerikFabrikasiMultiSceneRenderJobPlan {
    private final String opaqueJobRef;
    private final IcerikFabrikasiMultiSceneRenderJobStatus status;
    private final List<IcerikFabrikasiMultiSceneRenderSceneRef> scenes;
    private final List<IcerikFabrikasiMultiSceneRenderJobIssueCode> issueCodes;
    private final int sceneCount;
    private final int totalExpectedDurationSeconds;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxFps;
    private final int requiredArtifactCount;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;

    public IcerikFabrikasiMultiSceneRenderJobPlan(String opaqueJobRef,
            IcerikFabrikasiMultiSceneRenderJobStatus status,
            List<IcerikFabrikasiMultiSceneRenderSceneRef> scenes,
            List<IcerikFabrikasiMultiSceneRenderJobIssueCode> issueCodes) {
        this.opaqueJobRef = opaqueJobRef;
        this.status = status;
        this.scenes = scenes != null ? List.copyOf(scenes) : List.of();
        this.issueCodes = issueCodes != null ? List.copyOf(issueCodes) : List.of();
        this.sceneCount = this.scenes.size();
        this.totalExpectedDurationSeconds = this.scenes.stream()
                .mapToInt(IcerikFabrikasiMultiSceneRenderSceneRef::getExpectedDurationSeconds).sum();
        this.maxWidth = this.scenes.stream().mapToInt(IcerikFabrikasiMultiSceneRenderSceneRef::getExpectedWidth).max().orElse(0);
        this.maxHeight = this.scenes.stream().mapToInt(IcerikFabrikasiMultiSceneRenderSceneRef::getExpectedHeight).max().orElse(0);
        this.maxFps = this.scenes.stream().mapToInt(IcerikFabrikasiMultiSceneRenderSceneRef::getExpectedFps).max().orElse(0);
        this.requiredArtifactCount = (int) this.scenes.stream()
                .filter(IcerikFabrikasiMultiSceneRenderSceneRef::isHasPrimaryVideoExpectation).count();
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
    }

    public String getOpaqueJobRef() { return opaqueJobRef; }
    public IcerikFabrikasiMultiSceneRenderJobStatus getStatus() { return status; }
    public List<IcerikFabrikasiMultiSceneRenderSceneRef> getScenes() { return scenes; }
    public List<IcerikFabrikasiMultiSceneRenderJobIssueCode> getIssueCodes() { return issueCodes; }
    public int getSceneCount() { return sceneCount; }
    public int getTotalExpectedDurationSeconds() { return totalExpectedDurationSeconds; }
    public int getMaxWidth() { return maxWidth; }
    public int getMaxHeight() { return maxHeight; }
    public int getMaxFps() { return maxFps; }
    public int getRequiredArtifactCount() { return requiredArtifactCount; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }

    @Override
    public String toString() {
        return "MultiSceneRenderJobPlan{ref=" + opaqueJobRef
                + ", status=" + status
                + ", scenes=" + sceneCount
                + ", totalDuration=" + totalExpectedDurationSeconds + "s"
                + ", issues=" + issueCodes + "}";
    }
}
