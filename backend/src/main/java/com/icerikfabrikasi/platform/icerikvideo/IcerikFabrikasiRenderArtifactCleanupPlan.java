package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-023 Task 04: Render artifact cleanup plan.
 * Metadata-only. No real file deletion. No filesystem access.
 */
public final class IcerikFabrikasiRenderArtifactCleanupPlan {
    private final UUID planId;
    private final UUID artifactId;
    private final String opaqueRef;
    private final IcerikFabrikasiRenderArtifactRetentionPolicy retentionPolicy;
    private final boolean eligible;
    private final String reason;

    public IcerikFabrikasiRenderArtifactCleanupPlan(UUID artifactId, String opaqueRef,
                                            IcerikFabrikasiRenderArtifactRetentionPolicy retentionPolicy,
                                            boolean eligible, String reason) {
        this.planId = UUID.randomUUID();
        this.artifactId = artifactId;
        this.opaqueRef = opaqueRef;
        this.retentionPolicy = retentionPolicy;
        this.eligible = eligible;
        this.reason = reason;
    }

    public UUID getPlanId() { return planId; }
    public UUID getArtifactId() { return artifactId; }
    public String getOpaqueRef() { return opaqueRef; }
    public IcerikFabrikasiRenderArtifactRetentionPolicy getRetentionPolicy() { return retentionPolicy; }
    public boolean isEligible() { return eligible; }
    public String getReason() { return reason; }
}
