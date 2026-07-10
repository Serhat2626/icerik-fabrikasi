package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * RP-005: Aggregates the full RP chain into a single readiness assessment.
 * Deterministic fake/test-double only. No provider/render/FFmpeg calls.
 */
public class IcerikFabrikasiMvpReadinessService {

    public IcerikFabrikasiMvpReadinessResult assess(
            IcerikFabrikasiNameStoryboardRequestResult request,
            IcerikFabrikasiStoryboardDraftResult draft,
            IcerikFabrikasiSceneAssetPlanResult assetPlan,
            IcerikFabrikasiAnimaticTimelinePlanResult timeline) {

        List<IcerikFabrikasiMvpReadinessIssueCode> issues = new ArrayList<>();

        // Request check
        if (request == null) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.REQUEST_MISSING);
        } else if (request.getStatus() != IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.REQUEST_NOT_READY);
        }

        // Storyboard check
        if (draft == null) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.STORYBOARD_MISSING);
        } else if (draft.getStatus() != IcerikFabrikasiStoryboardDraftStatus.READY_FOR_ADMIN_PREVIEW) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.STORYBOARD_NOT_READY);
        }

        // Asset plan check
        boolean hasBlockedScenes = false;
        if (assetPlan == null) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.ASSET_PLAN_MISSING);
        } else if (assetPlan.getOverallStatus() != IcerikFabrikasiSceneAssetPlanStatus.READY_FOR_ASSET_GENERATION_BOUNDARY
                && assetPlan.getOverallStatus() != IcerikFabrikasiSceneAssetPlanStatus.VISUAL_BRIEF_GENERATED) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.ASSET_PLAN_NOT_READY);
        } else if (assetPlan.getBlockedSceneCount() > 0) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.ASSET_PLAN_HAS_BLOCKED_SCENES);
            hasBlockedScenes = true;
        }

        // Timeline check
        if (timeline == null) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.TIMELINE_MISSING);
        } else if (timeline.getStatus() != IcerikFabrikasiAnimaticTimelinePlanStatus.READY_FOR_ASSEMBLY_BOUNDARY) {
            issues.add(IcerikFabrikasiMvpReadinessIssueCode.TIMELINE_NOT_READY);
        }

        // Determine status
        IcerikFabrikasiMvpReadinessStatus status;
        if (issues.isEmpty()) {
            status = IcerikFabrikasiMvpReadinessStatus.READY_FOR_MANUAL_REVIEW;
        } else if (hasBlockedScenes && issues.size() == 1) {
            status = IcerikFabrikasiMvpReadinessStatus.PARTIALLY_READY;
        } else {
            status = issues.stream().anyMatch(i ->
                    i == IcerikFabrikasiMvpReadinessIssueCode.ASSET_PLAN_HAS_BLOCKED_SCENES)
                    ? IcerikFabrikasiMvpReadinessStatus.BLOCKED_BY_SAFETY
                    : IcerikFabrikasiMvpReadinessStatus.NOT_READY;
        }

        // Summaries
        String reqRef = request != null ? request.getOpaqueStoryboardRequestRef() : null;
        String title = request != null ? request.getSafeIcerikFabrikasiTitleLabel() : null;
        boolean copyrightSafe = request != null && request.getCopyrightSafeIntentSummary() != null;

        String sbSummary = draft != null
                ? String.format("%d sahne, %s", draft.getTotalSceneCount(), draft.getOrchestrationMode())
                : "Eksik";
        String apSummary = assetPlan != null
                ? String.format("%d planlanm\u0131\u015f, %d engelli", assetPlan.getTotalPlannedScenes(), assetPlan.getBlockedSceneCount())
                : "Eksik";
        String tlSummary = timeline != null
                ? String.format("%d \u00f6\u011fe, %dms toplam", timeline.getTotalItems(), timeline.getTotalDurationMs())
                : "Eksik";

        int totalScenes = assetPlan != null ? assetPlan.getTotalPlannedScenes() : 0;
        int blocked = assetPlan != null ? assetPlan.getBlockedSceneCount() : 0;
        int tlItems = timeline != null ? timeline.getTotalItems() : 0;
        int tlDuration = timeline != null ? timeline.getTotalDurationMs() : 0;

        boolean allPlanned = assetPlan != null && assetPlan.getTotalPlannedScenes() > 0;
        boolean tlComplete = timeline != null && timeline.getTotalItems() > 0;

        // Next steps — labels only
        List<String> nextSteps = new ArrayList<>();
        if (status == IcerikFabrikasiMvpReadinessStatus.READY_FOR_MANUAL_REVIEW) {
            nextSteps.add("G\u00f6rsel varl\u0131k \u00fcretimi i\u00e7in manuel sa\u011flay\u0131c\u0131 yap\u0131land\u0131rmas\u0131");
            nextSteps.add("Sahne g\u00f6rselleri i\u00e7in manuel onay");
            nextSteps.add("Montaj i\u00e7in manuel FFmpeg yap\u0131land\u0131rmas\u0131");
        } else {
            nextSteps.add("Eksik ad\u0131mlar\u0131 tamamlay\u0131n");
            if (hasBlockedScenes) nextSteps.add("Engellenen sahneleri \u00e7\u00f6z\u00fcn");
        }

        return new IcerikFabrikasiMvpReadinessResult(
                status, issues, reqRef, title,
                sbSummary, apSummary, tlSummary,
                totalScenes, blocked, tlItems, tlDuration,
                copyrightSafe, allPlanned, tlComplete, blocked == 0,
                nextSteps);
    }
}
