package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-039: Per-scene clip generation request. Metadata-only.
 */
public final class IcerikFabrikasiMultiClipGenerationSceneRequest {
    private final String opaqueSceneRef;
    private final int sceneOrder;
    private final int durationSeconds;
    private final int fps;
    private final int width;
    private final int height;
    private final String format;

    public IcerikFabrikasiMultiClipGenerationSceneRequest(String opaqueSceneRef, int sceneOrder,
            int durationSeconds, int fps, int width, int height, String format) {
        this.opaqueSceneRef = opaqueSceneRef;
        this.sceneOrder = sceneOrder;
        this.durationSeconds = durationSeconds;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.format = format != null ? format : "mp4";
    }

    public String getOpaqueSceneRef() { return opaqueSceneRef; }
    public int getSceneOrder() { return sceneOrder; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getFormat() { return format; }

    public boolean isValid() {
        return opaqueSceneRef != null && !opaqueSceneRef.isBlank()
                && sceneOrder >= 1 && durationSeconds >= 1 && durationSeconds <= 5
                && fps >= 1 && fps <= 30 && width >= 1 && width <= 1280
                && height >= 1 && height <= 720;
    }

    /**
     * Build from FP-038 scene ref.
     */
    public static IcerikFabrikasiMultiClipGenerationSceneRequest fromSceneRef(IcerikFabrikasiMultiSceneRenderSceneRef ref) {
        if (ref == null) return new IcerikFabrikasiMultiClipGenerationSceneRequest(null, 0, 0, 0, 0, 0, "mp4");
        return new IcerikFabrikasiMultiClipGenerationSceneRequest(ref.getOpaqueSceneRef(), ref.getSceneOrder(),
                ref.getExpectedDurationSeconds(), ref.getExpectedFps(),
                ref.getExpectedWidth(), ref.getExpectedHeight(), "mp4");
    }
}
