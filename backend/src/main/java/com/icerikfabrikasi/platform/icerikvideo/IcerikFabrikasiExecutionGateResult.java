package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-001: Execution safety gate result — audit-safe metadata.
 */
public final class IcerikFabrikasiExecutionGateResult {
    private final IcerikFabrikasiExecutionGateStatus status;
    private final List<IcerikFabrikasiExecutionGateIssueCode> blockedReasons;
    private final String opaqueExecutionRef;
    private final String opaqueWorkOrderRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String auditDecisionSummary;
    private final List<IcerikFabrikasiExecutionCapability> capabilities;
    private final List<String> requiredOperatorGates;
    private final List<String> futureExecutionStepLabels;

    public IcerikFabrikasiExecutionGateResult(
            IcerikFabrikasiExecutionGateStatus status, List<IcerikFabrikasiExecutionGateIssueCode> blockedReasons,
            String opaqueExecutionRef, String opaqueWorkOrderRef,
            String safeIcerikFabrikasiTitleLabel, String auditDecisionSummary,
            List<IcerikFabrikasiExecutionCapability> capabilities,
            List<String> requiredOperatorGates, List<String> futureExecutionStepLabels) {
        this.status = status;
        this.blockedReasons = blockedReasons == null ? List.of() : List.copyOf(blockedReasons);
        this.opaqueExecutionRef = opaqueExecutionRef;
        this.opaqueWorkOrderRef = opaqueWorkOrderRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.auditDecisionSummary = auditDecisionSummary;
        this.capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        this.requiredOperatorGates = requiredOperatorGates == null ? List.of() : List.copyOf(requiredOperatorGates);
        this.futureExecutionStepLabels = futureExecutionStepLabels == null ? List.of() : List.copyOf(futureExecutionStepLabels);
    }

    public IcerikFabrikasiExecutionGateStatus getStatus() { return status; }
    public List<IcerikFabrikasiExecutionGateIssueCode> getBlockedReasons() { return blockedReasons; }
    public String getOpaqueExecutionRef() { return opaqueExecutionRef; }
    public String getOpaqueWorkOrderRef() { return opaqueWorkOrderRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getAuditDecisionSummary() { return auditDecisionSummary; }
    public List<IcerikFabrikasiExecutionCapability> getCapabilities() { return capabilities; }
    public List<String> getRequiredOperatorGates() { return requiredOperatorGates; }
    public List<String> getFutureExecutionStepLabels() { return futureExecutionStepLabels; }
}
