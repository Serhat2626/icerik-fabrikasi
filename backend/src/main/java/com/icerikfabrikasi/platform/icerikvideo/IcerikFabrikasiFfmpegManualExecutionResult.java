package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-031 Task 01: Safe manual execution result. No stdout/stderr/path/command.
 */
public final class IcerikFabrikasiFfmpegManualExecutionResult {
    private final IcerikFabrikasiFfmpegManualExecutionStatus status;
    private final IcerikFabrikasiFfmpegManualExecutionReasonCode reasonCode;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegFixtureMetadata fixtureMetadata;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus status,
            IcerikFabrikasiFfmpegManualExecutionReasonCode reasonCode, String outputRef,
            IcerikFabrikasiFfmpegFixtureMetadata metadata) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.outputRef = outputRef;
        this.fixtureMetadata = metadata;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "LOCAL_ONLY_MANUAL", "NO_PUBLISH",
                "NO_EXPORT", "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult denied(IcerikFabrikasiFfmpegManualExecutionStatus status,
            IcerikFabrikasiFfmpegManualExecutionReasonCode reason) {
        return new IcerikFabrikasiFfmpegManualExecutionResult(status, reason, null, null);
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult skipped() {
        return new IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus.EXECUTION_SKIPPED_FAKE,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.EXECUTION_SKIPPED_SAFE, null, null);
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult completed(String outputRef, IcerikFabrikasiFfmpegFixtureMetadata metadata) {
        return new IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus.EXECUTION_COMPLETED_SAFE,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.EXECUTION_COMPLETED_SAFE, outputRef, metadata);
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult failedSafe() {
        return new IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus.EXECUTION_FAILED_SAFE,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.EXECUTION_FAILED_INTERNAL, null, null);
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult verificationPassed(String outputRef, IcerikFabrikasiFfmpegFixtureMetadata metadata) {
        return new IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus.VERIFICATION_PASSED,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.VERIFICATION_PASSED_SAFE, outputRef, metadata);
    }

    public static IcerikFabrikasiFfmpegManualExecutionResult verificationFailed() {
        return new IcerikFabrikasiFfmpegManualExecutionResult(IcerikFabrikasiFfmpegManualExecutionStatus.VERIFICATION_FAILED,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.VERIFICATION_BOUNDS_FAILED, null, null);
    }

    public IcerikFabrikasiFfmpegManualExecutionStatus getStatus() { return status; }
    public IcerikFabrikasiFfmpegManualExecutionReasonCode getReasonCode() { return reasonCode; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegFixtureMetadata getFixtureMetadata() { return fixtureMetadata; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
