package com.icerikfabrikasi.platform.icerikvideo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FP-019 Task 02: Project detail response DTO. Safe fields only.
 */
public final class IcerikFabrikasiVideoProjectResponse {
    private final UUID id;
    private final String icerikFabrikasiName;
    private final String region;
    private final String sourceNotes;
    private final String mood;
    private final Integer targetDurationMinutes;
    private final String narrationStyle;
    private final String visualStyle;
    private final IcerikFabrikasiVideoWorkflowStage workflowStage;
    private final IcerikFabrikasiSourceConfidence sourceConfidence;
    private final boolean needsHumanReview;
    private final boolean copyrightReviewRequired;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private IcerikFabrikasiVideoProjectResponse(IcerikFabrikasiVideoProject p) {
        this.id = p.getId();
        this.icerikFabrikasiName = p.getIcerikFabrikasiName();
        this.region = p.getRegion();
        this.sourceNotes = p.getSourceNotes();
        this.mood = p.getMood();
        this.targetDurationMinutes = p.getTargetDurationMinutes();
        this.narrationStyle = p.getNarrationStyle();
        this.visualStyle = p.getVisualStyle();
        this.workflowStage = p.getWorkflowStage();
        this.sourceConfidence = p.getSourceConfidence();
        this.needsHumanReview = p.isNeedsHumanReview();
        this.copyrightReviewRequired = p.isCopyrightReviewRequired();
        this.createdAt = p.getCreatedAt();
        this.updatedAt = p.getUpdatedAt();
    }

    public static IcerikFabrikasiVideoProjectResponse from(IcerikFabrikasiVideoProject p) {
        return new IcerikFabrikasiVideoProjectResponse(p);
    }

    public UUID getId() { return id; }
    public String getIcerikFabrikasiName() { return icerikFabrikasiName; }
    public String getRegion() { return region; }
    public String getSourceNotes() { return sourceNotes; }
    public String getMood() { return mood; }
    public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
    public String getNarrationStyle() { return narrationStyle; }
    public String getVisualStyle() { return visualStyle; }
    public IcerikFabrikasiVideoWorkflowStage getWorkflowStage() { return workflowStage; }
    public IcerikFabrikasiSourceConfidence getSourceConfidence() { return sourceConfidence; }
    public boolean isNeedsHumanReview() { return needsHumanReview; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
