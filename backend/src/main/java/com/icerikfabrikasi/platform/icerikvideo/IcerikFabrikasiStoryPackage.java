package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-019 Task 03: Türkü story package — AI-researched cultural/historical context.
 *
 * <p>Does not store lyrics, copyrighted media, public URLs, raw provider payloads,
 * or credential/session data.</p>
 */
public final class IcerikFabrikasiStoryPackage {

    private final String icerikFabrikasiName;
    private final String regionContext;
    private final String originSummary;
    private final String emotionalTheme;
    private final String culturalSignificance;
    private final String narrativeAngle;
    private final String storySummary;
    private final IcerikFabrikasiSourceConfidence sourceConfidence;
    private final boolean needsHumanReview;
    private final boolean copyrightReviewRequired;
    private final List<String> reviewFlags;

    private IcerikFabrikasiStoryPackage(Builder b) {
        this.icerikFabrikasiName = b.icerikFabrikasiName;
        this.regionContext = b.regionContext;
        this.originSummary = b.originSummary;
        this.emotionalTheme = b.emotionalTheme;
        this.culturalSignificance = b.culturalSignificance;
        this.narrativeAngle = b.narrativeAngle;
        this.storySummary = b.storySummary;
        this.sourceConfidence = b.sourceConfidence;
        this.needsHumanReview = b.needsHumanReview;
        this.copyrightReviewRequired = b.copyrightReviewRequired;
        this.reviewFlags = b.reviewFlags != null ? List.copyOf(b.reviewFlags) : List.of();
    }

    public String getIcerikFabrikasiName() { return icerikFabrikasiName; }
    public String getRegionContext() { return regionContext; }
    public String getOriginSummary() { return originSummary; }
    public String getEmotionalTheme() { return emotionalTheme; }
    public String getCulturalSignificance() { return culturalSignificance; }
    public String getNarrativeAngle() { return narrativeAngle; }
    public String getStorySummary() { return storySummary; }
    public IcerikFabrikasiSourceConfidence getSourceConfidence() { return sourceConfidence; }
    public boolean isNeedsHumanReview() { return needsHumanReview; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public List<String> getReviewFlags() { return reviewFlags; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String icerikFabrikasiName;
        private String regionContext;
        private String originSummary;
        private String emotionalTheme;
        private String culturalSignificance;
        private String narrativeAngle;
        private String storySummary;
        private IcerikFabrikasiSourceConfidence sourceConfidence = IcerikFabrikasiSourceConfidence.UNKNOWN;
        private boolean needsHumanReview = true;
        private boolean copyrightReviewRequired = true;
        private List<String> reviewFlags;

        public Builder icerikFabrikasiName(String v) { this.icerikFabrikasiName = v; return this; }
        public Builder regionContext(String v) { this.regionContext = v; return this; }
        public Builder originSummary(String v) { this.originSummary = v; return this; }
        public Builder emotionalTheme(String v) { this.emotionalTheme = v; return this; }
        public Builder culturalSignificance(String v) { this.culturalSignificance = v; return this; }
        public Builder narrativeAngle(String v) { this.narrativeAngle = v; return this; }
        public Builder storySummary(String v) { this.storySummary = v; return this; }
        public Builder sourceConfidence(IcerikFabrikasiSourceConfidence v) { this.sourceConfidence = v; return this; }
        public Builder needsHumanReview(boolean v) { this.needsHumanReview = v; return this; }
        public Builder copyrightReviewRequired(boolean v) { this.copyrightReviewRequired = v; return this; }
        public Builder reviewFlags(List<String> v) { this.reviewFlags = v; return this; }

        public IcerikFabrikasiStoryPackage build() { return new IcerikFabrikasiStoryPackage(this); }
    }
}
