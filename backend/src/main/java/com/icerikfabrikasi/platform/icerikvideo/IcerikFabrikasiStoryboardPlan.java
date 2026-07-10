package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-019 Task 03: Storyboard plan — scene-by-scene visual plan.
 * No public URLs, no copyrighted media, no raw provider data.
 */
public final class IcerikFabrikasiStoryboardPlan {

    private final List<IcerikFabrikasiStoryboardScene> scenes;
    private final int totalEstimatedDurationSeconds;
    private final List<String> reviewFlags;

    public IcerikFabrikasiStoryboardPlan(List<IcerikFabrikasiStoryboardScene> scenes, int totalEstimatedDurationSeconds,
                                List<String> reviewFlags) {
        this.scenes = scenes != null ? List.copyOf(scenes) : List.of();
        this.totalEstimatedDurationSeconds = totalEstimatedDurationSeconds;
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
    }

    public List<IcerikFabrikasiStoryboardScene> getScenes() { return scenes; }
    public int getTotalEstimatedDurationSeconds() { return totalEstimatedDurationSeconds; }
    public List<String> getReviewFlags() { return reviewFlags; }
}
