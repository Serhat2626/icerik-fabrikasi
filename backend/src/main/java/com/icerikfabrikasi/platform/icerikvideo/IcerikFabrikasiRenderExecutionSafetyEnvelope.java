package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-022 Task 03: Safety envelope for render execution.
 * Wraps all safety flags and review gates.
 */
public final class IcerikFabrikasiRenderExecutionSafetyEnvelope {
    private final boolean humanReviewRequired;
    private final boolean copyrightReviewRequired;
    private final boolean autoApproved;
    private final boolean lyricsIncluded;
    private final boolean copyrightClearanceVerified;
    private final String renderMode;
    private final List<String> safetyFlags;
    private final List<String> reviewFlags;
    private final List<IcerikFabrikasiRenderFailureCode> failureCodes;
    private final List<String> blockedReasons;

    public IcerikFabrikasiRenderExecutionSafetyEnvelope(String renderMode,
                                               List<String> safetyFlags,
                                               List<String> reviewFlags,
                                               List<IcerikFabrikasiRenderFailureCode> failureCodes,
                                               List<String> blockedReasons) {
        this.humanReviewRequired = true;
        this.copyrightReviewRequired = true;
        this.autoApproved = false;
        this.lyricsIncluded = false;
        this.copyrightClearanceVerified = false;
        this.renderMode = renderMode;
        this.safetyFlags = safetyFlags != null ? List.copyOf(safetyFlags) : List.of();
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
        this.failureCodes = failureCodes != null ? List.copyOf(failureCodes) : List.of();
        this.blockedReasons = blockedReasons != null ? List.copyOf(blockedReasons) : List.of();
    }

    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public boolean isLyricsIncluded() { return lyricsIncluded; }
    public boolean isCopyrightClearanceVerified() { return copyrightClearanceVerified; }
    public String getRenderMode() { return renderMode; }
    public List<String> getSafetyFlags() { return safetyFlags; }
    public List<String> getReviewFlags() { return reviewFlags; }
    public List<IcerikFabrikasiRenderFailureCode> getFailureCodes() { return failureCodes; }
    public List<String> getBlockedReasons() { return blockedReasons; }
}
