package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-023 Task 02: FFmpeg command plan.
 * Safe, pre-validated argument list. No shell string.
 * This is a representation only — no process execution.
 */
public final class IcerikFabrikasiFfmpegCommandPlan {
    private final String executableToken;
    private final List<String> safeArgs;
    private final IcerikFabrikasiFfmpegExecutionLimits limits;
    private final IcerikFabrikasiFfmpegInputSpec inputSpec;
    private final IcerikFabrikasiFfmpegOutputSpec outputSpec;
    private final boolean validated;
    private final String validationError;

    public IcerikFabrikasiFfmpegCommandPlan(String executableToken, List<String> safeArgs,
                                   IcerikFabrikasiFfmpegExecutionLimits limits,
                                   IcerikFabrikasiFfmpegInputSpec inputSpec,
                                   IcerikFabrikasiFfmpegOutputSpec outputSpec,
                                   boolean validated, String validationError) {
        this.executableToken = executableToken;
        this.safeArgs = safeArgs != null ? List.copyOf(safeArgs) : List.of();
        this.limits = limits;
        this.inputSpec = inputSpec;
        this.outputSpec = outputSpec;
        this.validated = validated;
        this.validationError = validationError;
    }

    public String getExecutableToken() { return executableToken; }
    public List<String> getSafeArgs() { return safeArgs; }
    public IcerikFabrikasiFfmpegExecutionLimits getLimits() { return limits; }
    public IcerikFabrikasiFfmpegInputSpec getInputSpec() { return inputSpec; }
    public IcerikFabrikasiFfmpegOutputSpec getOutputSpec() { return outputSpec; }
    public boolean isValidated() { return validated; }
    public String getValidationError() { return validationError; }
}
