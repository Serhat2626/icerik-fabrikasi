package com.icerikfabrikasi.platform.icerikvideo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class IcerikFabrikasiRenderJob {
    private final UUID jobId;
    private final UUID projectId;
    private final String manifestRef;
    private IcerikFabrikasiRenderJobStatus status;
    private final boolean humanReviewRequired;
    private final boolean copyrightReviewRequired;
    private final boolean autoApproved;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<IcerikFabrikasiRenderAttempt> attempts;
    private IcerikFabrikasiRenderArtifact artifact;
    private final List<String> reviewFlags;

    public IcerikFabrikasiRenderJob(UUID jobId, UUID projectId, String manifestRef,
                           List<String> reviewFlags) {
        this.jobId = jobId;
        this.projectId = projectId;
        this.manifestRef = manifestRef;
        this.status = IcerikFabrikasiRenderJobStatus.PREPARED;
        this.humanReviewRequired = true;
        this.copyrightReviewRequired = true;
        this.autoApproved = false;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.attempts = new ArrayList<>();
        this.artifact = null;
        this.reviewFlags = reviewFlags != null ? new ArrayList<>(reviewFlags) : new ArrayList<>();
    }

    public UUID getJobId() { return jobId; }
    public UUID getProjectId() { return projectId; }
    public String getManifestRef() { return manifestRef; }
    public IcerikFabrikasiRenderJobStatus getStatus() { return status; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isCopyrightReviewRequired() { return copyrightReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<IcerikFabrikasiRenderAttempt> getAttempts() { return List.copyOf(attempts); }
    public IcerikFabrikasiRenderArtifact getArtifact() { return artifact; }
    public List<String> getReviewFlags() { return List.copyOf(reviewFlags); }

    public void setStatus(IcerikFabrikasiRenderJobStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    public void addAttempt(IcerikFabrikasiRenderAttempt attempt) {
        this.attempts.add(attempt);
        this.updatedAt = Instant.now();
    }
    public void setArtifact(IcerikFabrikasiRenderArtifact artifact) {
        this.artifact = artifact;
        this.updatedAt = Instant.now();
    }
}
