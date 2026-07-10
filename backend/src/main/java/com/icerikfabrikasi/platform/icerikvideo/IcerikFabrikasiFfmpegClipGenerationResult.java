package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-034: Video clip generation result.
 * No stdout/stderr/path/command exposure.
 */
public final class IcerikFabrikasiFfmpegClipGenerationResult {
    private final IcerikFabrikasiFfmpegClipGenerationStatus status;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegFixtureMetadata fixtureMetadata;
    private final long durationMs;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegClipGenerationResult(IcerikFabrikasiFfmpegClipGenerationStatus status,
            String outputRef, IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        this.status = status;
        this.outputRef = outputRef;
        this.fixtureMetadata = metadata;
        this.durationMs = durationMs;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "LOCAL_ONLY_MANUAL",
                "NO_PUBLISH", "NO_EXPORT", "NO_DOWNLOAD", "NO_PUBLIC_URL",
                "SYNTHETIC_INPUT_ONLY", "NO_COPYRIGHTED_MEDIA");
    }

    public static IcerikFabrikasiFfmpegClipGenerationResult notAttempted(IcerikFabrikasiFfmpegClipGenerationStatus reason) {
        return new IcerikFabrikasiFfmpegClipGenerationResult(reason, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegClipGenerationResult succeeded(String outputRef,
            IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        return new IcerikFabrikasiFfmpegClipGenerationResult(
                IcerikFabrikasiFfmpegClipGenerationStatus.CLIP_GENERATION_SUCCEEDED,
                outputRef, metadata, durationMs);
    }

    public static IcerikFabrikasiFfmpegClipGenerationResult verified(String outputRef,
            IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        return new IcerikFabrikasiFfmpegClipGenerationResult(
                IcerikFabrikasiFfmpegClipGenerationStatus.VERIFICATION_PASSED,
                outputRef, metadata, durationMs);
    }

    public static IcerikFabrikasiFfmpegClipGenerationResult failedSafe() {
        return new IcerikFabrikasiFfmpegClipGenerationResult(
                IcerikFabrikasiFfmpegClipGenerationStatus.CLIP_GENERATION_FAILED_SAFE, null, null, 0);
    }

    public IcerikFabrikasiFfmpegClipGenerationStatus getStatus() { return status; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegFixtureMetadata getFixtureMetadata() { return fixtureMetadata; }
    public long getDurationMs() { return durationMs; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
