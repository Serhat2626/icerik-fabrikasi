package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 02: Bootstrap request DTO.
 *
 * <p>Does not contain actorRole (handled by resolver),
 * tokens, credentials, lyrics, or copyrighted media references.</p>
 */
public final class IcerikFabrikasiVideoBootstrapRequest {

    private String icerikFabrikasiName;
    private String region;
    private String sourceNotes;
    private String mood;
    private Integer targetDurationMinutes;
    private String narrationStyle;
    private String visualStyle;

    public IcerikFabrikasiVideoBootstrapRequest() {}

    public String getIcerikFabrikasiName() { return icerikFabrikasiName; }
    public void setIcerikFabrikasiName(String v) { this.icerikFabrikasiName = v; }
    public String getRegion() { return region; }
    public void setRegion(String v) { this.region = v; }
    public String getSourceNotes() { return sourceNotes; }
    public void setSourceNotes(String v) { this.sourceNotes = v; }
    public String getMood() { return mood; }
    public void setMood(String v) { this.mood = v; }
    public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
    public void setTargetDurationMinutes(Integer v) { this.targetDurationMinutes = v; }
    public String getNarrationStyle() { return narrationStyle; }
    public void setNarrationStyle(String v) { this.narrationStyle = v; }
    public String getVisualStyle() { return visualStyle; }
    public void setVisualStyle(String v) { this.visualStyle = v; }

    @Override
    public String toString() {
        return "IcerikFabrikasiVideoBootstrapRequest{icerikFabrikasiName='" + icerikFabrikasiName + "'}";
    }
}
