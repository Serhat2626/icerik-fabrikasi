package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * FP-035: Artifact lifecycle verification service for LOCAL-only generated renders.
 * Accepts metadata from FP-034 clip generation, verifies bounded limits,
 * produces safe lifecycle report with issue codes only.
 * No ProcessBuilder. No path/command/stdout/stderr exposure.
 */
public class IcerikFabrikasiRenderArtifactLifecycleService {

    // FP-035 bounded limits for manual LOCAL clips
    private static final int MAX_DURATION_SECONDS = 5;
    private static final int MAX_FPS = 30;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;
    private static final long MAX_SIZE_BYTES = 50L * 1024 * 1024; // 50MB

    /**
     * Verify artifact metadata against bounded limits.
     * Returns safe report with issue codes only.
     */
    public IcerikFabrikasiRenderArtifactLifecycleReport verify(
            String opaqueRef, String formatStr, int durationSeconds, int fps,
            int width, int height, long sizeBytes) {

        var format = IcerikFabrikasiRenderArtifactFormat.fromString(formatStr);
        var issues = new ArrayList<IcerikFabrikasiRenderArtifactIssueCode>();

        if (opaqueRef == null || opaqueRef.isBlank())
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.REF_MISSING);
        if (!format.isAllowed())
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.FORMAT_NOT_ALLOWED);
        if (durationSeconds > MAX_DURATION_SECONDS || durationSeconds < 1)
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.DURATION_EXCEEDS_LIMIT);
        if (fps > MAX_FPS || fps < 1)
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.FPS_EXCEEDS_LIMIT);
        if (width > MAX_WIDTH || width < 1)
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.WIDTH_EXCEEDS_LIMIT);
        if (height > MAX_HEIGHT || height < 1)
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.HEIGHT_EXCEEDS_LIMIT);
        if (sizeBytes > MAX_SIZE_BYTES || sizeBytes < 0)
            issues.add(IcerikFabrikasiRenderArtifactIssueCode.SIZE_EXCEEDS_LIMIT);

        var status = issues.isEmpty()
                ? IcerikFabrikasiRenderArtifactVerificationStatus.PASSED
                : (issues.size() == 1 ? mapSingleIssue(issues.get(0))
                        : IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_MULTIPLE);

        return new IcerikFabrikasiRenderArtifactLifecycleReport(opaqueRef, format,
                durationSeconds, fps, width, height, sizeBytes, status, issues);
    }

    /**
     * Verify from FP-034 fixture metadata.
     */
    public IcerikFabrikasiRenderArtifactLifecycleReport verifyFromFixture(IcerikFabrikasiFfmpegFixtureMetadata metadata) {
        if (metadata == null)
            return new IcerikFabrikasiRenderArtifactLifecycleReport(null, IcerikFabrikasiRenderArtifactFormat.UNKNOWN,
                    0, 0, 0, 0, 0, IcerikFabrikasiRenderArtifactVerificationStatus.FAILED_SAFE,
                    List.of(IcerikFabrikasiRenderArtifactIssueCode.METADATA_MISSING));

        return verify(metadata.getOutputRef(), metadata.getFormat(),
                metadata.getDurationSeconds(), metadata.getFps(),
                metadata.getWidth(), metadata.getHeight(),
                metadata.getSizeMB() * 1024 * 1024);
    }

    /**
     * Register cleanup (metadata-only, no path exposure).
     */
    public IcerikFabrikasiRenderArtifactLifecycleStatus registerCleanup(String opaqueRef) {
        if (opaqueRef == null || opaqueRef.isBlank())
            return IcerikFabrikasiRenderArtifactLifecycleStatus.FAILED_SAFE;
        return IcerikFabrikasiRenderArtifactLifecycleStatus.CLEANUP_SCHEDULED_OR_REGISTERED;
    }

    /**
     * Confirm cleanup (best-effort, metadata-only).
     */
    public IcerikFabrikasiRenderArtifactLifecycleStatus confirmCleanup(String opaqueRef) {
        if (opaqueRef == null || opaqueRef.isBlank())
            return IcerikFabrikasiRenderArtifactLifecycleStatus.FAILED_SAFE;
        return IcerikFabrikasiRenderArtifactLifecycleStatus.CLEANUP_CONFIRMED_OR_BEST_EFFORT;
    }

    private IcerikFabrikasiRenderArtifactVerificationStatus mapSingleIssue(IcerikFabrikasiRenderArtifactIssueCode issue) {
        return switch (issue) {
            case FORMAT_NOT_ALLOWED -> IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_FORMAT;
            case DURATION_EXCEEDS_LIMIT -> IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_DURATION;
            case FPS_EXCEEDS_LIMIT -> IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_FPS;
            case WIDTH_EXCEEDS_LIMIT, HEIGHT_EXCEEDS_LIMIT ->
                    IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_RESOLUTION;
            case SIZE_EXCEEDS_LIMIT -> IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_SIZE;
            default -> IcerikFabrikasiRenderArtifactVerificationStatus.FAILED_SAFE;
        };
    }
}
