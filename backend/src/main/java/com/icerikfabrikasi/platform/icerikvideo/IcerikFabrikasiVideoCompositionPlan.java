package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-021 Task 02: Video composition plan — assembles storyboard + scene images +
 * narration plan + timing into an internal composition package.
 *
 * <p>No lyrics, no copyrighted audio/video, no rendered video file,
 * no downloadable file, no public/signed/CDN URL, no storage key,
 * no base64/binary, no raw provider payload.</p>
 */
public final class IcerikFabrikasiVideoCompositionPlan {

    private final UUID compositionId;
    private final UUID projectId;
    private final int totalDurationSeconds;
    private final List<IcerikFabrikasiVideoCompositionScene> scenes;
    private final List<IcerikFabrikasiNarrationSegment> narrationSegments;
    private final IcerikFabrikasiSceneTimingPlan timingPlan;
    private final IcerikFabrikasiTransitionPlan transitionPlan;
    private final boolean humanReviewRequired;
    private final boolean copyrightReviewRequired;
    private final boolean autoApproved;
    private final List<String> reviewFlags;

    public IcerikFabrikasiVideoCompositionPlan(UUID compositionId, UUID projectId,
                                      int totalDurationSeconds,
                                      List<IcerikFabrikasiVideoCompositionScene> scenes,
                                      List<IcerikFabrikasiNarrationSegment> narrationSegments,
                                      IcerikFabrikasiSceneTimingPlan timingPlan,
                                      IcerikFabrikasiTransitionPlan transitionPlan,
                                      List<String> reviewFlags) {
        this.compositionId = compositionId;
        this.projectId = projectId;
        this.totalDurationSeconds = totalDurationSeconds;
        this.scenes = scenes != null ? List.copyOf(scenes) : List.of();
        this.narrationSegments = narrationSegments != null ? List.copyOf(narrationSegments) : List.of();
        this.timingPlan = timingPlan;
        this.transitionPlan = transitionPlan;
        this.humanReviewRequired = true;
        this.copyrightReviewRequired = true;
        this.autoApproved = false;
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
    }

    public UUID getCompositionId() { return compositionId; }
    public UUID getProjectId() { return projectId; }
    public int getTotalDurationSeconds() { return totalDurationSeconds; }
    public List<IcerikFabrikasiVideoCompositionScene> getScenes() { return scenes; }
    public List<IcerikFabrikasiNarrationSegment> getNarrationSegments() { return narrationSegments; }
    public IcerikFabrikasiSceneTimingPlan getTimingPlan() { return timingPlan; }
    public IcerikFabrikasiTransitionPlan getTransitionPlan() { return transitionPlan; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public List<String> getReviewFlags() { return reviewFlags; }
}
