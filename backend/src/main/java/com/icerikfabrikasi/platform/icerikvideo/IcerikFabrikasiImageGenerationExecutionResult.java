package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-006: Image generation execution candidate result.
 */
public final class IcerikFabrikasiImageGenerationExecutionResult {
    private final IcerikFabrikasiImageGenerationStatus status;
    private final List<IcerikFabrikasiImageGenerationIssueCode> issues;
    private final String opaqueGenerationRef;
    private final String opaquePromptPackageRef;
    private final IcerikFabrikasiImageGenerationExecutionMode executionMode;
    private final String providerDisabledReason;
    private final boolean manualGatePassed;
    private final List<IcerikFabrikasiFakeImageResultMetadata> fakeResults;
    private final int totalScenes;
    private final int fakeResultCount;
    private final String validationSummary;

    public IcerikFabrikasiImageGenerationExecutionResult(
            IcerikFabrikasiImageGenerationStatus status, List<IcerikFabrikasiImageGenerationIssueCode> issues,
            String opaqueGenerationRef, String opaquePromptPackageRef,
            IcerikFabrikasiImageGenerationExecutionMode executionMode, String providerDisabledReason,
            boolean manualGatePassed, List<IcerikFabrikasiFakeImageResultMetadata> fakeResults,
            int totalScenes, int fakeResultCount, String validationSummary) {
        this.status = status;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaqueGenerationRef = opaqueGenerationRef;
        this.opaquePromptPackageRef = opaquePromptPackageRef;
        this.executionMode = executionMode;
        this.providerDisabledReason = providerDisabledReason;
        this.manualGatePassed = manualGatePassed;
        this.fakeResults = fakeResults == null ? List.of() : List.copyOf(fakeResults);
        this.totalScenes = totalScenes;
        this.fakeResultCount = fakeResultCount;
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiImageGenerationStatus getStatus() { return status; }
    public List<IcerikFabrikasiImageGenerationIssueCode> getIssues() { return issues; }
    public String getOpaqueGenerationRef() { return opaqueGenerationRef; }
    public String getOpaquePromptPackageRef() { return opaquePromptPackageRef; }
    public IcerikFabrikasiImageGenerationExecutionMode getExecutionMode() { return executionMode; }
    public String getProviderDisabledReason() { return providerDisabledReason; }
    public boolean isManualGatePassed() { return manualGatePassed; }
    public List<IcerikFabrikasiFakeImageResultMetadata> getFakeResults() { return fakeResults; }
    public int getTotalScenes() { return totalScenes; }
    public int getFakeResultCount() { return fakeResultCount; }
    public String getValidationSummary() { return validationSummary; }
}
