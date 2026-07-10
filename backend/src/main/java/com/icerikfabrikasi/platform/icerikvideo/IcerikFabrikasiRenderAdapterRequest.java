package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-023 Task 02: Render adapter request.
 * Contains only safe internal references. No file paths, URLs, or secrets.
 */
public final class IcerikFabrikasiRenderAdapterRequest {
    private final UUID projectId;
    private final UUID jobId;
    private final IcerikFabrikasiRenderExecutionPlan executionPlan;
    private final IcerikFabrikasiRenderAdapterMode requestedMode;

    public IcerikFabrikasiRenderAdapterRequest(UUID projectId, UUID jobId,
                                      IcerikFabrikasiRenderExecutionPlan executionPlan,
                                      IcerikFabrikasiRenderAdapterMode requestedMode) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.executionPlan = executionPlan;
        this.requestedMode = requestedMode;
    }

    public UUID getProjectId() { return projectId; }
    public UUID getJobId() { return jobId; }
    public IcerikFabrikasiRenderExecutionPlan getExecutionPlan() { return executionPlan; }
    public IcerikFabrikasiRenderAdapterMode getRequestedMode() { return requestedMode; }
}
