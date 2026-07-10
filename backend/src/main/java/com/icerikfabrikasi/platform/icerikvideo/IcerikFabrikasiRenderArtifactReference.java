package com.icerikfabrikasi.platform.icerikvideo;

import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FP-023 Task 04: Render artifact reference.
 * Opaque internal ref only. No file path, no URL, no storage key.
 */
@Getter
public final class IcerikFabrikasiRenderArtifactReference {
    private final UUID artifactId;
    private final String opaqueRef;
    private final IcerikFabrikasiRenderAdapterMode rendererMode;
    private final String format;
    private final double durationSeconds;
    private final String resolution;
    private final String sizeLabel;
    private final Instant createdAt;

    public IcerikFabrikasiRenderArtifactReference(UUID artifactId, String opaqueRef,
                                         IcerikFabrikasiRenderAdapterMode rendererMode,
                                         String format, double durationSeconds,
                                         String resolution, String sizeLabel) {
        this.artifactId = artifactId;
        this.opaqueRef = opaqueRef;
        this.rendererMode = rendererMode;
        this.format = format;
        this.durationSeconds = durationSeconds;
        this.resolution = resolution;
        this.sizeLabel = sizeLabel;
        this.createdAt = Instant.now();
    }

}
