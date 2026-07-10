package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Expiration reason codes.
 */
public enum IcerikFabrikasiInternalRenderExpirationReasonCode {
    QUEUE_DURATION_EXCEEDED,
    RETRY_WAIT_DURATION_EXCEEDED,
    STALE_INTERNAL_STATE,
    ADMIN_FORCED_EXPIRATION,
    NOT_EXPIRED
}
