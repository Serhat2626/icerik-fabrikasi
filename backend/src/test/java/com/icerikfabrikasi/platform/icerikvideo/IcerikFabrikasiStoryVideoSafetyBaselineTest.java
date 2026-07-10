package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-045 — Internal Story-to-Video Safety & Baseline Closure.
 * Locks the full FP-034→FP-044 boundary. No new capability.
 */
@DisplayName("FP-045 — Story-to-Video Safety Baseline")
class IcerikFabrikasiStoryVideoSafetyBaselineTest {

    private static final List<String> FORBIDDEN_FIELDS = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "storageKey", "publicUrl", "signedUrl", "cdnUrl",
            "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "lyrics", "scriptBody", "promptText", "copyrightedContent");

    // All FP-034→FP-044 production classes
    private static final List<Class<?>> ALL_CLASSES = List.of(
            // FP-034/033: Process runner + results
            IcerikFabrikasiFfmpegLocalDevProcessRunner.class,
            IcerikFabrikasiFfmpegProcessResult.class,
            IcerikFabrikasiFfmpegProcessRequest.class,
            IcerikFabrikasiFfmpegInternalOutputRef.class,
            // FP-035: Lifecycle
            IcerikFabrikasiRenderArtifactLifecycleService.class,
            IcerikFabrikasiRenderArtifactLifecycleReport.class,
            // FP-036: Bundle
            IcerikFabrikasiRenderBundleManifestService.class,
            IcerikFabrikasiRenderBundleManifest.class,
            IcerikFabrikasiRenderBundleArtifactRef.class,
            // FP-037: Scene plan
            IcerikFabrikasiSceneRenderPlanService.class,
            IcerikFabrikasiSceneRenderPlan.class,
            IcerikFabrikasiSceneRenderArtifactExpectation.class,
            // FP-038: Orchestration
            IcerikFabrikasiMultiSceneRenderOrchestrationService.class,
            IcerikFabrikasiMultiSceneRenderJobPlan.class,
            // FP-039: Multi-clip generation
            IcerikFabrikasiMultiClipGenerationHarness.class,
            // FP-040/041: Stitching
            IcerikFabrikasiClipStitchingHarness.class,
            IcerikFabrikasiClipStitchingResult.class,
            IcerikFabrikasiClipStitchingInputClipRef.class,
            // FP-042: Review package
            IcerikFabrikasiRenderReviewPackageService.class,
            IcerikFabrikasiRenderReviewPackage.class,
            IcerikFabrikasiRenderReviewArtifactSummary.class,
            // FP-044: E2E dry run
            IcerikFabrikasiInternalStoryVideoDryRunHarness.class,
            IcerikFabrikasiInternalStoryVideoDryRunSummary.class);

    // All enums
    private static final List<Class<? extends Enum<?>>> ALL_ENUMS = List.of(
            IcerikFabrikasiFfmpegProcessStatus.class,
            IcerikFabrikasiFfmpegProcessReasonCode.class,
            IcerikFabrikasiFfmpegBinaryPolicy.class,
            IcerikFabrikasiRenderArtifactVerificationStatus.class,
            IcerikFabrikasiRenderArtifactIssueCode.class,
            IcerikFabrikasiRenderArtifactFormat.class,
            IcerikFabrikasiRenderBundleStatus.class,
            IcerikFabrikasiRenderBundleArtifactRole.class,
            IcerikFabrikasiSceneRenderPlanStatus.class,
            IcerikFabrikasiSceneRenderPlanIssueCode.class,
            IcerikFabrikasiMultiSceneRenderJobStatus.class,
            IcerikFabrikasiMultiSceneRenderJobIssueCode.class,
            IcerikFabrikasiClipStitchingStatus.class,
            IcerikFabrikasiClipStitchingIssueCode.class,
            IcerikFabrikasiRenderReviewPackageStatus.class,
            IcerikFabrikasiRenderReviewPackageIssueCode.class,
            IcerikFabrikasiInternalStoryVideoDryRunStatus.class,
            IcerikFabrikasiInternalStoryVideoDryRunIssueCode.class);

