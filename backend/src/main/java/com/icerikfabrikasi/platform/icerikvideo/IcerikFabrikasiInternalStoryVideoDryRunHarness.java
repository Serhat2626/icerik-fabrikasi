package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-044: Internal story-to-video dry run harness.
 * Orchestrates the full internal chain using fake/test-double metadata.
 * No ProcessBuilder. No real FFmpeg. CI-safe.
 */
public class IcerikFabrikasiInternalStoryVideoDryRunHarness {

    private final IcerikFabrikasiSceneRenderPlanService planService;
    private final IcerikFabrikasiMultiSceneRenderOrchestrationService orchestrationService;
    private final IcerikFabrikasiClipStitchingHarness stitchingHarness;
    private final IcerikFabrikasiRenderReviewPackageService reviewService;

    public IcerikFabrikasiInternalStoryVideoDryRunHarness() {
        this.planService = new IcerikFabrikasiSceneRenderPlanService();
        this.orchestrationService = new IcerikFabrikasiMultiSceneRenderOrchestrationService();
        this.stitchingHarness = new IcerikFabrikasiClipStitchingHarness();
        this.reviewService = new IcerikFabrikasiRenderReviewPackageService();
    }

    /**
     * Execute full E2E internal dry run with fake metadata.
     * Returns summary aggregating all stages.
     */
    public IcerikFabrikasiInternalStoryVideoDryRunSummary execute(int sceneCount) {
        String dryRunRef = "dry-run:" + UUID.randomUUID();
        var issues = new ArrayList<IcerikFabrikasiInternalStoryVideoDryRunIssueCode>();

        if (sceneCount < 1 || sceneCount > 20) {
            issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.SCENE_PLAN_INVALID);
            return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.REJECTED_UNSAFE, issues);
        }

        // 1. Scene plans (FP-037)
        var primaryExpectation = new IcerikFabrikasiSceneRenderArtifactExpectation(
                IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP, true);
        var plans = new ArrayList<IcerikFabrikasiSceneRenderPlan>();
        for (int i = 1; i <= sceneCount; i++) {
            var plan = planService.validate("scene:" + UUID.randomUUID(),
                    "plan:" + UUID.randomUUID(), i, 2, 24, 640, 360,
                    List.of(primaryExpectation));
            if (plan.getStatus() != IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY) {
                issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.SCENE_PLAN_INVALID);
                return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.REJECTED_UNSAFE, issues);
            }
            plans.add(plan);
        }

        // 2. Multi-scene job (FP-038)
        var jobPlan = orchestrationService.buildJobPlan("job:" + UUID.randomUUID(), plans);
        if (jobPlan.getStatus() != IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER) {
            issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.MULTI_SCENE_JOB_FAILED);
            return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.FAILED_SAFE, issues);
        }

        // 3. Fake multi-clip generation (FP-039 style)
        int clipCount = Math.max(sceneCount, 2); // stitching requires min 2
        var clipRefs = new ArrayList<IcerikFabrikasiClipStitchingInputClipRef>();
        for (int i = 0; i < clipCount; i++) {
            clipRefs.add(new IcerikFabrikasiClipStitchingInputClipRef(
                    "clip-" + UUID.randomUUID(), i + 1, 2, 24, 640, 360,
                    1024 * 1024, "mp4", true));
        }

        // 4. Fake stitching (FP-040)
        var stitchResult = stitchingHarness.fakeStitch(clipRefs);
        boolean stitched = stitchResult.getStatus() == IcerikFabrikasiClipStitchingStatus.STITCHED_ARTIFACT_VERIFIED;
        if (!stitched) {
            issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.STITCHING_FAILED);
            return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.FAILED_SAFE, issues);
        }

        // 5. Lifecycle verification (FP-035) — per clip
        var lifecycleService = new IcerikFabrikasiRenderArtifactLifecycleService();
        for (var clip : clipRefs) {
            var report = lifecycleService.verify(clip.getOpaqueClipRef(), clip.getFormat(),
                    clip.getDurationSeconds(), clip.getFps(), clip.getWidth(), clip.getHeight(),
                    clip.getSizeBytes());
            if (report.getVerificationStatus() != IcerikFabrikasiRenderArtifactVerificationStatus.PASSED) {
                issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.LIFECYCLE_VERIFICATION_FAILED);
                return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.REJECTED_UNSAFE, issues);
            }
        }

        // 6. Review package (FP-042)
        var artifactSummary = new IcerikFabrikasiRenderReviewArtifactSummary(
                stitchResult.getOpaqueOutputRef(),
                IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP,
                IcerikFabrikasiRenderArtifactVerificationStatus.PASSED,
                "mp4", stitchResult.getTotalDurationSeconds(), 24, 640, 360,
                stitchResult.getEstimatedOutputSizeBytes());
        var reviewPkg = reviewService.build(jobPlan.getOpaqueJobRef(),
                "bundle:" + UUID.randomUUID(), List.of(artifactSummary),
                sceneCount, sceneCount);
        if (reviewPkg.getStatus() != IcerikFabrikasiRenderReviewPackageStatus.READY_FOR_INTERNAL_REVIEW_ONLY) {
            issues.add(IcerikFabrikasiInternalStoryVideoDryRunIssueCode.REVIEW_PACKAGE_REJECTED);
            return fail(dryRunRef, IcerikFabrikasiInternalStoryVideoDryRunStatus.REJECTED_UNSAFE, issues);
        }

        // 7. Admin visibility — metadata-only
        return new IcerikFabrikasiInternalStoryVideoDryRunSummary(dryRunRef,
                jobPlan.getOpaqueJobRef(), reviewPkg.getOpaqueBundleRef(),
                reviewPkg.getOpaqueReviewPackageRef(),
                IcerikFabrikasiInternalStoryVideoDryRunStatus.ADMIN_METADATA_VISIBLE,
                List.of(), sceneCount, clipCount, true,
                reviewPkg.getStatus(), true,
                stitchResult.getTotalDurationSeconds(), 640, 360, 24,
                stitchResult.getEstimatedOutputSizeBytes());
    }

    private IcerikFabrikasiInternalStoryVideoDryRunSummary fail(String ref,
            IcerikFabrikasiInternalStoryVideoDryRunStatus status,
            List<IcerikFabrikasiInternalStoryVideoDryRunIssueCode> issues) {
        return new IcerikFabrikasiInternalStoryVideoDryRunSummary(ref,
                null, null, null, status, issues,
                0, 0, false, null, false,
                0, 0, 0, 0, 0);
    }
}
