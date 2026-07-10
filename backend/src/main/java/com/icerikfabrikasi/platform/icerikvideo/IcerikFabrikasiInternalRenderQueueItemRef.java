package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-027 Task 02: Opaque queue item reference.
 * Format: internal-render-queue-item:{uuid}
 */
public final class IcerikFabrikasiInternalRenderQueueItemRef {

    private static final String PREFIX = "internal-render-queue-item:";
    private final UUID itemId;

    private IcerikFabrikasiInternalRenderQueueItemRef(UUID itemId) {
        this.itemId = itemId;
    }

    public static IcerikFabrikasiInternalRenderQueueItemRef create() {
        return new IcerikFabrikasiInternalRenderQueueItemRef(UUID.randomUUID());
    }

    public static IcerikFabrikasiInternalRenderQueueItemRef of(UUID itemId) {
        if (itemId == null) throw new IllegalArgumentException("Queue item ID cannot be null");
        return new IcerikFabrikasiInternalRenderQueueItemRef(itemId);
    }

    public static IcerikFabrikasiInternalRenderQueueItemRef parse(String ref) {
        if (ref == null || ref.isBlank()) return null;
        if (!ref.startsWith(PREFIX)) return null;
        try {
            return new IcerikFabrikasiInternalRenderQueueItemRef(UUID.fromString(ref.substring(PREFIX.length())));
        } catch (IllegalArgumentException e) { return null; }
    }

    public static boolean isValid(String ref) { return parse(ref) != null; }
    public UUID getItemId() { return itemId; }
    public String toOpaqueRef() { return PREFIX + itemId; }
    @Override public String toString() { return toOpaqueRef(); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return itemId.equals(((IcerikFabrikasiInternalRenderQueueItemRef) o).itemId);
    }
    @Override public int hashCode() { return itemId.hashCode(); }
}
