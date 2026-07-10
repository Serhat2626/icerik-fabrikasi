package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-025 Task 02: Opaque internal render job reference.
 * Format: internal-render-job:{uuid}
 * Safe toString — no path/storage/provider leak.
 */
public final class IcerikFabrikasiInternalRenderJobRef {

    private static final String PREFIX = "internal-render-job:";
    private final UUID jobId;

    private IcerikFabrikasiInternalRenderJobRef(UUID jobId) {
        this.jobId = jobId;
    }

    public static IcerikFabrikasiInternalRenderJobRef create() {
        return new IcerikFabrikasiInternalRenderJobRef(UUID.randomUUID());
    }

    public static IcerikFabrikasiInternalRenderJobRef of(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID cannot be null");
        }
        return new IcerikFabrikasiInternalRenderJobRef(jobId);
    }

    /**
     * Parse from opaque ref string. Returns null if invalid.
     */
    public static IcerikFabrikasiInternalRenderJobRef parse(String ref) {
        if (ref == null || ref.isBlank()) return null;
        if (!ref.startsWith(PREFIX)) return null;
        String uuidPart = ref.substring(PREFIX.length());
        try {
            return new IcerikFabrikasiInternalRenderJobRef(UUID.fromString(uuidPart));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validate ref string without creating instance.
     */
    public static boolean isValid(String ref) {
        return parse(ref) != null;
    }

    public UUID getJobId() { return jobId; }

    public String toOpaqueRef() {
        return PREFIX + jobId;
    }

    @Override
    public String toString() {
        return toOpaqueRef();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return jobId.equals(((IcerikFabrikasiInternalRenderJobRef) o).jobId);
    }

    @Override
    public int hashCode() { return jobId.hashCode(); }
}
