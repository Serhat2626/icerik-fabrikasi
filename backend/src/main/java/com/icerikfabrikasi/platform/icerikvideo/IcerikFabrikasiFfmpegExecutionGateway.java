package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * FP-024 Task 04: Controlled FFmpeg execution gateway.
 * Disabled by default. Supports dry-run and simulated execution.
 * No ProcessBuilder. No Runtime.exec. No filesystem access.
 */
public class IcerikFabrikasiFfmpegExecutionGateway {

    private final IcerikFabrikasiRenderConfigValidator configValidator;
    private final IcerikFabrikasiRenderWorkspaceValidator workspaceValidator;

    public IcerikFabrikasiFfmpegExecutionGateway() {
        this.configValidator = new IcerikFabrikasiRenderConfigValidator();
        this.workspaceValidator = new IcerikFabrikasiRenderWorkspaceValidator();
    }

    /**
     * Execute in controlled mode. Never starts a real process.
     * Validates config → workspace → command plan → returns safe result.
     */
    public IcerikFabrikasiFfmpegExecutionResult execute(IcerikFabrikasiFfmpegExecutionRequest request) {
        if (request == null) {
            return IcerikFabrikasiFfmpegExecutionResult.failedSafe(
                    IcerikFabrikasiFfmpegExecutionFailureCode.GENERAL_FAILURE,
                    "Execution request is null");
        }

        // Step 1: Validate config
        var configResult = configValidator.validate(request.getConfig());
        if (configResult.getResolvedMode() == IcerikFabrikasiRenderConfigMode.FFMPEG_CONFIG_MISSING) {
            return IcerikFabrikasiFfmpegExecutionResult.configMissing();
        }
        if (configResult.getResolvedMode() == IcerikFabrikasiRenderConfigMode.FFMPEG_DISABLED) {
            return IcerikFabrikasiFfmpegExecutionResult.disabled();
        }
        if (!configResult.isValid()) {
            List<String> notes = configResult.getIssues().stream()
                    .map(IcerikFabrikasiRenderConfigValidationIssue::getSafeMessage)
                    .toList();
            return IcerikFabrikasiFfmpegExecutionResult.configInvalid(notes);
        }

        // Step 2: Validate workspace ref
        var wsResult = workspaceValidator.validateWorkspaceRef(request.getWorkspaceRef());
        if (!wsResult.isValid()) {
            return IcerikFabrikasiFfmpegExecutionResult.dryRunFailed(
                    IcerikFabrikasiFfmpegExecutionFailureCode.WORKSPACE_INVALID,
                    "Workspace ref validation failed",
                    wsResult.getIssues().stream()
                            .map(IcerikFabrikasiRenderWorkspaceIssue::getSafeMessage).toList());
        }

        // Step 3: Validate command plan
        if (request.getCommandPlan() == null) {
            return IcerikFabrikasiFfmpegExecutionResult.dryRunFailed(
                    IcerikFabrikasiFfmpegExecutionFailureCode.COMMAND_PLAN_INVALID,
                    "Command plan is null", List.of());
        }
        if (!request.getCommandPlan().isValidated()) {
            return IcerikFabrikasiFfmpegExecutionResult.dryRunFailed(
                    IcerikFabrikasiFfmpegExecutionFailureCode.COMMAND_PLAN_INVALID,
                    "Command plan not validated", List.of());
        }

        // Step 4: Check command plan for unsafe values
        if (request.getCommandPlan().getSafeArgs() != null) {
            for (String arg : request.getCommandPlan().getSafeArgs()) {
                if (workspaceValidator.isShellMeta(arg)) {
                    return IcerikFabrikasiFfmpegExecutionResult.dryRunFailed(
                            IcerikFabrikasiFfmpegExecutionFailureCode.COMMAND_PLAN_UNSAFE,
                            "Unsafe value in command plan args",
                            List.of("Shell metacharacter detected"));
                }
            }
        }

        // Step 5: Validate limits from render plan
        if (request.getRenderPlan() != null) {
            var plan = request.getRenderPlan();
            if (plan.getTotalDurationSeconds() > request.getConfig().getMaxDurationSeconds()) {
                return IcerikFabrikasiFfmpegExecutionResult.dryRunFailed(
                        IcerikFabrikasiFfmpegExecutionFailureCode.LIMITS_EXCEEDED,
                        "Duration exceeds config limit", List.of());
            }
        }

        // Step 6: Dry-run success — all validation passed, no real execution
        long startMs = System.currentTimeMillis();
        List<String> notes = new ArrayList<>();
        notes.add("Config: valid");
        notes.add("Workspace: valid");
        notes.add("Command plan: validated");
        notes.add("Limits: within bounds");
        notes.add("Mode: DRY_RUN — no real execution");
        long elapsed = System.currentTimeMillis() - startMs;

        return IcerikFabrikasiFfmpegExecutionResult.dryRunSuccess(notes, elapsed);
    }

    /**
     * Simulated execution — returns success with opaque artifact ref.
     * No real process, no real file.
     */
    public IcerikFabrikasiFfmpegExecutionResult simulatedExecution(IcerikFabrikasiFfmpegExecutionRequest request) {
        if (request == null) {
            return IcerikFabrikasiFfmpegExecutionResult.failedSafe(
                    IcerikFabrikasiFfmpegExecutionFailureCode.GENERAL_FAILURE,
                    "Request is null");
        }
        // Validate first
        var dryResult = execute(request);
        if (!dryResult.isSuccess()) {
            return dryResult;
        }
        // Simulated success
        return IcerikFabrikasiFfmpegExecutionResult.simulatedSuccess(1);
    }

    /**
     * Returns the current execution mode (always DISABLED in this implementation).
     */
    public IcerikFabrikasiFfmpegExecutionMode getDefaultMode() {
        return IcerikFabrikasiFfmpegExecutionMode.DISABLED;
    }
}
