package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-003: Render pipeline configuration result — audit-safe metadata.
 */
public final class IcerikFabrikasiRenderPipelineConfigResult {
    private final IcerikFabrikasiRenderConfigStatus status;
    private final List<IcerikFabrikasiRenderConfigIssueCode> issues;
    private final String opaqueRenderConfigRef;
    private final IcerikFabrikasiRenderEnvironmentBoundary environmentBoundary;
    private final List<IcerikFabrikasiFfmpegCapabilityMetadata> ffmpegCapabilities;
    private final IcerikFabrikasiOutputFormatPolicy outputFormatPolicy;
    private final List<String> safetyPrerequisiteLabels;
    private final boolean renderExecutionEnabled;
    private final String validationSummary;

    public IcerikFabrikasiRenderPipelineConfigResult(
            IcerikFabrikasiRenderConfigStatus status, List<IcerikFabrikasiRenderConfigIssueCode> issues,
            String opaqueRenderConfigRef, IcerikFabrikasiRenderEnvironmentBoundary environmentBoundary,
            List<IcerikFabrikasiFfmpegCapabilityMetadata> ffmpegCapabilities, IcerikFabrikasiOutputFormatPolicy outputFormatPolicy,
            List<String> safetyPrerequisiteLabels, boolean renderExecutionEnabled, String validationSummary) {
        this.status = status;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaqueRenderConfigRef = opaqueRenderConfigRef;
        this.environmentBoundary = environmentBoundary;
        this.ffmpegCapabilities = ffmpegCapabilities == null ? List.of() : List.copyOf(ffmpegCapabilities);
        this.outputFormatPolicy = outputFormatPolicy;
        this.safetyPrerequisiteLabels = safetyPrerequisiteLabels == null ? List.of() : List.copyOf(safetyPrerequisiteLabels);
        this.renderExecutionEnabled = renderExecutionEnabled;
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiRenderConfigStatus getStatus() { return status; }
    public List<IcerikFabrikasiRenderConfigIssueCode> getIssues() { return issues; }
    public String getOpaqueRenderConfigRef() { return opaqueRenderConfigRef; }
    public IcerikFabrikasiRenderEnvironmentBoundary getEnvironmentBoundary() { return environmentBoundary; }
    public List<IcerikFabrikasiFfmpegCapabilityMetadata> getFfmpegCapabilities() { return ffmpegCapabilities; }
    public IcerikFabrikasiOutputFormatPolicy getOutputFormatPolicy() { return outputFormatPolicy; }
    public List<String> getSafetyPrerequisiteLabels() { return safetyPrerequisiteLabels; }
    public boolean isRenderExecutionEnabled() { return renderExecutionEnabled; }
    public String getValidationSummary() { return validationSummary; }
}
