package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-030 Task 01: Safe process result. No stdout/stderr/rawLog/path.
 */
public final class IcerikFabrikasiFfmpegProcessResult {
    private final IcerikFabrikasiFfmpegProcessStatus status;
    private final IcerikFabrikasiFfmpegProcessReasonCode reasonCode;
    private final String outputRef;
    private final long durationMs;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus status,
            IcerikFabrikasiFfmpegProcessReasonCode reasonCode, String outputRef, long durationMs) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.outputRef = outputRef;
        this.durationMs = durationMs;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegProcessResult denied(IcerikFabrikasiFfmpegProcessReasonCode reason) {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.GATE_DENIED, reason, null, 0);
    }

    public static IcerikFabrikasiFfmpegProcessResult invalid(IcerikFabrikasiFfmpegProcessReasonCode reason) {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.REQUEST_INVALID, reason, null, 0);
    }

    public static IcerikFabrikasiFfmpegProcessResult completed(String outputRef, long durationMs) {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.PROCESS_COMPLETED_SAFE,
                IcerikFabrikasiFfmpegProcessReasonCode.PROCESS_COMPLETED_SAFE, outputRef, durationMs);
    }

    public static IcerikFabrikasiFfmpegProcessResult timedOut(long durationMs) {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.PROCESS_TIMED_OUT,
                IcerikFabrikasiFfmpegProcessReasonCode.PROCESS_TIMED_OUT_SAFE, null, durationMs);
    }

    public static IcerikFabrikasiFfmpegProcessResult failed() {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.PROCESS_FAILED_SAFE,
                IcerikFabrikasiFfmpegProcessReasonCode.PROCESS_FAILED_INTERNAL, null, 0);
    }

    public static IcerikFabrikasiFfmpegProcessResult skipped() {
        return new IcerikFabrikasiFfmpegProcessResult(IcerikFabrikasiFfmpegProcessStatus.PROCESS_SKIPPED,
                IcerikFabrikasiFfmpegProcessReasonCode.PROCESS_SKIPPED_SAFE, null, 0);
    }

    public IcerikFabrikasiFfmpegProcessStatus getStatus() { return status; }
    public IcerikFabrikasiFfmpegProcessReasonCode getReasonCode() { return reasonCode; }
    public String getOutputRef() { return outputRef; }
    public long getDurationMs() { return durationMs; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
