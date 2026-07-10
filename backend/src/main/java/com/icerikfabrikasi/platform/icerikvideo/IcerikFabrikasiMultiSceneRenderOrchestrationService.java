package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FP-038: Multi-scene render orchestration service.
 * Validates scene plan batches, produces metadata-only job plans.
 * No ProcessBuilder. No paths/commands/content. No FFmpeg execution.
 */
public class IcerikFabrikasiMultiSceneRenderOrchestrationService {

    private static final int MAX_SCENES = 20;
    private static final int MAX_DURATION_PER_SCENE = 5;
    private static final int MAX_TOTAL_DURATION = 60;
    private static final int MAX_FPS = 30;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;

    /**
     * Build job plan from validated scene render plans.
     */
    public IcerikFabrikasiMultiSceneRenderJobPlan buildJobPlan(String opaqueJobRef,
            List<IcerikFabrikasiSceneRenderPlan> scenePlans) {

        var issues = new ArrayList<IcerikFabrikasiMultiSceneRenderJobIssueCode>();
        var safePlans = scenePlans != null ? scenePlans : List.<IcerikFabrikasiSceneRenderPlan>of();

        // 1. Job ref
        if (opaqueJobRef == null || opaqueJobRef.isBlank())
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.JOB_REF_MISSING);

        // 2. Empty
        if (safePlans.isEmpty()) {
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.EMPTY_SCENE_LIST);
            return new IcerikFabrikasiMultiSceneRenderJobPlan(opaqueJobRef,
                    IcerikFabrikasiMultiSceneRenderJobStatus.REJECTED_UNSAFE, List.of(), issues);
        }

        // 3. Too many
        if (safePlans.size() > MAX_SCENES)
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.TOO_MANY_SCENES);

        // 4. Sort by sceneOrder
        var sorted = safePlans.stream()
                .sorted(Comparator.comparingInt(IcerikFabrikasiSceneRenderPlan::getSceneOrder))
                .toList();

        // 5. Scene order checks
        var orders = sorted.stream().map(IcerikFabrikasiSceneRenderPlan::getSceneOrder).toList();
        var orderSet = new HashSet<>(orders);
        if (orderSet.size() != orders.size())
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_ORDER_DUPLICATE);
        if (orders.stream().anyMatch(o -> o < 1))
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_ORDER_NOT_POSITIVE);
        // Contiguity: must be 1,2,3,...,N
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i) != i + 1) {
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_ORDER_NOT_CONTIGUOUS);
                break;
            }
        }

        // 6. Scene ref checks
        var sceneRefs = sorted.stream().map(IcerikFabrikasiSceneRenderPlan::getOpaqueSceneRef).toList();
        if (sceneRefs.stream().anyMatch(r -> r == null || r.isBlank()))
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_REF_MISSING);
        var refSet = new HashSet<>(sceneRefs);
        if (refSet.size() != sceneRefs.size())
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_REF_DUPLICATE);

        // 7. Per-scene validation
        for (var plan : sorted) {
            if (plan.getStatus() == IcerikFabrikasiSceneRenderPlanStatus.FAILED_SAFE)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_FAILED);
            else if (plan.getStatus() == IcerikFabrikasiSceneRenderPlanStatus.REJECTED_UNSAFE)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_REJECTED);
            else if (plan.getStatus() != IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_NOT_READY);

            if (plan.getExpectedDurationSeconds() > MAX_DURATION_PER_SCENE)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.DURATION_PER_SCENE_EXCEEDS_LIMIT);
            if (plan.getExpectedFps() > MAX_FPS)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.FPS_EXCEEDS_LIMIT);
            if (plan.getExpectedWidth() > MAX_WIDTH || plan.getExpectedHeight() > MAX_HEIGHT)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.RESOLUTION_EXCEEDS_LIMIT);

            boolean hasPrimary = plan.getArtifactExpectations().stream()
                    .anyMatch(e -> e.getRole() == IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP);
            if (!hasPrimary)
                issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.MISSING_PRIMARY_VIDEO);
        }

        // 8. Total duration
        int totalDuration = sorted.stream().mapToInt(IcerikFabrikasiSceneRenderPlan::getExpectedDurationSeconds).sum();
        if (totalDuration > MAX_TOTAL_DURATION)
            issues.add(IcerikFabrikasiMultiSceneRenderJobIssueCode.TOTAL_DURATION_EXCEEDS_LIMIT);

        // 9. Build scene refs
        var sceneRefList = sorted.stream()
                .map(IcerikFabrikasiMultiSceneRenderSceneRef::fromPlan)
                .toList();

        // 10. Determine status
        IcerikFabrikasiMultiSceneRenderJobStatus status;
        if (issues.stream().anyMatch(i -> i == IcerikFabrikasiMultiSceneRenderJobIssueCode.SCENE_FAILED))
            status = IcerikFabrikasiMultiSceneRenderJobStatus.FAILED_SAFE;
        else if (!issues.isEmpty())
            status = IcerikFabrikasiMultiSceneRenderJobStatus.REJECTED_UNSAFE;
        else
            status = IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER;

        return new IcerikFabrikasiMultiSceneRenderJobPlan(opaqueJobRef, status, sceneRefList, issues);
    }
}
