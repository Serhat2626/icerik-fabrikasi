package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-054 — Final Production Readiness Boundary + Admin Internal Operations Closure.
 * Dense hardening across FP-034→FP-053. No new capability.
 */
@DisplayName("FP-054 — Production Readiness Boundary")
class IcerikFabrikasiProductionReadinessBoundaryTest {

    private static final List<String> FORBIDDEN_FIELDS = List.of(
            "stdout", "stderr", "rawLog", "commandLine", "shellCommand", "executablePath",
            "filePath", "absolutePath", "inputPath", "outputPath", "workspacePath",
            "storageKey", "publicUrl", "signedUrl", "cdnUrl", "storageProvider",
            "base64", "binaryData", "rawPayload", "downloadUrl", "publishUrl",
            "token", "jwt", "session", "principal", "authorizationHeader",
            "lyrics", "scriptBody", "promptText", "copyrightedContent", "userMedia",
            "unsafeHtml", "html", "mediaBody", "authHeader");

    /** All FP-034→FP-053 production-boundary classes */
    private static final List<Class<?>> ALL_BOUNDARY_CLASSES = List.of(
            // FP-037 scene render
            IcerikFabrikasiSceneRenderPlanStatus.class, IcerikFabrikasiSceneRenderPlan.class,
            // FP-038 multi-scene
            IcerikFabrikasiMultiSceneRenderJobStatus.class, IcerikFabrikasiMultiSceneRenderJobPlan.class,
            // FP-039 clip generation
            IcerikFabrikasiMultiClipGenerationStatus.class, IcerikFabrikasiMultiClipGenerationResult.class,
            // FP-040 stitching
            IcerikFabrikasiClipStitchingStatus.class, IcerikFabrikasiClipStitchingResult.class,
            // FP-041 lifecycle
            IcerikFabrikasiRenderArtifactLifecycleStatus.class, IcerikFabrikasiRenderArtifactLifecycleReport.class,
            // FP-042 bundle
            IcerikFabrikasiRenderBundleStatus.class, IcerikFabrikasiRenderBundleManifest.class,
            // FP-043 admin visibility
            IcerikFabrikasiInternalRenderReviewState.class, IcerikFabrikasiInternalRenderReviewSummary.class,
            // FP-044 dry run
            IcerikFabrikasiInternalStoryVideoDryRunStatus.class, IcerikFabrikasiInternalStoryVideoDryRunSummary.class,
            // FP-046 storyboard adapter
            IcerikFabrikasiStoryboardRenderAdapterStatus.class, IcerikFabrikasiStoryboardRenderPlanAdapterResult.class,
            // FP-047 storyboard chain
            IcerikFabrikasiStoryboardInternalRenderChainStatus.class, IcerikFabrikasiStoryboardInternalRenderChainFixture.class,
            // FP-049 trigger
            IcerikFabrikasiInternalDryRunTriggerRequest.class, IcerikFabrikasiInternalDryRunTriggerResponse.class,
            // FP-051 record
            IcerikFabrikasiInternalDryRunRecord.class, IcerikFabrikasiInternalDryRunRecordSummary.class,
            IcerikFabrikasiInternalDryRunRecordService.class,
            // FP-053 operational
            IcerikFabrikasiInternalDryRunOperationalSummary.class, IcerikFabrikasiInternalDryRunOperationalSummaryService.class);

    // ── A. Full Forbidden Field Sweep (27 classes) ──
    @Nested @DisplayName("A. Forbidden Field Sweep (FP-034→FP-053)")
    class ForbiddenSweep {
        @Test @DisplayName("all 27 boundary classes — 0 forbidden fields")
        void sweep() {
            for (var c : ALL_BOUNDARY_CLASSES) {
                Set<String> fields = Arrays.stream(c.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN_FIELDS)
                    assertThat(fields).as("forbidden '%s' in %s", f, c.getSimpleName()).doesNotContain(f);
            }
        }
    }

    // ── B. No Publish/Export/Auto-Approved Statuses ──
    @Nested @DisplayName("B. No READY_FOR_PUBLISH / COMPLETED / AUTO_APPROVED")
    class NoPublishStatuses {
        @Test @DisplayName("all status enums clean")
        void allClean() {
            List<Class<? extends Enum<?>>> enums = List.of(
                    IcerikFabrikasiSceneRenderPlanStatus.class, IcerikFabrikasiMultiSceneRenderJobStatus.class,
                    IcerikFabrikasiMultiClipGenerationStatus.class, IcerikFabrikasiClipStitchingStatus.class,
                    IcerikFabrikasiRenderArtifactLifecycleStatus.class, IcerikFabrikasiRenderBundleStatus.class,
                    IcerikFabrikasiInternalRenderReviewState.class, IcerikFabrikasiInternalStoryVideoDryRunStatus.class,
                    IcerikFabrikasiStoryboardRenderAdapterStatus.class, IcerikFabrikasiStoryboardInternalRenderChainStatus.class,
                    IcerikFabrikasiInternalDryRunRecordStatus.class, IcerikFabrikasiInternalDryRunOperationalSummaryStatus.class);
            for (var e : enums) {
                for (var v : e.getEnumConstants()) {
                    assertThat(v.name()).as("%s.%s", e.getSimpleName(), v.name())
                            .doesNotContain("READY_FOR_PUBLISH")
                            .doesNotContain("COMPLETED")
                            .doesNotContain("AUTO_APPROVED")
                            .doesNotContain("EXPORTED")
                            .doesNotContain("PUBLISHED");
                }
            }
        }
    }

