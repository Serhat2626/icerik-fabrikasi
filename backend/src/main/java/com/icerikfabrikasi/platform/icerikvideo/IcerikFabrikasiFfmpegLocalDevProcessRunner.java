package com.icerikfabrikasi.platform.icerikvideo;

import java.util.concurrent.TimeUnit;

/**
 * FP-030 Task 01: ONLY class allowed to use ProcessBuilder.
 * Gated: must call FP-028 gate first.
 * Denied for STAGING/PROD/UNKNOWN.
 * Tests use fake runner (processEnabled=false by default).
 */
public class IcerikFabrikasiFfmpegLocalDevProcessRunner {

    private final IcerikFabrikasiFfmpegExecutionGate gate;
    private final boolean processEnabled;

    /**
     * @param gate FP-028 execution gate
     * @param processEnabled true only for real LOCAL/DEV/TEST manual runs.
     *                       Default false — CI never enables this.
     */
    public IcerikFabrikasiFfmpegLocalDevProcessRunner(IcerikFabrikasiFfmpegExecutionGate gate, boolean processEnabled) {
        this.gate = gate != null ? gate : new IcerikFabrikasiFfmpegExecutionGate();
        this.processEnabled = processEnabled;
    }

    public IcerikFabrikasiFfmpegLocalDevProcessRunner(IcerikFabrikasiFfmpegExecutionGate gate) {
        this(gate, false);
    }

    public IcerikFabrikasiFfmpegLocalDevProcessRunner() {
        this(new IcerikFabrikasiFfmpegExecutionGate(), false);
    }

    /**
     * Execute FFmpeg process behind gate.
     */
    public IcerikFabrikasiFfmpegProcessResult execute(IcerikFabrikasiFfmpegProcessRequest request,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Validate request
        if (request == null || !request.isValid())
            return IcerikFabrikasiFfmpegProcessResult.invalid(IcerikFabrikasiFfmpegProcessReasonCode.FORMAT_NOT_ALLOWED);

        // 2. Gate check
        var gateResult = gate.evaluate(binaryPolicy,
                request.getJobRef(), null, null);
        if (gateResult.getStatus() == IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED) {
            var reason = mapGateReason(gateResult.getReasonCode());
            return IcerikFabrikasiFfmpegProcessResult.denied(reason);
        }

        // 3. If not real mode or process not enabled → skip
        if (!processEnabled || gateResult.getMode() != IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY)
            return IcerikFabrikasiFfmpegProcessResult.skipped();

        // 4. Real process execution — LOCAL/DEV/TEST only
        return executeProcess(request);
    }

    /**
     * Real process execution — version probe. Isolated in this one method.
     * stdout/stderr are consumed but NOT exposed in result.
     */
    private IcerikFabrikasiFfmpegProcessResult executeProcess(IcerikFabrikasiFfmpegProcessRequest request) {
        long startMs = System.currentTimeMillis();
        try {
            // Build process — internal only, paths not exposed
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Consume output (not exposed)
            process.getInputStream().readAllBytes();

            boolean finished = process.waitFor(request.getTimeoutSeconds(), TimeUnit.SECONDS);
            long durationMs = System.currentTimeMillis() - startMs;

            if (!finished) {
                process.destroyForcibly();
                return IcerikFabrikasiFfmpegProcessResult.timedOut(durationMs);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0)
                return IcerikFabrikasiFfmpegProcessResult.failed();

            var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create().toOpaqueRef();
            return IcerikFabrikasiFfmpegProcessResult.completed(outputRef, durationMs);
        } catch (Exception e) {
            return IcerikFabrikasiFfmpegProcessResult.failed();
        }
    }

