package com.icerikfabrikasi.platform.icerikvideo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * FP-020 Task 04: Response DTO for provider-backed image batch run.
 *
 * <p>Safe output only. Uses opaque internal refs.
 * No base64, no URLs, no storage keys, no raw provider payloads.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcerikFabrikasiImageBatchRunResponse {

    private UUID projectId;
    private UUID batchId;
    private IcerikFabrikasiProviderMode providerMode;
    private IcerikFabrikasiImageBatchStatus batchStatus;
    private IcerikFabrikasiVideoWorkflowStage currentStage;
    private String safeOutputSummary;
    private String safeFailureCode;
    private IcerikFabrikasiProviderTraceReference traceRef;
    private int requestCount;
    private int successCount;
    private int failureCount;
    private boolean humanReviewRequired;
    private boolean autoApproved;
    private boolean copyrightReviewRequired;
    private List<IcerikFabrikasiImageBatchRunSceneStatus> sceneStatuses;
    private List<String> reviewFlags;
    private List<String> safeWarnings;
}
