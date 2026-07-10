package com.icerikfabrikasi.platform.icerikvideo;

/**
 * RPE-001: Disabled execution capability flag.
 * All capabilities disabled by default in RPE-001.
 */
public final class IcerikFabrikasiExecutionCapability {
    private final String capabilityLabel;
    private final boolean enabled;
    private final String disabledReason;

    public IcerikFabrikasiExecutionCapability(String capabilityLabel, boolean enabled, String disabledReason) {
        this.capabilityLabel = capabilityLabel;
        this.enabled = enabled;
        this.disabledReason = disabledReason;
    }

    public String getCapabilityLabel() { return capabilityLabel; }
    public boolean isEnabled() { return enabled; }
    public String getDisabledReason() { return disabledReason; }
}