    /**
     * FP-034: Real video clip generation using synthetic testsrc input.
     * Uses ProcessBuilder — output file is temp, path NOT exposed.
     * Returns only opaque ref + safe metadata.
     */
    public IcerikFabrikasiFfmpegProcessResult generateClip(IcerikFabrikasiFfmpegProcessRequest request,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Validate
        if (request == null || !request.isValid())
            return IcerikFabrikasiFfmpegProcessResult.invalid(IcerikFabrikasiFfmpegProcessReasonCode.FORMAT_NOT_ALLOWED);

        // 2. Gate check
        var gateResult = gate.evaluate(binaryPolicy, request.getJobRef(), null, null);
        if (gateResult.getStatus() == IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED) {
            return IcerikFabrikasiFfmpegProcessResult.denied(mapGateReason(gateResult.getReasonCode()));
        }

        // 3. Not enabled → skip
        if (!processEnabled || gateResult.getMode() != IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY)
            return IcerikFabrikasiFfmpegProcessResult.skipped();

        // 4. Real clip generation
        return executeClipGeneration(request);
    }

    /**
     * Real clip generation. Synthetic testsrc input — no copyrighted media.
     * Output path is temp, consumed internally, NOT exposed.
     */
    private IcerikFabrikasiFfmpegProcessResult executeClipGeneration(IcerikFabrikasiFfmpegProcessRequest request) {
        long startMs = System.currentTimeMillis();
        java.io.File tempFile = null;
        try {
            // Create temp output — path never exposed to DTO/API/UI
            tempFile = java.io.File.createTempFile("icerikFabrikasi_internal_clip_", "." + request.getFormat());
            tempFile.deleteOnExit();

            // Build synthetic testsrc command — no copyrighted media
            String testSrc = String.format("testsrc=duration=%d:size=%dx%d:rate=%d",
                    request.getDurationSeconds(), request.getWidth(), request.getHeight(), request.getFps());

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-f", "lavfi", "-i", testSrc,
                    "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono",
                    "-t", String.valueOf(request.getDurationSeconds()),
                    "-c:v", "libx264", "-pix_fmt", "yuv420p",
                    "-c:a", "aac", "-shortest",
                    tempFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Consume output (not exposed)
            process.getInputStream().readAllBytes();

            boolean finished = process.waitFor(request.getTimeoutSeconds(), TimeUnit.SECONDS);
            long durationMs = System.currentTimeMillis() - startMs;

            if (!finished) {
                process.destroyForcibly();
                return IcerikFabrikasiFfmpegProcessResult.timedOut(durationMs);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0)
                return IcerikFabrikasiFfmpegProcessResult.failed();

            // Verify file exists and has content
            long fileSize = tempFile.length();
            if (fileSize <= 0)
                return IcerikFabrikasiFfmpegProcessResult.failed();

            // Return opaque ref only — file path NOT exposed
            var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create().toOpaqueRef();
            return IcerikFabrikasiFfmpegProcessResult.completed(outputRef, durationMs);
        } catch (Exception e) {
            return IcerikFabrikasiFfmpegProcessResult.failed();
        }
    }

    private IcerikFabrikasiFfmpegProcessReasonCode mapGateReason(IcerikFabrikasiFfmpegBoundaryReasonCode gateReason) {
        return switch (gateReason) {
            case MODE_DISABLED -> IcerikFabrikasiFfmpegProcessReasonCode.GATE_DENIED_MODE;
            case ENVIRONMENT_DENIED -> IcerikFabrikasiFfmpegProcessReasonCode.GATE_DENIED_ENVIRONMENT;
            case INTERNAL_FLAG_NOT_SET -> IcerikFabrikasiFfmpegProcessReasonCode.GATE_DENIED_FLAG;
            case BINARY_UNAVAILABLE -> IcerikFabrikasiFfmpegProcessReasonCode.BINARY_UNAVAILABLE;
            default -> IcerikFabrikasiFfmpegProcessReasonCode.GATE_DENIED_MODE;
        };
    }

