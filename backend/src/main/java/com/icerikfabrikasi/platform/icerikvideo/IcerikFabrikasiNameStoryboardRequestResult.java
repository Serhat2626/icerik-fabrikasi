package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RP-001: Storyboard request result — safe metadata only.
 */
public final class IcerikFabrikasiNameStoryboardRequestResult {
    private final IcerikFabrikasiNameStoryboardRequestStatus status;
    private final List<IcerikFabrikasiNameStoryboardRequestIssueCode> issueCodes;
    private final String opaqueStoryboardRequestRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String inspirationMode;
    private final int targetSceneCount;
    private final int targetDurationMs;
    private final String targetFormat;
    private final int targetWidth;
    private final int targetHeight;
    private final int targetFps;
    private final String copyrightSafeIntentSummary;

    public IcerikFabrikasiNameStoryboardRequestResult(
            IcerikFabrikasiNameStoryboardRequestStatus status,
            List<IcerikFabrikasiNameStoryboardRequestIssueCode> issueCodes,
            String opaqueStoryboardRequestRef, String safeIcerikFabrikasiTitleLabel,
            String inspirationMode, int targetSceneCount, int targetDurationMs,
            String targetFormat, int targetWidth, int targetHeight, int targetFps,
            String copyrightSafeIntentSummary) {
        this.status = status;
        this.issueCodes = issueCodes == null ? List.of() : List.copyOf(issueCodes);
        this.opaqueStoryboardRequestRef = opaqueStoryboardRequestRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.inspirationMode = inspirationMode;
        this.targetSceneCount = targetSceneCount;
        this.targetDurationMs = targetDurationMs;
        this.targetFormat = targetFormat;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.targetFps = targetFps;
        this.copyrightSafeIntentSummary = copyrightSafeIntentSummary;
    }

    public static IcerikFabrikasiNameStoryboardRequestResult rejected(IcerikFabrikasiNameStoryboardRequestIssueCode... codes) {
        return new IcerikFabrikasiNameStoryboardRequestResult(
                IcerikFabrikasiNameStoryboardRequestStatus.REJECTED_UNSAFE, List.of(codes),
                null, null, null, 0, 0, null, 0, 0, 0, null);
    }

    public static IcerikFabrikasiNameStoryboardRequestResult failed(IcerikFabrikasiNameStoryboardRequestIssueCode... codes) {
        return new IcerikFabrikasiNameStoryboardRequestResult(
                IcerikFabrikasiNameStoryboardRequestStatus.FAILED_SAFE, List.of(codes),
                null, null, null, 0, 0, null, 0, 0, 0, null);
    }

    public IcerikFabrikasiNameStoryboardRequestStatus getStatus() { return status; }
    public List<IcerikFabrikasiNameStoryboardRequestIssueCode> getIssueCodes() { return issueCodes; }
    public String getOpaqueStoryboardRequestRef() { return opaqueStoryboardRequestRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getInspirationMode() { return inspirationMode; }
    public int getTargetSceneCount() { return targetSceneCount; }
    public int getTargetDurationMs() { return targetDurationMs; }
    public String getTargetFormat() { return targetFormat; }
    public int getTargetWidth() { return targetWidth; }
    public int getTargetHeight() { return targetHeight; }
    public int getTargetFps() { return targetFps; }
    public String getCopyrightSafeIntentSummary() { return copyrightSafeIntentSummary; }
}
