package com.icerikfabrikasi.platform.icerikvideo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FP-020 Task 04: Per-scene status within image batch run response.
 *
 * <p>Opaque internal ref only — no base64, no URLs, no storage keys.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcerikFabrikasiImageBatchRunSceneStatus {

    private int sceneNo;
    private boolean success;
    private String safeInternalRef;
    private String safeErrorCode;
    private boolean needsHumanReview;
}
