package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 04: Image generation batch.
 * Safe internal references only — no base64, no public URLs, no storage keys.
 */
public final class IcerikFabrikasiSceneImageBatch {

    private final UUID batchId;
    private final UUID projectId;
    private IcerikFabrikasiImageBatchStatus status;
    private final List<IcerikFabrikasiSceneImageRequest> imageRequests;
    private List<IcerikFabrikasiSceneImageResult> imageResults;
    private final List<String> reviewFlags;

    public IcerikFabrikasiSceneImageBatch(UUID batchId, UUID projectId,
                                 IcerikFabrikasiImageBatchStatus status,
                                 List<IcerikFabrikasiSceneImageRequest> imageRequests,
                                 List<String> reviewFlags) {
        this.batchId = batchId;
        this.projectId = projectId;
        this.status = status;
        this.imageRequests = imageRequests != null ? List.copyOf(imageRequests) : List.of();
        this.imageResults = List.of();
        this.reviewFlags = reviewFlags != null ? List.copyOf(reviewFlags) : List.of();
    }

    public UUID getBatchId() { return batchId; }
    public UUID getProjectId() { return projectId; }
    public IcerikFabrikasiImageBatchStatus getStatus() { return status; }
    public List<IcerikFabrikasiSceneImageRequest> getImageRequests() { return imageRequests; }
    public List<IcerikFabrikasiSceneImageResult> getImageResults() { return imageResults; }
    public List<String> getReviewFlags() { return reviewFlags; }

    public void setStatus(IcerikFabrikasiImageBatchStatus status) { this.status = status; }
    public void setImageResults(List<IcerikFabrikasiSceneImageResult> results) {
        this.imageResults = results != null ? List.copyOf(results) : List.of();
    }
}
