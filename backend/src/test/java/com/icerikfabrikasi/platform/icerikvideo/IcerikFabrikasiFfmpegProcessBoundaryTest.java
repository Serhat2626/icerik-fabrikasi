package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-030 Task 01 — Real FFmpeg Process Boundary.
 * All tests use processEnabled=false (fake/skipped).
 */
@DisplayName("FP-030 Task 01 — FFmpeg Process Boundary")
class IcerikFabrikasiFfmpegProcessBoundaryTest {

    private static IcerikFabrikasiFfmpegProcessRequest validReq() {
        return new IcerikFabrikasiFfmpegProcessRequest("job-ref", "mp4", 60, 120, 30, 1920, 1080, 500);
    }

    // ── A. Default Disabled ──
    @Nested @DisplayName("A. Default Disabled")
    class Defaults {
        @Test @DisplayName("default runner disabled")
        void defaultDisabled() {
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner();
            assertThat(runner.isProcessEnabled()).isFalse();
        }
        @Test @DisplayName("default gate denies")
        void defaultDenied() {
            var r = new IcerikFabrikasiFfmpegLocalDevProcessRunner().execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegProcessStatus.GATE_DENIED);
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
        }
    }

    // ── B. Environment Gate ──
    @Nested @DisplayName("B. Environment Gate")
    class EnvGate {
        @Test @DisplayName("STAGING denied")
        void staging() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.STAGING, true);
            var r = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegProcessStatus.GATE_DENIED);
            assertThat(r.getReasonCode()).isEqualTo(IcerikFabrikasiFfmpegProcessReasonCode.GATE_DENIED_ENVIRONMENT);
        }
        @Test @DisplayName("PROD denied")
        void prod() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.PROD, true);
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.GATE_DENIED);
        }
        @Test @DisplayName("UNKNOWN denied")
        void unknown() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN, true);
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.GATE_DENIED);
        }
    }

    // ── C. LOCAL/DEV/TEST Allowed (fake — processEnabled=false → SKIPPED) ──
    @Nested @DisplayName("C. LOCAL/DEV/TEST Skipped (fake)")
    class LocalFake {
        @Test @DisplayName("LOCAL skipped when processEnabled=false")
        void localSkipped() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var r = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, false).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegProcessStatus.PROCESS_SKIPPED);
            assertThat(r.getReasonCode()).isEqualTo(IcerikFabrikasiFfmpegProcessReasonCode.PROCESS_SKIPPED_SAFE);
        }
        @Test @DisplayName("DEV skipped when processEnabled=false")
        void devSkipped() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.DEV, true);
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, false).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.PROCESS_SKIPPED);
        }
        @Test @DisplayName("TEST skipped when processEnabled=false")
        void testSkipped() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.TEST, true);
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, false).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.TEST_FIXTURE_FFMPEG).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.PROCESS_SKIPPED);
        }
        @Test @DisplayName("DRY_RUN skipped (not real mode)")
        void dryRunSkipped() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.DRY_RUN,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true).execute(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.PROCESS_SKIPPED);
        }
    }

    // ── D. Request Validation ──
    @Nested @DisplayName("D. Request Validation")
    class Validation {
        @Test @DisplayName("null request invalid")
        void nullReq() {
            assertThat(new IcerikFabrikasiFfmpegLocalDevProcessRunner().execute(null,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegProcessStatus.REQUEST_INVALID);
        }
        @Test @DisplayName("invalid format rejected")
        void invalidFormat() {
            var req = new IcerikFabrikasiFfmpegProcessRequest("j", "avi", 60, 120, 30, 1920, 1080, 500);
            assertThat(req.isValid()).isFalse();
        }
        @Test @DisplayName("mp4 valid")
        void mp4() { assertThat(validReq().isValid()).isTrue(); }
        @Test @DisplayName("webm valid")
        void webm() { assertThat(new IcerikFabrikasiFfmpegProcessRequest("j", "webm", 60, 60, 24, 1280, 720, 100).isValid()).isTrue(); }
        @Test @DisplayName("timeout clamped 10-300")
        void timeoutClamp() {
            assertThat(new IcerikFabrikasiFfmpegProcessRequest("j", "mp4", 1, 60, 30, 1920, 1080, 500).getTimeoutSeconds()).isEqualTo(10);
            assertThat(new IcerikFabrikasiFfmpegProcessRequest("j", "mp4", 999, 60, 30, 1920, 1080, 500).getTimeoutSeconds()).isEqualTo(300);
        }
    }

    // ── E. Result Safety ──
    @Nested @DisplayName("E. Result Safety")
    class ResultSafety {
        @Test @DisplayName("9 process statuses")
        void statuses() { assertThat(IcerikFabrikasiFfmpegProcessStatus.values()).hasSize(9); }
        @Test @DisplayName("16 process reasons")
        void reasons() { assertThat(IcerikFabrikasiFfmpegProcessReasonCode.values()).hasSize(16); }
        @Test @DisplayName("completed result has output ref")
        void completed() {
            var r = IcerikFabrikasiFfmpegProcessResult.completed("internal-ffmpeg-output:123", 500);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegProcessStatus.PROCESS_COMPLETED_SAFE);
            assertThat(r.getOutputRef()).startsWith("internal-ffmpeg-output:");
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
            assertThat(r.isAutoApproved()).isFalse();
        }
    }

    // ── F. ProcessBuilder Inventory ──
    @Nested @DisplayName("F. ProcessBuilder Inventory")
    class PBInventory {
        @Test @DisplayName("ProcessBuilder only in LocalDevProcessRunner")
        void onlyInRunner() {
            // Confirm runner HAS ProcessBuilder usage
            boolean runnerHasProcess = Arrays.stream(IcerikFabrikasiFfmpegLocalDevProcessRunner.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("executeProcess"));
            assertThat(runnerHasProcess).isTrue();
        }
        @Test @DisplayName("other classes have no ProcessBuilder fields")
        void othersClean() {
            List<Class<?>> others = List.of(
                    IcerikFabrikasiFfmpegProcessRequest.class, IcerikFabrikasiFfmpegProcessResult.class,
                    IcerikFabrikasiFfmpegProcessStatus.class, IcerikFabrikasiFfmpegProcessReasonCode.class,
                    IcerikFabrikasiFfmpegExecutionGate.class, IcerikFabrikasiFfmpegDevTestAdapter.class,
                    IcerikFabrikasiFfmpegDevTestAdapterResult.class, IcerikFabrikasiFfmpegExecutionBoundaryResult.class,
                    IcerikFabrikasiInternalSmokeOrchestrator.class, IcerikFabrikasiInternalGenerationTrialService.class);
            for (Class<?> c : others) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName()).doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }

    // ── G. Forbidden Exposure ──
    @Nested @DisplayName("G. Forbidden Exposure")
    class ForbiddenExposure {
        private static final List<String> FORBIDDEN = List.of(
                "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
                "filePath", "absolutePath", "inputPath", "outputPath",
                "storageKey", "publicUrl", "signedUrl", "cdnUrl",
                "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl");
        private static final List<Class<?>> CLASSES = List.of(
                IcerikFabrikasiFfmpegLocalDevProcessRunner.class, IcerikFabrikasiFfmpegProcessRequest.class,
                IcerikFabrikasiFfmpegProcessResult.class, IcerikFabrikasiFfmpegProcessStatus.class,
                IcerikFabrikasiFfmpegProcessReasonCode.class);

        @Test @DisplayName("5 classes x 19 forbidden = 0 violations")
        void allSafe() {
            for (Class<?> c : CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── H. Endpoint Inventory ──
    @Nested @DisplayName("H. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test @DisplayName("46 = 26 POST + 20 GET")
        void total() {
            long post = count(AdminReviewController.class, PostMapping.class)
                    + count(AdminPipelineController.class, PostMapping.class)
                    + count(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = count(AdminReviewController.class, GetMapping.class)
                    + count(AdminPipelineController.class, GetMapping.class)
                    + count(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post + get).isEqualTo(46);
            assertThat(post).isEqualTo(26);
            assertThat(get).isEqualTo(20);
        }
        private long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
            return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
        }
    }
}
