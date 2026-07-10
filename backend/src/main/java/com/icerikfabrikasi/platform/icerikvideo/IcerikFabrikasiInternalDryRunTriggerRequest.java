package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-049: Safe admin request for internal story-to-video dry-run trigger.
 * Metadata-only. No prompt/script/lyrics/user media/path/command.
 */
public final class IcerikFabrikasiInternalDryRunTriggerRequest {
    private String opaqueStoryboardRef;
    private String opaqueStoryRef;
    private List<IcerikFabrikasiStoryboardSceneRenderInput> sceneInputs;

    public IcerikFabrikasiInternalDryRunTriggerRequest() {}

    public IcerikFabrikasiInternalDryRunTriggerRequest(String opaqueStoryboardRef,
            String opaqueStoryRef, List<IcerikFabrikasiStoryboardSceneRenderInput> sceneInputs) {
        this.opaqueStoryboardRef = opaqueStoryboardRef;
        this.opaqueStoryRef = opaqueStoryRef;
        this.sceneInputs = sceneInputs;
    }

    public String getOpaqueStoryboardRef() { return opaqueStoryboardRef; }
    public void setOpaqueStoryboardRef(String v) { this.opaqueStoryboardRef = v; }
    public String getOpaqueStoryRef() { return opaqueStoryRef; }
    public void setOpaqueStoryRef(String v) { this.opaqueStoryRef = v; }
    public List<IcerikFabrikasiStoryboardSceneRenderInput> getSceneInputs() { return sceneInputs; }
    public void setSceneInputs(List<IcerikFabrikasiStoryboardSceneRenderInput> v) { this.sceneInputs = v; }
}
