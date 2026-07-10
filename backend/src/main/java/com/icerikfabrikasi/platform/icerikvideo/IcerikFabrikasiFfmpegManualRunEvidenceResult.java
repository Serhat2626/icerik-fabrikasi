package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-033: Manual local FFmpeg run evidence result.
 * No stdout/stderr/path/command exposure.
 */
public final class IcerikFabrikasiFfmpegManualRunEvidenceResult {
    private final IcerikFabrikasiFfmpegManualRunEvidenceStatus status;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegFixtureMetadata fixtureMetadata;
    private final long durationMs;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegManualRunEvidenceResult(IcerikFabrikasiFfmpegManualRunEvidenceStatus status,
            String outputRef, IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        this.status = status;
        this.outputRef = outputRef;
        this.fixtureMetadata = metadata;
        this.durationMs = durationMs;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "LOCAL_ONLY_MANUAL",
                "NO_PUBLISH", "NO_EXPORT", "NO_DOWNLOAD", "NO_PUBLIC_URL");
    }

    public static IcerikFabrikasiFfmpegManualRunEvidenceResult notAttempted(IcerikFabrikasiFfmpegManualRunEvidenceStatus reason) {
        return new IcerikFabrikasiFfmpegManualRunEvidenceResult(reason, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegManualRunEvidenceResult succeeded(String outputRef,
            IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        return new IcerikFabrikasiFfmpegManualRunEvidenceResult(
                IcerikFabrikasiFfmpegManualRunEvidenceStatus.REAL_LOCAL_EXECUTION_SUCCEEDED,
                outputRef, metadata, durationMs);
    }

    public static IcerikFabrikasiFfmpegManualRunEvidenceResult failedSafe() {
        return new IcerikFabrikasiFfmpegManualRunEvidenceResult(
                IcerikFabrikasiFfmpegManualRunEvidenceStatus.REAL_LOCAL_EXECUTION_FAILED_SAFE,
                null, null, 0);
    }

    public IcerikFabrikasiFfmpegManualRunEvidenceStatus getStatus() { return status; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegFixtureMetadata getFixtureMetadata() { return fixtureMetadata; }
    public long getDurationMs() { return durationMs; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
