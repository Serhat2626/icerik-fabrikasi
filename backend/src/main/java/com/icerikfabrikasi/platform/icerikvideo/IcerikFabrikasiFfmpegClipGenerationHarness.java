package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-034: Manual local video clip generation harness.
 * Disabled by default. Does NOT run in CI.
 * No ProcessBuilder in THIS class — delegates to runner.generateClip().
 */
public class IcerikFabrikasiFfmpegClipGenerationHarness {

    private final IcerikFabrikasiFfmpegLocalDevProcessRunner runner;
    private final boolean clipGenerationEnabled;

    public IcerikFabrikasiFfmpegClipGenerationHarness(IcerikFabrikasiFfmpegLocalDevProcessRunner runner,
            boolean clipGenerationEnabled) {
        this.runner = runner != null ? runner : new IcerikFabrikasiFfmpegLocalDevProcessRunner();
        this.clipGenerationEnabled = clipGenerationEnabled;
    }

    public IcerikFabrikasiFfmpegClipGenerationHarness() {
        this(new IcerikFabrikasiFfmpegLocalDevProcessRunner(), false);
    }

    /**
     * Attempt manual local video clip generation.
     */
    public IcerikFabrikasiFfmpegClipGenerationResult attempt(
            IcerikFabrikasiFfmpegProcessRequest request,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Manual flag
        if (!clipGenerationEnabled)
            return IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.MANUAL_FLAG_NOT_ENABLED);

        // 2. Request validation
        if (request == null || !request.isValid())
            return IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.GATE_DENIED);

        // 3. Process enabled
        if (!runner.isProcessEnabled())
            return IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.PROCESS_DISABLED);

        // 4. Binary policy
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.FFMPEG_UNAVAILABLE);

        // 5. Delegate to runner for real clip generation
        var processResult = runner.generateClip(request, binaryPolicy);

        // 6. Map result
        return switch (processResult.getStatus()) {
            case PROCESS_COMPLETED_SAFE -> {
                var outputRef = processResult.getOutputRef();
                var metadata = new IcerikFabrikasiFfmpegFixtureMetadata(outputRef, request.getFormat(),
                        request.getDurationSeconds(), request.getFps(),
                        request.getWidth(), request.getHeight(), request.getMaxOutputSizeMB());
                if (metadata.isBoundsValid())
                    yield IcerikFabrikasiFfmpegClipGenerationResult.verified(outputRef, metadata,
                            processResult.getDurationMs());
                else
                    yield IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                            IcerikFabrikasiFfmpegClipGenerationStatus.VERIFICATION_FAILED);
            }
            case GATE_DENIED -> IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.GATE_DENIED);
            case PROCESS_SKIPPED -> IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.PROCESS_DISABLED);
            case PROCESS_TIMED_OUT -> IcerikFabrikasiFfmpegClipGenerationResult.notAttempted(
                    IcerikFabrikasiFfmpegClipGenerationStatus.CLIP_GENERATION_TIMED_OUT);
            default -> IcerikFabrikasiFfmpegClipGenerationResult.failedSafe();
        };
    }

    public boolean isClipGenerationEnabled() { return clipGenerationEnabled; }
}
