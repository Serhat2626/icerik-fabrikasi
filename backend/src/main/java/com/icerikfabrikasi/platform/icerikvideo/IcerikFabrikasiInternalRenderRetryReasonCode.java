package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-027 Task 02: Retry reason codes.
 */
public enum IcerikFabrikasiInternalRenderRetryReasonCode {
    EXECUTION_FAILED_RETRIABLE,
    TIMEOUT_RETRIABLE,
    TRANSIENT_ERROR_RETRIABLE,
    MAX_RETRIES_EXHAUSTED,
    NOT_RETRIABLE_PERMANENT,
    CANCELLED_NO_RETRY,
    EXPIRED_NO_RETRY
}
