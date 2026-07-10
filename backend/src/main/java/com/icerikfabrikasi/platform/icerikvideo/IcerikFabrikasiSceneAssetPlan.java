package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 04: Scene asset plan — collection of asset items per storyboard.
 * No public URLs, no base64, no storage keys.
 */
public final class IcerikFabrikasiSceneAssetPlan {

    private final UUID planId;
    private final UUID projectId;
    private final List<IcerikFabrikasiSceneAssetItem> items;
    private final List<String> reviewFlags;

    public IcerikFabrikasiSceneAssetPlan(UUID planId, UUID projectId,
                                List<IcerikFabrikasiSceneAssetItem> items, List<String> reviewFlags) {
        this.planId = planId;
        this.projectId = projectId;
        this.items = items != null ? List.copyOf(items) : List.of();
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
    }

    public UUID getPlanId() { return planId; }
    public UUID getProjectId() { return projectId; }
    public List<IcerikFabrikasiSceneAssetItem> getItems() { return items; }
    public List<String> getReviewFlags() { return reviewFlags; }
}
