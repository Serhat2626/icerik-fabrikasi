package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-020 Task 02: Provider gateway contract.
 *
 * <p>Implementations provide real or stub AI/image generation.
 * All calls must be wrapped in a safety envelope.</p>
 */
public interface IcerikFabrikasiGenerationProviderGateway {

    /**
     * Generates AI content (story, scenario, storyboard) through the provider.
     */
    IcerikFabrikasiAiGenerationResult generateAi(
            IcerikFabrikasiAiGenerationRequest request,
            IcerikFabrikasiGenerationSafetyEnvelope envelope);

    /**
     * Generates an image through the provider.
     */
    IcerikFabrikasiImageGenerationResult generateImage(
            IcerikFabrikasiImageGenerationRequest request,
            IcerikFabrikasiGenerationSafetyEnvelope envelope);
}