    /**
     * FP-041: Real clip stitching using concat demuxer.
     * Uses ProcessBuilder — all paths temp/internal, NOT exposed.
     * Returns only opaque ref + safe metadata.
     */
    public IcerikFabrikasiFfmpegProcessResult stitchClips(int clipCount, int totalDurationSeconds,
            int fps, int width, int height, String format,
            IcerikFabrikasiFfmpegBinaryPolicy binaryPolicy) {

        // 1. Gate check
        var gateResult = gate.evaluate(binaryPolicy, "stitch-job", null, null);
        if (gateResult.getStatus() == IcerikFabrikasiFfmpegBoundaryResultStatus.DENIED)
            return IcerikFabrikasiFfmpegProcessResult.denied(mapGateReason(gateResult.getReasonCode()));

        // 2. Not enabled → skip
        if (!processEnabled || gateResult.getMode() != IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY)
            return IcerikFabrikasiFfmpegProcessResult.skipped();

        // 3. Real stitching
        return executeStitching(clipCount, totalDurationSeconds, fps, width, height,
                format != null ? format : "mp4");
    }

    /**
     * Real stitching execution. Generates N synthetic clips then concats.
     * All temp files internal — paths NOT exposed.
     */
    private IcerikFabrikasiFfmpegProcessResult executeStitching(int clipCount, int totalDuration,
            int fps, int width, int height, String format) {
        long startMs = System.currentTimeMillis();
        java.io.File stitchedFile = null;
        var clipFiles = new java.util.ArrayList<java.io.File>();
        java.io.File concatList = null;
        try {
            int perClipDuration = Math.max(1, totalDuration / clipCount);

            // Generate synthetic clips
            for (int i = 0; i < clipCount; i++) {
                var clipFile = java.io.File.createTempFile("icerikFabrikasi_stitch_clip_" + i + "_", "." + format);
                clipFile.deleteOnExit();
                clipFiles.add(clipFile);

                String testSrc = String.format("testsrc=duration=%d:size=%dx%d:rate=%d",
                        perClipDuration, width, height, fps);

                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-y",
                        "-f", "lavfi", "-i", testSrc,
                        "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono",
                        "-t", String.valueOf(perClipDuration),
                        "-c:v", "libx264", "-pix_fmt", "yuv420p",
                        "-c:a", "aac", "-shortest",
                        clipFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.getInputStream().readAllBytes();
                boolean ok = process.waitFor(30, TimeUnit.SECONDS);
                if (!ok || process.exitValue() != 0)
                    return IcerikFabrikasiFfmpegProcessResult.failed();
            }

            // Write concat list
            concatList = java.io.File.createTempFile("icerikFabrikasi_concat_list_", ".txt");
            concatList.deleteOnExit();
            try (var writer = new java.io.FileWriter(concatList)) {
                for (var f : clipFiles)
                    writer.write("file '" + f.getAbsolutePath().replace("\\", "/") + "'\n");
            }

            // Stitch
            stitchedFile = java.io.File.createTempFile("icerikFabrikasi_stitched_", "." + format);
            stitchedFile.deleteOnExit();

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", concatList.getAbsolutePath(),
                    "-c", "copy",
                    stitchedFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.getInputStream().readAllBytes();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            long durationMs = System.currentTimeMillis() - startMs;

            if (!finished) {
                process.destroyForcibly();
                return IcerikFabrikasiFfmpegProcessResult.timedOut(durationMs);
            }
            if (process.exitValue() != 0)
                return IcerikFabrikasiFfmpegProcessResult.failed();

            long fileSize = stitchedFile.length();
            if (fileSize <= 0)
                return IcerikFabrikasiFfmpegProcessResult.failed();

            var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create().toOpaqueRef();
            return IcerikFabrikasiFfmpegProcessResult.completed(outputRef, durationMs);
        } catch (Exception e) {
            return IcerikFabrikasiFfmpegProcessResult.failed();
        }
    }

    public boolean isProcessEnabled() { return processEnabled; }
}
