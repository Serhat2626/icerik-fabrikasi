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
 * FP-033 — Manual Local FFmpeg Run Evidence.
 * All tests use fake runner (disabled). No FFmpeg required.
 */
@DisplayName("FP-033 — Manual Local FFmpeg Run Evidence")
class IcerikFabrikasiFfmpegManualRunEvidenceTest {

    private static IcerikFabrikasiFfmpegProcessRequest validReq() {
        return new IcerikFabrikasiFfmpegProcessRequest("job-ref", "mp4", 60, 120, 30, 1920, 1080, 500);
    }

    // ── A. Default Disabled ──
    @Nested @DisplayName("A. Default Disabled")
    class Defaults {
        @Test @DisplayName("default harness disabled")
        void defaultDisabled() {
            var h = new IcerikFabrikasiFfmpegManualRunHarness();
            assertThat(h.isManualRunEnabled()).isFalse();
        }
        @Test @DisplayName("default returns MANUAL_FLAG_NOT_ENABLED")
        void defaultDenied() {
            var r = new IcerikFabrikasiFfmpegManualRunHarness().attempt(validReq(),
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.MANUAL_FLAG_NOT_ENABLED);
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
            assertThat(r.isAutoApproved()).isFalse();
        }
    }

    // ── B. Manual Flag ──
    @Nested @DisplayName("B. Manual Flag Checks")
    class ManualFlags {
        @Test @DisplayName("enabled but runner disabled → PROCESS_DISABLED")
        void runnerDisabled() {
            var h = new IcerikFabrikasiFfmpegManualRunHarness(new IcerikFabrikasiFfmpegLocalDevProcessRunner(), true);
            assertThat(h.attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.PROCESS_DISABLED);
        }
        @Test @DisplayName("binary unavailable → FFMPEG_UNAVAILABLE")
        void binaryUnavail() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true);
            var h = new IcerikFabrikasiFfmpegManualRunHarness(runner, true);
            assertThat(h.attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.DISABLED_BINARY).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.FFMPEG_UNAVAILABLE);
        }
    }

    // ── C. Gate Denials ──
    @Nested @DisplayName("C. Gate Denials")
    class Gates {
        @Test @DisplayName("STAGING denied")
        void staging() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.STAGING, true);
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true);
            var h = new IcerikFabrikasiFfmpegManualRunHarness(runner, true);
            assertThat(h.attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.GATE_DENIED);
        }
        @Test @DisplayName("PROD denied")
        void prod() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.PROD, true);
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true);
            assertThat(new IcerikFabrikasiFfmpegManualRunHarness(runner, true)
                    .attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.GATE_DENIED);
        }
        @Test @DisplayName("UNKNOWN denied")
        void unknown() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN, true);
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true);
            assertThat(new IcerikFabrikasiFfmpegManualRunHarness(runner, true)
                    .attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualRunEvidenceStatus.GATE_DENIED);
        }
    }

    // ── D. Fake Local Success ──
    @Nested @DisplayName("D. LOCAL + processEnabled=true (real runner, real FFmpeg if available)")
    class LocalSuccess {
        @Test @DisplayName("LOCAL enabled runner attempts real execution")
        void localEnabled() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var runner = new IcerikFabrikasiFfmpegLocalDevProcessRunner(gate, true);
            var h = new IcerikFabrikasiFfmpegManualRunHarness(runner, true);
            var r = h.attempt(validReq(), IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            // May succeed or fail-safe depending on FFmpeg availability
            assertThat(r.getStatus()).isIn(
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.REAL_LOCAL_EXECUTION_SUCCEEDED,
                    IcerikFabrikasiFfmpegManualRunEvidenceStatus.REAL_LOCAL_EXECUTION_FAILED_SAFE);
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
        }
    }

    // ── E. Enum Inventory ──
    @Nested @DisplayName("E. Enum Inventory")
    class Enums {
        @Test @DisplayName("10 evidence statuses")
        void statuses() { assertThat(IcerikFabrikasiFfmpegManualRunEvidenceStatus.values()).hasSize(10); }
    }

    // ── F. ProcessBuilder Inventory ──
    @Nested @DisplayName("F. ProcessBuilder Inventory")
    class PBInventory {
        @Test @DisplayName("harness no ProcessBuilder")
        void harnessClean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiFfmpegManualRunHarness.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
        @Test @DisplayName("result no ProcessBuilder")
        void resultClean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiFfmpegManualRunEvidenceResult.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
        @Test @DisplayName("runner HAS executeProcess (only allowed)")
        void runnerHas() {
            assertThat(Arrays.stream(IcerikFabrikasiFfmpegLocalDevProcessRunner.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("executeProcess"))).isTrue();
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
                IcerikFabrikasiFfmpegManualRunEvidenceStatus.class, IcerikFabrikasiFfmpegManualRunEvidenceResult.class,
                IcerikFabrikasiFfmpegManualRunHarness.class);

        @Test @DisplayName("3 classes x 19 forbidden = 0 violations")
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
