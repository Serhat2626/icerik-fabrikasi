package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-021 Task 03: Single scene entry in the render manifest.
 * Safe internal refs only — no public URL, no base64, no storage key.
 */
public final class IcerikFabrikasiRenderManifestScene {

    private final int sceneNo;
    private final String sceneTitle;
    private final String imageRef;
    private final String narrationRef;
    private final String musicRef;
    private final double durationSeconds;
    private final String transitionIn;
    private final String transitionOut;
    private final String cameraDirection;

    public IcerikFabrikasiRenderManifestScene(int sceneNo, String sceneTitle, String imageRef,
                                     String narrationRef, String musicRef,
                                     double durationSeconds, String transitionIn,
                                     String transitionOut, String cameraDirection) {
        this.sceneNo = sceneNo;
        this.sceneTitle = sceneTitle;
        this.imageRef = imageRef;
        this.narrationRef = narrationRef;
        this.musicRef = musicRef;
        this.durationSeconds = durationSeconds;
        this.transitionIn = transitionIn;
        this.transitionOut = transitionOut;
        this.cameraDirection = cameraDirection;
    }

    public int getSceneNo() { return sceneNo; }
    public String getSceneTitle() { return sceneTitle; }
    public String getImageRef() { return imageRef; }
    public String getNarrationRef() { return narrationRef; }
    public String getMusicRef() { return musicRef; }
    public double getDurationSeconds() { return durationSeconds; }
    public String getTransitionIn() { return transitionIn; }
    public String getTransitionOut() { return transitionOut; }
    public String getCameraDirection() { return cameraDirection; }
}