    // ── C. Endpoint Inventory ──
    @Nested @DisplayName("C. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class EndpointInventory {
        @Test @DisplayName("controller: 17 POST + 8 GET = 25")
        void controller() {
            long post = count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post).as("POST").isEqualTo(17);
            assertThat(get).as("GET").isEqualTo(8);
        }
        @Test @DisplayName("total: 46 = 26 POST + 20 GET")
        void total() {
            long post = count(AdminReviewController.class, PostMapping.class)
                    + count(AdminPipelineController.class, PostMapping.class)
                    + count(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = count(AdminReviewController.class, GetMapping.class)
                    + count(AdminPipelineController.class, GetMapping.class)
                    + count(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + count(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post + get).as("total").isEqualTo(46);
            assertThat(post).as("POST").isEqualTo(26);
            assertThat(get).as("GET").isEqualTo(20);
        }
        @Test @DisplayName("all controller paths contain /admin")
        void adminOnly() {
            for (var c : List.of(AdminReviewController.class, AdminPipelineController.class,
                    AdminWorkflowObservabilityController.class, IcerikFabrikasiVideoProjectController.class)) {
                var mapping = c.getAnnotation(RequestMapping.class);
                assertThat(mapping).as(c.getSimpleName()).isNotNull();
                assertThat(mapping.value()[0]).as(c.getSimpleName()).contains("admin");
            }
        }
    }

    // ── D. ProcessBuilder Boundary ──
    @Nested @DisplayName("D. ProcessBuilder Boundary")
    class ProcessBoundary {
        @Test @DisplayName("no ProcessBuilder/Process/Runtime fields in boundary classes")
        void noProcess() {
            for (var c : ALL_BOUNDARY_CLASSES) {
                Set<String> types = Arrays.stream(c.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName()).collect(Collectors.toSet());
                assertThat(types).as(c.getSimpleName())
                        .doesNotContain("ProcessBuilder", "Process", "Runtime");
            }
        }
    }

    // ── E. Internal Chain Integration ──
    @Nested @DisplayName("E. Internal Chain Integration")
    class ChainIntegration {
        @Test @DisplayName("dry-run → record → summary chain works")
        void fullChain() {
            var harness = new IcerikFabrikasiStoryboardInternalRenderChainHarness();
            var fixture = IcerikFabrikasiStoryboardInternalRenderChainFixture.icerikFabrikasiFixture(3);
            var chainResult = harness.execute(fixture);
            var triggerResponse = new IcerikFabrikasiInternalDryRunTriggerResponse(chainResult);
            var recordService = new IcerikFabrikasiInternalDryRunRecordService();
            var record = recordService.createFromDryRunResponse(triggerResponse);
            var summaryService = new IcerikFabrikasiInternalDryRunOperationalSummaryService();
            var opSummary = summaryService.summarize(recordService);

            assertThat(chainResult.getStatus()).isEqualTo(IcerikFabrikasiStoryboardInternalRenderChainStatus.ADMIN_METADATA_VISIBLE);
            assertThat(triggerResponse.isInternalOnly()).isTrue();
            assertThat(record.getStatus()).isEqualTo(IcerikFabrikasiInternalDryRunRecordStatus.READY_FOR_ADMIN_HISTORY_ONLY);
            assertThat(opSummary.getOverallStatus()).isEqualTo(IcerikFabrikasiInternalDryRunOperationalSummaryStatus.ALL_ACCEPTED);
            assertThat(opSummary.isInternalOnly()).isTrue();
        }
        @Test @DisplayName("rejected chain → rejected record → rejected summary")
        void rejectedChain() {
            var harness = new IcerikFabrikasiStoryboardInternalRenderChainHarness();
            var chainResult = harness.execute(null);
            var triggerResponse = new IcerikFabrikasiInternalDryRunTriggerResponse(chainResult);
            var recordService = new IcerikFabrikasiInternalDryRunRecordService();
            recordService.createFromDryRunResponse(triggerResponse);
            var opSummary = new IcerikFabrikasiInternalDryRunOperationalSummaryService().summarize(recordService);

            assertThat(chainResult.getStatus()).isEqualTo(IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE);
            assertThat(opSummary.getOverallStatus()).isEqualTo(IcerikFabrikasiInternalDryRunOperationalSummaryStatus.ALL_REJECTED);
        }
    }

    // ── F. No Public Delivery / Generic Update ──
    @Nested @DisplayName("F. No Public Delivery / Generic Update")
    class NoPublicDelivery {
        @Test @DisplayName("IcerikFabrikasiVideoWorkflowStage has no PUBLIC/DELIVER/EXPORT/DOWNLOAD")
        void workflowStage() {
            for (var v : IcerikFabrikasiVideoWorkflowStage.values()) {
                assertThat(v.name()).doesNotContain("PUBLIC");
                assertThat(v.name()).doesNotContain("DELIVER");
                assertThat(v.name()).doesNotContain("EXPORT");
                assertThat(v.name()).doesNotContain("DOWNLOAD");
            }
        }
    }

    private static long count(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
        return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a)).count();
    }
}
