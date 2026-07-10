package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-029 Task 01: Internal E2E smoke path status.
 */
public enum IcerikFabrikasiInternalSmokePathStatus {
    NOT_STARTED,
    READINESS_CHECK_PASSED,
    REVIEW_CHECK_PASSED,
    DISPATCH_CHECK_PASSED,
    GATE_CHECK_PASSED,
    PLAN_CREATED,
    CANDIDATE_CREATED,
    SMOKE_COMPLETE_INTERNAL,
    DENIED,
    FAILED_SAFE
}
