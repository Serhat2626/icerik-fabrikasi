package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-029 Task 02 — Internal Generation Trial.
 */
@DisplayName("FP-029 Task 02 — Internal Generation Trial")
class IcerikFabrikasiInternalGenerationTrialTest {

    private IcerikFabrikasiInternalSmokePathResult validSmoke;
    private IcerikFabrikasiInternalRenderReadinessSnapshot readySnapshot;

    @BeforeEach
    void setUp() {
        var plan = new IcerikFabrikasiInternalSmokeRenderPlan("mp4", 120, 30, 1920, 1080, 500, 5, 10);
        var outputRef = IcerikFabrikasiFfmpegInternalOutputRef.create().toOpaqueRef();
        validSmoke = IcerikFabrikasiInternalSmokePathResult.candidateCreated(outputRef, plan);
        readySnapshot = new IcerikFabrikasiInternalRenderReadinessSnapshot(
                IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY, List.of());
    }

    // ── A. Happy Path LOCAL ──
    @Nested @DisplayName("A. Happy Path LOCAL")
    class HappyPath {
        @Test @DisplayName("REAL_DEV_TEST_ONLY LOCAL produces attempted")
        void localAttempted() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var svc = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate));
            var r = svc.runTrial(validSmoke, readySnapshot,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.EXECUTION_ATTEMPTED_DEV_TEST);
            assertThat(r.getOutputRef()).startsWith("internal-ffmpeg-output:");
            assertThat(r.getVerificationCandidate()).isNotNull();
            assertThat(r.getVerificationCandidate().isBoundsValid()).isTrue();
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
            assertThat(r.isAutoApproved()).isFalse();
        }
        @Test @DisplayName("DRY_RUN produces simulated")
        void dryRunSimulated() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.DRY_RUN,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.EXECUTION_SIMULATED);
        }
        @Test @DisplayName("DEV allowed")
        void devAllowed() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.DEV, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.EXECUTION_ATTEMPTED_DEV_TEST);
        }
    }

    // ── B. Gate Denied ──
    @Nested @DisplayName("B. Gate Denied")
    class GateDenied {
        @Test @DisplayName("STAGING denied")
        void staging() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.STAGING, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
        }
        @Test @DisplayName("PROD denied")
        void prod() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.PROD, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
        }
        @Test @DisplayName("UNKNOWN denied")
        void unknown() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN, true);
            assertThat(new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
        }
        @Test @DisplayName("DISABLED denied")
        void disabled() {
            assertThat(new IcerikFabrikasiInternalGenerationTrialService().runTrial(validSmoke, readySnapshot,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
        }
    }

    // ── C. Prerequisites ──
    @Nested @DisplayName("C. Prerequisites")
    class Prerequisites {
        @Test @DisplayName("no smoke result denied")
        void noSmoke() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(null, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
        }
        @Test @DisplayName("no readiness denied")
        void noReadiness() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, null,
                            IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
            assertThat(r.getReasonCode()).isEqualTo(IcerikFabrikasiInternalTrialReasonCode.READINESS_NOT_MET);
        }
        @Test @DisplayName("review not approved denied")
        void reviewDenied() {
            var gate = new IcerikFabrikasiFfmpegExecutionGate(IcerikFabrikasiFfmpegRealExecutionMode.REAL_DEV_TEST_ONLY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL, true);
            var r = new IcerikFabrikasiInternalGenerationTrialService(new IcerikFabrikasiFfmpegDevTestAdapter(gate))
                    .runTrial(validSmoke, readySnapshot,
                            IcerikFabrikasiInternalRenderReviewState.IN_REVIEW,
                            IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiInternalTrialStatus.GATE_DENIED);
            assertThat(r.getReasonCode()).isEqualTo(IcerikFabrikasiInternalTrialReasonCode.REVIEW_NOT_APPROVED);
        }
    }

    // ── D. Enum Inventory ──
    @Nested @DisplayName("D. Enum Inventory")
    class Enums {
        @Test @DisplayName("9 trial statuses")
        void statuses() { assertThat(IcerikFabrikasiInternalTrialStatus.values()).hasSize(9); }
        @Test @DisplayName("16 trial reason codes")
        void reasons() { assertThat(IcerikFabrikasiInternalTrialReasonCode.values()).hasSize(16); }
    }

    // ── E. Forbidden Exposure ──
    @Nested @DisplayName("E. Forbidden Exposure")
    class ForbiddenExposure {
        private static final List<String> FORBIDDEN = List.of(
                "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
                "filePath", "absolutePath", "inputPath", "outputPath",
                "storageKey", "publicUrl", "signedUrl", "cdnUrl",
                "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl");
        private static final List<Class<?>> CLASSES = List.of(
                IcerikFabrikasiInternalTrialStatus.class, IcerikFabrikasiInternalTrialReasonCode.class,
                IcerikFabrikasiInternalTrialResult.class, IcerikFabrikasiInternalGenerationTrialService.class);

        @Test @DisplayName("4 classes x 19 forbidden = 0 violations")
        void allSafe() {
            for (Class<?> c : CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── F. No ProcessBuilder ──
    @Nested @DisplayName("F. No ProcessBuilder")
    class NoProcess {
        @Test @DisplayName("service no ProcessBuilder")
        void svc() {
            Set<String> types = Arrays.stream(IcerikFabrikasiInternalGenerationTrialService.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
    }

    // ── G. Endpoint Inventory ──
    @Nested @DisplayName("G. Endpoint Inventory (46 = 26 POST + 20 GET)")
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
