package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-023 Task 03: Render runner resolver.
 * Selects between stub and FFmpeg gateways based on adapter mode.
 * Default is always STUB. FFmpeg path is disabled by default.
 */
public class IcerikFabrikasiRenderRunnerResolver {

    private final StubIcerikFabrikasiRenderRunnerGateway stubRunner;
    private final FfmpegIcerikFabrikasiRenderRunnerGateway ffmpegRunner;
    private final IcerikFabrikasiRenderAdapterMode activeMode;

    public IcerikFabrikasiRenderRunnerResolver(StubIcerikFabrikasiRenderRunnerGateway stubRunner,
                                      FfmpegIcerikFabrikasiRenderRunnerGateway ffmpegRunner,
                                      boolean ffmpegEnabled, boolean ffmpegConfigPresent) {
        this.stubRunner = stubRunner;
        this.ffmpegRunner = ffmpegRunner;
        IcerikFabrikasiRenderAdapterPolicy policy = new IcerikFabrikasiRenderAdapterPolicy();
        this.activeMode = policy.resolveMode(ffmpegEnabled, ffmpegConfigPresent);
    }

    public IcerikFabrikasiRenderRunnerResolver(StubIcerikFabrikasiRenderRunnerGateway stubRunner) {
        this(stubRunner, new FfmpegIcerikFabrikasiRenderRunnerGateway(), false, false);
    }

    public IcerikFabrikasiRenderRunnerGateway resolve() {
        if (activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_READY) {
            return ffmpegRunner;
        }
        return stubRunner;
    }

    public IcerikFabrikasiRenderAdapterMode getActiveMode() {
        return activeMode;
    }

    public boolean isStubActive() {
        return activeMode == IcerikFabrikasiRenderAdapterMode.STUB
                || activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_DISABLED
                || activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_CONFIG_MISSING
                || activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_FAILED_SAFE;
    }

    public boolean isFfmpegActive() {
        return activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_READY;
    }

    public IcerikFabrikasiRenderAdapterResult resolveWithDiagnostics(IcerikFabrikasiRenderExecutionPlan plan) {
        IcerikFabrikasiRenderAdapterTraceReference trace = new IcerikFabrikasiRenderAdapterTraceReference(activeMode);
        IcerikFabrikasiRenderAdapterSafetyEnvelope envelope = new IcerikFabrikasiRenderAdapterSafetyEnvelope(activeMode);

        if (activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_DISABLED) {
            return new IcerikFabrikasiRenderAdapterResult(
                    false, activeMode,
                    IcerikFabrikasiRenderAdapterFailureCode.RENDER_PROVIDER_DISABLED,
                    "FFmpeg adapter is disabled — using stub runner",
                    trace, envelope, 0);
        }
        if (activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_CONFIG_MISSING) {
            return new IcerikFabrikasiRenderAdapterResult(
                    false, activeMode,
                    IcerikFabrikasiRenderAdapterFailureCode.RENDER_CONFIG_MISSING,
                    "FFmpeg config not present — using stub runner",
                    trace, envelope, 0);
        }
        if (activeMode == IcerikFabrikasiRenderAdapterMode.FFMPEG_FAILED_SAFE) {
            return new IcerikFabrikasiRenderAdapterResult(
                    false, activeMode,
                    IcerikFabrikasiRenderAdapterFailureCode.RENDER_FAILED_SAFE,
                    "FFmpeg adapter failed safe — using stub runner",
                    trace, envelope, 0);
        }

        // STUB or FFMPEG_READY — proceed with execution via resolved runner
        return new IcerikFabrikasiRenderAdapterResult(
                true, activeMode, null,
                "Adapter resolved: " + activeMode.name(),
                trace, envelope, 0);
    }
}
