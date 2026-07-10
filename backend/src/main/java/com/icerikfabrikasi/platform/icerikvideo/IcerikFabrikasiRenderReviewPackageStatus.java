package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-042: Render review package status.
 * No READY_FOR_PUBLISH. No COMPLETED. No auto-approval.
 */
public enum IcerikFabrikasiRenderReviewPackageStatus {
    CREATED_INTERNAL_ONLY,
    ARTIFACT_METADATA_ATTACHED,
    READY_FOR_INTERNAL_REVIEW_ONLY,
    REJECTED_UNSAFE,
    FAILED_SAFE
}
