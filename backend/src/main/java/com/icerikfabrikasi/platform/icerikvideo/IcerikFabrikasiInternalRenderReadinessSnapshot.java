package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-025 Task 03: Readiness snapshot.
 * Safe, bounded, no forbidden exposure.
 */
public final class IcerikFabrikasiInternalRenderReadinessSnapshot {
    private final IcerikFabrikasiInternalRenderReadinessStatus status;
    private final List<IcerikFabrikasiInternalRenderReadinessIssue> issues;
    private final boolean humanReviewRequired;
    private final boolean autoApproved;
    private final boolean publicDeliveryAvailable;
    private final List<String> safetyFlags;

    public IcerikFabrikasiInternalRenderReadinessSnapshot(IcerikFabrikasiInternalRenderReadinessStatus status,
                                                  List<IcerikFabrikasiInternalRenderReadinessIssue> issues) {
        this.status = status;
        this.issues = issues != null ? List.copyOf(issues) : List.of();
        this.humanReviewRequired = true;
        this.autoApproved = false;
        this.publicDeliveryAvailable = false;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "HUMAN_REVIEW_REQUIRED");
    }

    public IcerikFabrikasiInternalRenderReadinessStatus getStatus() { return status; }
    public List<IcerikFabrikasiInternalRenderReadinessIssue> getIssues() { return issues; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
