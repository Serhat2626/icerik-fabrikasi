package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-046: Adapter result — storyboard metadata → scene render plans.
 * Metadata-only. No public delivery.
 */
public final class IcerikFabrikasiStoryboardRenderPlanAdapterResult {
    private final String opaqueAdapterRef;
    private final String opaqueStoryboardRef;
    private final String opaqueStoryRef;
    private final IcerikFabrikasiStoryboardRenderAdapterStatus status;
    private final List<IcerikFabrikasiStoryboardRenderAdapterIssueCode> issueCodes;
    private final List<IcerikFabrikasiSceneRenderPlan> scenePlans;
    private final int sceneCount;
    private final boolean internalOnly;

    public IcerikFabrikasiStoryboardRenderPlanAdapterResult(String opaqueAdapterRef,
            String opaqueStoryboardRef, String opaqueStoryRef,
            IcerikFabrikasiStoryboardRenderAdapterStatus status,
            List<IcerikFabrikasiStoryboardRenderAdapterIssueCode> issueCodes,
            List<IcerikFabrikasiSceneRenderPlan> scenePlans, int sceneCount) {
        this.opaqueAdapterRef = opaqueAdapterRef;
        this.opaqueStoryboardRef = opaqueStoryboardRef;
        this.opaqueStoryRef = opaqueStoryRef;
        this.status = status;
        this.issueCodes = issueCodes != null ? List.copyOf(issueCodes) : List.of();
        this.scenePlans = scenePlans != null ? List.copyOf(scenePlans) : List.of();
        this.sceneCount = sceneCount;
        this.internalOnly = true;
    }

    public String getOpaqueAdapterRef() { return opaqueAdapterRef; }
    public String getOpaqueStoryboardRef() { return opaqueStoryboardRef; }
    public String getOpaqueStoryRef() { return opaqueStoryRef; }
    public IcerikFabrikasiStoryboardRenderAdapterStatus getStatus() { return status; }
    public List<IcerikFabrikasiStoryboardRenderAdapterIssueCode> getIssueCodes() { return issueCodes; }
    public List<IcerikFabrikasiSceneRenderPlan> getScenePlans() { return scenePlans; }
    public int getSceneCount() { return sceneCount; }
    public boolean isInternalOnly() { return internalOnly; }

    @Override
    public String toString() {
        return "AdapterResult{ref=" + opaqueAdapterRef + ", status=" + status
                + ", scenes=" + sceneCount + ", internalOnly=" + internalOnly + "}";
    }
}
