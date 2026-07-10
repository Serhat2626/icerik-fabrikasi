package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-047: Safe storyboard fixture input for the full render chain.
 * Metadata-only. No lyrics/script/prompt/user media.
 */
public final class IcerikFabrikasiStoryboardInternalRenderChainFixture {
    private final String opaqueStoryboardRef;
    private final String opaqueStoryRef;
    private final List<IcerikFabrikasiStoryboardSceneRenderInput> sceneInputs;

    public IcerikFabrikasiStoryboardInternalRenderChainFixture(String opaqueStoryboardRef,
            String opaqueStoryRef, List<IcerikFabrikasiStoryboardSceneRenderInput> sceneInputs) {
        this.opaqueStoryboardRef = opaqueStoryboardRef;
        this.opaqueStoryRef = opaqueStoryRef;
        this.sceneInputs = sceneInputs != null ? List.copyOf(sceneInputs) : List.of();
    }

    public String getOpaqueStoryboardRef() { return opaqueStoryboardRef; }
    public String getOpaqueStoryRef() { return opaqueStoryRef; }
    public List<IcerikFabrikasiStoryboardSceneRenderInput> getSceneInputs() { return sceneInputs; }
    public int getSceneCount() { return sceneInputs.size(); }

    /** Create a safe Türkü-themed test fixture. */
    public static IcerikFabrikasiStoryboardInternalRenderChainFixture icerikFabrikasiFixture(int sceneCount) {
        var inputs = new java.util.ArrayList<IcerikFabrikasiStoryboardSceneRenderInput>();
        for (int i = 1; i <= sceneCount; i++) {
            inputs.add(new IcerikFabrikasiStoryboardSceneRenderInput(
                    "scene-icerikFabrikasi-" + i, i, 2, 24, 640, 360, "mp4"));
        }
        return new IcerikFabrikasiStoryboardInternalRenderChainFixture(
                "sb-icerikFabrikasi-fixture", "story-icerikFabrikasi-fixture", inputs);
    }
}
