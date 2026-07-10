package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-037: Scene render plan. Metadata-only, no content/paths/commands.
 */
public final class IcerikFabrikasiSceneRenderPlan {
    private final String opaqueSceneRef;
    private final String opaquePlanRef;
    private final int sceneOrder;
    private final int expectedDurationSeconds;
    private final int expectedFps;
    private final int expectedWidth;
    private final int expectedHeight;
    private final List<IcerikFabrikasiSceneRenderArtifactExpectation> artifactExpectations;
    private final IcerikFabrikasiSceneRenderPlanStatus status;
    private final List<IcerikFabrikasiSceneRenderPlanIssueCode> issueCodes;

    public IcerikFabrikasiSceneRenderPlan(String opaqueSceneRef, String opaquePlanRef,
            int sceneOrder, int expectedDurationSeconds, int expectedFps,
            int expectedWidth, int expectedHeight,
            List<IcerikFabrikasiSceneRenderArtifactExpectation> expectations,
            IcerikFabrikasiSceneRenderPlanStatus status,
            List<IcerikFabrikasiSceneRenderPlanIssueCode> issueCodes) {
        this.opaqueSceneRef = opaqueSceneRef;
        this.opaquePlanRef = opaquePlanRef;
        this.sceneOrder = sceneOrder;
        this.expectedDurationSeconds = expectedDurationSeconds;
        this.expectedFps = expectedFps;
        this.expectedWidth = expectedWidth;
        this.expectedHeight = expectedHeight;
        this.artifactExpectations = expectations != null ? List.copyOf(expectations) : List.of();
        this.status = status;
        this.issueCodes = issueCodes != null ? List.copyOf(issueCodes) : List.of();
    }

    public String getOpaqueSceneRef() { return opaqueSceneRef; }
    public String getOpaquePlanRef() { return opaquePlanRef; }
    public int getSceneOrder() { return sceneOrder; }
    public int getExpectedDurationSeconds() { return expectedDurationSeconds; }
    public int getExpectedFps() { return expectedFps; }
    public int getExpectedWidth() { return expectedWidth; }
    public int getExpectedHeight() { return expectedHeight; }
    public List<IcerikFabrikasiSceneRenderArtifactExpectation> getArtifactExpectations() { return artifactExpectations; }
    public IcerikFabrikasiSceneRenderPlanStatus getStatus() { return status; }
    public List<IcerikFabrikasiSceneRenderPlanIssueCode> getIssueCodes() { return issueCodes; }

    @Override
    public String toString() {
        return "SceneRenderPlan{scene=" + opaqueSceneRef
                + ", plan=" + opaquePlanRef
                + ", order=" + sceneOrder
                + ", status=" + status
                + ", issues=" + issueCodes + "}";
    }
}
