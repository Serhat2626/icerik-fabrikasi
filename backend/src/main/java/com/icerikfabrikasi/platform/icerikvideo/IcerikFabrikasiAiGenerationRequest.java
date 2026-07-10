package com.icerikfabrikasi.platform.icerikvideo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * FP-020 Task 02: Safe AI generation request.
 *
 * <p>No API keys, secrets, credentials, authorization, or raw provider URLs.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcerikFabrikasiAiGenerationRequest {

    private UUID projectId;
    private String generationStage;
    private IcerikFabrikasiNarrativeContext context;
    private String promptTemplateId;
    private Map<String, String> safeParams;
}
