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
 * FP-031 Task 02 — First Actual Local FFmpeg Smoke Run.
 * All tests use fake runner (processEnabled=false in runner). No FFmpeg required.
 */
@DisplayName("FP-031 Task 02 — Actual Local Smoke Run + Closure")
class IcerikFabrikasiFfmpegActualLocalRunTest {

    private IcerikFabrikasiInternalRenderReadinessSnapshot ready;
    private IcerikFabrikasiInternalRenderSafetyChainSnapshot safeChain;
    private IcerikFabrikasiFfmpegActualLocalRunHarness harness;

    @BeforeEach
    void setUp() {
        ready = new IcerikFabrikasiInternalRenderReadinessSnapshot(
                IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY, List.of());
        safeChain = new IcerikFabrikasiInternalRenderSafetyChainSnapshot(
                true, true, true, true, "STUB", "DISABLED", "VERIFIED");
        // Default harness uses default service → default runner (processEnabled=false)
        harness = new IcerikFabrikasiFfmpegActualLocalRunHarness();
    }

    private IcerikFabrikasiFfmpegManualExecutionRequest req(boolean confirmed, boolean processEnabled) {
        return new IcerikFabrikasiFfmpegManualExecutionRequest("job-ref", "queue-ref", "review-ref",
                IcerikFabrikasiFfmpegManualExecutionPolicy.LOCAL_ONLY_MANUAL, confirmed, processEnabled,
                "mp4", 60, 120, 30, 1920, 1080, 500);
    }

    // ── A. Default (fake runner → REAL_LOCAL_RUN_ATTEMPTED) ──
    @Nested @DisplayName("A. Default Fake Runner")
    class DefaultFake {
        @Test @DisplayName("default harness maps skipped to REAL_LOCAL_RUN_ATTEMPTED")
        void defaultMapped() {
            var r = harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_ATTEMPTED);
            assertThat(r.getReasonCode()).isEqualTo(IcerikFabrikasiFfmpegManualExecutionReasonCode.REAL_LOCAL_ATTEMPTED_SAFE);
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
            assertThat(r.isAutoApproved()).isFalse();
        }
    }

    // ── B. Manual Denials Pass Through ──
    @Nested @DisplayName("B. Denial Pass-through")
    class Denials {
        @Test @DisplayName("unconfirmed → MANUAL_CONFIRMATION_MISSING")
        void unconfirmed() {
            assertThat(harness.attempt(req(false, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.MANUAL_CONFIRMATION_MISSING);
        }
        @Test @DisplayName("processEnabled=false → PROCESS_DISABLED")
        void processDisabled() {
            assertThat(harness.attempt(req(true, false), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.PROCESS_DISABLED);
        }
        @Test @DisplayName("DISABLED policy → POLICY_DISABLED")
        void policyDisabled() {
            var disabledReq = new IcerikFabrikasiFfmpegManualExecutionRequest("j", "q", "r",
                    IcerikFabrikasiFfmpegManualExecutionPolicy.DISABLED, true, true,
                    "mp4", 60, 120, 30, 1920, 1080, 500);
            assertThat(harness.attempt(disabledReq, ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.POLICY_DISABLED);
        }
    }

    // ── C. Environment Denials ──
    @Nested @DisplayName("C. Environment Denials")
    class EnvDenials {
        @Test @DisplayName("DEV denied")
        void dev() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.DEV).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("STAGING denied")
        void staging() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.STAGING).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("PROD denied")
        void prod() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.PROD).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("UNKNOWN denied")
        void unknown() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
    }

    // ── D. Prerequisites ──
    @Nested @DisplayName("D. Prerequisites")
    class Prerequisites {
        @Test @DisplayName("binary unavailable → REAL_LOCAL_RUN_SKIPPED_UNAVAILABLE")
        void binary() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.DISABLED_BINARY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.REAL_LOCAL_RUN_SKIPPED_UNAVAILABLE);
        }
        @Test @DisplayName("readiness denied")
        void readiness() {
            assertThat(harness.attempt(req(true, true), null, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.READINESS_DENIED);
        }
        @Test @DisplayName("review denied")
        void review() {
            assertThat(harness.attempt(req(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.IN_REVIEW,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.REVIEW_DENIED);
        }
    }

    // ── E. Enum Inventory ──
    @Nested @DisplayName("E. Enum Inventory")
    class Enums {
        @Test @DisplayName("19 manual statuses (15+4)")
        void statuses() { assertThat(IcerikFabrikasiFfmpegManualExecutionStatus.values()).hasSize(19); }
        @Test @DisplayName("20 manual reason codes (16+4)")
        void reasons() { assertThat(IcerikFabrikasiFfmpegManualExecutionReasonCode.values()).hasSize(20); }
    }

    // ── F. ProcessBuilder Inventory ──
    @Nested @DisplayName("F. ProcessBuilder Inventory")
    class PBInventory {
        @Test @DisplayName("harness no ProcessBuilder")
        void harnessClean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiFfmpegActualLocalRunHarness.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
        @Test @DisplayName("runner HAS executeProcess")
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

        @Test @DisplayName("harness 0 violations")
        void harnessSafe() {
            Set<String> fields = Arrays.stream(IcerikFabrikasiFfmpegActualLocalRunHarness.class.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            for (String f : FORBIDDEN)
                assertThat(fields).as(f).doesNotContain(f);
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
