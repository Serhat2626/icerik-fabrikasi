package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-042: Safe artifact summary for review package. Metadata-only.
 */
public final class IcerikFabrikasiRenderReviewArtifactSummary {
    private final String opaqueArtifactRef;
    private final IcerikFabrikasiRenderBundleArtifactRole role;
    private final IcerikFabrikasiRenderArtifactVerificationStatus verificationStatus;
    private final String format;
    private final int durationSeconds;
    private final int fps;
    private final int width;
    private final int height;
    private final long sizeBytes;
    private final boolean verified;

    public IcerikFabrikasiRenderReviewArtifactSummary(String opaqueArtifactRef,
            IcerikFabrikasiRenderBundleArtifactRole role,
            IcerikFabrikasiRenderArtifactVerificationStatus verificationStatus,
            String format, int durationSeconds, int fps,
            int width, int height, long sizeBytes) {
        this.opaqueArtifactRef = opaqueArtifactRef;
        this.role = role;
        this.verificationStatus = verificationStatus;
        this.format = format;
        this.durationSeconds = durationSeconds;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.verified = verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.PASSED;
    }

    public String getOpaqueArtifactRef() { return opaqueArtifactRef; }
    public IcerikFabrikasiRenderBundleArtifactRole getRole() { return role; }
    public IcerikFabrikasiRenderArtifactVerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getFormat() { return format; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getSizeBytes() { return sizeBytes; }
    public boolean isVerified() { return verified; }

    public boolean isPrimary() {
        return role == IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP;
    }
}
