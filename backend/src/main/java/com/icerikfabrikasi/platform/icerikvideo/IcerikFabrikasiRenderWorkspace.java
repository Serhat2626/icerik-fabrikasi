package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-024 Task 03: Composite workspace model.
 * Groups workspace ref, policy, file refs, cleanup metadata.
 * No filesystem access. No real files.
 */
public final class IcerikFabrikasiRenderWorkspace {
    private final IcerikFabrikasiRenderWorkspaceRef workspaceRef;
    private final IcerikFabrikasiRenderWorkspacePolicy policy;
    private final UUID projectId;
    private final UUID jobId;
    private final boolean cleanupEligible;
    private final boolean humanReviewRequired;
    private final boolean autoApproved;
    private final boolean notPublishReady;
    private final boolean notExportReady;

    public IcerikFabrikasiRenderWorkspace(IcerikFabrikasiRenderWorkspaceRef workspaceRef,
                                 IcerikFabrikasiRenderWorkspacePolicy policy,
                                 UUID projectId, UUID jobId) {
        this.workspaceRef = workspaceRef;
        this.policy = policy;
        this.projectId = projectId;
        this.jobId = jobId;
        this.cleanupEligible = true;
        this.humanReviewRequired = true;
        this.autoApproved = false;
        this.notPublishReady = true;
        this.notExportReady = true;
    }

    public IcerikFabrikasiRenderWorkspaceRef getWorkspaceRef() { return workspaceRef; }
    public IcerikFabrikasiRenderWorkspacePolicy getPolicy() { return policy; }
    public UUID getProjectId() { return projectId; }
    public UUID getJobId() { return jobId; }
    public boolean isCleanupEligible() { return cleanupEligible; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public boolean isAutoApproved() { return autoApproved; }
    public boolean isNotPublishReady() { return notPublishReady; }
    public boolean isNotExportReady() { return notExportReady; }
}
