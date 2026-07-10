package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 03: Request DTO for storyboard preparation.
 */
public final class IcerikFabrikasiStoryboardPreparationRequest {
    private String visualStyleHint;
    private String cameraStyleHint;

    public IcerikFabrikasiStoryboardPreparationRequest() {}
    public String getVisualStyleHint() { return visualStyleHint; }
    public void setVisualStyleHint(String v) { this.visualStyleHint = v; }
    public String getCameraStyleHint() { return cameraStyleHint; }
    public void setCameraStyleHint(String v) { this.cameraStyleHint = v; }
}
