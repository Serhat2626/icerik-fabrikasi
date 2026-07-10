package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-022 Task 03: Render execution safety response DTO.
 * Read-only safety status. No rendered/binary/URL data.
 */
public final class IcerikFabrikasiRenderExecutionSafetyResponse {
    private final UUID projectId;
    private final UUID jobId;
    private final IcerikFabrikasiRenderJobStatus jobStatus;
    private final IcerikFabrikasiVideoWorkflowStage currentStage;
    private final IcerikFabrikasiRenderExecutionSafetyEnvelope safetyEnvelope;
    private final String summary;

    public IcerikFabrikasiRenderExecutionSafetyResponse(UUID projectId, UUID jobId,
                                                IcerikFabrikasiRenderJobStatus jobStatus,
                                                IcerikFabrikasiVideoWorkflowStage currentStage,
                                                IcerikFabrikasiRenderExecutionSafetyEnvelope safetyEnvelope,
                                                String summary) {
        this.projectId = projectId;
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.currentStage = currentStage;
        this.safetyEnvelope = safetyEnvelope;
        this.summary = summary;
    }

    public UUID getProjectId() { return projectId; }
    public UUID getJobId() { return jobId; }
    public IcerikFabrikasiRenderJobStatus getJobStatus() { return jobStatus; }
    public IcerikFabrikasiVideoWorkflowStage getCurrentStage() { return currentStage; }
    public IcerikFabrikasiRenderExecutionSafetyEnvelope getSafetyEnvelope() { return safetyEnvelope; }
    public String getSummary() { return summary; }
}
