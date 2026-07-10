package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-024 Task 05: Artifact metadata integration service.
 * Connects execution result → verification → artifact metadata.
 * Metadata-only. No filesystem.
 */
public class IcerikFabrikasiRenderArtifactMetadataIntegrationService {

    private final IcerikFabrikasiRenderArtifactVerifier verifier;
    private final IcerikFabrikasiRenderArtifactService artifactService;

    public IcerikFabrikasiRenderArtifactMetadataIntegrationService(
            IcerikFabrikasiRenderArtifactVerifier verifier,
            IcerikFabrikasiRenderArtifactService artifactService) {
        this.verifier = verifier;
        this.artifactService = artifactService;
    }

    public IcerikFabrikasiRenderArtifactMetadataIntegrationService() {
        this(new IcerikFabrikasiRenderArtifactVerifier(), new IcerikFabrikasiRenderArtifactService());
    }

    /**
     * Integrate execution result into artifact metadata with verification.
     */
    public IcerikFabrikasiRenderArtifactVerificationReport integrateFromExecution(
            IcerikFabrikasiFfmpegExecutionResult executionResult,
            IcerikFabrikasiRenderExecutionPlan plan) {

        // Step 1: Verify
        var report = verifier.verifyFromExecution(executionResult, plan);

        // Step 2: If verified, create artifact metadata via existing service
        if (report.getStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED
                && executionResult != null && executionResult.isSuccess()) {
            var artifact = new IcerikFabrikasiRenderArtifact(
                    java.util.UUID.randomUUID(),
                    executionResult.getOpaqueArtifactRef(),
                    plan != null ? plan.getTargetFormat() : "mp4",
                    plan != null ? plan.getTotalDurationSeconds() : 0,
                    plan != null ? plan.getTargetResolution() : "1920x1080",
                    "internal");
            var executionSafeResult = new IcerikFabrikasiRenderExecutionResult(
                    true, executionResult.getDurationMs(),
                    null, null, artifact,
                    "verification-integrated:" + java.util.UUID.randomUUID());
            artifactService.createFromExecutionResult(
                    executionSafeResult, IcerikFabrikasiRenderAdapterMode.STUB);
        }

        return report;
    }

    /**
     * Verify-only without creating artifact metadata.
     */
    public IcerikFabrikasiRenderArtifactVerificationReport verifyOnly(
            String opaqueRef, String rendererMode,
            String format, double duration, int sizeMb,
            String resolution, int fps) {
        return verifier.verify(opaqueRef, rendererMode, format, duration, sizeMb, resolution, fps);
    }
}
