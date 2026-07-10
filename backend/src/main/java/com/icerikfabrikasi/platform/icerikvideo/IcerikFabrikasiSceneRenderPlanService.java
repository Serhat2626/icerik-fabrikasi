package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-037: Scene render plan service.
 * Validates scene plans and assembles bundles via FP-036.
 * No ProcessBuilder. No paths/commands/content.
 */
public class IcerikFabrikasiSceneRenderPlanService {

    private static final int MAX_SCENES_PER_BATCH = 20;
    private static final int MAX_DURATION_SECONDS = 5;
    private static final int MAX_FPS = 30;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;

    private final IcerikFabrikasiRenderBundleManifestService bundleService;

    public IcerikFabrikasiSceneRenderPlanService(IcerikFabrikasiRenderBundleManifestService bundleService) {
        this.bundleService = bundleService != null ? bundleService : new IcerikFabrikasiRenderBundleManifestService();
    }

    public IcerikFabrikasiSceneRenderPlanService() {
        this(new IcerikFabrikasiRenderBundleManifestService());
    }

    /**
     * Validate a single scene render plan.
     */
    public IcerikFabrikasiSceneRenderPlan validate(String opaqueSceneRef, String opaquePlanRef,
            int sceneOrder, int durationSeconds, int fps, int width, int height,
            List<IcerikFabrikasiSceneRenderArtifactExpectation> expectations) {

        var issues = new ArrayList<IcerikFabrikasiSceneRenderPlanIssueCode>();
        var safeExpectations = expectations != null ? expectations : List.<IcerikFabrikasiSceneRenderArtifactExpectation>of();

        if (opaqueSceneRef == null || opaqueSceneRef.isBlank())
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.SCENE_REF_MISSING);
        if (opaquePlanRef == null || opaquePlanRef.isBlank())
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.PLAN_REF_MISSING);
        if (sceneOrder < 1)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.SCENE_ORDER_INVALID);
        if (durationSeconds < 1 || durationSeconds > MAX_DURATION_SECONDS)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.DURATION_EXCEEDS_LIMIT);
        if (fps < 1 || fps > MAX_FPS)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.FPS_EXCEEDS_LIMIT);
        if (width < 1 || width > MAX_WIDTH)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.WIDTH_EXCEEDS_LIMIT);
        if (height < 1 || height > MAX_HEIGHT)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.HEIGHT_EXCEEDS_LIMIT);

        // Primary video expectation
        long primaryCount = safeExpectations.stream()
                .filter(e -> e.getRole() == IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP).count();
        if (primaryCount == 0)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.MISSING_PRIMARY_VIDEO_EXPECTATION);
        else if (primaryCount > 1)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.DUPLICATE_PRIMARY_VIDEO_EXPECTATION);

        // Unknown roles
        if (safeExpectations.stream().anyMatch(e -> !e.getRole().isAllowed()))
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.UNSUPPORTED_ARTIFACT_ROLE);

        var status = issues.isEmpty()
                ? IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY
                : IcerikFabrikasiSceneRenderPlanStatus.REJECTED_UNSAFE;

        return new IcerikFabrikasiSceneRenderPlan(opaqueSceneRef, opaquePlanRef,
                sceneOrder, durationSeconds, fps, width, height,
                safeExpectations, status, issues);
    }

    /**
     * Validate a batch of scene orders for uniqueness/count.
     */
    public List<IcerikFabrikasiSceneRenderPlanIssueCode> validateBatch(List<Integer> sceneOrders) {
        var issues = new ArrayList<IcerikFabrikasiSceneRenderPlanIssueCode>();
        if (sceneOrders == null) return issues;
        if (sceneOrders.size() > MAX_SCENES_PER_BATCH)
            issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.TOO_MANY_SCENES);
        var seen = new HashSet<Integer>();
        for (int order : sceneOrders) {
            if (!seen.add(order))
                issues.add(IcerikFabrikasiSceneRenderPlanIssueCode.SCENE_ORDER_DUPLICATE);
        }
        return issues;
    }

    /**
     * Assemble bundle from validated plan + artifact refs.
     * Uses FP-036 bundle manifest service.
     */
    public IcerikFabrikasiRenderBundleManifest assembleBundle(IcerikFabrikasiSceneRenderPlan plan,
            List<IcerikFabrikasiRenderBundleArtifactRef> artifactRefs) {

        // Plan must be ready
        if (plan == null || plan.getStatus() != IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY)
            return bundleService.build(null, List.of());

        var safeRefs = artifactRefs != null ? artifactRefs : List.<IcerikFabrikasiRenderBundleArtifactRef>of();

        // Check required expectations are met
        for (var expectation : plan.getArtifactExpectations()) {
            if (expectation.isRequired()) {
                boolean found = safeRefs.stream()
                        .anyMatch(r -> r.getRole() == expectation.getRole());
                if (!found) {
                    // Missing required artifact — pass through so bundle service detects it
                    return bundleService.build(plan.getOpaquePlanRef(), safeRefs);
                }
            }
        }

        // Check role matches
        for (var ref : safeRefs) {
            boolean expectedRole = plan.getArtifactExpectations().stream()
                    .anyMatch(e -> e.getRole() == ref.getRole());
            if (!expectedRole) {
                // Unexpected role — still pass through, bundle service handles UNKNOWN
            }
        }

        return bundleService.build(plan.getOpaquePlanRef(), safeRefs);
    }
}
