package com.icerikfabrikasi.platform.icerikvideo;

/**
 * LIVE-002: Sanitized image result metadata.
 * No URL/key/path/base64/binary/raw payload.
 */
public final class IcerikFabrikasiLiveImageResultMetadata {
    private final int sceneNo;
    private final String opaqueArtifactRef;
    private final String artifactLabel;
    private final String formatHint;
    private final int widthHint;
    private final int heightHint;
    private final IcerikFabrikasiLiveGenerationMode generationMode;
    private final String sanitizedProviderSummary;
    private final boolean humanReviewRequired;

    public IcerikFabrikasiLiveImageResultMetadata(int sceneNo, String opaqueArtifactRef, String artifactLabel,
                                         String formatHint, int widthHint, int heightHint,
                                         IcerikFabrikasiLiveGenerationMode generationMode,
                                         String sanitizedProviderSummary, boolean humanReviewRequired) {
        this.sceneNo = sceneNo;
        this.opaqueArtifactRef = opaqueArtifactRef;
        this.artifactLabel = artifactLabel;
        this.formatHint = formatHint;
        this.widthHint = widthHint;
        this.heightHint = heightHint;
        this.generationMode = generationMode;
        this.sanitizedProviderSummary = sanitizedProviderSummary;
        this.humanReviewRequired = humanReviewRequired;
    }

    public int getSceneNo() { return sceneNo; }
    public String getOpaqueArtifactRef() { return opaqueArtifactRef; }
    public String getArtifactLabel() { return artifactLabel; }
    public String getFormatHint() { return formatHint; }
    public int getWidthHint() { return widthHint; }
    public int getHeightHint() { return heightHint; }
    public IcerikFabrikasiLiveGenerationMode getGenerationMode() { return generationMode; }
    public String getSanitizedProviderSummary() { return sanitizedProviderSummary; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
}
