package com.icerikfabrikasi.platform.icerikvideo;

/**
 * LIVE-001: Kill switch — global execution blocker.
 * When active, all real provider execution is blocked regardless of other flags.
 */
public final class IcerikFabrikasiKillSwitch {
    private final boolean active;
    private final String reason;
    private final String activatedBy;

    public IcerikFabrikasiKillSwitch(boolean active, String reason, String activatedBy) {
        this.active = active;
        this.reason = reason;
        this.activatedBy = activatedBy;
    }

    /** Default: kill switch ON (blocks execution). */
    public static IcerikFabrikasiKillSwitch defaultActive() {
        return new IcerikFabrikasiKillSwitch(true, "Varsayilan: gercek saglayici yurutmesi devre disi", "SYSTEM");
    }

    public boolean isActive() { return active; }
    public String getReason() { return reason; }
    public String getActivatedBy() { return activatedBy; }
}
