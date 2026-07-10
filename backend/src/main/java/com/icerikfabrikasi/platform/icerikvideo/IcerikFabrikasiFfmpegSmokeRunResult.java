package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-030 Task 02: Smoke run result. No stdout/stderr/path/command.
 */
public final class IcerikFabrikasiFfmpegSmokeRunResult {
    private final IcerikFabrikasiFfmpegSmokeRunStatus status;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegVerificationCandidate verificationCandidate;
    private final long durationMs;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus status,
            String outputRef, IcerikFabrikasiFfmpegVerificationCandidate candidate, long durationMs) {
        this.status = status;
        this.outputRef = outputRef;
        this.verificationCandidate = candidate;
        this.durationMs = durationMs;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult denied(IcerikFabrikasiFfmpegSmokeRunStatus status) {
        return new IcerikFabrikasiFfmpegSmokeRunResult(status, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult skippedDisabled() {
        return new IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus.EXECUTION_SKIPPED_DISABLED, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult skippedUnavailable() {
        return new IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus.EXECUTION_SKIPPED_UNAVAILABLE, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult failedSafe() {
        return new IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus.EXECUTION_FAILED_SAFE, null, null, 0);
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult completed(String outputRef,
            IcerikFabrikasiFfmpegVerificationCandidate candidate, long durationMs) {
        return new IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus.EXECUTION_COMPLETED_SAFE,
                outputRef, candidate, durationMs);
    }

    public static IcerikFabrikasiFfmpegSmokeRunResult candidateCreated(String outputRef,
            IcerikFabrikasiFfmpegVerificationCandidate candidate) {
        return new IcerikFabrikasiFfmpegSmokeRunResult(IcerikFabrikasiFfmpegSmokeRunStatus.VERIFICATION_CANDIDATE_CREATED,
                outputRef, candidate, 0);
    }

    public IcerikFabrikasiFfmpegSmokeRunStatus getStatus() { return status; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegVerificationCandidate getVerificationCandidate() { return verificationCandidate; }
    public long getDurationMs() { return durationMs; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
