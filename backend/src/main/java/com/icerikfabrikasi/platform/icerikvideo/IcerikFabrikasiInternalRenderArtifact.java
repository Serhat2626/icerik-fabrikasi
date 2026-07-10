package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-023 Task 04: Internal render artifact.
 * Combines reference, safety, retention into single safe model.
 * No file path, no URL, no storage key, no binary.
 */
public final class IcerikFabrikasiInternalRenderArtifact {
    private final IcerikFabrikasiRenderArtifactReference reference;
    private final IcerikFabrikasiRenderArtifactSafetyReport safetyReport;
    private final IcerikFabrikasiRenderArtifactRetentionPolicy retentionPolicy;
    private IcerikFabrikasiRenderArtifactStatus status;

    public IcerikFabrikasiInternalRenderArtifact(IcerikFabrikasiRenderArtifactReference reference,
                                        IcerikFabrikasiRenderAdapterMode rendererMode) {
        this.reference = reference;
        this.safetyReport = new IcerikFabrikasiRenderArtifactSafetyReport(
                reference.getArtifactId(),
                IcerikFabrikasiRenderArtifactStatus.CREATED, rendererMode);
        this.retentionPolicy = new IcerikFabrikasiRenderArtifactRetentionPolicy();
        this.status = IcerikFabrikasiRenderArtifactStatus.CREATED;
    }

    public IcerikFabrikasiRenderArtifactReference getReference() { return reference; }
    public IcerikFabrikasiRenderArtifactSafetyReport getSafetyReport() { return safetyReport; }
    public IcerikFabrikasiRenderArtifactRetentionPolicy getRetentionPolicy() { return retentionPolicy; }
    public IcerikFabrikasiRenderArtifactStatus getStatus() { return status; }

    public void markValidated() { this.status = IcerikFabrikasiRenderArtifactStatus.VALIDATED; }
    public void markCleanupEligible() { this.status = IcerikFabrikasiRenderArtifactStatus.CLEANUP_ELIGIBLE; }
    public void markRejected() { this.status = IcerikFabrikasiRenderArtifactStatus.REJECTED; }
}
