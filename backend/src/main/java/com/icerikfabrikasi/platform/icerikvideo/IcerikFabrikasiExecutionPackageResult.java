package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-004: Execution package result — aggregates work order + provider + render config.
 */
public final class IcerikFabrikasiExecutionPackageResult {
    private final IcerikFabrikasiExecutionPackageStatus status;
    private final List<IcerikFabrikasiExecutionPackageIssueCode> blockedReasons;
    private final String opaquePackageRef;
    private final String opaqueWorkOrderRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String providerConfigSummary;
    private final String renderConfigSummary;
    private final List<IcerikFabrikasiManualGate> manualGates;
    private final List<IcerikFabrikasiExecutionCapability> executionCapabilities;
    private final String operatorExecutionSummary;
    private final List<String> futureExecutionStepLabels;
    private final String validationSummary;

    public IcerikFabrikasiExecutionPackageResult(
            IcerikFabrikasiExecutionPackageStatus status, List<IcerikFabrikasiExecutionPackageIssueCode> blockedReasons,
            String opaquePackageRef, String opaqueWorkOrderRef, String safeIcerikFabrikasiTitleLabel,
            String providerConfigSummary, String renderConfigSummary,
            List<IcerikFabrikasiManualGate> manualGates, List<IcerikFabrikasiExecutionCapability> executionCapabilities,
            String operatorExecutionSummary, List<String> futureExecutionStepLabels, String validationSummary) {
        this.status = status;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.opaquePackageRef = opaquePackageRef;
        this.opaqueWorkOrderRef = opaqueWorkOrderRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.providerConfigSummary = providerConfigSummary;
        this.renderConfigSummary = renderConfigSummary;
        this.manualGates = manualGates == null ? List.of() : List.copyOf(manualGates);
        this.executionCapabilities = executionCapabilities == null ? List.of() : List.copyOf(executionCapabilities);
        this.operatorExecutionSummary = operatorExecutionSummary;
        this.futureExecutionStepLabels = futureExecutionStepLabels == null ? List.of() : List.copyOf(futureExecutionStepLabels);
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiExecutionPackageStatus getStatus() { return status; }
    public List<IcerikFabrikasiExecutionPackageIssueCode> getBlockedReasons() { return blockedReasons; }
    public String getOpaquePackageRef() { return opaquePackageRef; }
    public String getOpaqueWorkOrderRef() { return opaqueWorkOrderRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getProviderConfigSummary() { return providerConfigSummary; }
    public String getRenderConfigSummary() { return renderConfigSummary; }
    public List<IcerikFabrikasiManualGate> getManualGates() { return manualGates; }
    public List<IcerikFabrikasiExecutionCapability> getExecutionCapabilities() { return executionCapabilities; }
    public String getOperatorExecutionSummary() { return operatorExecutionSummary; }
    public List<String> getFutureExecutionStepLabels() { return futureExecutionStepLabels; }
    public String getValidationSummary() { return validationSummary; }
}
