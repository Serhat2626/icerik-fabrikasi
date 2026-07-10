package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-046: Adapter service — converts safe storyboard metadata into FP-037 scene render plans.
 * No ProcessBuilder. No FFmpeg. No paths/commands/content.
 */
public class IcerikFabrikasiStoryboardRenderPlanAdapterService {

    private static final int MAX_SCENES = 20;
    private static final int MAX_DURATION = 5;
    private static final int MAX_FPS = 30;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;

    private final IcerikFabrikasiSceneRenderPlanService planService;

    public IcerikFabrikasiStoryboardRenderPlanAdapterService(IcerikFabrikasiSceneRenderPlanService planService) {
        this.planService = planService != null ? planService : new IcerikFabrikasiSceneRenderPlanService();
    }

    public IcerikFabrikasiStoryboardRenderPlanAdapterService() {
        this(new IcerikFabrikasiSceneRenderPlanService());
    }

    /**
     * Convert storyboard scene inputs into validated scene render plans.
     */
    public IcerikFabrikasiStoryboardRenderPlanAdapterResult adapt(String opaqueStoryboardRef,
            String opaqueStoryRef, List<IcerikFabrikasiStoryboardSceneRenderInput> sceneInputs) {

        String adapterRef = "adapter:" + UUID.randomUUID();
        var issues = new ArrayList<IcerikFabrikasiStoryboardRenderAdapterIssueCode>();
        var safe = sceneInputs != null ? sceneInputs : List.<IcerikFabrikasiStoryboardSceneRenderInput>of();

        // Validate refs
        if (opaqueStoryboardRef == null || opaqueStoryboardRef.isBlank())
            issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.STORYBOARD_REF_MISSING);
        if (opaqueStoryRef == null || opaqueStoryRef.isBlank())
            issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.STORY_REF_MISSING);

        // Validate scene count
        if (safe.isEmpty()) {
            issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.NO_SCENES);
            return fail(adapterRef, opaqueStoryboardRef, opaqueStoryRef, issues);
        }
        if (safe.size() > MAX_SCENES) {
            issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.TOO_MANY_SCENES);
            return fail(adapterRef, opaqueStoryboardRef, opaqueStoryRef, issues);
        }

        // Validate scene orders
        var orderSet = new HashSet<Integer>();
        for (var input : safe) {
            if (input.getOpaqueSceneRef() == null || input.getOpaqueSceneRef().isBlank())
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_REF_MISSING);
            if (input.getSceneOrder() < 1)
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_ORDER_INVALID);
            if (!orderSet.add(input.getSceneOrder()))
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_ORDER_DUPLICATE);
            if (input.getTargetDurationSeconds() < 1 || input.getTargetDurationSeconds() > MAX_DURATION)
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.DURATION_EXCEEDS_LIMIT);
            if (input.getTargetFps() < 1 || input.getTargetFps() > MAX_FPS)
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.FPS_EXCEEDS_LIMIT);
            if (input.getTargetWidth() < 1 || input.getTargetWidth() > MAX_WIDTH)
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.WIDTH_EXCEEDS_LIMIT);
            if (input.getTargetHeight() < 1 || input.getTargetHeight() > MAX_HEIGHT)
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.HEIGHT_EXCEEDS_LIMIT);
        }

        if (!issues.isEmpty())
            return fail(adapterRef, opaqueStoryboardRef, opaqueStoryRef, issues);

        // Convert to FP-037 scene render plans
        var primaryExpectation = new IcerikFabrikasiSceneRenderArtifactExpectation(
                IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP, true);
        var plans = new ArrayList<IcerikFabrikasiSceneRenderPlan>();
        for (var input : safe) {
            var plan = planService.validate(input.getOpaqueSceneRef(),
                    "plan:" + UUID.randomUUID(), input.getSceneOrder(),
                    input.getTargetDurationSeconds(), input.getTargetFps(),
                    input.getTargetWidth(), input.getTargetHeight(),
                    List.of(primaryExpectation));
            if (plan.getStatus() != IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY) {
                issues.add(IcerikFabrikasiStoryboardRenderAdapterIssueCode.SCENE_PLAN_VALIDATION_FAILED);
                return fail(adapterRef, opaqueStoryboardRef, opaqueStoryRef, issues);
            }
            plans.add(plan);
        }

        return new IcerikFabrikasiStoryboardRenderPlanAdapterResult(adapterRef,
                opaqueStoryboardRef, opaqueStoryRef,
                IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN,
                List.of(), plans, plans.size());
    }

    private IcerikFabrikasiStoryboardRenderPlanAdapterResult fail(String ref, String sbRef, String storyRef,
            List<IcerikFabrikasiStoryboardRenderAdapterIssueCode> issues) {
        return new IcerikFabrikasiStoryboardRenderPlanAdapterResult(ref, sbRef, storyRef,
                issues.stream().anyMatch(i ->
                        i == IcerikFabrikasiStoryboardRenderAdapterIssueCode.STORYBOARD_REF_MISSING
                        || i == IcerikFabrikasiStoryboardRenderAdapterIssueCode.STORY_REF_MISSING)
                        ? IcerikFabrikasiStoryboardRenderAdapterStatus.REJECTED_UNSAFE
                        : IcerikFabrikasiStoryboardRenderAdapterStatus.FAILED_SAFE,
                issues, List.of(), 0);
    }
}
