package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 04: Request for scene asset plan preparation.
 */
public final class IcerikFabrikasiSceneAssetPreparationRequest {
    private String styleOverrideHint;
    private String aspectRatioHint;

    public IcerikFabrikasiSceneAssetPreparationRequest() {}
    public String getStyleOverrideHint() { return styleOverrideHint; }
    public void setStyleOverrideHint(String v) { this.styleOverrideHint = v; }
    public String getAspectRatioHint() { return aspectRatioHint; }
    public void setAspectRatioHint(String v) { this.aspectRatioHint = v; }
}