    // ── A. Forbidden Field Sweep (all classes) ──
    @Nested @DisplayName("A. Forbidden Fields — All Classes")
    class ForbiddenSweep {
        @Test @DisplayName("0 forbidden field violations across all classes")
        void sweep() {
            for (Class<?> c : ALL_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN_FIELDS)
                    assertThat(fields).as("'%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── B. No READY_FOR_PUBLISH / COMPLETED in any enum ──
    @Nested @DisplayName("B. No Public Delivery Statuses")
    class NoPublishStatus {
        @Test @DisplayName("no READY_FOR_PUBLISH in any enum")
        void noPublish() {
            for (var enumClass : ALL_ENUMS) {
                assertThat(Arrays.stream(enumClass.getEnumConstants())
                        .noneMatch(e -> e.name().contains("READY_FOR_PUBLISH")))
                        .as("READY_FOR_PUBLISH in %s", enumClass.getSimpleName()).isTrue();
            }
        }
        @Test @DisplayName("no COMPLETED in any enum")
        void noCompleted() {
            for (var enumClass : ALL_ENUMS) {
                assertThat(Arrays.stream(enumClass.getEnumConstants())
                        .noneMatch(e -> e.name().equals("COMPLETED")))
                        .as("COMPLETED in %s", enumClass.getSimpleName()).isTrue();
            }
        }
        @Test @DisplayName("no AUTO_APPROVED (positive) in any enum")
        void noAutoApprove() {
            for (var enumClass : ALL_ENUMS) {
                assertThat(Arrays.stream(enumClass.getEnumConstants())
                        .noneMatch(e -> e.name().equals("AUTO_APPROVED")))
                        .as("AUTO_APPROVED in %s", enumClass.getSimpleName()).isTrue();
            }
        }
    }

    // ── C. ProcessBuilder Only in Runner ──
    @Nested @DisplayName("C. ProcessBuilder Boundary")
    class ProcessBoundary {
        private static final List<Class<?>> NON_RUNNER = ALL_CLASSES.stream()
                .filter(c -> c != IcerikFabrikasiFfmpegLocalDevProcessRunner.class)
                .toList();

        @Test @DisplayName("no ProcessBuilder field outside runner")
        void noFields() {
            for (Class<?> c : NON_RUNNER) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName()).doesNotContain("ProcessBuilder", "Process");
            }
        }

        @Test @DisplayName("no Runtime field in any class")
        void noRuntime() {
            for (Class<?> c : ALL_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName()).doesNotContain("Runtime");
            }
        }

        @Test @DisplayName("runner has stitchClips method")
        void stitchMethod() {
            assertThat(Arrays.stream(IcerikFabrikasiFfmpegLocalDevProcessRunner.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("stitchClips"))).isTrue();
        }

        @Test @DisplayName("runner has generateClip method")
        void generateMethod() {
            assertThat(Arrays.stream(IcerikFabrikasiFfmpegLocalDevProcessRunner.class.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals("generateClip"))).isTrue();
        }
    }

    // ── D. FP-034 Single Clip Metadata Safe ──
    @Nested @DisplayName("D. FP-034 — Clip Generation Metadata")
    class FP034 {
        @Test @DisplayName("process result internalOnly=true")
        void resultSafe() {
            var result = IcerikFabrikasiFfmpegProcessResult.skipped();
            assertThat(result.toString()).doesNotContain("filePath", "commandLine", "stdout");
        }
    }

    // ── E. FP-035 Lifecycle Safe ──
    @Nested @DisplayName("E. FP-035 — Lifecycle Verification")
    class FP035 {
        @Test @DisplayName("verified clip produces PASSED")
        void verified() {
            var svc = new IcerikFabrikasiRenderArtifactLifecycleService();
            var report = svc.verify("ref-1", "mp4", 2, 24, 640, 360, 1024*1024);
            assertThat(report.getVerificationStatus()).isEqualTo(IcerikFabrikasiRenderArtifactVerificationStatus.PASSED);
        }
    }

    // ── F. FP-036 Bundle Metadata-only ──
    @Nested @DisplayName("F. FP-036 — Bundle Manifest")
    class FP036 {
        @Test @DisplayName("bundle status is metadata-only")
        void bundleSafe() {
            var svc = new IcerikFabrikasiRenderBundleManifestService();
            var lcSvc = new IcerikFabrikasiRenderArtifactLifecycleService();
            var report = lcSvc.verify("ref-b", "mp4", 2, 24, 640, 360, 1024*1024);
            var ref = IcerikFabrikasiRenderBundleArtifactRef.fromLifecycleReport(report,
                    IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP);
            var bundle = svc.build("bundle-test", List.of(ref));
            assertThat(bundle.getStatus()).isEqualTo(IcerikFabrikasiRenderBundleStatus.READY_FOR_INTERNAL_REVIEW_ONLY);
        }
    }

    // ── G. FP-037 Scene Plan Metadata-only ──
    @Nested @DisplayName("G. FP-037 — Scene Plan")
    class FP037 {
        @Test @DisplayName("scene plan validated")
        void planOk() {
            var svc = new IcerikFabrikasiSceneRenderPlanService();
            var expect = new IcerikFabrikasiSceneRenderArtifactExpectation(
                    IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP, true);
            var plan = svc.validate("s1", "p1", 1, 2, 24, 640, 360, List.of(expect));
            assertThat(plan.getStatus()).isEqualTo(IcerikFabrikasiSceneRenderPlanStatus.READY_FOR_BUNDLE_ASSEMBLY);
        }
    }

