package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-023 Task 03: FFmpeg render runner gateway.
 * <p>Disabled by default. Does NOT execute any OS process.
 * Does NOT call ProcessBuilder. Does NOT invoke FFmpeg.
 * Builds a command plan for validation purposes only,
 * then returns a safe disabled/sandbox result.</p>
 *
 * <p>When disabled (default), returns RENDER_PROVIDER_DISABLED result.
 * When config is missing, returns RENDER_CONFIG_MISSING result.
 * No real rendering occurs in FP-023.</p>
 */
public class FfmpegIcerikFabrikasiRenderRunnerGateway implements IcerikFabrikasiRenderRunnerGateway {

    private final IcerikFabrikasiFfmpegCommandBuilder commandBuilder;
    private final IcerikFabrikasiFfmpegExecutionLimits limits;
    private final IcerikFabrikasiRenderAdapterSanitizer sanitizer;
    private final boolean enabled;

    public FfmpegIcerikFabrikasiRenderRunnerGateway() {
        this(false);
    }

    public FfmpegIcerikFabrikasiRenderRunnerGateway(boolean enabled) {
        this.commandBuilder = new IcerikFabrikasiFfmpegCommandBuilder(new IcerikFabrikasiRenderAdapterPolicy());
        this.limits = new IcerikFabrikasiFfmpegExecutionLimits();
        this.sanitizer = new IcerikFabrikasiRenderAdapterSanitizer();
        this.enabled = enabled;
    }

    @Override
    public IcerikFabrikasiRenderExecutionResult execute(IcerikFabrikasiRenderExecutionPlan plan) {
        // Step 1: Check if enabled
        if (!enabled) {
            return disabledResult("FFmpeg adapter is disabled by default — no process execution");
        }

        // Step 2: Build command plan (validation only, no execution)
        IcerikFabrikasiFfmpegCommandPlan commandPlan = commandBuilder.buildPlan(plan, limits);

        // Step 3: If command plan validation failed, return safe failure
        if (!commandPlan.isValidated()) {
            return failedResult(
                    IcerikFabrikasiRenderFailureCode.SAFETY_VIOLATION,
                    sanitizer.sanitizeMessage("Command plan rejected: " + commandPlan.getValidationError()));
        }

        // Step 4: Sandbox mode — do NOT execute the command plan
        // In FP-023, even when enabled, we do not call ProcessBuilder.
        // Return a sandbox result with opaque artifact ref.
        return sandboxResult(plan, commandPlan);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public IcerikFabrikasiRenderAdapterMode getAdapterMode() {
        return enabled ? IcerikFabrikasiRenderAdapterMode.FFMPEG_READY : IcerikFabrikasiRenderAdapterMode.FFMPEG_DISABLED;
    }

    public IcerikFabrikasiFfmpegCommandPlan buildCommandPlanOnly(IcerikFabrikasiRenderExecutionPlan plan) {
        return commandBuilder.buildPlan(plan, limits);
    }

    public IcerikFabrikasiRenderAdapterResult getAdapterDiagnostics() {
        IcerikFabrikasiRenderAdapterMode mode = getAdapterMode();
        IcerikFabrikasiRenderAdapterTraceReference trace = new IcerikFabrikasiRenderAdapterTraceReference(mode);
        IcerikFabrikasiRenderAdapterSafetyEnvelope envelope = new IcerikFabrikasiRenderAdapterSafetyEnvelope(mode);
        if (!enabled) {
            return new IcerikFabrikasiRenderAdapterResult(
                    false, mode,
                    IcerikFabrikasiRenderAdapterFailureCode.RENDER_PROVIDER_DISABLED,
                    "FFmpeg adapter disabled",
                    trace, envelope, 0);
        }
        return new IcerikFabrikasiRenderAdapterResult(
                true, mode, null,
                "FFmpeg adapter ready (sandbox mode)",
                trace, envelope, 0);
    }

    private IcerikFabrikasiRenderExecutionResult disabledResult(String message) {
        return new IcerikFabrikasiRenderExecutionResult(
                false, 0L,
                IcerikFabrikasiRenderFailureCode.RENDERER_UNAVAILABLE,
                sanitizer.sanitizeMessage(message),
                null,
                "ffmpeg-disabled-trace:" + UUID.randomUUID());
    }

    private IcerikFabrikasiRenderExecutionResult failedResult(IcerikFabrikasiRenderFailureCode code, String message) {
        return new IcerikFabrikasiRenderExecutionResult(
                false, 0L, code,
                sanitizer.sanitizeMessage(message),
                null,
                "ffmpeg-failed-trace:" + UUID.randomUUID());
    }

    private IcerikFabrikasiRenderExecutionResult sandboxResult(IcerikFabrikasiRenderExecutionPlan plan,
                                                      IcerikFabrikasiFfmpegCommandPlan commandPlan) {
        // Sandbox: produce opaque artifact metadata without real rendering
        IcerikFabrikasiRenderArtifact artifact = new IcerikFabrikasiRenderArtifact(
                UUID.randomUUID(),
                "internal-ffmpeg-sandbox:" + UUID.randomUUID(),
                plan.getTargetFormat(),
                plan.getTotalDurationSeconds(),
                plan.getTargetResolution(),
                "sandbox: ~" + Math.round(plan.getTotalDurationSeconds() * 2) + "MB");

        return new IcerikFabrikasiRenderExecutionResult(
                true, 42L, null, null, artifact,
                "ffmpeg-sandbox-trace:" + UUID.randomUUID());
    }
}
