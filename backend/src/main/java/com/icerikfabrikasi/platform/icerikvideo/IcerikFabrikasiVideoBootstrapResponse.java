package com.icerikfabrikasi.platform.icerikvideo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FP-019 Task 02: Bootstrap response DTO. Safe fields only.
 */
public final class IcerikFabrikasiVideoBootstrapResponse {
    private final UUID id;
    private final String icerikFabrikasiName;
    private final IcerikFabrikasiVideoWorkflowStage workflowStage;
    private final LocalDateTime createdAt;
    private final String safeMessage;

    public IcerikFabrikasiVideoBootstrapResponse(UUID id, String icerikFabrikasiName,
                                        IcerikFabrikasiVideoWorkflowStage workflowStage,
                                        LocalDateTime createdAt, String safeMessage) {
        this.id = id;
        this.icerikFabrikasiName = icerikFabrikasiName;
        this.workflowStage = workflowStage;
        this.createdAt = createdAt;
        this.safeMessage = safeMessage;
    }

    public UUID getId() { return id; }
    public String getIcerikFabrikasiName() { return icerikFabrikasiName; }
    public IcerikFabrikasiVideoWorkflowStage getWorkflowStage() { return workflowStage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSafeMessage() { return safeMessage; }

    public static IcerikFabrikasiVideoBootstrapResponse from(IcerikFabrikasiVideoProject project) {
        return new IcerikFabrikasiVideoBootstrapResponse(
                project.getId(), project.getIcerikFabrikasiName(),
                project.getWorkflowStage(), project.getCreatedAt(),
                "Türkü video projesi başarıyla oluşturuldu");
    }
}
