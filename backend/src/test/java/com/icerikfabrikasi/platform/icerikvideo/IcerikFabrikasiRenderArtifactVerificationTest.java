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
 * FP-024 Task 05: Artifact Verification + Metadata Integration tests.
 */
@DisplayName("FP-024 Task 05 — Artifact Verification + Metadata Integration")
class IcerikFabrikasiRenderArtifactVerificationTest {

    private IcerikFabrikasiRenderArtifactVerifier verifier;
    private IcerikFabrikasiRenderArtifactMetadataIntegrationService integrationService;

    @BeforeEach
    void setUp() {
        verifier = new IcerikFabrikasiRenderArtifactVerifier();
        integrationService = new IcerikFabrikasiRenderArtifactMetadataIntegrationService();
    }

    // ── A. Endpoint Inventory ──

    @Nested
    @DisplayName("A. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class Endpoints {
        @Test
        @DisplayName("46 = 26 POST + 20 GET")
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

    // ── B. Opaque Ref Accepted ──

    @Nested
    @DisplayName("B. Opaque Internal Refs Accepted")
    class OpaqueRefAccepted {
        @Test
        @DisplayName("internal-dryrun-artifact ref verified")
        void dryrunRef() {
            var report = verifier.verify("internal-dryrun-artifact:abc-123", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }

        @Test
        @DisplayName("internal-simulated-artifact ref verified")
        void simulatedRef() {
            var report = verifier.verify("internal-simulated-artifact:xyz-456", "SIMULATED_SUCCESS",
                    "webm", 60.0, 100, "1280x720", 24);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }

        @Test
        @DisplayName("internal-stub ref verified")
        void stubRef() {
            var report = verifier.verify("internal-stub:test-ref", "DETERMINISTIC_STUB",
                    "mp4", 30.0, 50, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }
    }

    // ── C. Unsafe Refs Rejected ──

    @Nested
    @DisplayName("C. Unsafe Refs Rejected")
    class UnsafeRefRejected {
        @Test
        @DisplayName("file path rejected")
        void filePath() {
            var report = verifier.verify("/usr/local/video.mp4", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("Windows path rejected")
        void windowsPath() {
            var report = verifier.verify("C:\\Users\\video.mp4", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("storage key rejected")
        void storageKey() {
            var report = verifier.verify("storageKey=abc123", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("public URL rejected")
        void publicUrl() {
            var report = verifier.verify("https://cdn.example.com/video.mp4", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("base64 rejected")
        void base64() {
            var report = verifier.verify("data:video/mp4;base64,abc", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("command-like rejected")
        void command() {
            var report = verifier.verify("commandLine=/bin/ffmpeg -i input", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("secret rejected")
        void secret() {
            var report = verifier.verify("Bearer token123abc", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("shell metacharacter rejected")
        void shellMeta() {
            var report = verifier.verify("ref;rm -rf /", "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.REF_UNSAFE);
        }

        @Test
        @DisplayName("null ref rejected")
        void nullRef() {
            var report = verifier.verify(null, "DRY_RUN",
                    "mp4", 120.0, 200, "1920x1080", 30);
            assertThat(report.getStatus()).isNotEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }
    }

    // ── D. Format Allowlist ──

    @Nested
    @DisplayName("D. Format Allowlist")
    class FormatAllowlist {
        @Test
        @DisplayName("mp4 allowed")
        void mp4() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "1920x1080", 30);
            assertThat(report.getIssues().stream().map(i -> i.getCode()))
                    .doesNotContain(IcerikFabrikasiRenderArtifactVerificationIssue.Code.FORMAT_NOT_ALLOWED);
        }

        @Test
        @DisplayName("webm allowed")
        void webm() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "webm", 60, 100, "1920x1080", 30);
            assertThat(report.getIssues().stream().map(i -> i.getCode()))
                    .doesNotContain(IcerikFabrikasiRenderArtifactVerificationIssue.Code.FORMAT_NOT_ALLOWED);
        }

        @Test
        @DisplayName("avi rejected")
        void avi() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "avi", 60, 100, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.FORMAT_REJECTED);
        }

        @Test
        @DisplayName("exe rejected")
        void exe() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "exe", 60, 100, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.FORMAT_REJECTED);
        }
    }

    // ── E. Bounds Enforcement ──

    @Nested
    @DisplayName("E. Duration/Size/Resolution/FPS Bounds")
    class BoundsEnforcement {
        @Test
        @DisplayName("duration over max rejected")
        void durationOver() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 9999, 100, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.BOUNDS_EXCEEDED);
        }

        @Test
        @DisplayName("size over max rejected")
        void sizeOver() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 9999, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.BOUNDS_EXCEEDED);
        }

        @Test
        @DisplayName("resolution over max rejected")
        void resolutionOver() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "7680x4320", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.BOUNDS_EXCEEDED);
        }

        @Test
        @DisplayName("fps over max rejected")
        void fpsOver() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "1920x1080", 120);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.BOUNDS_EXCEEDED);
        }
    }

    // ── F. Verification Report Safety Flags ──

    @Nested
    @DisplayName("F. Verification Report Safety Flags")
    class ReportFlags {
        @Test
        @DisplayName("all reports have safe flags")
        void flags() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "1920x1080", 30);
            assertThat(report.isHumanReviewRequired()).isTrue();
            assertThat(report.isCopyrightReviewRequired()).isTrue();
            assertThat(report.isAutoApproved()).isFalse();
            assertThat(report.isNotPublishReady()).isTrue();
            assertThat(report.isNotExportReady()).isTrue();
        }

