package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-024 Task 02: Config validation result.
 */
public final class IcerikFabrikasiRenderConfigValidationResult {
    private final boolean valid;
    private final IcerikFabrikasiRenderConfigMode resolvedMode;
    private final List<IcerikFabrikasiRenderConfigValidationIssue> issues;

    public IcerikFabrikasiRenderConfigValidationResult(boolean valid,
                                              IcerikFabrikasiRenderConfigMode resolvedMode,
                                              List<IcerikFabrikasiRenderConfigValidationIssue> issues) {
        this.valid = valid;
        this.resolvedMode = resolvedMode;
        this.issues = issues != null ? List.copyOf(issues) : List.of();
    }

    public boolean isValid() { return valid; }
    public IcerikFabrikasiRenderConfigMode getResolvedMode() { return resolvedMode; }
    public List<IcerikFabrikasiRenderConfigValidationIssue> getIssues() { return issues; }

    public static IcerikFabrikasiRenderConfigValidationResult missing() {
        return new IcerikFabrikasiRenderConfigValidationResult(false,
                IcerikFabrikasiRenderConfigMode.FFMPEG_CONFIG_MISSING,
                List.of(new IcerikFabrikasiRenderConfigValidationIssue(
                        IcerikFabrikasiRenderConfigFailureCode.CONFIG_MISSING,
                        "config", "Render config not present — using stub")));
    }

    public static IcerikFabrikasiRenderConfigValidationResult disabled() {
        return new IcerikFabrikasiRenderConfigValidationResult(false,
                IcerikFabrikasiRenderConfigMode.FFMPEG_DISABLED,
                List.of(new IcerikFabrikasiRenderConfigValidationIssue(
                        IcerikFabrikasiRenderConfigFailureCode.ENABLED_FLAG_MISSING,
                        "enabled", "FFmpeg disabled — using stub")));
    }

    public static IcerikFabrikasiRenderConfigValidationResult stubOnly() {
        return new IcerikFabrikasiRenderConfigValidationResult(true,
                IcerikFabrikasiRenderConfigMode.STUB_ONLY, List.of());
    }
}
