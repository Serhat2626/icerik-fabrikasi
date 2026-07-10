package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RPE-007: Image artifact verification report.
 */
public final class IcerikFabrikasiArtifactVerificationReport {
    private final IcerikFabrikasiArtifactVerificationStatus status;
    private final String opaqueVerificationRef;
    private final String opaqueGenerationRef;
    private final List<IcerikFabrikasiSceneArtifactReadiness> sceneReadiness;
    private final int totalScenes;
    private final int verifiedScenes;
    private final String validationSummary;

    public IcerikFabrikasiArtifactVerificationReport(
            IcerikFabrikasiArtifactVerificationStatus status, String opaqueVerificationRef,
            String opaqueGenerationRef, List<IcerikFabrikasiSceneArtifactReadiness> sceneReadiness,
            int totalScenes, int verifiedScenes, String validationSummary) {
        this.status = status;
        this.opaqueVerificationRef = opaqueVerificationRef;
        this.opaqueGenerationRef = opaqueGenerationRef;
        this.sceneReadiness = sceneReadiness == null ? List.of() : List.copyOf(sceneReadiness);
        this.totalScenes = totalScenes;
        this.verifiedScenes = verifiedScenes;
        this.validationSummary = validationSummary;
    }

    public IcerikFabrikasiArtifactVerificationStatus getStatus() { return status; }
    public String getOpaqueVerificationRef() { return opaqueVerificationRef; }
    public String getOpaqueGenerationRef() { return opaqueGenerationRef; }
    public List<IcerikFabrikasiSceneArtifactReadiness> getSceneReadiness() { return sceneReadiness; }
    public int getTotalScenes() { return totalScenes; }
    public int getVerifiedScenes() { return verifiedScenes; }
    public String getValidationSummary() { return validationSummary; }
}
