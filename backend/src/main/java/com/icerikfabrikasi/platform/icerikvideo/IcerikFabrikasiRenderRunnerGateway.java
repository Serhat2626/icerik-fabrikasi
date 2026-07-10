package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-022 Task 03: Gateway interface for render execution.
 * Implementations may be stub or real renderer adapters.
 */
public interface IcerikFabrikasiRenderRunnerGateway {
    IcerikFabrikasiRenderExecutionResult execute(IcerikFabrikasiRenderExecutionPlan plan);
}
