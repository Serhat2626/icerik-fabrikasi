package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-042: Internal render review package. Metadata-only.
 * No public delivery/publish readiness. No auto-approval.
 */
public final class IcerikFabrikasiRenderReviewPackage {
    private final String opaqueReviewPackageRef;
    private final String opaqueJobRef;
    private final String opaqueBundleRef;
    private final IcerikFabrikasiRenderReviewPackageStatus status;
    private final List<IcerikFabrikasiRenderReviewPackageIssueCode> issueCodes;
    private final List<IcerikFabrikasiRenderReviewArtifactSummary> artifactSummaries;
    private final int sceneCount;
    private final int inputClipCount;
    private final int totalDurationSeconds;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxFps;
    private final long totalSizeBytes;
    private final boolean internalOnly;

    public IcerikFabrikasiRenderReviewPackage(String opaqueReviewPackageRef,
            String opaqueJobRef, String opaqueBundleRef,
            IcerikFabrikasiRenderReviewPackageStatus status,
            List<IcerikFabrikasiRenderReviewPackageIssueCode> issueCodes,
            List<IcerikFabrikasiRenderReviewArtifactSummary> artifactSummaries,
            int sceneCount, int inputClipCount,
            int totalDurationSeconds, int maxWidth, int maxHeight, int maxFps,
            long totalSizeBytes) {
        this.opaqueReviewPackageRef = opaqueReviewPackageRef;
        this.opaqueJobRef = opaqueJobRef;
        this.opaqueBundleRef = opaqueBundleRef;
        this.status = status;
        this.issueCodes = issueCodes != null ? List.copyOf(issueCodes) : List.of();
        this.artifactSummaries = artifactSummaries != null ? List.copyOf(artifactSummaries) : List.of();
        this.sceneCount = sceneCount;
        this.inputClipCount = inputClipCount;
        this.totalDurationSeconds = totalDurationSeconds;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxFps = maxFps;
        this.totalSizeBytes = totalSizeBytes;
        this.internalOnly = true;
    }

    public String getOpaqueReviewPackageRef() { return opaqueReviewPackageRef; }
    public String getOpaqueJobRef() { return opaqueJobRef; }
    public String getOpaqueBundleRef() { return opaqueBundleRef; }
    public IcerikFabrikasiRenderReviewPackageStatus getStatus() { return status; }
    public List<IcerikFabrikasiRenderReviewPackageIssueCode> getIssueCodes() { return issueCodes; }
    public List<IcerikFabrikasiRenderReviewArtifactSummary> getArtifactSummaries() { return artifactSummaries; }
    public int getSceneCount() { return sceneCount; }
    public int getInputClipCount() { return inputClipCount; }
    public int getTotalDurationSeconds() { return totalDurationSeconds; }
    public int getMaxWidth() { return maxWidth; }
    public int getMaxHeight() { return maxHeight; }
    public int getMaxFps() { return maxFps; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public boolean isInternalOnly() { return internalOnly; }

    @Override
    public String toString() {
        return "ReviewPackage{ref=" + opaqueReviewPackageRef
                + ", status=" + status
                + ", artifacts=" + artifactSummaries.size()
                + ", clips=" + inputClipCount
                + ", duration=" + totalDurationSeconds + "s"
                + ", internalOnly=" + internalOnly + "}";
    }
}