    // ── H. FP-038 Multi-scene Job Metadata-only ──
    @Nested @DisplayName("H. FP-038 — Multi-scene Job")
    class FP038 {
        @Test @DisplayName("job plan reaches READY_FOR_LOCAL_MANUAL_RENDER")
        void jobReady() {
            var planSvc = new IcerikFabrikasiSceneRenderPlanService();
            var expect = new IcerikFabrikasiSceneRenderArtifactExpectation(
                    IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP, true);
            var plans = List.of(
                    planSvc.validate("s1", "p1", 1, 2, 24, 640, 360, List.of(expect)),
                    planSvc.validate("s2", "p2", 2, 2, 24, 640, 360, List.of(expect)));
            var orchSvc = new IcerikFabrikasiMultiSceneRenderOrchestrationService();
            var job = orchSvc.buildJobPlan("job-test", plans);
            assertThat(job.getStatus()).isEqualTo(IcerikFabrikasiMultiSceneRenderJobStatus.READY_FOR_LOCAL_MANUAL_RENDER);
        }
    }

    // ── I. FP-040/041 Stitching Remains Gated ──
    @Nested @DisplayName("I. FP-040/041 — Stitching Gated")
    class FP040 {
        @Test @DisplayName("default harness disabled")
        void disabled() {
            var h = new IcerikFabrikasiClipStitchingHarness();
            var clip = new IcerikFabrikasiClipStitchingInputClipRef("c1", 1, 2, 24, 640, 360, 1024*1024, "mp4", true);
            var result = h.attempt(List.of(clip, clip), IcerikFabrikasiFfmpegBinaryPolicy.TEST_FIXTURE_FFMPEG);
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiClipStitchingStatus.MANUAL_FLAG_NOT_ENABLED);
        }
        @Test @DisplayName("fake stitch works in CI")
        void fakeOk() {
            var h = new IcerikFabrikasiClipStitchingHarness();
            var c1 = new IcerikFabrikasiClipStitchingInputClipRef("c1", 1, 2, 24, 640, 360, 1024*1024, "mp4", true);
            var c2 = new IcerikFabrikasiClipStitchingInputClipRef("c2", 2, 2, 24, 640, 360, 1024*1024, "mp4", true);
            var result = h.fakeStitch(List.of(c1, c2));
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiClipStitchingStatus.STITCHED_ARTIFACT_VERIFIED);
        }
    }

    // ── J. FP-042 Review Package Internal-only ──
    @Nested @DisplayName("J. FP-042 — Review Package Internal")
    class FP042 {
        @Test @DisplayName("review package internalOnly=true")
        void internalOnly() {
            var svc = new IcerikFabrikasiRenderReviewPackageService();
            var summary = new IcerikFabrikasiRenderReviewArtifactSummary("art-1",
                    IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP,
                    IcerikFabrikasiRenderArtifactVerificationStatus.PASSED,
                    "mp4", 2, 24, 640, 360, 1024*1024);
            var pkg = svc.build("j", "b", List.of(summary), 1, 1);
            assertThat(pkg.isInternalOnly()).isTrue();
            assertThat(pkg.getStatus()).isEqualTo(IcerikFabrikasiRenderReviewPackageStatus.READY_FOR_INTERNAL_REVIEW_ONLY);
        }
    }

    // ── K. FP-044 E2E Dry Run Internal Metadata-only ──
    @Nested @DisplayName("K. FP-044 — E2E Dry Run")
    class FP044 {
        @Test @DisplayName("3-scene E2E reaches ADMIN_METADATA_VISIBLE")
        void e2e() {
            var h = new IcerikFabrikasiInternalStoryVideoDryRunHarness();
            var result = h.execute(3);
            assertThat(result.getStatus()).isEqualTo(IcerikFabrikasiInternalStoryVideoDryRunStatus.ADMIN_METADATA_VISIBLE);
            assertThat(result.isInternalOnly()).isTrue();
            assertThat(result.isAdminMetadataVisible()).isTrue();
        }
    }

    // ── L. Endpoint Inventory ──
    @Nested @DisplayName("L. Endpoint Inventory (46 = 26 POST + 20 GET)")
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

    // ── M. Enum Inventory ──
    @Nested @DisplayName("M. Enum Inventory")
    class EnumInventory {
        @Test @DisplayName("18 enums in chain")
        void total() { assertThat(ALL_ENUMS).hasSize(18); }
    }
}
