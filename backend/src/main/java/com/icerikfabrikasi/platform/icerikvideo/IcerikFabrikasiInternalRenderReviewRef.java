package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-026 Task 02: Opaque human review reference.
 * Format: internal-render-review:{uuid}
 * Safe toString — no path/storage/provider leak.
 */
public final class IcerikFabrikasiInternalRenderReviewRef {

    private static final String PREFIX = "internal-render-review:";
    private final UUID reviewId;

    private IcerikFabrikasiInternalRenderReviewRef(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public static IcerikFabrikasiInternalRenderReviewRef create() {
        return new IcerikFabrikasiInternalRenderReviewRef(UUID.randomUUID());
    }

    public static IcerikFabrikasiInternalRenderReviewRef of(UUID reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        return new IcerikFabrikasiInternalRenderReviewRef(reviewId);
    }

    public static IcerikFabrikasiInternalRenderReviewRef parse(String ref) {
        if (ref == null || ref.isBlank()) return null;
        if (!ref.startsWith(PREFIX)) return null;
        String uuidPart = ref.substring(PREFIX.length());
        try {
            return new IcerikFabrikasiInternalRenderReviewRef(UUID.fromString(uuidPart));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isValid(String ref) {
        return parse(ref) != null;
    }

    public UUID getReviewId() { return reviewId; }

    public String toOpaqueRef() {
        return PREFIX + reviewId;
    }

    @Override
    public String toString() {
        return toOpaqueRef();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return reviewId.equals(((IcerikFabrikasiInternalRenderReviewRef) o).reviewId);
    }

    @Override
    public int hashCode() { return reviewId.hashCode(); }
}
