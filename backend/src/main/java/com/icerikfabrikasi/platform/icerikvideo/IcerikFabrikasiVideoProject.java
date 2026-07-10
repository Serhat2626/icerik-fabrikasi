package com.icerikfabrikasi.platform.icerikvideo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FP-019 Task 02: Türkü video project domain object.
 *
 * <p>In-memory representation for the bootstrap phase. Does not require
 * database migration — stored in a ConcurrentHashMap repository.</p>
 *
 * <p>Does not store/expose: lyrics, copyrighted media, public URLs,
 * signed URLs, CDN URLs, storage keys, provider payloads, base64,
 * secrets, tokens, JWT, session, or credentials.</p>
 */
public final class IcerikFabrikasiVideoProject {

    private final UUID id;
    private final String icerikFabrikasiName;
    private final String region;
    private final String sourceNotes;
    private final String mood;
    private final Integer targetDurationMinutes;
    private final String narrationStyle;
    private final String visualStyle;
    private IcerikFabrikasiVideoWorkflowStage workflowStage;
    private IcerikFabrikasiSourceConfidence sourceConfidence;
    private boolean needsHumanReview;
    private boolean copyrightReviewRequired;
    private final String createdByActorReference;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Planning outputs (Task 03) ──
    private IcerikFabrikasiStoryPackage storyPackage;
    private IcerikFabrikasiScenarioPlan scenarioPlan;
    private IcerikFabrikasiStoryboardPlan storyboardPlan;

    // ── Asset outputs (Task 04) ──
    private IcerikFabrikasiSceneAssetPlan sceneAssetPlan;
    private IcerikFabrikasiSceneImageBatch imageBatch;

    // ── Provider mode (FP-020 Task 02) ──
    private IcerikFabrikasiProviderMode providerMode = IcerikFabrikasiProviderMode.DETERMINISTIC_STUB;

    // ── Composition (FP-021 Task 02) ──
    private IcerikFabrikasiVideoCompositionPlan compositionPlan;

    // ── Render (FP-021 Task 03) ──
    private IcerikFabrikasiRenderManifest renderManifest;

    private IcerikFabrikasiVideoProject(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.icerikFabrikasiName = builder.icerikFabrikasiName;
        this.region = builder.region;
        this.sourceNotes = builder.sourceNotes;
        this.mood = builder.mood;
        this.targetDurationMinutes = builder.targetDurationMinutes;
        this.narrationStyle = builder.narrationStyle;
        this.visualStyle = builder.visualStyle;
        this.workflowStage = builder.workflowStage != null ? builder.workflowStage : IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED;
        this.sourceConfidence = builder.sourceConfidence != null ? builder.sourceConfidence : IcerikFabrikasiSourceConfidence.UNKNOWN;
        this.needsHumanReview = builder.needsHumanReview;
        this.copyrightReviewRequired = builder.copyrightReviewRequired;
        this.createdByActorReference = builder.createdByActorReference;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ── Getters ──

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
    public String getCreatedByActorReference() { return createdByActorReference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public IcerikFabrikasiStoryPackage getStoryPackage() { return storyPackage; }
    public IcerikFabrikasiScenarioPlan getScenarioPlan() { return scenarioPlan; }
    public IcerikFabrikasiStoryboardPlan getStoryboardPlan() { return storyboardPlan; }

    public void setStoryPackage(IcerikFabrikasiStoryPackage v) { this.storyPackage = v; }
    public void setScenarioPlan(IcerikFabrikasiScenarioPlan v) { this.scenarioPlan = v; }
    public void setStoryboardPlan(IcerikFabrikasiStoryboardPlan v) { this.storyboardPlan = v; }
    public IcerikFabrikasiSceneAssetPlan getSceneAssetPlan() { return sceneAssetPlan; }
    public IcerikFabrikasiSceneImageBatch getImageBatch() { return imageBatch; }
    public void setSceneAssetPlan(IcerikFabrikasiSceneAssetPlan v) { this.sceneAssetPlan = v; }
    public void setImageBatch(IcerikFabrikasiSceneImageBatch v) { this.imageBatch = v; }
    public IcerikFabrikasiProviderMode getProviderMode() { return providerMode; }
    public void setProviderMode(IcerikFabrikasiProviderMode v) { this.providerMode = v; }
    public IcerikFabrikasiVideoCompositionPlan getCompositionPlan() { return compositionPlan; }
    public void setCompositionPlan(IcerikFabrikasiVideoCompositionPlan v) { this.compositionPlan = v; }
    public IcerikFabrikasiRenderManifest getRenderManifest() { return renderManifest; }
    public void setRenderManifest(IcerikFabrikasiRenderManifest v) { this.renderManifest = v; }

    // ── Render Job (FP-022 Task 02) ──
    private IcerikFabrikasiRenderJob renderJob;
    public IcerikFabrikasiRenderJob getRenderJob() { return renderJob; }
    public void setRenderJob(IcerikFabrikasiRenderJob v) { this.renderJob = v; }

    // ── Stage Mutations (controlled) ──

    public void advanceTo(IcerikFabrikasiVideoWorkflowStage newStage) {
        this.workflowStage = newStage;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IcerikFabrikasiVideoProject{" +
                "id=" + id +
                ", icerikFabrikasiName='" + icerikFabrikasiName + '\'' +
                ", workflowStage=" + workflowStage +
                ", sourceConfidence=" + sourceConfidence +
                ", needsHumanReview=" + needsHumanReview +
                ", copyrightReviewRequired=" + copyrightReviewRequired +
                '}';
    }

    // ── Builder ──

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID id;
        private String icerikFabrikasiName;
        private String region;
        private String sourceNotes;
        private String mood;
        private Integer targetDurationMinutes;
        private String narrationStyle;
        private String visualStyle;
        private IcerikFabrikasiVideoWorkflowStage workflowStage;
        private IcerikFabrikasiSourceConfidence sourceConfidence;
        private boolean needsHumanReview = true;
        private boolean copyrightReviewRequired = true;
        private String createdByActorReference;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder icerikFabrikasiName(String v) { this.icerikFabrikasiName = v; return this; }
        public Builder region(String v) { this.region = v; return this; }
        public Builder sourceNotes(String v) { this.sourceNotes = v; return this; }
        public Builder mood(String v) { this.mood = v; return this; }
        public Builder targetDurationMinutes(Integer v) { this.targetDurationMinutes = v; return this; }
        public Builder narrationStyle(String v) { this.narrationStyle = v; return this; }
        public Builder visualStyle(String v) { this.visualStyle = v; return this; }
        public Builder workflowStage(IcerikFabrikasiVideoWorkflowStage v) { this.workflowStage = v; return this; }
        public Builder sourceConfidence(IcerikFabrikasiSourceConfidence v) { this.sourceConfidence = v; return this; }
        public Builder needsHumanReview(boolean v) { this.needsHumanReview = v; return this; }
        public Builder copyrightReviewRequired(boolean v) { this.copyrightReviewRequired = v; return this; }
        public Builder createdByActorReference(String v) { this.createdByActorReference = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }

        public IcerikFabrikasiVideoProject build() {
            if (icerikFabrikasiName == null || icerikFabrikasiName.isBlank()) {
                throw new IllegalArgumentException("icerikFabrikasiName is required");
            }
            return new IcerikFabrikasiVideoProject(this);
        }
    }
}
