package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-023 Task 02: Opaque trace reference for render adapter execution.
 * No file path, no URL, no secrets.
 */
public final class IcerikFabrikasiRenderAdapterTraceReference {
    private final String traceRef;
    private final IcerikFabrikasiRenderAdapterMode adapterMode;
    private final long timestampMs;

    public IcerikFabrikasiRenderAdapterTraceReference(IcerikFabrikasiRenderAdapterMode adapterMode) {
        this.traceRef = adapterMode.name().toLowerCase() + "-trace:" + UUID.randomUUID();
        this.adapterMode = adapterMode;
        this.timestampMs = System.currentTimeMillis();
    }

    public String getTraceRef() { return traceRef; }
    public IcerikFabrikasiRenderAdapterMode getAdapterMode() { return adapterMode; }
    public long getTimestampMs() { return timestampMs; }
}
