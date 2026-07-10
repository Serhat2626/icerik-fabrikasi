package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-037: Scene render plan status.
 * No READY_FOR_PUBLISH. No COMPLETED.
 */
public enum IcerikFabrikasiSceneRenderPlanStatus {
    CREATED_INTERNAL_ONLY,
    VALIDATED_FOR_INTERNAL_RENDER_ONLY,
    READY_FOR_BUNDLE_ASSEMBLY,
    REJECTED_UNSAFE,
    FAILED_SAFE
}
