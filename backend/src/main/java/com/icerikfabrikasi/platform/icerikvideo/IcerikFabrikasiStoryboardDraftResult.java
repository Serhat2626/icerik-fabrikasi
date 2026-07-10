package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * RP-002: Storyboard draft result — safe metadata + scene list.
 */
public final class IcerikFabrikasiStoryboardDraftResult {
    private final IcerikFabrikasiStoryboardDraftStatus status;
    private final List<IcerikFabrikasiStoryboardDraftIssueCode> issueCodes;
    private final String opaqueDraftRef;
    private final String opaqueRequestRef;
    private final String safeIcerikFabrikasiTitleLabel;
    private final String copyrightSafeIntentSummary;
    private final String orchestrationMode;
    private final List<IcerikFabrikasiStoryboardDraftScene> scenes;
    private final int totalSceneCount;
    private final int totalDurationMs;
    private final String targetFormat;
    private final int targetWidth;
    private final int targetHeight;
    private final int targetFps;

    public IcerikFabrikasiStoryboardDraftResult(
            IcerikFabrikasiStoryboardDraftStatus status,
            List<IcerikFabrikasiStoryboardDraftIssueCode> issueCodes,
            String opaqueDraftRef, String opaqueRequestRef,
            String safeIcerikFabrikasiTitleLabel, String copyrightSafeIntentSummary,
            String orchestrationMode, List<IcerikFabrikasiStoryboardDraftScene> scenes,
            int totalSceneCount, int totalDurationMs,
            String targetFormat, int targetWidth, int targetHeight, int targetFps) {
        this.status = status;
        this.issueCodes = issueCodes == null ? List.of() : List.copyOf(issueCodes);
        this.opaqueDraftRef = opaqueDraftRef;
        this.opaqueRequestRef = opaqueRequestRef;
        this.safeIcerikFabrikasiTitleLabel = safeIcerikFabrikasiTitleLabel;
        this.copyrightSafeIntentSummary = copyrightSafeIntentSummary;
        this.orchestrationMode = orchestrationMode;
        this.scenes = scenes == null ? List.of() : List.copyOf(scenes);
        this.totalSceneCount = totalSceneCount;
        this.totalDurationMs = totalDurationMs;
        this.targetFormat = targetFormat;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.targetFps = targetFps;
    }

    public static IcerikFabrikasiStoryboardDraftResult rejected(IcerikFabrikasiStoryboardDraftIssueCode... codes) {
        return new IcerikFabrikasiStoryboardDraftResult(
                IcerikFabrikasiStoryboardDraftStatus.REJECTED_UNSAFE, List.of(codes),
                null, null, null, null, null, List.of(), 0, 0, null, 0, 0, 0);
    }

    public static IcerikFabrikasiStoryboardDraftResult failed(IcerikFabrikasiStoryboardDraftIssueCode... codes) {
        return new IcerikFabrikasiStoryboardDraftResult(
                IcerikFabrikasiStoryboardDraftStatus.FAILED_SAFE, List.of(codes),
                null, null, null, null, null, List.of(), 0, 0, null, 0, 0, 0);
    }

    public IcerikFabrikasiStoryboardDraftStatus getStatus() { return status; }
    public List<IcerikFabrikasiStoryboardDraftIssueCode> getIssueCodes() { return issueCodes; }
    public String getOpaqueDraftRef() { return opaqueDraftRef; }
    public String getOpaqueRequestRef() { return opaqueRequestRef; }
    public String getSafeIcerikFabrikasiTitleLabel() { return safeIcerikFabrikasiTitleLabel; }
    public String getCopyrightSafeIntentSummary() { return copyrightSafeIntentSummary; }
    public String getOrchestrationMode() { return orchestrationMode; }
    public List<IcerikFabrikasiStoryboardDraftScene> getScenes() { return scenes; }
    public int getTotalSceneCount() { return totalSceneCount; }
    public int getTotalDurationMs() { return totalDurationMs; }
    public String getTargetFormat() { return targetFormat; }
    public int getTargetWidth() { return targetWidth; }
    public int getTargetHeight() { return targetHeight; }
    public int getTargetFps() { return targetFps; }
}
