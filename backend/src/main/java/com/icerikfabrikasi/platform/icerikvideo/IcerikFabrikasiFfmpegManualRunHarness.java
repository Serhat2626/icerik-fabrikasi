package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-033: Manual local FFmpeg run harness.
 * Disabled by default. Does NOT run in CI.
 * Requires explicit manual flags to reach real execution.
 * No ProcessBuilder in THIS class — delegates to runner.
 */
public class IcerikFabrikasiFfmpegManualRunHarness {

    private final IcerikFabrikasiFfmpegLocalDevProcessRunner runner;
    private final boolean manualRunEnabled;

    public IcerikFabrikasiFfmpegManualRunHarness(IcerikFabrikasiFfmpegLocalDevProcessRunner runner, boolean manualRunEnabled) {
        this.runner = runner != null ? runner : new IcerikFabrikasiFfmpegLocalDevProcessRunner();
        this.manualRunEnabled = manualRunEnabled;
    }

    public IcerikFabrikasiFfmpegManualRunHarness() {
        this(new IcerikFabrikasiFfmpegLocalDevProcessRunner(), false);
    }

    /**
     * Attempt manual local FFmpeg run.
     */
    public IcerikFabrikasiFfmpegManualRunEvidenceResult attempt(
            IcerikFabrikasiFfmpegProcessRequest request,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Manual flag check
        if (!manualRunEnabled)
            return IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.MANUAL_FLAG_NOT_ENABLED);

        // 2. Request validation
        if (request == null || !request.isValid())
            return IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.GATE_DENIED);

        // 3. Process enabled check
        if (!runner.isProcessEnabled())
            return IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.PROCESS_DISABLED);

        // 4. Binary policy check
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.FFMPEG_UNAVAILABLE);

        // 5. Execute via runner (gate inside)
        var processResult = runner.execute(request, binaryPolicy);

        // 6. Map result
        return switch (processResult.getStatus()) {
            case PROCESS_COMPLETED_SAFE -> {
                var outputRef = processResult.getOutputRef();
                var metadata = new IcerikFabrikasiFfmpegFixtureMetadata(outputRef, request.getFormat(),
                        request.getDurationSeconds(), request.getFps(),
                        request.getWidth(), request.getHeight(), request.getMaxOutputSizeMB());
                yield IcerikFabrikasiFfmpegManualRunEvidenceResult.succeeded(outputRef, metadata,
                        processResult.getDurationMs());
            }
            case GATE_DENIED -> IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.GATE_DENIED);
            case PROCESS_SKIPPED -> IcerikFabrikasiFfmpegManualRunEvidenceResult.notAttempted(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.PROCESS_DISABLED);
            default -> IcerikFabrikasiFfmpegManualRunEvidenceResult.failedSafe();
        };
    }

    public boolean isManualRunEnabled() { return manualRunEnabled; }
}
