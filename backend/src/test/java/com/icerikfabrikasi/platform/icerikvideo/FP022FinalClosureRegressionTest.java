package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.review.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * FP-022 Task 05: E2E Render Execution Safety Regression + Final Closure.
 *
 * <p>Full regression covering render job → stub execution → safety envelope →
 * execution safety, DTO safety, endpoint inventory, stage machine,
 * and forbidden behavior across entire FP-022 scope.</p>
 */
@DisplayName("FP-022 Task 05 — E2E Render Execution Safety Regression")
class FP022FinalClosureRegressionTest {

    private IcerikFabrikasiVideoProjectRepository repository;
    private IcerikFabrikasiVideoProjectBootstrapService bootstrapService;
    private IcerikFabrikasiRenderExecutionService executionService;
    private IcerikFabrikasiVideoProjectController controller;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIcerikFabrikasiVideoProjectRepository();
        var gateway = new StubIcerikFabrikasiGenerationProviderGateway();
        var sanitizer = new IcerikFabrikasiProviderSafetySanitizer();
        var policy = new IcerikFabrikasiProviderSafetyPolicy();
        bootstrapService = new IcerikFabrikasiVideoProjectBootstrapService(repository);
        var planningService = new IcerikFabrikasiVideoPlanningService(repository);
        var assetService = new IcerikFabrikasiVideoAssetPlanningService(repository);
        var draftService = new IcerikFabrikasiProviderDraftPlanningService(repository, gateway, sanitizer, policy);
        var imageBatchService = new IcerikFabrikasiProviderImageBatchService(repository, gateway, sanitizer, policy);
        var compositionService = new IcerikFabrikasiVideoCompositionService(repository);
        var renderManifestService = new IcerikFabrikasiRenderManifestService(repository);
        var renderJobService = new IcerikFabrikasiRenderJobService(repository, renderManifestService);
        var renderRunner = new StubIcerikFabrikasiRenderRunnerGateway();
        executionService = new IcerikFabrikasiRenderExecutionService(repository, renderRunner);
        var actorResolver = new AdminActorResolver();
        controller = new IcerikFabrikasiVideoProjectController(
                bootstrapService, planningService, assetService,
                draftService, imageBatchService, compositionService,
                renderManifestService, renderJobService, executionService, new IcerikFabrikasiStoryboardInternalRenderChainHarness(), actorResolver);
    }

    private IcerikFabrikasiVideoProject bootstrapProject() {
        IcerikFabrikasiVideoBootstrapRequest req = new IcerikFabrikasiVideoBootstrapRequest();
        req.setIcerikFabrikasiName("\u00c7anakkale T\u00fcrk\u00fcs\u00fc");
        req.setRegion("\u00c7anakkale");
        req.setMood("Kahramanl\u0131k");
        return bootstrapService.bootstrap(req, "test-admin");
    }

    private UUID runFullPipeline() {
        var project = bootstrapProject();
        UUID id = project.getId();
        controller.prepareStory(id, null, null, null);
        controller.prepareScenario(id, null, null, null);
        controller.prepareStoryboard(id, null, null, null);
        controller.prepareSceneAssets(id, null, null, null);
        controller.prepareImageBatch(id, null, null, null);
        controller.completeImageBatch(id, null, null, null);
        controller.prepareCompositionPlan(id, null, null, null);
        controller.prepareRenderManifest(id, null, null, null);
        return id;
    }

    private UUID runToRenderCompleted() {
        UUID id = runFullPipeline();
        controller.prepareRenderJob(id, null, null, null);
        controller.runRenderJob(id, null, null, null);
        return id;
    }

    // ── A. Endpoint Inventory (46 = 26 POST + 20 GET) ──

    @Nested
    @DisplayName("A. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class EndpointInventory {

        @Test
        @DisplayName("IcerikFabrikasiVideoProjectController: 17 POST + 8 GET = 23")
        void icerikFabrikasiControllerCounts() {
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, PostMapping.class))
                    .as("POST").isEqualTo(17);
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, GetMapping.class))
                    .as("GET").isEqualTo(8);
        }

        @Test
        @DisplayName("total 46 = 26 POST + 20 GET")
        void totalEndpoints() {
            long post = countMethods(AdminReviewController.class, PostMapping.class)
                    + countMethods(AdminPipelineController.class, PostMapping.class)
                    + countMethods(AdminWorkflowObservabilityController.class, PostMapping.class)
                    + countMethods(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = countMethods(AdminReviewController.class, GetMapping.class)
                    + countMethods(AdminPipelineController.class, GetMapping.class)
                    + countMethods(AdminWorkflowObservabilityController.class, GetMapping.class)
                    + countMethods(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post).as("total POST").isEqualTo(26);
            assertThat(get).as("total GET").isEqualTo(20);
            assertThat(post + get).as("total endpoints").isEqualTo(46);
        }

        @Test
        @DisplayName("no PUT/DELETE endpoints")
        void noUnsafe() {
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, PutMapping.class)).isEqualTo(0);
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, DeleteMapping.class)).isEqualTo(0);
        }

        @Test
        @DisplayName("FP-022 endpoints exist")
        void fp022Endpoints() {
            var methods = IcerikFabrikasiVideoProjectController.class.getDeclaredMethods();
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(PostMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(PostMapping.class).value()))
                    .anyMatch(v -> v.contains("prepare-render-job"))).isTrue();
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(GetMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(GetMapping.class).value()))
                    .anyMatch(v -> v.contains("render-job"))).isTrue();
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(PostMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(PostMapping.class).value()))
                    .anyMatch(v -> v.contains("run-render-job"))).isTrue();
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(GetMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(GetMapping.class).value()))
                    .anyMatch(v -> v.contains("render-execution-safety"))).isTrue();
        }

        private long countMethods(Class<?> c, Class<? extends java.lang.annotation.Annotation> ann) {
            return Arrays.stream(c.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ann)).count();
        }
    }

    // ── B. E2E Render Execution Flow ───────────────────

    @Nested
    @DisplayName("B. E2E Render Execution Flow")
    class E2EFlow {

        @Test
        @DisplayName("full pipeline to RENDER_JOB_COMPLETED_INTERNAL")
        void fullPipeline() {
            UUID id = runToRenderCompleted();
            var project = bootstrapService.getById(id);
            assertThat(project.getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_COMPLETED_INTERNAL);
        }

        @Test
        @DisplayName("render job PREPARED after prepare-render-job")
        void prepareRenderJob() {
            UUID id = runFullPipeline();
            controller.prepareRenderJob(id, null, null, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED);
        }

        @Test
        @DisplayName("render job response has safe metadata")
        void renderJobResponse() {
            UUID id = runFullPipeline();
            var resp = controller.prepareRenderJob(id, null, null, null);
            var data = resp.getBody().getData();
            assertThat(data.getJobId()).isNotNull();
            assertThat(data.isHumanReviewRequired()).isTrue();
            assertThat(data.isAutoApproved()).isFalse();
            assertThat(data.isNotPublishReady()).isTrue();
            assertThat(data.isNotExportReady()).isTrue();
        }

        @Test
        @DisplayName("GET render-job returns prepared job")
        void getRenderJob() {
            UUID id = runFullPipeline();
            controller.prepareRenderJob(id, null, null, null);
            var resp = controller.getRenderJob(id);
            var data = resp.getBody().getData();
            assertThat(data.getStatus()).isEqualTo(IcerikFabrikasiRenderJobStatus.PREPARED);
        }

        @Test
        @DisplayName("run render job returns COMPLETED_INTERNAL via stub")
        void runRenderJob() {
            UUID id = runFullPipeline();
            controller.prepareRenderJob(id, null, null, null);
            var resp = controller.runRenderJob(id, null, null, null);
            var data = resp.getBody().getData();
            assertThat(data.getStatus()).isEqualTo(IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL);
            assertThat(data.getSafeTraceRef()).startsWith("stub-trace:");
            assertThat(data.getArtifactRef()).startsWith("internal-stub-artifact:");
        }

        @Test
        @DisplayName("execution safety envelope after render")
        void executionSafety() {
            UUID id = runToRenderCompleted();
            var resp = controller.getRenderExecutionSafety(id);
            var data = resp.getBody().getData();
            assertThat(data.getJobStatus()).isEqualTo(IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL);
            assertThat(data.getSafetyEnvelope().isHumanReviewRequired()).isTrue();
            assertThat(data.getSafetyEnvelope().isAutoApproved()).isFalse();
            assertThat(data.getSafetyEnvelope().isLyricsIncluded()).isFalse();
            assertThat(data.getSafetyEnvelope().isCopyrightClearanceVerified()).isFalse();
        }

        @Test
        @DisplayName("render attempt stored on job")
        void attemptStored() {
            UUID id = runToRenderCompleted();
            var job = bootstrapService.getById(id).getRenderJob();
            assertThat(job.getAttempts()).hasSize(1);
            assertThat(job.getAttempts().get(0).getStatus())
                    .isEqualTo(IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL);
            assertThat(job.getAttempts().get(0).getDurationMs()).isGreaterThan(0);
        }

        @Test
        @DisplayName("render artifact on job is safe")
        void artifactSafe() {
            UUID id = runToRenderCompleted();
            var artifact = bootstrapService.getById(id).getRenderJob().getArtifact();
            assertThat(artifact).isNotNull();
            assertThat(artifact.getArtifactRef()).startsWith("internal-stub-artifact:");
            assertThat(artifact.isHumanReviewRequired()).isTrue();
            assertThat(artifact.isAutoApproved()).isFalse();
        }

        @Test
        @DisplayName("progress response after render")
        void progressResponse() {
            UUID id = runToRenderCompleted();
            var resp = controller.getProgress(id);
            var data = resp.getBody().getData();
            assertThat(data.getCurrentStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_COMPLETED_INTERNAL);
        }
    }

    // ── C. Stage Machine ────────────────────────────────

    @Nested
    @DisplayName("C. Workflow Stage Machine")
    class StageMachine {

        @Test
        @DisplayName("stage enum has exactly 19 values")
        void enumSize() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.values()).hasSize(19);
        }

        @Test
        @DisplayName("FP-022 stages exist")
        void fp022Stages() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("RENDER_JOB_PREPARED")).isNotNull();
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("RENDER_JOB_RUNNING")).isNotNull();
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("RENDER_JOB_COMPLETED_INTERNAL")).isNotNull();
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("RENDER_JOB_FAILED_SAFE")).isNotNull();
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("INTERNAL_RENDER_ARTIFACT_READY")).isNotNull();
        }

        @Test
        @DisplayName("no forbidden stages")
        void noForbiddenStages() {
            var stages = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(stages).doesNotContain(
                    "PUBLISHED", "EXPORTED", "AUTO_APPROVED",
                    "PUBLIC_READY", "CDN_DELIVERED", "DOWNLOAD_READY",
                    "PUBLICLY_DELIVERED", "COMPLETED_AS_PUBLIC_VIDEO");
        }

        @Test
        @DisplayName("stage ordering")
        void stageOrdering() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED.ordinal())
                    .isGreaterThan(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED.ordinal());
            assertThat(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_RUNNING.ordinal())
                    .isGreaterThan(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED.ordinal());
            assertThat(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_COMPLETED_INTERNAL.ordinal())
                    .isGreaterThan(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_RUNNING.ordinal());
        }
    }

    // ── D. DTO Safety ──────────────────────────────────

    @Nested
    @DisplayName("D. DTO Safety — Forbidden Fields")
    class DtoSafety {

        private static final List<String> FORBIDDEN = List.of(
                "rawPayload", "rawProviderResponse", "rawProviderRequest",
                "rawRendererPayload", "token", "jwt", "session", "credential",
                "password", "secret", "apiKey", "authorizationHeader", "principalRaw",
                "publicUrl", "signedUrl", "cdnUrl", "exportUrl", "publishUrl",
                "downloadUrl", "storageKey", "base64", "imageData", "binaryData",
                "lyrics", "videoData", "audioData", "providerEndpoint",
                "renderedVideo", "videoFile", "downloadFile", "videoStream",
                "filePath", "renderedVideoUrl", "commandLine", "ffmpegCommand");

        @Test
        @DisplayName("IcerikFabrikasiRenderJobResponse safe")
        void jobResponse() { assertNoForbidden(IcerikFabrikasiRenderJobResponse.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionResponse safe")
        void execResponse() { assertNoForbidden(IcerikFabrikasiRenderExecutionResponse.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionSafetyResponse safe")
        void safetyResponse() { assertNoForbidden(IcerikFabrikasiRenderExecutionSafetyResponse.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionSafetyEnvelope safe")
        void envelope() { assertNoForbidden(IcerikFabrikasiRenderExecutionSafetyEnvelope.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionPlan safe")
        void plan() { assertNoForbidden(IcerikFabrikasiRenderExecutionPlan.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionResult safe")
        void result() { assertNoForbidden(IcerikFabrikasiRenderExecutionResult.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderExecutionRequest safe")
        void request() { assertNoForbidden(IcerikFabrikasiRenderExecutionRequest.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderJob safe")
        void renderJob() { assertNoForbidden(IcerikFabrikasiRenderJob.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderAttempt safe")
        void attempt() { assertNoForbidden(IcerikFabrikasiRenderAttempt.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderArtifact safe")
        void artifact() { assertNoForbidden(IcerikFabrikasiRenderArtifact.class); }

        @Test
        @DisplayName("StubIcerikFabrikasiRenderRunnerGateway safe")
        void stubRunner() { assertNoForbidden(StubIcerikFabrikasiRenderRunnerGateway.class); }

        private void assertNoForbidden(Class<?> clazz) {
            Set<String> fields = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            for (String f : FORBIDDEN) {
                assertThat(fields).as("field '%s' in %s", f, clazz.getSimpleName())
                        .doesNotContain(f);
            }
        }
    }

    // ── E. Forbidden Behavior ────────────────────────────

    @Nested
    @DisplayName("E. Forbidden Behavior")
    class ForbiddenBehavior {

        @Test
        @DisplayName("no publish/export/download controller methods")
        void noPublish() {
            var methods = Arrays.stream(IcerikFabrikasiVideoProjectController.class.getDeclaredMethods())
                    .map(java.lang.reflect.Method::getName).collect(Collectors.toSet());
            for (String m : List.of("publish", "export", "download", "publishVideo",
                    "exportVideo", "downloadVideo", "renderVideo", "finalizeVideo",
                    "autoApprove", "updateStatus", "genericStatusUpdate")) {
                assertThat(methods).doesNotContain(m);
            }
        }

        @Test
        @DisplayName("SYSTEM blocked on run-render-job")
        void systemBlocked() {
            UUID id = runFullPipeline();
            controller.prepareRenderJob(id, null, null, null);
            assertThatThrownBy(() ->
                    controller.runRenderJob(id, null, ReviewActorRole.SYSTEM, null))
                    .isInstanceOf(AdminActorResolutionException.class);
        }

        @Test
        @DisplayName("SYSTEM blocked on prepare-render-job")
        void systemBlockedPrepare() {
            UUID id = runFullPipeline();
            assertThatThrownBy(() ->
                    controller.prepareRenderJob(id, null, ReviewActorRole.SYSTEM, null))
                    .isInstanceOf(AdminActorResolutionException.class);
        }

        @Test
        @DisplayName("stub runner produces no real file")
        void stubNoFile() {
            var runner = new StubIcerikFabrikasiRenderRunnerGateway();
            var plan = new IcerikFabrikasiRenderExecutionPlan(
                    UUID.randomUUID(), 3, 120.0, "1920x1080", 30, "mp4", 60.0,
                    "deterministic-stub", "DETERMINISTIC_STUB");
            var result = runner.execute(plan);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getArtifact()).isNotNull();
            assertThat(result.getArtifact().getArtifactRef()).startsWith("internal-stub-artifact:");
            assertThat(result.getArtifact().getArtifactRef()).doesNotContain(
                    "http", "cdn", "public", "signed", "storage", "file://");
        }

        @Test
        @DisplayName("artifact ref is opaque")
        void opaqueRef() {
            UUID id = runToRenderCompleted();
            var artifact = bootstrapService.getById(id).getRenderJob().getArtifact();
            assertThat(artifact.getArtifactRef()).doesNotContain(
                    "http", "cdn", "public", "signed", "storage", "file://",
                    ".mp4", ".avi", ".mov", ".mkv", ".webm");
        }

        @Test
        @DisplayName("no RenderJobStatus PUBLISHED/EXPORTED/AUTO_APPROVED")
        void noForbiddenStatuses() {
            var statuses = Arrays.stream(IcerikFabrikasiRenderJobStatus.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(statuses).doesNotContain(
                    "PUBLISHED", "EXPORTED", "AUTO_APPROVED", "DOWNLOAD_READY", "PUBLIC_READY");
        }
    }

    // ── F. Safety Flags ─────────────────────────────────

    @Nested
    @DisplayName("F. Safety Flags")
    class SafetyFlags {

        @Test
        @DisplayName("execution response safety flags")
        void executionFlags() {
            UUID id = runFullPipeline();
            controller.prepareRenderJob(id, null, null, null);
            var resp = controller.runRenderJob(id, null, null, null);
            var data = resp.getBody().getData();
            assertThat(data.isHumanReviewRequired()).isTrue();
            assertThat(data.isCopyrightReviewRequired()).isTrue();
            assertThat(data.isAutoApproved()).isFalse();
            assertThat(data.isNotPublishReady()).isTrue();
            assertThat(data.isNotExportReady()).isTrue();
        }

        @Test
        @DisplayName("safety envelope flags")
        void envelopeFlags() {
            UUID id = runToRenderCompleted();
            var resp = controller.getRenderExecutionSafety(id);
            var envelope = resp.getBody().getData().getSafetyEnvelope();
            assertThat(envelope.getSafetyFlags()).contains(
                    "NO_REAL_RENDER", "NO_BINARY_OUTPUT", "NO_PUBLIC_DELIVERY", "NO_EXPORT");
            assertThat(envelope.getReviewFlags()).contains(
                    "HUMAN_REVIEW_REQUIRED", "NOT_PUBLISH_READY", "NOT_EXPORT_READY");
        }

        @Test
        @DisplayName("render job safety flags")
        void jobFlags() {
            UUID id = runFullPipeline();
            var resp = controller.prepareRenderJob(id, null, null, null);
            var data = resp.getBody().getData();
            assertThat(data.getReviewFlags()).contains("HUMAN_REVIEW_REQUIRED", "NOT_PUBLISH_READY");
        }
    }

    // ── G. Backward Compatibility ───────────────────────

    @Nested
    @DisplayName("G. Backward Compatibility")
    class BackwardCompatibility {

        @Test
        @DisplayName("bootstrap still works")
        void bootstrap() {
            var project = bootstrapProject();
            assertThat(project.getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);
        }

        @Test
        @DisplayName("full pipeline still works")
        void fullPipeline() {
            UUID id = runFullPipeline();
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED);
        }

        @Test
        @DisplayName("composition preview still works")
        void compositionPreview() {
            UUID id = runFullPipeline();
            var resp = controller.getCompositionPreview(id);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("render readiness still works")
        void renderReadiness() {
            UUID id = runFullPipeline();
            var resp = controller.getRenderReadiness(id);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("generation safety still works")
        void generationSafety() {
            UUID id = runFullPipeline();
            var resp = controller.getGenerationSafety(id);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }
    }
}
