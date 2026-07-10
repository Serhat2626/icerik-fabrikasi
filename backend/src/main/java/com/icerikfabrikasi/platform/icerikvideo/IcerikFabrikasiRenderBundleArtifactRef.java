package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-036: Safe artifact reference within a bundle. Metadata-only.
 * No path/command/binary/provider/URL exposure.
 */
public final class IcerikFabrikasiRenderBundleArtifactRef {
    private final String opaqueArtifactRef;
    private final IcerikFabrikasiRenderBundleArtifactRole role;
    private final IcerikFabrikasiRenderArtifactFormat format;
    private final IcerikFabrikasiRenderArtifactVerificationStatus verificationStatus;
    private final int durationSeconds;
    private final int fps;
    private final int width;
    private final int height;
    private final long sizeBytes;

    public IcerikFabrikasiRenderBundleArtifactRef(String opaqueArtifactRef,
            IcerikFabrikasiRenderBundleArtifactRole role, IcerikFabrikasiRenderArtifactFormat format,
            IcerikFabrikasiRenderArtifactVerificationStatus verificationStatus,
            int durationSeconds, int fps, int width, int height, long sizeBytes) {
        this.opaqueArtifactRef = opaqueArtifactRef;
        this.role = role != null ? role : IcerikFabrikasiRenderBundleArtifactRole.UNKNOWN;
        this.format = format != null ? format : IcerikFabrikasiRenderArtifactFormat.UNKNOWN;
        this.verificationStatus = verificationStatus != null ? verificationStatus
                : IcerikFabrikasiRenderArtifactVerificationStatus.NOT_VERIFIED;
        this.durationSeconds = durationSeconds;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
    }

    /**
     * Build from FP-035 lifecycle report.
     */
    public static IcerikFabrikasiRenderBundleArtifactRef fromLifecycleReport(
            IcerikFabrikasiRenderArtifactLifecycleReport report, IcerikFabrikasiRenderBundleArtifactRole role) {
        if (report == null) return new IcerikFabrikasiRenderBundleArtifactRef(null,
                IcerikFabrikasiRenderBundleArtifactRole.UNKNOWN, IcerikFabrikasiRenderArtifactFormat.UNKNOWN,
                IcerikFabrikasiRenderArtifactVerificationStatus.NOT_VERIFIED, 0, 0, 0, 0, 0);
        return new IcerikFabrikasiRenderBundleArtifactRef(report.getOpaqueArtifactRef(), role,
                report.getFormat(), report.getVerificationStatus(),
                report.getDurationSeconds(), report.getFps(),
                report.getWidth(), report.getHeight(), report.getSizeBytes());
    }

    public String getOpaqueArtifactRef() { return opaqueArtifactRef; }
    public IcerikFabrikasiRenderBundleArtifactRole getRole() { return role; }
    public IcerikFabrikasiRenderArtifactFormat getFormat() { return format; }
    public IcerikFabrikasiRenderArtifactVerificationStatus getVerificationStatus() { return verificationStatus; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getSizeBytes() { return sizeBytes; }

    public boolean isVerified() {
        return verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.PASSED
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED;
    }

    public boolean isRejected() {
        return verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_FORMAT
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_DURATION
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_FPS
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_RESOLUTION
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_SIZE
                || verificationStatus == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_MULTIPLE;
    }
}
