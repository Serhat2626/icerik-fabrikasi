package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-024 Task 04: FFmpeg execution request.
 * Contains only opaque refs and validated metadata — no raw paths.
 */
public final class IcerikFabrikasiFfmpegExecutionRequest {
    private final UUID projectId;
    private final UUID jobId;
    private final IcerikFabrikasiRenderSecureConfig config;
    private final IcerikFabrikasiRenderWorkspaceRef workspaceRef;
    private final IcerikFabrikasiFfmpegCommandPlan commandPlan;
    private final IcerikFabrikasiRenderExecutionPlan renderPlan;

    public IcerikFabrikasiFfmpegExecutionRequest(UUID projectId, UUID jobId,
                                        IcerikFabrikasiRenderSecureConfig config,
                                        IcerikFabrikasiRenderWorkspaceRef workspaceRef,
                                        IcerikFabrikasiFfmpegCommandPlan commandPlan,
                                        IcerikFabrikasiRenderExecutionPlan renderPlan) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.config = config;
        this.workspaceRef = workspaceRef;
        this.commandPlan = commandPlan;
        this.renderPlan = renderPlan;
    }

    public UUID getProjectId() { return projectId; }
    public UUID getJobId() { return jobId; }
    public IcerikFabrikasiRenderSecureConfig getConfig() { return config; }
    public IcerikFabrikasiRenderWorkspaceRef getWorkspaceRef() { return workspaceRef; }
    public IcerikFabrikasiFfmpegCommandPlan getCommandPlan() { return commandPlan; }
    public IcerikFabrikasiRenderExecutionPlan getRenderPlan() { return renderPlan; }
}
