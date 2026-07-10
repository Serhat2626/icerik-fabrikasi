package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-036: Internal render bundle manifest. Metadata-only.
 * No path/command/binary/provider/storage/URL exposure.
 */
public final class IcerikFabrikasiRenderBundleManifest {
    private final String opaqueBundleRef;
    private final IcerikFabrikasiRenderBundleStatus status;
    private final List<IcerikFabrikasiRenderBundleArtifactRef> artifacts;
    private final List<IcerikFabrikasiRenderBundleIssueCode> issueCodes;
    private final int artifactCount;
    private final int verifiedArtifactCount;
    private final int rejectedArtifactCount;
    private final long totalSizeBytes;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;

    public IcerikFabrikasiRenderBundleManifest(String opaqueBundleRef, IcerikFabrikasiRenderBundleStatus status,
            List<IcerikFabrikasiRenderBundleArtifactRef> artifacts, List<IcerikFabrikasiRenderBundleIssueCode> issues) {
        this.opaqueBundleRef = opaqueBundleRef;
        this.status = status;
        this.artifacts = artifacts != null ? List.copyOf(artifacts) : List.of();
        this.issueCodes = issues != null ? List.copyOf(issues) : List.of();
        this.artifactCount = this.artifacts.size();
        this.verifiedArtifactCount = (int) this.artifacts.stream().filter(IcerikFabrikasiRenderBundleArtifactRef::isVerified).count();
        this.rejectedArtifactCount = (int) this.artifacts.stream().filter(IcerikFabrikasiRenderBundleArtifactRef::isRejected).count();
        this.totalSizeBytes = this.artifacts.stream().mapToLong(IcerikFabrikasiRenderBundleArtifactRef::getSizeBytes).sum();
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
    }

    public String getOpaqueBundleRef() { return opaqueBundleRef; }
    public IcerikFabrikasiRenderBundleStatus getStatus() { return status; }
    public List<IcerikFabrikasiRenderBundleArtifactRef> getArtifacts() { return artifacts; }
    public List<IcerikFabrikasiRenderBundleIssueCode> getIssueCodes() { return issueCodes; }
    public int getArtifactCount() { return artifactCount; }
    public int getVerifiedArtifactCount() { return verifiedArtifactCount; }
    public int getRejectedArtifactCount() { return rejectedArtifactCount; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }

    @Override
    public String toString() {
        return "BundleManifest{ref=" + opaqueBundleRef
                + ", status=" + status
                + ", artifacts=" + artifactCount
                + ", verified=" + verifiedArtifactCount
                + ", rejected=" + rejectedArtifactCount
                + ", issues=" + issueCodes + "}";
    }
}
