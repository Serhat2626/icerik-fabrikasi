package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-028 Task 02: Adapter result wrapping gate result + output ref.
 */
public final class IcerikFabrikasiFfmpegDevTestAdapterResult {
    private final boolean allowed;
    private final IcerikFabrikasiFfmpegExecutionBoundaryResult gateResult;
    private final String outputRef;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiFfmpegDevTestAdapterResult(boolean allowed,
            IcerikFabrikasiFfmpegExecutionBoundaryResult gateResult, String outputRef) {
        this.allowed = allowed;
        this.gateResult = gateResult;
        this.outputRef = outputRef;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiFfmpegDevTestAdapterResult denied(IcerikFabrikasiFfmpegExecutionBoundaryResult gate) {
        return new IcerikFabrikasiFfmpegDevTestAdapterResult(false, gate, null);
    }

    public static IcerikFabrikasiFfmpegDevTestAdapterResult completed(
            IcerikFabrikasiFfmpegExecutionBoundaryResult gate, IcerikFabrikasiFfmpegInternalOutputRef ref) {
        return new IcerikFabrikasiFfmpegDevTestAdapterResult(true, gate, ref.toOpaqueRef());
    }

    public boolean isAllowed() { return allowed; }
    public IcerikFabrikasiFfmpegExecutionBoundaryResult getGateResult() { return gateResult; }
    public String getOutputRef() { return outputRef; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
