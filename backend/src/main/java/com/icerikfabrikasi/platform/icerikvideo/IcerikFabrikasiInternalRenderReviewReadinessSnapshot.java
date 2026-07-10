package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * FP-026 Task 03: Review readiness snapshot derived from FP-025 readiness/safety-chain.
 */
public final class IcerikFabrikasiInternalRenderReviewReadinessSnapshot {
    private final IcerikFabrikasiInternalRenderReviewReadinessStatus status;
    private final List<IcerikFabrikasiInternalRenderReviewReadinessIssue> issues;
    private final boolean humanReviewRequired;
    private final boolean autoApproved;
    private final boolean publicDeliveryAvailable;
    private final boolean internalOnly;
    private final List<String> safetyFlags;

    public IcerikFabrikasiInternalRenderReviewReadinessSnapshot(
            IcerikFabrikasiInternalRenderReviewReadinessStatus status,
            List<IcerikFabrikasiInternalRenderReviewReadinessIssue> issues) {
        this.status = status;
        this.issues = issues != null ? List.copyOf(issues) : List.of();
        this.humanReviewRequired = true;
        this.autoApproved = false;
        this.publicDeliveryAvailable = false;
        this.internalOnly = true;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "HUMAN_REVIEW_REQUIRED", "NO_AUTO_APPROVE");
    }

    /**
     * Derive review readiness from FP-025 readiness + safety-chain snapshots.
     */
    public static IcerikFabrikasiInternalRenderReviewReadinessSnapshot fromFP025(
            IcerikFabrikasiInternalRenderReadinessSnapshot readiness,
            IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain) {

        List<IcerikFabrikasiInternalRenderReviewReadinessIssue> issues = new ArrayList<>();
        issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.HUMAN_REVIEW_REQUIRED);
        issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.PUBLIC_DELIVERY_FORBIDDEN);
        issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.AUTO_APPROVAL_FORBIDDEN);

        boolean jobReady = readiness != null
                && readiness.getStatus() == IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY;
        boolean chainSafe = safetyChain != null
                && safetyChain.isConfigValid()
                && safetyChain.isWorkspaceSafe()
                && safetyChain.isExecutionBoundarySafe()
                && safetyChain.isArtifactVerified();

        if (!jobReady) {
            issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.JOB_NOT_READY);
            if (readiness != null && readiness.getStatus() != IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY) {
                issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.READINESS_BLOCKED);
            }
        }
        if (!chainSafe) {
            if (safetyChain != null && !safetyChain.isArtifactVerified()) {
                issues.add(IcerikFabrikasiInternalRenderReviewReadinessIssue.ARTIFACT_VERIFICATION_FAILED);
            }
        }

        IcerikFabrikasiInternalRenderReviewReadinessStatus status;
        if (jobReady && chainSafe) {
            status = IcerikFabrikasiInternalRenderReviewReadinessStatus.READY_FOR_HUMAN_REVIEW;
        } else {
            status = IcerikFabrikasiInternalRenderReviewReadinessStatus.NOT_READY_FOR_REVIEW;
        }

        return new IcerikFabrikasiInternalRenderReviewReadinessSnapshot(status, issues);
    }

    public IcerikFabrikasiInternalRenderReviewReadinessStatus getStatus() { return status; }
    public List<IcerikFabrikasiInternalRenderReviewReadinessIssue> getIssues() { return issues; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isInternalOnly() { return internalOnly; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
