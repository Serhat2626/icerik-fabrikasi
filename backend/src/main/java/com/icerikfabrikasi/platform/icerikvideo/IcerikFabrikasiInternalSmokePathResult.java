package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-029 Task 01: Smoke path result.
 */
public final class IcerikFabrikasiInternalSmokePathResult {
    private final IcerikFabrikasiInternalSmokePathStatus status;
    private final IcerikFabrikasiInternalSmokePathReasonCode reasonCode;
    private final String outputRef;
    private final IcerikFabrikasiInternalSmokeRenderPlan plan;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiInternalSmokePathResult(IcerikFabrikasiInternalSmokePathStatus status,
            IcerikFabrikasiInternalSmokePathReasonCode reasonCode, String outputRef,
            IcerikFabrikasiInternalSmokeRenderPlan plan) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.outputRef = outputRef;
        this.plan = plan;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiInternalSmokePathResult denied(IcerikFabrikasiInternalSmokePathReasonCode reason) {
        return new IcerikFabrikasiInternalSmokePathResult(IcerikFabrikasiInternalSmokePathStatus.DENIED, reason, null, null);
    }

    public static IcerikFabrikasiInternalSmokePathResult completed(String outputRef, IcerikFabrikasiInternalSmokeRenderPlan plan) {
        return new IcerikFabrikasiInternalSmokePathResult(IcerikFabrikasiInternalSmokePathStatus.SMOKE_COMPLETE_INTERNAL,
                IcerikFabrikasiInternalSmokePathReasonCode.SMOKE_COMPLETE_SAFE, outputRef, plan);
    }

    public static IcerikFabrikasiInternalSmokePathResult candidateCreated(String outputRef, IcerikFabrikasiInternalSmokeRenderPlan plan) {
        return new IcerikFabrikasiInternalSmokePathResult(IcerikFabrikasiInternalSmokePathStatus.CANDIDATE_CREATED,
                IcerikFabrikasiInternalSmokePathReasonCode.CANDIDATE_CREATED_SAFE, outputRef, plan);
    }

    public IcerikFabrikasiInternalSmokePathStatus getStatus() { return status; }
    public IcerikFabrikasiInternalSmokePathReasonCode getReasonCode() { return reasonCode; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiInternalSmokeRenderPlan getPlan() { return plan; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
