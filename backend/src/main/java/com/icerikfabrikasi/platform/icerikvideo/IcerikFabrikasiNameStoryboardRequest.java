package com.icerikfabrikasi.platform.icerikvideo;

/**
 * RP-001: Türkü name storyboard request — safe metadata only.
 * No lyrics, no audio/video, no media URL, no prompt body.
 */
public final class IcerikFabrikasiNameStoryboardRequest {
    private final String icerikFabrikasiTitle;
    private final String region;
    private final String category;
    private final String style;
    private final int targetSceneCount;
    private final int targetDurationMs;
    private final String targetFormat;
    private final int targetWidth;
    private final int targetHeight;
    private final int targetFps;

    public IcerikFabrikasiNameStoryboardRequest(
            String icerikFabrikasiTitle, String region, String category, String style,
            int targetSceneCount, int targetDurationMs, String targetFormat,
            int targetWidth, int targetHeight, int targetFps) {
        this.icerikFabrikasiTitle = icerikFabrikasiTitle;
        this.region = region;
        this.category = category;
        this.style = style;
        this.targetSceneCount = targetSceneCount;
        this.targetDurationMs = targetDurationMs;
        this.targetFormat = targetFormat;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.targetFps = targetFps;
    }

    public String getIcerikFabrikasiTitle() { return icerikFabrikasiTitle; }
    public String getRegion() { return region; }
    public String getCategory() { return category; }
    public String getStyle() { return style; }
    public int getTargetSceneCount() { return targetSceneCount; }
    public int getTargetDurationMs() { return targetDurationMs; }
    public String getTargetFormat() { return targetFormat; }
    public int getTargetWidth() { return targetWidth; }
    public int getTargetHeight() { return targetHeight; }
    public int getTargetFps() { return targetFps; }
}
