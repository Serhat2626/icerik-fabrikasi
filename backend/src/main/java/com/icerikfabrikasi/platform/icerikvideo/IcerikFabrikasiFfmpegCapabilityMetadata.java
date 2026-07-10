package com.icerikfabrikasi.platform.icerikvideo;

/**
 * RPE-003: FFmpeg capability metadata — not execution.
 */
public final class IcerikFabrikasiFfmpegCapabilityMetadata {
    private final String capabilityLabel;
    private final boolean available;
    private final String availabilityNote;

    public IcerikFabrikasiFfmpegCapabilityMetadata(String capabilityLabel, boolean available, String availabilityNote) {
        this.capabilityLabel = capabilityLabel;
        this.available = available;
        this.availabilityNote = availabilityNote;
    }

    public String getCapabilityLabel() { return capabilityLabel; }
    public boolean isAvailable() { return available; }
    public String getAvailabilityNote() { return availabilityNote; }
}
