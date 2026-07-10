package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-024 Task 03: Workspace validation result.
 */
public final class IcerikFabrikasiRenderWorkspaceValidationResult {
    private final boolean valid;
    private final List<IcerikFabrikasiRenderWorkspaceIssue> issues;

    public IcerikFabrikasiRenderWorkspaceValidationResult(boolean valid,
                                                  List<IcerikFabrikasiRenderWorkspaceIssue> issues) {
        this.valid = valid;
        this.issues = issues != null ? List.copyOf(issues) : List.of();
    }

    public boolean isValid() { return valid; }
    public List<IcerikFabrikasiRenderWorkspaceIssue> getIssues() { return issues; }

    public static IcerikFabrikasiRenderWorkspaceValidationResult ok() {
        return new IcerikFabrikasiRenderWorkspaceValidationResult(true, List.of());
    }

    public static IcerikFabrikasiRenderWorkspaceValidationResult fail(List<IcerikFabrikasiRenderWorkspaceIssue> issues) {
        return new IcerikFabrikasiRenderWorkspaceValidationResult(false, issues);
    }
}
