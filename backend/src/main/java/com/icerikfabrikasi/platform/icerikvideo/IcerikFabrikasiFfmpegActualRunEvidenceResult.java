package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-032: Actual local smoke execution evidence result.
 * No stdout/stderr/path/command exposure.
 */
public final class IcerikFabrikasiFfmpegActualRunEvidenceResult {
    private final IcerikFabrikasiFfmpegActualRunEvidenceStatus status;
    private final IcerikFabrikasiFfmpegLocalAvailabilityStatus availabilityStatus;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegFixtureMetadata fixtureMetadata;
    private final long durationMs;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegActualRunEvidenceResult(IcerikFabrikasiFfmpegActualRunEvidenceStatus status,
            IcerikFabrikasiFfmpegLocalAvailabilityStatus availability,
            String outputRef, IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        this.status = status;
        this.availabilityStatus = availability;
        this.outputRef = outputRef;
        this.fixtureMetadata = metadata;
        this.durationMs = durationMs;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "LOCAL_ONLY", "NO_PUBLISH",
                "NO_EXPORT", "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult unavailable() {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.FFMPEG_UNAVAILABLE,
                IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNAVAILABLE, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult disabled() {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.REAL_LOCAL_RUN_SKIPPED_DISABLED,
                IcerikFabrikasiFfmpegLocalAvailabilityStatus.DISABLED, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult unsupportedEnv() {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.REAL_LOCAL_RUN_SKIPPED_UNSUPPORTED_ENVIRONMENT,
                IcerikFabrikasiFfmpegLocalAvailabilityStatus.UNSUPPORTED_ENVIRONMENT, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult gateDenied() {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.GATE_DENIED, null, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult succeeded(String outputRef,
            IcerikFabrikasiFfmpegFixtureMetadata metadata, long durationMs) {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.REAL_LOCAL_RUN_SUCCEEDED,
                IcerikFabrikasiFfmpegLocalAvailabilityStatus.AVAILABLE, outputRef, metadata, durationMs);
    }

    public static IcerikFabrikasiFfmpegActualRunEvidenceResult failedSafe() {
        return new IcerikFabrikasiFfmpegActualRunEvidenceResult(
                IcerikFabrikasiFfmpegActualRunEvidenceStatus.REAL_LOCAL_RUN_FAILED_SAFE,
                IcerikFabrikasiFfmpegLocalAvailabilityStatus.AVAILABLE, null, null, 0);
    }

    public IcerikFabrikasiFfmpegActualRunEvidenceStatus getStatus() { return status; }
    public IcerikFabrikasiFfmpegLocalAvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegFixtureMetadata getFixtureMetadata() { return fixtureMetadata; }
    public long getDurationMs() { return durationMs; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
