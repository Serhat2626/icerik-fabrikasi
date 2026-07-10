package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-022 Task 03: Deterministic stub renderer.
 * Produces safe internal metadata only. No real rendering.
 * No binary file. No FFmpeg. No cloud execution.
 */
public class StubIcerikFabrikasiRenderRunnerGateway implements IcerikFabrikasiRenderRunnerGateway {

    @Override
    public IcerikFabrikasiRenderExecutionResult execute(IcerikFabrikasiRenderExecutionPlan plan) {
        IcerikFabrikasiRenderArtifact artifact = new IcerikFabrikasiRenderArtifact(
                UUID.randomUUID(),
                "internal-stub-artifact:" + UUID.randomUUID(),
                plan.getTargetFormat(),
                plan.getTotalDurationSeconds(),
                plan.getTargetResolution(),
                "estimated: ~" + Math.round(plan.getTotalDurationSeconds() * 2) + "MB");

        return new IcerikFabrikasiRenderExecutionResult(
                true, 42L, null, null, artifact,
                "stub-trace:" + UUID.randomUUID());
    }
}
