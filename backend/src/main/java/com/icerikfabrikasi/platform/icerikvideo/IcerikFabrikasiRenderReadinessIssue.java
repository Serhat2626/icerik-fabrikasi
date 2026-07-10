package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-021 Task 03: Single readiness issue blocking or requiring review.
 */
public final class IcerikFabrikasiRenderReadinessIssue {

    private final String code;
    private final String description;
    private final String severity; // BLOCKING, WARNING, INFO

    public IcerikFabrikasiRenderReadinessIssue(String code, String description, String severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
}
