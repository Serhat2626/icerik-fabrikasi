package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 03: Unified response for prepare-story/scenario/storyboard.
 * Safe fields only. No provider data, no public URLs, no credentials.
 */
public final class IcerikFabrikasiPreparationResponse {

    private final UUID projectId;
    private final IcerikFabrikasiVideoWorkflowStage newStage;
    private final String safeMessage;
    private final IcerikFabrikasiSourceConfidence sourceConfidence;
    private final boolean needsHumanReview;
    private final boolean copyrightReviewRequired;
    private final List<String> reviewFlags;

    public IcerikFabrikasiPreparationResponse(UUID projectId, IcerikFabrikasiVideoWorkflowStage newStage,
                                     String safeMessage, IcerikFabrikasiSourceConfidence sourceConfidence,
                                     boolean needsHumanReview, boolean copyrightReviewRequired,
                                     List<String> reviewFlags) {
        this.projectId = projectId;
        this.newStage = newStage;
        this.safeMessage = safeMessage;
        this.sourceConfidence = sourceConfidence;
        this.needsHumanReview = needsHumanReview;
        this.copyrightReviewRequired = copyrightReviewRequired;
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
    }

    public UUID getProjectId() { return projectId; }
    public IcerikFabrikasiVideoWorkflowStage getNewStage() { return newStage; }
    public String getSafeMessage() { return safeMessage; }
    public IcerikFabrikasiSourceConfidence getSourceConfidence() { return sourceConfidence; }
    public boolean isNeedsHumanReview() { return needsHumanReview; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public List<String> getReviewFlags() { return reviewFlags; }
}
