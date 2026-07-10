package com.icerikfabrikasi.platform.shorts;

/**
 * Shorts video script durumu.
 */
public enum ShortsScriptStatus {

    /** AI tarafından üretildi, review bekliyor */
    DRAFT,

    /** Admin tarafından inceleniyor */
    IN_REVIEW,

    /** Onaylandı — render'a hazır */
    APPROVED,

    /** Revizyon istendi */
    REVISION_REQUESTED,

    /** Reddedildi */
    REJECTED,

    /** Render edildi — video hazır */
    RENDERED,

    /** Yayınlandı */
    PUBLISHED
}
