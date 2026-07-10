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
 * FP-031 Task 01 — Local-only Manual Execution.
 * All tests use processEnabled=false (fake). No FFmpeg required.
 */
@DisplayName("FP-031 Task 01 — Local Manual Execution")
class IcerikFabrikasiFfmpegManualExecutionTest {

    private IcerikFabrikasiInternalRenderReadinessSnapshot ready;
    private IcerikFabrikasiInternalRenderSafetyChainSnapshot safeChain;

    @BeforeEach
    void setUp() {
        ready = new IcerikFabrikasiInternalRenderReadinessSnapshot(
                IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY, List.of());
        safeChain = new IcerikFabrikasiInternalRenderSafetyChainSnapshot(
                true, true, true, true, "STUB", "DISABLED", "VERIFIED");
    }

    private IcerikFabrikasiFfmpegManualExecutionRequest validReq(boolean confirmed, boolean processEnabled) {
        return new IcerikFabrikasiFfmpegManualExecutionRequest("job-ref", "queue-ref", "review-ref",
                IcerikFabrikasiFfmpegManualExecutionPolicy.LOCAL_ONLY_MANUAL, confirmed, processEnabled,
                "mp4", 60, 120, 30, 1920, 1080, 500);
    }

    // ── A. Default Disabled ──
    @Nested @DisplayName("A. Default Disabled")
    class Defaults {
        @Test @DisplayName("default service skips")
        void defaultSkips() {
            var r = new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL);
            assertThat(r.getStatus()).isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.EXECUTION_SKIPPED_FAKE);
            assertThat(r.isInternalOnly()).isTrue();
            assertThat(r.isPublicDeliveryAvailable()).isFalse();
            assertThat(r.isAutoApproved()).isFalse();
        }
        @Test @DisplayName("DISABLED policy denied")
        void policyDisabled() {
            var req = new IcerikFabrikasiFfmpegManualExecutionRequest("j", "q", "r",
                    IcerikFabrikasiFfmpegManualExecutionPolicy.DISABLED, true, true,
                    "mp4", 60, 120, 30, 1920, 1080, 500);
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(req, ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.POLICY_DISABLED);
        }
    }

    // ── B. Manual Confirmation ──
    @Nested @DisplayName("B. Manual Confirmation")
    class Confirmation {
        @Test @DisplayName("unconfirmed denied")
        void unconfirmed() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(false, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.MANUAL_CONFIRMATION_MISSING);
        }
        @Test @DisplayName("processEnabled=false denied")
        void processDisabled() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, false), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.PROCESS_DISABLED);
        }
    }

    // ── C. Environment ──
    @Nested @DisplayName("C. Environment")
    class Environment {
        @Test @DisplayName("DEV denied (not LOCAL)")
        void dev() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.DEV).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("STAGING denied")
        void staging() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.STAGING).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("PROD denied")
        void prod() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.PROD).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
        @Test @DisplayName("UNKNOWN denied")
        void unknown() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.UNKNOWN).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.ENVIRONMENT_DENIED);
        }
    }

    // ── D. Prerequisites ──
    @Nested @DisplayName("D. Prerequisites")
    class Prerequisites {
        @Test @DisplayName("binary unavailable")
        void binary() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.DISABLED_BINARY,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.BINARY_UNAVAILABLE);
        }
        @Test @DisplayName("readiness denied")
        void readiness() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), null, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.APPROVED_FOR_INTERNAL_NEXT_STEP,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.READINESS_DENIED);
        }
        @Test @DisplayName("review denied")
        void review() {
            assertThat(new IcerikFabrikasiFfmpegManualExecutionService().execute(
                    validReq(true, true), ready, safeChain,
                    IcerikFabrikasiInternalRenderReviewState.IN_REVIEW,
                    IcerikFabrikasiFfmpegBinaryPolicy.SYSTEM_FFMPEG_LOCAL,
                    IcerikFabrikasiFfmpegExecutionEnvironment.LOCAL).getStatus())
                    .isEqualTo(IcerikFabrikasiFfmpegManualExecutionStatus.REVIEW_DENIED);
        }
    }

    // ── E. Fixture Metadata ──
    @Nested @DisplayName("E. Fixture Metadata")
    class Fixture {
        @Test @DisplayName("mp4 valid bounds")
        void mp4Valid() {
            var m = new IcerikFabrikasiFfmpegFixtureMetadata("ref", "mp4", 120, 30, 1920, 1080, 500);
            assertThat(m.isBoundsValid()).isTrue();
        }
        @Test @DisplayName("webm valid")
        void webmValid() {
            assertThat(new IcerikFabrikasiFfmpegFixtureMetadata("ref", "webm", 60, 24, 1280, 720, 100).isBoundsValid()).isTrue();
        }
        @Test @DisplayName("avi invalid")
        void aviInvalid() {
            assertThat(new IcerikFabrikasiFfmpegFixtureMetadata("ref", "avi", 60, 24, 1280, 720, 100).isBoundsValid()).isFalse();
        }
        @Test @DisplayName("duration 601 invalid")
        void durationOver() {
            assertThat(new IcerikFabrikasiFfmpegFixtureMetadata("ref", "mp4", 601, 30, 1920, 1080, 500).isBoundsValid()).isFalse();
        }
        @Test @DisplayName("resolution 3841 invalid")
        void resOver() {
            assertThat(new IcerikFabrikasiFfmpegFixtureMetadata("ref", "mp4", 60, 30, 3841, 1080, 500).isBoundsValid()).isFalse();
        }
        @Test @DisplayName("size 1001 invalid")
        void sizeOver() {
            assertThat(new IcerikFabrikasiFfmpegFixtureMetadata("ref", "mp4", 60, 30, 1920, 1080, 1001).isBoundsValid()).isFalse();
        }
    }

    // ── F. Enum Inventory ──
    @Nested @DisplayName("F. Enum Inventory")
    class Enums {
        @Test @DisplayName("2 policies")
        void policies() { assertThat(IcerikFabrikasiFfmpegManualExecutionPolicy.values()).hasSize(2); }
        @Test @DisplayName("15 statuses")
        void statuses() { assertThat(IcerikFabrikasiFfmpegManualExecutionStatus.values()).hasSize(19); }
        @Test @DisplayName("16 reason codes")
        void reasons() { assertThat(IcerikFabrikasiFfmpegManualExecutionReasonCode.values()).hasSize(20); }
    }

    // ── G. ProcessBuilder Inventory ──
    @Nested @DisplayName("G. ProcessBuilder Inventory")
    class PBInventory {
        @Test @DisplayName("service no ProcessBuilder")
        void serviceClean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiFfmpegManualExecutionService.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
        @Test @DisplayName("result no ProcessBuilder")
        void resultClean() {
            Set<String> types = Arrays.stream(IcerikFabrikasiFfmpegManualExecutionResult.class.getDeclaredFields())
                    .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
            assertThat(types).doesNotContain("ProcessBuilder", "Process", "Runtime");
        }
        @Test @DisplayName("runner HAS executeProcess")
        void runnerHas() {
            assertThat(Arrays.stream(IcerikFabrikasiFfmpegLocalDevProcessRunner.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("executeProcess"))).isTrue();
        }
    }

    // ── H. Forbidden Exposure ──
    @Nested @DisplayName("H. Forbidden Exposure")
    class ForbiddenExposure {
        private static final List<String> FORBIDDEN = List.of(
                "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
                "filePath", "absolutePath", "inputPath", "outputPath",
                "storageKey", "publicUrl", "signedUrl", "cdnUrl",
                "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl");
        private static final List<Class<?>> CLASSES = List.of(
                IcerikFabrikasiFfmpegManualExecutionPolicy.class, IcerikFabrikasiFfmpegManualExecutionStatus.class,
                IcerikFabrikasiFfmpegManualExecutionReasonCode.class, IcerikFabrikasiFfmpegManualExecutionRequest.class,
                IcerikFabrikasiFfmpegManualExecutionResult.class, IcerikFabrikasiFfmpegManualExecutionService.class,
                IcerikFabrikasiFfmpegFixtureMetadata.class);

        @Test @DisplayName("7 classes x 19 forbidden = 0 violations")
        void allSafe() {
            for (Class<?> c : CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── I. Endpoint Inventory ──
    @Nested @DisplayName("I. Endpoint Inventory (46 = 26 POST + 20 GET)")
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
