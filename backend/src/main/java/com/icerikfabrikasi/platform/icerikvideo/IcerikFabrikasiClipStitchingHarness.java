package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-040: Clip stitching harness. Disabled by default.
 * Does NOT use ProcessBuilder — delegates to runner if real execution needed.
 * CI uses fake/test-double only.
 */
public class IcerikFabrikasiClipStitchingHarness {

    private static final int MIN_CLIPS = 2;
    private static final int MAX_CLIPS = 10;
    private static final int MAX_DURATION_PER_CLIP = 5;
    private static final int MAX_TOTAL_DURATION = 60;
    private static final int MAX_FPS = 30;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;
    private static final long MAX_OUTPUT_SIZE = 150L * 1024 * 1024;

    private final IcerikFabrikasiRenderArtifactLifecycleService lifecycleService;
    private final IcerikFabrikasiFfmpegLocalDevProcessRunner runner;
    private final boolean stitchingEnabled;

    public IcerikFabrikasiClipStitchingHarness(IcerikFabrikasiRenderArtifactLifecycleService lifecycleService,
            IcerikFabrikasiFfmpegLocalDevProcessRunner runner, boolean stitchingEnabled) {
        this.lifecycleService = lifecycleService != null ? lifecycleService : new IcerikFabrikasiRenderArtifactLifecycleService();
        this.runner = runner != null ? runner : new IcerikFabrikasiFfmpegLocalDevProcessRunner();
        this.stitchingEnabled = stitchingEnabled;
    }

    public IcerikFabrikasiClipStitchingHarness(IcerikFabrikasiRenderArtifactLifecycleService lifecycleService,
            boolean stitchingEnabled) {
        this(lifecycleService, null, stitchingEnabled);
    }

    public IcerikFabrikasiClipStitchingHarness() { this(null, null, false); }

