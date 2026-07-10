package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-029 Task 02: Trial result — safe wrapper.
 */
public final class IcerikFabrikasiInternalTrialResult {
    private final IcerikFabrikasiInternalTrialStatus status;
    private final IcerikFabrikasiInternalTrialReasonCode reasonCode;
    private final String outputRef;
    private final IcerikFabrikasiFfmpegVerificationCandidate verificationCandidate;
    private final boolean internalOnly;
    private final boolean publicDeliveryAvailable;
    private final boolean autoApproved;
    private final List<String> safetyFlags;

    private IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus status,
            IcerikFabrikasiInternalTrialReasonCode reasonCode, String outputRef,
            IcerikFabrikasiFfmpegVerificationCandidate candidate) {
        this.status = status;
        this.reasonCode = reasonCode;
        this.outputRef = outputRef;
        this.verificationCandidate = candidate;
        this.internalOnly = true;
        this.publicDeliveryAvailable = false;
        this.autoApproved = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "NO_PUBLIC_URL", "NO_AUTO_APPROVE");
    }

    public static IcerikFabrikasiInternalTrialResult denied(IcerikFabrikasiInternalTrialReasonCode reason) {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.GATE_DENIED, reason, null, null);
    }

    public static IcerikFabrikasiInternalTrialResult skipped(IcerikFabrikasiInternalTrialReasonCode reason) {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.EXECUTION_SKIPPED, reason, null, null);
    }

    public static IcerikFabrikasiInternalTrialResult simulated(String outputRef, IcerikFabrikasiFfmpegVerificationCandidate candidate) {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.EXECUTION_SIMULATED,
                IcerikFabrikasiInternalTrialReasonCode.EXECUTION_SIMULATED_SAFE, outputRef, candidate);
    }

    public static IcerikFabrikasiInternalTrialResult attempted(String outputRef, IcerikFabrikasiFfmpegVerificationCandidate candidate) {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.EXECUTION_ATTEMPTED_DEV_TEST,
                IcerikFabrikasiInternalTrialReasonCode.EXECUTION_ATTEMPTED_SAFE, outputRef, candidate);
    }

    public static IcerikFabrikasiInternalTrialResult candidateCreated(String outputRef, IcerikFabrikasiFfmpegVerificationCandidate candidate) {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.VERIFICATION_CANDIDATE_CREATED,
                IcerikFabrikasiInternalTrialReasonCode.CANDIDATE_CREATED_SAFE, outputRef, candidate);
    }

    public static IcerikFabrikasiInternalTrialResult planInvalid() {
        return new IcerikFabrikasiInternalTrialResult(IcerikFabrikasiInternalTrialStatus.PLAN_INVALID,
                IcerikFabrikasiInternalTrialReasonCode.PLAN_BOUNDS_INVALID, null, null);
    }

    public IcerikFabrikasiInternalTrialStatus getStatus() { return status; }
    public IcerikFabrikasiInternalTrialReasonCode getReasonCode() { return reasonCode; }
    public String getOutputRef() { return outputRef; }
    public IcerikFabrikasiFfmpegVerificationCandidate getVerificationCandidate() { return verificationCandidate; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
