package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-026 Task 02: Safe review summary DTO.
 * Internal-only. No forbidden exposure fields.
 */
public final class IcerikFabrikasiInternalRenderReviewSummary {
    private final String reviewRef;
    private final String jobRef;
    private final IcerikFabrikasiInternalRenderReviewState reviewState;
    private final IcerikFabrikasiInternalRenderReviewReasonCode reasonCode;
    private final boolean humanReviewRequired;
    private final boolean autoApproved;
    private final boolean publicDeliveryAvailable;
    private final boolean internalOnly;
    private final boolean notPublishReady;
    private final boolean notExportReady;
    private final List<String> safetyFlags;

    public IcerikFabrikasiInternalRenderReviewSummary(String reviewRef, String jobRef,
            IcerikFabrikasiInternalRenderReviewState reviewState,
            IcerikFabrikasiInternalRenderReviewReasonCode reasonCode) {
        this.reviewRef = reviewRef;
        this.jobRef = jobRef;
        this.reviewState = reviewState;
        this.reasonCode = reasonCode;
        this.humanReviewRequired = true;
        this.autoApproved = false;
        this.publicDeliveryAvailable = false;
        this.internalOnly = true;
        this.notPublishReady = true;
        this.notExportReady = true;
        this.safetyFlags = List.of("INTERNAL_ONLY", "NO_PUBLISH", "NO_EXPORT",
                "NO_DOWNLOAD", "HUMAN_REVIEW_REQUIRED", "NO_AUTO_APPROVE");
    }

    public String getReviewRef() { return reviewRef; }
    public String getJobRef() { return jobRef; }
    public IcerikFabrikasiInternalRenderReviewState getReviewState() { return reviewState; }
    public IcerikFabrikasiInternalRenderReviewReasonCode getReasonCode() { return reasonCode; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public boolean isPublicDeliveryAvailable() { return publicDeliveryAvailable; }
    public boolean isInternalOnly() { return internalOnly; }
    public boolean isNotPublishReady() { return notPublishReady; }
    public boolean isNotExportReady() { return notExportReady; }
    public List<String> getSafetyFlags() { return safetyFlags; }
}