    /**
     * Validate and fake-stitch input clips (CI-safe).
     */
    public IcerikFabrikasiClipStitchingResult fakeStitch(List<IcerikFabrikasiClipStitchingInputClipRef> inputClips) {
        var validation = validate(inputClips);
        if (validation != null) return validation;

        var clips = inputClips != null ? inputClips : List.<IcerikFabrikasiClipStitchingInputClipRef>of();
        int totalDuration = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getDurationSeconds).sum();
        int maxW = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getWidth).max().orElse(0);
        int maxH = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getHeight).max().orElse(0);
        int maxFps = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getFps).max().orElse(0);
        long totalSize = clips.stream().mapToLong(IcerikFabrikasiClipStitchingInputClipRef::getSizeBytes).sum();
        String format = clips.get(0).getFormat();

        String stitchRef = "stitch:" + UUID.randomUUID();
        String outputRef = "stitched-output:" + UUID.randomUUID();

        // Verify each input clip individually through FP-035 (per-clip limits)
        boolean allVerified = true;
        for (var clip : clips) {
            var lcReport = lifecycleService.verify(clip.getOpaqueClipRef(), clip.getFormat(),
                    clip.getDurationSeconds(), clip.getFps(), clip.getWidth(), clip.getHeight(),
                    clip.getSizeBytes());
            if (lcReport.getVerificationStatus() != IcerikFabrikasiRenderArtifactVerificationStatus.PASSED)
                allVerified = false;
        }

        return new IcerikFabrikasiClipStitchingResult(stitchRef,
                allVerified ? IcerikFabrikasiClipStitchingStatus.STITCHED_ARTIFACT_VERIFIED
                        : IcerikFabrikasiClipStitchingStatus.REJECTED_UNSAFE,
                allVerified ? List.of() : List.of(IcerikFabrikasiClipStitchingIssueCode.VERIFICATION_FAILED),
                clips.size(), totalDuration, maxW, maxH, maxFps, totalSize,
                format, outputRef, false, allVerified);
    }

    /**
     * Attempt real stitching (disabled by default, LOCAL-only manual).
     */
    public IcerikFabrikasiClipStitchingResult attempt(List<IcerikFabrikasiClipStitchingInputClipRef> inputClips,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        if (!stitchingEnabled)
            return IcerikFabrikasiClipStitchingResult.gateDenied(IcerikFabrikasiClipStitchingStatus.MANUAL_FLAG_NOT_ENABLED);
        if (binaryPolicy == null || !binaryPolicy.isAvailable())
            return IcerikFabrikasiClipStitchingResult.gateDenied(IcerikFabrikasiClipStitchingStatus.FFMPEG_UNAVAILABLE);

        var validation = validate(inputClips);
        if (validation != null) return validation;

        // Real stitching — delegate to runner
        var clips = inputClips != null ? inputClips : List.<IcerikFabrikasiClipStitchingInputClipRef>of();
        int totalDuration = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getDurationSeconds).sum();
        int maxW = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getWidth).max().orElse(0);
        int maxH = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getHeight).max().orElse(0);
        int maxFps = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getFps).max().orElse(0);
        String format = clips.get(0).getFormat();

        var processResult = runner.stitchClips(clips.size(), totalDuration,
                maxFps, maxW, maxH, format, binaryPolicy);

        if (processResult.getStatus() == IcerikFabrikasiFfmpegProcessStatus.PROCESS_COMPLETED_SAFE) {
            String stitchRef = "stitch:" + UUID.randomUUID();
            long totalSize = clips.stream().mapToLong(IcerikFabrikasiClipStitchingInputClipRef::getSizeBytes).sum();
            return new IcerikFabrikasiClipStitchingResult(stitchRef,
                    IcerikFabrikasiClipStitchingStatus.STITCHED_ARTIFACT_VERIFIED, List.of(),
                    clips.size(), totalDuration, maxW, maxH, maxFps, totalSize,
                    format, processResult.getOutputRef(), true, true);
        }
        return IcerikFabrikasiClipStitchingResult.gateDenied(IcerikFabrikasiClipStitchingStatus.FAILED_SAFE);
    }

    private IcerikFabrikasiClipStitchingResult validate(List<IcerikFabrikasiClipStitchingInputClipRef> inputClips) {
        var issues = new ArrayList<IcerikFabrikasiClipStitchingIssueCode>();
        var clips = inputClips != null ? inputClips : List.<IcerikFabrikasiClipStitchingInputClipRef>of();

        if (clips.size() < MIN_CLIPS) issues.add(IcerikFabrikasiClipStitchingIssueCode.TOO_FEW_CLIPS);
        if (clips.size() > MAX_CLIPS) issues.add(IcerikFabrikasiClipStitchingIssueCode.TOO_MANY_CLIPS);

        var refSet = new HashSet<String>();
        for (var clip : clips) {
            if (clip.getOpaqueClipRef() == null || clip.getOpaqueClipRef().isBlank())
                issues.add(IcerikFabrikasiClipStitchingIssueCode.CLIP_REF_MISSING);
            if (!refSet.add(clip.getOpaqueClipRef()))
                issues.add(IcerikFabrikasiClipStitchingIssueCode.DUPLICATE_CLIP_REF);
            if (!clip.isVerified())
                issues.add(IcerikFabrikasiClipStitchingIssueCode.CLIP_NOT_VERIFIED);
            if (clip.getDurationSeconds() > MAX_DURATION_PER_CLIP)
                issues.add(IcerikFabrikasiClipStitchingIssueCode.CLIP_DURATION_EXCEEDED);
            if (clip.getFps() > MAX_FPS)
                issues.add(IcerikFabrikasiClipStitchingIssueCode.CLIP_FPS_EXCEEDED);
            if (clip.getWidth() > MAX_WIDTH || clip.getHeight() > MAX_HEIGHT)
                issues.add(IcerikFabrikasiClipStitchingIssueCode.CLIP_RESOLUTION_EXCEEDED);
            if (!"mp4".equalsIgnoreCase(clip.getFormat()) && !"webm".equalsIgnoreCase(clip.getFormat()))
                issues.add(IcerikFabrikasiClipStitchingIssueCode.FORMAT_UNSUPPORTED);
        }

        int totalDuration = clips.stream().mapToInt(IcerikFabrikasiClipStitchingInputClipRef::getDurationSeconds).sum();
        if (totalDuration > MAX_TOTAL_DURATION)
            issues.add(IcerikFabrikasiClipStitchingIssueCode.TOTAL_DURATION_EXCEEDED);

        long totalSize = clips.stream().mapToLong(IcerikFabrikasiClipStitchingInputClipRef::getSizeBytes).sum();
        if (totalSize > MAX_OUTPUT_SIZE)
            issues.add(IcerikFabrikasiClipStitchingIssueCode.OUTPUT_SIZE_EXCEEDED);

        if (!issues.isEmpty())
            return IcerikFabrikasiClipStitchingResult.rejected("stitch:" + UUID.randomUUID(), issues);
        return null;
    }

    public boolean isStitchingEnabled() { return stitchingEnabled; }
}
