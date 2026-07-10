package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 03: Request DTO for scenario preparation.
 */
public final class IcerikFabrikasiScenarioPreparationRequest {
    private String narrationToneHint;
    private Integer sceneCountHint;

    public IcerikFabrikasiScenarioPreparationRequest() {}
    public String getNarrationToneHint() { return narrationToneHint; }
    public void setNarrationToneHint(String v) { this.narrationToneHint = v; }
    public Integer getSceneCountHint() { return sceneCountHint; }
    public void setSceneCountHint(Integer v) { this.sceneCountHint = v; }
}
