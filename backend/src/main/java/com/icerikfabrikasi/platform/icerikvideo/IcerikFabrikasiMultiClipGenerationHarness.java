package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-039: Multi-clip generation harness. Disabled by default.
 * Does NOT use ProcessBuilder — delegates to single-clip harness.
 * Real execution only when ALL gates pass.
 */
public class IcerikFabrikasiMultiClipGenerationHarness {

    private final IcerikFabrikasiFfmpegClipGenerationHarness clipHarness;
    private final IcerikFabrikasiRenderArtifactLifecycleService lifecycleService;
    private final IcerikFabrikasiRenderBundleManifestService bundleService;
    private final boolean multiClipEnabled;

    public IcerikFabrikasiMultiClipGenerationHarness(IcerikFabrikasiFfmpegClipGenerationHarness clipHarness,
            IcerikFabrikasiRenderArtifactLifecycleService lifecycleService,
            IcerikFabrikasiRenderBundleManifestService bundleService,
            boolean multiClipEnabled) {
        this.clipHarness = clipHarness != null ? clipHarness : new IcerikFabrikasiFfmpegClipGenerationHarness();
        this.lifecycleService = lifecycleService != null ? lifecycleService : new IcerikFabrikasiRenderArtifactLifecycleService();
        this.bundleService = bundleService != null ? bundleService : new IcerikFabrikasiRenderBundleManifestService();
        this.multiClipEnabled = multiClipEnabled;
    }

    public IcerikFabrikasiMultiClipGenerationHarness() {
        this(null, null, null, false);
    }

    /**
     * Attempt multi-clip generation from validated job plan.
     */
    public IcerikFabrikasiMultiClipGenerationResult attempt(
            IcerikFabrikasiMultiSceneRenderJobPlan jobPlan,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        String jobRef = jobPlan != null ? jobPlan.getOpaqueJobRef() : null;

        // 1. Manual flag
        if (!multiClipEnabled)
            return IcerikFabrikasiMultiClipGenerationResult.gateDenied(jobRef,
                    IcerikFabrikasiMultiClipGenerationStatus.MANUAL_FLAG_NOT_ENABLED);

        // 2. Clip harness must be enabled
        if (!clipHarness.isClipGenerationEnabled())
            return IcerikFabrikasiMultiClipGenerationResult.gateDenied(jobRef,
                    IcerikFabrikasiMultiClipGenerationStatus.PROCESS_DISABLED);

        // 3. Binary policy
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiMultiClipGenerationResult.gateDenied(jobRef,
                    IcerikFabrikasiMultiClipGenerationStatus.FFMPEG_UNAVAILABLE);

        // 4. Job plan must be ready
        if (jobPlan == null || jobPlan.getStatus() != IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER)
            return IcerikFabrikasiMultiClipGenerationResult.gateDenied(jobRef,
                    IcerikFabrikasiMultiClipGenerationStatus.JOB_NOT_READY);

        // 5. Generate clips per scene
        var clipRefs = new ArrayList<String>();
        var bundleArtifacts = new ArrayList<IcerikFabrikasiRenderBundleArtifactRef>();
        var issues = new ArrayList<IcerikFabrikasiMultiClipGenerationIssueCode>();
        int generated = 0, verified = 0, rejected = 0;
        int totalDuration = 0, maxW = 0, maxH = 0, maxFps = 0;
        long totalSize = 0;

        for (var sceneRef : jobPlan.getScenes()) {
            var req = IcerikFabrikasiMultiClipGenerationSceneRequest.fromSceneRef(sceneRef);
            if (!req.isValid()) {
                issues.add(IcerikFabrikasiMultiClipGenerationIssueCode.CLIP_GENERATION_FAILED);
                rejected++;
                continue;
            }

            // Build process request for single clip
            var processReq = new IcerikFabrikasiFfmpegProcessRequest(
                    req.getOpaqueSceneRef(), req.getFormat(), 30,
                    req.getDurationSeconds(), req.getFps(),
                    req.getWidth(), req.getHeight(), 50);
            var clipResult = clipHarness.attempt(processReq, binaryPolicy);

            if (clipResult.getStatus() == IcerikFabrikasiFfmpegClipGenerationStatus.VERIFICATION_PASSED) {
                generated++;
                // Verify via FP-035 lifecycle
                var lcReport = lifecycleService.verify(
                        clipResult.getOutputRef(), req.getFormat(),
                        req.getDurationSeconds(), req.getFps(),
                        req.getWidth(), req.getHeight(),
                        clipResult.getFixtureMetadata() != null ? clipResult.getFixtureMetadata().getSizeMB() * 1024L * 1024 : 0);

                if (lcReport.getVerificationStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.PASSED) {
                    verified++;
                    clipRefs.add(clipResult.getOutputRef());
                    bundleArtifacts.add(IcerikFabrikasiRenderBundleArtifactRef.fromLifecycleReport(lcReport,
                            IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP));
                    totalDuration += req.getDurationSeconds();
                    maxW = Math.max(maxW, req.getWidth());
                    maxH = Math.max(maxH, req.getHeight());
                    maxFps = Math.max(maxFps, req.getFps());
                    totalSize += lcReport.getSizeBytes();
                } else {
                    rejected++;
                    issues.add(IcerikFabrikasiMultiClipGenerationIssueCode.CLIP_VERIFICATION_FAILED);
                }
            } else {
                rejected++;
                issues.add(IcerikFabrikasiMultiClipGenerationIssueCode.CLIP_GENERATION_FAILED);
            }
        }

        // 6. Determine status
        IcerikFabrikasiMultiClipGenerationStatus status;
        if (generated == 0 && rejected > 0)
            status = IcerikFabrikasiMultiClipGenerationStatus.FAILED_SAFE;
        else if (rejected > 0)
            status = IcerikFabrikasiMultiClipGenerationStatus.REJECTED_UNSAFE;
        else
            status = IcerikFabrikasiMultiClipGenerationStatus.GENERATED_CLIPS_VERIFIED;

        String runRef = "multi-clip-run:" + UUID.randomUUID();
        return new IcerikFabrikasiMultiClipGenerationResult(jobRef, runRef, status, issues,
                jobPlan.getSceneCount(), generated, verified, rejected,
                totalDuration, maxW, maxH, maxFps, totalSize, clipRefs, true);
    }

