package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-028 Task 01: Safe execution boundary result.
 * No stdout/stderr/raw log/command line/paths/binary.
 */
public final class IcerikFabrikasiFfmpegExecutionBoundaryResult {
    private final IcerikFabrikasiFfmpegBoundaryResultStatus status;
    private final IcerikFabrikasiFfmpegBoundaryReasonCode reasonCode;
    private final IcerikFabrikasiFfmpegRealExecutionMode mode;
    private final IcerikFabrikasiFfmpegExecutionEnvironment environment;
    private final String jobRef;
    private final String queueItemRef;
    private final String reviewRef;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegExecutionBoundaryResult(IcerikFabrikasiFfmpegBoundaryResultStatus status,
            IcerikFabrikasiFfmpegBoundaryReasonCode reasonCode, IcerikFabrikasiFfmpegRealExecutionMode mode,
            IcerikFabrikasiFfmpegExecutionEnvironment environment,
            String jobRef, String queueItemRef, String reviewRef) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.mode = mode;
        this.environment = environment;
        this.jobRef = jobRef;
        this.queueItemRef = queueItemRef;
        this.reviewRef = reviewRef;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_DELIVERY", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegExecutionBoundaryResult denied(IcerikFabrikasiFfmpegBoundaryReasonCode reason,
            IcerikFabrikasiFfmpegRealExecutionMode mode, IcerikFabrikasiFfmpegExecutionEnvironment env) {
        return new IcerikFabrikasiFfmpegExecutionBoundaryResult(IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED,
                reason, mode, env, null, null, null);
    }

    public static IcerikFabrikasiFfmpegExecutionBoundaryResult safe(IcerikFabrikasiFfmpegBoundaryResultStatus status,
            IcerikFabrikasiFfmpegBoundaryReasonCode reason, IcerikFabrikasiFfmpegRealExecutionMode mode,
            IcerikFabrikasiFfmpegExecutionEnvironment env, String jobRef, String queueRef, String reviewRef) {
        return new IcerikFabrikasiFfmpegExecutionBoundaryResult(status, reason, mode, env, jobRef, queueRef, reviewRef);
    }

    public IcerikFabrikasiFfmpegBoundaryResultStatus getStatus() { return status; }
    public IcerikFabrikasiFfmpegBoundaryReasonCode getReasonCode() { return reasonCode; }
    public IcerikFabrikasiFfmpegRealExecutionMode getMode() { return mode; }
    public IcerikFabrikasiFfmpegExecutionEnvironment getEnvironment() { return environment; }
    public String getJobRef() { return jobRef; }
    public String getQueueItemRef() { return queueItemRef; }
    public String getReviewRef() { return reviewRef; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
