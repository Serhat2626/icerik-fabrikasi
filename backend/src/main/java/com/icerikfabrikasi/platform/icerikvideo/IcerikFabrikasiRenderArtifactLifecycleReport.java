package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-035: Metadata-only lifecycle verification report.
 * Separate from FP-024's IcerikFabrikasiRenderArtifactVerificationReport.
 * No path/command/stdout/stderr/binary/provider/storage/URL exposure.
 */
public final class IcerikFabrikasiRenderArtifactLifecycleReport {
    private final String opaqueArtifactRef;
    private final IcerikFabrikasiRenderArtifactFormat format;
    private final int durationSeconds;
    private final int fps;
    private final int width;
    private final int height;
    private final long sizeBytes;
    private final IcerikFabrikasiRenderArtifactVerificationStatus verificationStatus;
    private final List<IcerikFabrikasiRenderArtifactIssueCode> issueCodes;

    public IcerikFabrikasiRenderArtifactLifecycleReport(String opaqueArtifactRef,
            IcerikFabrikasiRenderArtifactFormat format, int durationSeconds, int fps,
            int width, int height, long sizeBytes,
            IcerikFabrikasiRenderArtifactVerificationStatus status,
            List<IcerikFabrikasiRenderArtifactIssueCode> issues) {
        this.opaqueArtifactRef = opaqueArtifactRef;
        this.format = format != null ? format : IcerikFabrikasiRenderArtifactFormat.UNKNOWN;
        this.durationSeconds = durationSeconds;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.verificationStatus = status;
        this.issueCodes = issues != null ? List.copyOf(issues) : List.of();
    }

    public String getOpaqueArtifactRef() { return opaqueArtifactRef; }
    public IcerikFabrikasiRenderArtifactFormat getFormat() { return format; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getSizeBytes() { return sizeBytes; }
    public IcerikFabrikasiRenderArtifactVerificationStatus getVerificationStatus() { return verificationStatus; }
    public List<IcerikFabrikasiRenderArtifactIssueCode> getIssueCodes() { return issueCodes; }

    @Override
    public String toString() {
        return "LifecycleReport{ref=" + opaqueArtifactRef
                + ", format=" + format
                + ", status=" + verificationStatus
                + ", issues=" + issueCodes + "}";
    }
}
