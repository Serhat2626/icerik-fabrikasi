package com.icerikfabrikasi.platform.icerikvideo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * FP-020 Task 02: Safe AI generation result.
 *
 * <p>No raw provider response, tokens, base64, storage keys, URLs, lyrics, or binary data.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcerikFabrikasiAiGenerationResult {

    private UUID projectId;
    private String generationStage;
    private IcerikFabrikasiProviderTraceReference traceRef;
    private String safeOutputSummary;
    private IcerikFabrikasiSourceConfidence outputConfidence;
    private boolean needsHumanReview;
    private boolean copyrightReviewRequired;
    private List<String> reviewFlags;
    private List<String> safeWarnings;
}
