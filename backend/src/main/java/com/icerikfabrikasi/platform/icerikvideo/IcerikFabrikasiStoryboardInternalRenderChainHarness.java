package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FP-047: Storyboard internal render chain harness.
 * Orchestrates: FP-046 adapter → FP-038 job → fake clips → fake stitch →
 * FP-035 lifecycle → FP-042 review → admin visibility.
 * No ProcessBuilder. No real FFmpeg. CI-safe.
 */
@Service
public class IcerikFabrikasiStoryboardInternalRenderChainHarness {

    private final IcerikFabrikasiStoryboardRenderPlanAdapterService adapterService;
    private final IcerikFabrikasiMultiSceneRenderOrchestrationService orchestrationService;
    private final IcerikFabrikasiClipStitchingHarness stitchingHarness;
    private final IcerikFabrikasiRenderArtifactLifecycleService lifecycleService;
    private final IcerikFabrikasiRenderReviewPackageService reviewService;

    public IcerikFabrikasiStoryboardInternalRenderChainHarness() {
        this.adapterService = new IcerikFabrikasiStoryboardRenderPlanAdapterService();
        this.orchestrationService = new IcerikFabrikasiMultiSceneRenderOrchestrationService();
        this.stitchingHarness = new IcerikFabrikasiClipStitchingHarness();
        this.lifecycleService = new IcerikFabrikasiRenderArtifactLifecycleService();
        this.reviewService = new IcerikFabrikasiRenderReviewPackageService();
    }

    /**
     * Execute full storyboard → admin visibility chain with fake metadata.
     */
    public IcerikFabrikasiStoryboardInternalRenderChainSummary execute(
            IcerikFabrikasiStoryboardInternalRenderChainFixture fixture) {

        String chainRef = "chain:" + UUID.randomUUID();
        var issues = new ArrayList<IcerikFabrikasiStoryboardInternalRenderChainIssueCode>();

        if (fixture == null || fixture.getSceneInputs().isEmpty()) {
            issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.ADAPTER_FAILED);
            return fail(chainRef, null, IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE, issues);
        }

        // 1. FP-046: Storyboard adapter
        var adapterResult = adapterService.adapt(fixture.getOpaqueStoryboardRef(),
                fixture.getOpaqueStoryRef(), fixture.getSceneInputs());
        if (adapterResult.getStatus() != IcerikFabrikasiStoryboardRenderAdapterStatus.READY_FOR_INTERNAL_RENDER_CHAIN) {
            issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.ADAPTER_FAILED);
            return fail(chainRef, fixture.getOpaqueStoryboardRef(),
                    IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE, issues);
        }

        // 2. FP-038: Multi-scene job
        var jobPlan = orchestrationService.buildJobPlan("job:" + UUID.randomUUID(),
                adapterResult.getScenePlans());
        if (jobPlan.getStatus() != IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER) {
            issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.MULTI_SCENE_JOB_FAILED);
            return fail(chainRef, fixture.getOpaqueStoryboardRef(),
                    IcerikFabrikasiStoryboardInternalRenderChainStatus.FAILED_SAFE, issues);
        }

        // 3. Fake multi-clip generation
        int clipCount = Math.max(adapterResult.getSceneCount(), 2);
        var clipRefs = new ArrayList<IcerikFabrikasiClipStitchingInputClipRef>();
        for (int i = 0; i < clipCount; i++) {
            clipRefs.add(new IcerikFabrikasiClipStitchingInputClipRef(
                    "clip-" + UUID.randomUUID(), i + 1, 2, 24, 640, 360,
                    1024 * 1024, "mp4", true));
        }

        // 4. Fake stitching
        var stitchResult = stitchingHarness.fakeStitch(clipRefs);
        if (stitchResult.getStatus() != IcerikFabrikasiClipStitchingStatus.STITCHED_ARTIFACT_VERIFIED) {
            issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.STITCHING_FAILED);
            return fail(chainRef, fixture.getOpaqueStoryboardRef(),
                    IcerikFabrikasiStoryboardInternalRenderChainStatus.FAILED_SAFE, issues);
        }

        // 5. FP-035: Lifecycle verification per clip
        for (var clip : clipRefs) {
            var report = lifecycleService.verify(clip.getOpaqueClipRef(), clip.getFormat(),
                    clip.getDurationSeconds(), clip.getFps(), clip.getWidth(), clip.getHeight(),
                    clip.getSizeBytes());
            if (report.getVerificationStatus() != IcerikFabrikasiRenderArtifactVerificationStatus.PASSED) {
                issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.LIFECYCLE_VERIFICATION_FAILED);
                return fail(chainRef, fixture.getOpaqueStoryboardRef(),
                        IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE, issues);
            }
        }

        // 6. FP-042: Review package
        var artifactSummary = new IcerikFabrikasiRenderReviewArtifactSummary(
                stitchResult.getOpaqueOutputRef(),
                IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP,
                IcerikFabrikasiRenderArtifactVerificationStatus.PASSED,
                "mp4", stitchResult.getTotalDurationSeconds(), 24, 640, 360,
                stitchResult.getEstimatedOutputSizeBytes());
        String bundleRef = "bundle:" + UUID.randomUUID();
        var reviewPkg = reviewService.build(jobPlan.getOpaqueJobRef(),
                bundleRef, List.of(artifactSummary),
                adapterResult.getSceneCount(), clipCount);
        if (reviewPkg.getStatus() != IcerikFabrikasiRenderReviewPackageStatus.READY_FOR_INTERNAL_REVIEW_ONLY) {
            issues.add(IcerikFabrikasiStoryboardInternalRenderChainIssueCode.REVIEW_PACKAGE_FAILED);
            return fail(chainRef, fixture.getOpaqueStoryboardRef(),
                    IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE, issues);
        }

        // 7. Admin visibility — metadata-only
        return new IcerikFabrikasiStoryboardInternalRenderChainSummary(chainRef,
                fixture.getOpaqueStoryboardRef(), jobPlan.getOpaqueJobRef(),
                bundleRef, reviewPkg.getOpaqueReviewPackageRef(),
                IcerikFabrikasiStoryboardInternalRenderChainStatus.ADMIN_METADATA_VISIBLE,
                List.of(), adapterResult.getSceneCount(), clipCount, true,
                reviewPkg.getStatus(), true,
                stitchResult.getTotalDurationSeconds(), 640, 360, 24,
                stitchResult.getEstimatedOutputSizeBytes());
    }

    private IcerikFabrikasiStoryboardInternalRenderChainSummary fail(String ref, String sbRef,
            IcerikFabrikasiStoryboardInternalRenderChainStatus status,
            List<IcerikFabrikasiStoryboardInternalRenderChainIssueCode> issues) {
        return new IcerikFabrikasiStoryboardInternalRenderChainSummary(ref, sbRef,
                null, null, null, status, issues,
                0, 0, false, null, false,
                0, 0, 0, 0, 0);
    }
}
