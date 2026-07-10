package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-002: Image provider configuration result — audit-safe metadata.
 */
public final class IcerikFabrikasiProviderConfigResult {
    private final IcerikFabrikasiProviderConfigStatus status;
    private final List<IcerikFabrikasiProviderConfigIssueCode> issues;
    private final String opaqueConfigRef;
    private final String providerLabel;
    private final IcerikFabrikasiImageProviderType providerType;
    private final List<String> capabilityLabels;
    private final IcerikFabrikasiCredentialVaultBoundary vaultBoundary;
    private final String selectionPolicySummary;
    private final String fallbackPolicySummary;
    private final boolean executionEnabled;
    private final String safetyValidationSummary;

    public IcerikFabrikasiProviderConfigResult(
            IcerikFabrikasiProviderConfigStatus status, List<IcerikFabrikasiProviderConfigIssueCode> issues,
            String opaqueConfigRef, String providerLabel, IcerikFabrikasiImageProviderType providerType,
            List<String> capabilityLabels, IcerikFabrikasiCredentialVaultBoundary vaultBoundary,
            String selectionPolicySummary, String fallbackPolicySummary,
            boolean executionEnabled, String safetyValidationSummary) {
        this.status = status;
        this.issues = issues == null ? List.of() : List.copyOf(issues);
        this.opaqueConfigRef = opaqueConfigRef;
        this.providerLabel = providerLabel;
        this.providerType = providerType;
        this.capabilityLabels = capabilityLabels == null ? List.of() : List.copyOf(capabilityLabels);
        this.vaultBoundary = vaultBoundary;
        this.selectionPolicySummary = selectionPolicySummary;
        this.fallbackPolicySummary = fallbackPolicySummary;
        this.executionEnabled = executionEnabled;
        this.safetyValidationSummary = safetyValidationSummary;
    }

    public IcerikFabrikasiProviderConfigStatus getStatus() { return status; }
    public List<IcerikFabrikasiProviderConfigIssueCode> getIssues() { return issues; }
    public String getOpaqueConfigRef() { return opaqueConfigRef; }
    public String getProviderLabel() { return providerLabel; }
    public IcerikFabrikasiImageProviderType getProviderType() { return providerType; }
    public List<String> getCapabilityLabels() { return capabilityLabels; }
    public IcerikFabrikasiCredentialVaultBoundary getVaultBoundary() { return vaultBoundary; }
    public String getSelectionPolicySummary() { return selectionPolicySummary; }
    public String getFallbackPolicySummary() { return fallbackPolicySummary; }
    public boolean isExecutionEnabled() { return executionEnabled; }
    public String getSafetyValidationSummary() { return safetyValidationSummary; }
}
