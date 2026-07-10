package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RP-003: Scene-level asset plan entry — safe visual brief only.
 * Not a provider-ready prompt; a human-readable summary.
 */
public final class IcerikFabrikasiSceneAssetPlanEntry {
    private final int sceneNumber;
    private final IcerikFabrikasiSceneAssetPlanStatus status;
    private final List<IcerikFabrikasiSceneAssetPlanIssueCode> issueCodes;
    private final String visualBriefSummary;
    private final String shotType;
    private final String framing;
    private final String atmosphere;
    private final String historicalCulturalStyle;
    private final String safetyClassification;
    private final String blockedReason;

    public IcerikFabrikasiSceneAssetPlanEntry(
            int sceneNumber, IcerikFabrikasiSceneAssetPlanStatus status,
            List<IcerikFabrikasiSceneAssetPlanIssueCode> issueCodes,
            String visualBriefSummary, String shotType, String framing,
            String atmosphere, String historicalCulturalStyle,
            String safetyClassification, String blockedReason) {
        this.sceneNumber = sceneNumber;
        this.status = status;
        this.issueCodes = issueCodes == null ? List.of() : List.copyOf(issueCodes);
        this.visualBriefSummary = visualBriefSummary;
        this.shotType = shotType;
        this.framing = framing;
        this.atmosphere = atmosphere;
        this.historicalCulturalStyle = historicalCulturalStyle;
        this.safetyClassification = safetyClassification;
        this.blockedReason = blockedReason;
    }

    public int getSceneNumber() { return sceneNumber; }
    public IcerikFabrikasiSceneAssetPlanStatus getStatus() { return status; }
    public List<IcerikFabrikasiSceneAssetPlanIssueCode> getIssueCodes() { return issueCodes; }
    public String getVisualBriefSummary() { return visualBriefSummary; }
    public String getShotType() { return shotType; }
    public String getFraming() { return framing; }
    public String getAtmosphere() { return atmosphere; }
    public String getHistoricalCulturalStyle() { return historicalCulturalStyle; }
    public String getSafetyClassification() { return safetyClassification; }
    public String getBlockedReason() { return blockedReason; }
}
