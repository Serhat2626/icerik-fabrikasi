package com.icerikfabrikasi.platform.icerikvideo;

/**
 * RPE-006: Image generation execution mode.
 * CI/default = DISABLED or CI_FAKE. Real only via LOCAL_MANUAL_ONLY (future).
 */
public enum IcerikFabrikasiImageGenerationExecutionMode {
    DISABLED,
    CI_FAKE,
    LOCAL_MANUAL_ONLY
}
