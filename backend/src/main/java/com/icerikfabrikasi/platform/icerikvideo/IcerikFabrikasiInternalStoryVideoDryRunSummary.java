package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-044: Internal story-to-video dry run summary. Metadata-only.
 * No public delivery. No paths/commands/logs. No auto-approval.
 */
public final class IcerikFabrikasiInternalStoryVideoDryRunSummary {
    private final String opaqueDryRunRef;
    private final String opaqueJobRef;
    private final String opaqueBundleRef;
    private final String opaqueReviewPackageRef;
    private final IcerikFabrikasiInternalStoryVideoDryRunStatus status;
    private final List<IcerikFabrikasiInternalStoryVideoDryRunIssueCode> issueCodes;
    private final int sceneCount;
    private final int generatedClipCount;
    private final boolean stitchedArtifactPresent;
    private final IcerikFabrikasiRenderReviewPackageStatus reviewPackageStatus;
    private final boolean adminMetadataVisible;
    private final int totalDurationSeconds;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxFps;
    private final long totalSizeBytes;
    private final boolean internalOnly;

    public IcerikFabrikasiInternalStoryVideoDryRunSummary(String opaqueDryRunRef,
            String opaqueJobRef, String opaqueBundleRef, String opaqueReviewPackageRef,
            IcerikFabrikasiInternalStoryVideoDryRunStatus status,
            List<IcerikFabrikasiInternalStoryVideoDryRunIssueCode> issueCodes,
            int sceneCount, int generatedClipCount,
            boolean stitchedArtifactPresent,
            IcerikFabrikasiRenderReviewPackageStatus reviewPackageStatus,
            boolean adminMetadataVisible,
            int totalDurationSeconds, int maxWidth, int maxHeight, int maxFps,
            long totalSizeBytes) {
        this.opaqueDryRunRef = opaqueDryRunRef;
        this.opaqueJobRef = opaqueJobRef;
        this.opaqueBundleRef = opaqueBundleRef;
        this.opaqueReviewPackageRef = opaqueReviewPackageRef;
        this.status = status;
        this.issueCodes = issueCodes != null ? List.copyOf(issueCodes) : List.of();
        this.sceneCount = sceneCount;
        this.generatedClipCount = generatedClipCount;
        this.stitchedArtifactPresent = stitchedArtifactPresent;
        this.reviewPackageStatus = reviewPackageStatus;
        this.adminMetadataVisible = adminMetadataVisible;
        this.totalDurationSeconds = totalDurationSeconds;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxFps = maxFps;
        this.totalSizeBytes = totalSizeBytes;
        this.internalOnly = true;
    }

    public String getOpaqueDryRunRef() { return opaqueDryRunRef; }
    public String getOpaqueJobRef() { return opaqueJobRef; }
    public String getOpaqueBundleRef() { return opaqueBundleRef; }
    public String getOpaqueReviewPackageRef() { return opaqueReviewPackageRef; }
    public IcerikFabrikasiInternalStoryVideoDryRunStatus getStatus() { return status; }
    public List<IcerikFabrikasiInternalStoryVideoDryRunIssueCode> getIssueCodes() { return issueCodes; }
    public int getSceneCount() { return sceneCount; }
    public int getGeneratedClipCount() { return generatedClipCount; }
    public boolean isStitchedArtifactPresent() { return stitchedArtifactPresent; }
    public IcerikFabrikasiRenderReviewPackageStatus getReviewPackageStatus() { return reviewPackageStatus; }
    public boolean isAdminMetadataVisible() { return adminMetadataVisible; }
    public int getTotalDurationSeconds() { return totalDurationSeconds; }
    public int getMaxWidth() { return maxWidth; }
    public int getMaxHeight() { return maxHeight; }
    public int getMaxFps() { return maxFps; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public boolean isInternalOnly() { return internalOnly; }

    @Override
    public String toString() {
        return "DryRun{ref=" + opaqueDryRunRef
                + ", status=" + status
                + ", scenes=" + sceneCount
                + ", clips=" + generatedClipCount
                + ", stitched=" + stitchedArtifactPresent
                + ", review=" + reviewPackageStatus
                + ", adminVisible=" + adminMetadataVisible
                + ", internalOnly=" + internalOnly + "}";
    }
}