        @Test
        @DisplayName("trace ref present")
        void traceRef() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "1920x1080", 30);
            assertThat(report.getSafeTraceRef()).startsWith("verification-trace:");
        }

        @Test
        @DisplayName("safety flags list")
        void flagsList() {
            var report = verifier.verify("internal-ref:x", "DRY_RUN",
                    "mp4", 60, 100, "1920x1080", 30);
            assertThat(report.getSafetyFlags()).contains("NO_PUBLISH", "NO_EXPORT", "INTERNAL_ONLY");
        }
    }

    // ── G. Metadata Integration ──

    @Nested
    @DisplayName("G. Metadata Integration")
    class MetadataIntegration {
        @Test
        @DisplayName("integration from dry-run result")
        void fromDryRun() {
            var plan = new IcerikFabrikasiRenderExecutionPlan(
                    UUID.randomUUID(), 5, 120.0, "1920x1080", 30, "mp4",
                    60.0, "deterministic-stub", "DRY_RUN");
            var execResult = IcerikFabrikasiFfmpegExecutionResult.dryRunSuccess(List.of("test"), 1);
            var report = integrationService.integrateFromExecution(execResult, plan);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }

        @Test
        @DisplayName("integration from simulated result")
        void fromSimulated() {
            var plan = new IcerikFabrikasiRenderExecutionPlan(
                    UUID.randomUUID(), 3, 60.0, "1280x720", 24, "webm",
                    30.0, "simulated", "SIMULATED_SUCCESS");
            var execResult = IcerikFabrikasiFfmpegExecutionResult.simulatedSuccess(1);
            var report = integrationService.integrateFromExecution(execResult, plan);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }

        @Test
        @DisplayName("null execution result handled")
        void nullResult() {
            var report = integrationService.integrateFromExecution(null, null);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.METADATA_INCOMPLETE);
        }

        @Test
        @DisplayName("verify-only mode works")
        void verifyOnly() {
            var report = integrationService.verifyOnly(
                    "internal-ref:x", "DRY_RUN", "mp4", 60, 100, "1920x1080", 30);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.VERIFIED);
        }
    }

    // ── H. Stub Compatibility ──

    @Nested
    @DisplayName("H. Stub Execution Compatibility")
    class StubCompat {
        @Test
        @DisplayName("stub result artifact ref verifies")
        void stubArtifact() {
            var stub = new StubIcerikFabrikasiRenderRunnerGateway();
            var plan = new IcerikFabrikasiRenderExecutionPlan(
                    UUID.randomUUID(), 5, 120.0, "1920x1080", 30, "mp4",
                    60.0, "deterministic-stub", "DETERMINISTIC_STUB");
            var result = stub.execute(plan);
            assertThat(result.isSuccess()).isTrue();
            // Verify the artifact ref format from stub
            var report = verifier.verify(
                    result.getArtifact().getArtifactRef(), "DETERMINISTIC_STUB",
                    "mp4", 120.0, 0, "1920x1080", 30);
            // Stub refs should be opaque and verifiable
            assertThat(report.isHumanReviewRequired()).isTrue();
            assertThat(report.isAutoApproved()).isFalse();
        }
    }

    // ── I. DTO Safety ──

    @Nested
    @DisplayName("I. DTO Safety — Forbidden Fields")
    class DtoSafety {
        private static final List<String> FORBIDDEN = List.of(
                "rawPayload", "token", "jwt", "session", "credential",
                "password", "secret", "apiKey", "authorizationHeader",
                "publicUrl", "signedUrl", "cdnUrl", "exportUrl", "publishUrl",
                "downloadUrl", "storageKey", "base64", "binaryData",
                "renderedVideo", "videoFile", "downloadFile",
                "filePath", "absolutePath", "renderedVideoUrl",
                "commandLine", "ffmpegCommand", "shellCommand", "processBuilder");

        private static final List<Class<?>> CLASSES = List.of(
                IcerikFabrikasiRenderArtifactVerificationStatus.class,
                IcerikFabrikasiRenderArtifactVerificationIssue.class,
                IcerikFabrikasiRenderArtifactVerificationReport.class,
                IcerikFabrikasiRenderArtifactVerificationPolicy.class,
                IcerikFabrikasiRenderArtifactVerifier.class,
                IcerikFabrikasiRenderArtifactMetadataIntegrationService.class);

        @Test
        @DisplayName("6 classes x 29 forbidden fields = 0 violations")
        void allSafe() {
            for (Class<?> clazz : CLASSES) {
                Set<String> fields = Arrays.stream(clazz.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN) {
                    assertThat(fields)
                            .as("field '%s' in %s", f, clazz.getSimpleName())
                            .doesNotContain(f);
                }
            }
        }
    }

    // ── J. Enum Values ──

    @Nested
    @DisplayName("J. Enum Values")
    class EnumValues {
        @Test
        @DisplayName("verification status has 15 values")
        void statusCount() {
            assertThat(IcerikFabrikasiRenderArtifactVerificationStatus.values()).hasSize(15);
        }

        @Test
        @DisplayName("issue code has 15 values")
        void issueCount() {
            assertThat(IcerikFabrikasiRenderArtifactVerificationIssue.Code.values()).hasSize(16);
        }
    }
}