    /**
     * Fake/test-double multi-clip generation (CI-safe).
     */
    public IcerikFabrikasiMultiClipGenerationResult fakeGenerate(IcerikFabrikasiMultiSceneRenderJobPlan jobPlan) {
        if (jobPlan == null || jobPlan.getStatus() != IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER)
            return IcerikFabrikasiMultiClipGenerationResult.gateDenied(
                    jobPlan != null ? jobPlan.getOpaqueJobRef() : null,
                    IcerikFabrikasiMultiClipGenerationStatus.JOB_NOT_READY);

        var clipRefs = new ArrayList<String>();
        var issues = new ArrayList<IcerikFabrikasiMultiClipGenerationIssueCode>();
        int totalDuration = 0, maxW = 0, maxH = 0, maxFps = 0;
        long totalSize = 0;

        for (var sceneRef : jobPlan.getScenes()) {
            String fakeRef = "fake-clip:" + UUID.randomUUID();
            clipRefs.add(fakeRef);
            totalDuration += sceneRef.getExpectedDurationSeconds();
            maxW = Math.max(maxW, sceneRef.getExpectedWidth());
            maxH = Math.max(maxH, sceneRef.getExpectedHeight());
            maxFps = Math.max(maxFps, sceneRef.getExpectedFps());
            totalSize += 1024L * 1024; // 1MB fake
        }

        int count = jobPlan.getSceneCount();
        return new IcerikFabrikasiMultiClipGenerationResult(jobPlan.getOpaqueJobRef(),
                "fake-run:" + UUID.randomUUID(),
                IcerikFabrikasiMultiClipGenerationStatus.GENERATED_CLIPS_VERIFIED, issues,
                count, count, count, 0,
                totalDuration, maxW, maxH, maxFps, totalSize, clipRefs, false);
    }

    public boolean isMultiClipEnabled() { return multiClipEnabled; }
}
