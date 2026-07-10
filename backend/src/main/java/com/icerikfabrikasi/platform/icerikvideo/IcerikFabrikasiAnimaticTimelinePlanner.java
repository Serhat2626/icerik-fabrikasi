package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RP-004: Deterministic fake/test-double animatic timeline planner.
 * Generates timeline items from scene asset plan. No real render/FFmpeg/AI.
 */
public class IcerikFabrikasiAnimaticTimelinePlanner {

    private static final String[] TRANSITIONS = {
            "FADE_IN", "CROSS_DISSOLVE", "CUT", "WIPE_LEFT", "FADE_OUT"
    };
    private static final String[] CAMERA_MOVEMENTS = {
            "STATIC", "SLOW_PAN_RIGHT", "ZOOM_IN", "DOLLY_FORWARD", "SLOW_PAN_LEFT"
    };
    private static final String[] PACING = {
            "Yava\u015f ve huzurlu ba\u015flang\u0131\u00e7",
            "Kademeli tempo art\u0131\u015f\u0131",
            "Dramatik duraklama",
            "Co\u015fkulu zirve",
            "Sakin kapan\u0131\u015f"
    };
    private static final String[] BEATS = {
            "Tan\u0131t\u0131m \u2014 mekan ve d\u00f6nem",
            "Geli\u015fme \u2014 karakterler ve olaylar",
            "Duygusal derinle\u015fme",
            "Doruk noktas\u0131 \u2014 g\u00f6rsel zirve",
            "Sonu\u00e7 \u2014 miras ve mesaj"
    };

    public IcerikFabrikasiAnimaticTimelinePlanResult plan(IcerikFabrikasiSceneAssetPlanResult assetPlan) {
        if (assetPlan == null) {
            return IcerikFabrikasiAnimaticTimelinePlanResult.rejected(IcerikFabrikasiAnimaticTimelinePlanIssueCode.ASSET_PLAN_MISSING);
        }
        if (assetPlan.getOverallStatus() != IcerikFabrikasiSceneAssetPlanStatus.READY_FOR_ASSET_GENERATION_BOUNDARY
                && assetPlan.getOverallStatus() != IcerikFabrikasiSceneAssetPlanStatus.VISUAL_BRIEF_GENERATED) {
            return IcerikFabrikasiAnimaticTimelinePlanResult.rejected(IcerikFabrikasiAnimaticTimelinePlanIssueCode.ASSET_PLAN_NOT_READY);
        }
        if (assetPlan.getOpaqueDraftRef() == null) {
            return IcerikFabrikasiAnimaticTimelinePlanResult.rejected(IcerikFabrikasiAnimaticTimelinePlanIssueCode.DRAFT_REF_MISSING);
        }

        List<IcerikFabrikasiSceneAssetPlanEntry> safeEntries = assetPlan.getEntries().stream()
                .filter(e -> e.getStatus() == IcerikFabrikasiSceneAssetPlanStatus.READY_FOR_ASSET_GENERATION_BOUNDARY)
                .toList();

        if (safeEntries.isEmpty()) {
            return IcerikFabrikasiAnimaticTimelinePlanResult.rejected(IcerikFabrikasiAnimaticTimelinePlanIssueCode.NO_ENTRIES);
        }

        int perSceneDuration = 10_000; // default 10s per scene
        List<IcerikFabrikasiAnimaticTimelineItem> items = new ArrayList<>();
        int currentMs = 0;

        for (int i = 0; i < safeEntries.size(); i++) {
            var entry = safeEntries.get(i);
            items.add(new IcerikFabrikasiAnimaticTimelineItem(
                    entry.getSceneNumber(), currentMs, perSceneDuration,
                    TRANSITIONS[i % TRANSITIONS.length],
                    CAMERA_MOVEMENTS[i % CAMERA_MOVEMENTS.length],
                    PACING[i % PACING.length],
                    BEATS[i % BEATS.length]));
            currentMs += perSceneDuration;
        }

        return new IcerikFabrikasiAnimaticTimelinePlanResult(
                IcerikFabrikasiAnimaticTimelinePlanStatus.READY_FOR_ASSEMBLY_BOUNDARY,
                List.of(),
                "timeline:" + UUID.randomUUID().toString().substring(0, 8),
                assetPlan.getOpaquePlanRef(),
                assetPlan.getOpaqueDraftRef(),
                assetPlan.getSafeIcerikFabrikasiTitleLabel(),
                "DETERMINISTIC_FAKE_TEST_DOUBLE",
                items, items.size(), currentMs);
    }
}
