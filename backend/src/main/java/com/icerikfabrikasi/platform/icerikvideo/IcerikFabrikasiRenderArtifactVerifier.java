package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;

/**
 * FP-024 Task 05: Artifact verifier.
 * Metadata-only. No filesystem access. No binary parsing.
 */
public class IcerikFabrikasiRenderArtifactVerifier {

    private final IcerikFabrikasiRenderArtifactVerificationPolicy policy;
    private final IcerikFabrikasiRenderWorkspaceValidator refValidator;

    public IcerikFabrikasiRenderArtifactVerifier(IcerikFabrikasiRenderArtifactVerificationPolicy policy) {
        this.policy = policy != null ? policy : new IcerikFabrikasiRenderArtifactVerificationPolicy();
        this.refValidator = new IcerikFabrikasiRenderWorkspaceValidator();
    }

    public IcerikFabrikasiRenderArtifactVerifier() {
        this(new IcerikFabrikasiRenderArtifactVerificationPolicy());
    }

    /**
     * Verify artifact metadata from execution result.
     * No filesystem access. No binary parsing.
     */
    public IcerikFabrikasiRenderArtifactVerificationReport verify(
            String opaqueArtifactRef, String rendererMode,
            String format, double durationSeconds,
            int sizeMb, String resolution, int fps) {

        List<IcerikFabrikasiRenderArtifactVerificationIssue> issues = new ArrayList<>();

        // 1. Validate ref
        if (opaqueArtifactRef == null || opaqueArtifactRef.isBlank()) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_MISSING,
                    "artifactRef", "Artifact ref is missing"));
        } else {
            validateRefSafety(opaqueArtifactRef, "artifactRef", issues);
        }

        // 2. Validate format
        if (format == null || !policy.getAllowedFormats().contains(format.toLowerCase())) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.FORMAT_NOT_ALLOWED,
                    "format", "Format not allowed: " + format));
        }

        // 3. Validate duration
        if (durationSeconds < 0 || durationSeconds > policy.getMaxDurationSeconds()) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.DURATION_OUT_OF_RANGE,
                    "duration", "Duration out of range: " + durationSeconds));
        }

        // 4. Validate size
        if (sizeMb < 0 || sizeMb > policy.getMaxSizeMb()) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.SIZE_OUT_OF_RANGE,
                    "size", "Size out of range: " + sizeMb));
        }

        // 5. Validate resolution
        if (resolution != null) {
            validateResolution(resolution, issues);
        }

        // 6. Validate fps
        if (fps < 1 || fps > policy.getMaxFps()) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.FPS_OUT_OF_RANGE,
                    "fps", "FPS out of range: " + fps));
        }

        // Determine status
        IcerikFabrikasiRenderArtifactVerificationStatus status;
        if (issues.isEmpty()) {
            status = IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED;
        } else if (issues.stream().anyMatch(i ->
                i.getCode() == IcerikFabrikasiRenderArtifactVerificationIssue.Code.FORMAT_NOT_ALLOWED)) {
            status = IcerikFabrikasiRenderArtifactVerificationStatus.FORMAT_REJECTED;
        } else if (issues.stream().anyMatch(i ->
                i.getCode().name().startsWith("REF_UNSAFE"))) {
            status = IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE;
        } else if (issues.stream().anyMatch(i ->
                i.getCode().name().contains("OUT_OF_RANGE"))) {
            status = IcerikFabrikasiRenderArtifactVerificationStatus.BOUNDS_EXCEEDED;
        } else {
            status = IcerikFabrikasiRenderArtifactVerificationStatus.FAILED_SAFE;
        }

        return new IcerikFabrikasiRenderArtifactVerificationReport(
                opaqueArtifactRef, status, rendererMode, format, issues);
    }

    /**
     * Verify artifact from execution result.
     */
    public IcerikFabrikasiRenderArtifactVerificationReport verifyFromExecution(
            IcerikFabrikasiFfmpegExecutionResult executionResult,
            IcerikFabrikasiRenderExecutionPlan plan) {
        if (executionResult == null) {
            return new IcerikFabrikasiRenderArtifactVerificationReport(
                    null, IcerikFabrikasiRenderArtifactVerificationStatus.METADATA_INCOMPLETE,
                    "unknown", null,
                    List.of(new IcerikFabrikasiRenderArtifactVerificationIssue(
                            IcerikFabrikasiRenderArtifactVerificationIssue.Code.METADATA_MISSING,
                            "executionResult", "Execution result is null")));
        }

        String format = plan != null ? plan.getTargetFormat() : "mp4";
        double duration = plan != null ? plan.getTotalDurationSeconds() : 0;
        String resolution = plan != null ? plan.getTargetResolution() : "1920x1080";
        int fps = plan != null ? plan.getTargetFps() : 30;

        return verify(
                executionResult.getOpaqueArtifactRef(),
                executionResult.getExecutionMode().name(),
                format, duration, 0, resolution, fps);
    }

    // ── Ref Safety Checks ──

    private void validateRefSafety(String ref, String field,
                                    List<IcerikFabrikasiRenderArtifactVerificationIssue> issues) {
        if (refValidator.isPathTraversal(ref) || refValidator.isAbsolutePath(ref)
                || refValidator.isWindowsPath(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_PATH,
                    field, "Unsafe path in ref"));
        }
        if (refValidator.isUrl(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_URL,
                    field, "URL in ref"));
        }
        if (refValidator.isStorageKey(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_STORAGE,
                    field, "Storage key in ref"));
        }
        if (refValidator.isBase64(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_BASE64,
                    field, "Base64 in ref"));
        }
        if (refValidator.isSecret(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_SECRET,
                    field, "Secret in ref"));
        }
        if (refValidator.isCommand(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_COMMAND,
                    field, "Command in ref"));
        }
        if (refValidator.isShellMeta(ref)) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.REF_UNSAFE_SHELL,
                    field, "Shell metacharacter in ref"));
        }
    }

    private void validateResolution(String resolution,
                                     List<IcerikFabrikasiRenderArtifactVerificationIssue> issues) {
        String[] parts = resolution.toLowerCase().split("x");
        if (parts.length != 2) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.RESOLUTION_OUT_OF_RANGE,
                    "resolution", "Invalid format"));
            return;
        }
        try {
            int w = Integer.parseInt(parts[0]);
            int h = Integer.parseInt(parts[1]);
            if (w < 1 || w > policy.getMaxResolutionW() || h < 1 || h > policy.getMaxResolutionH()) {
                issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                        IcerikFabrikasiRenderArtifactVerificationIssue.Code.RESOLUTION_OUT_OF_RANGE,
                        "resolution", "Exceeds max"));
            }
        } catch (NumberFormatException e) {
            issues.add(new IcerikFabrikasiRenderArtifactVerificationIssue(
                    IcerikFabrikasiRenderArtifactVerificationIssue.Code.RESOLUTION_OUT_OF_RANGE,
                    "resolution", "Non-numeric"));
        }
    }
}
