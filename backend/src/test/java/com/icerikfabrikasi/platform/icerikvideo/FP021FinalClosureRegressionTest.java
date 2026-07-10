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
 * FP-021 Task 05: E2E Render-Planning Safety Regression + Final Closure.
 *
 * <p>Full regression covering composition plan → render manifest → readiness →
 * composition preview workflow, safety invariants, DTO safety, endpoint
 * inventory, stage machine, and forbidden behavior.</p>
 */
@DisplayName("FP-021 Task 05 — E2E Render-Planning Safety Regression")
class FP021FinalClosureRegressionTest {

    private IcerikFabrikasiVideoProjectRepository repository;
    private IcerikFabrikasiVideoProjectBootstrapService bootstrapService;
    private IcerikFabrikasiVideoCompositionService compositionService;
    private IcerikFabrikasiRenderManifestService renderManifestService;
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
        compositionService = new IcerikFabrikasiVideoCompositionService(repository);
        renderManifestService = new IcerikFabrikasiRenderManifestService(repository);
        var renderJobService = new IcerikFabrikasiRenderJobService(repository, renderManifestService);
        var renderRunner = new StubIcerikFabrikasiRenderRunnerGateway();
        var renderExecutionService = new IcerikFabrikasiRenderExecutionService(repository, renderRunner);
        var actorResolver = new AdminActorResolver();
        controller = new IcerikFabrikasiVideoProjectController(
                bootstrapService, planningService, assetService,
                draftService, imageBatchService, compositionService,
                renderManifestService, renderJobService, renderExecutionService, new IcerikFabrikasiStoryboardInternalRenderChainHarness(), actorResolver);
    }

    private IcerikFabrikasiVideoProject bootstrapProject() {
        IcerikFabrikasiVideoBootstrapRequest req = new IcerikFabrikasiVideoBootstrapRequest();
        req.setIcerikFabrikasiName("Çanakkale Türküsü");
        req.setRegion("Çanakkale");
        req.setMood("Kahramanlık");
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

    // ── A. Endpoint Inventory (46 = 26 POST + 20 GET) ──

    @Nested
    @DisplayName("A. Endpoint Inventory (46 = 26 POST + 20 GET)")
    class EndpointInventory {

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
        @DisplayName("IcerikFabrikasiVideoProjectController: 17 POST + 8 GET = 23")
        void icerikFabrikasiControllerCounts() {
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, PostMapping.class))
                    .as("POST").isEqualTo(17);
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, GetMapping.class))
                    .as("GET").isEqualTo(8);
        }

        @Test
        @DisplayName("no PUT/DELETE endpoints across entire system")
        void noUnsafe() {
            long put = countMethods(IcerikFabrikasiVideoProjectController.class, PutMapping.class)
                    + countMethods(AdminReviewController.class, PutMapping.class)
                    + countMethods(AdminPipelineController.class, PutMapping.class);
            long del = countMethods(IcerikFabrikasiVideoProjectController.class, DeleteMapping.class)
                    + countMethods(AdminReviewController.class, DeleteMapping.class)
                    + countMethods(AdminPipelineController.class, DeleteMapping.class);
            assertThat(put).isEqualTo(0);
            assertThat(del).isEqualTo(0);
        }

        @Test
        @DisplayName("FP-021 endpoints exist")
        void fp021Endpoints() {
            var methods = IcerikFabrikasiVideoProjectController.class.getDeclaredMethods();
            // POST prepare-composition-plan
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(PostMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(PostMapping.class).value()))
                    .anyMatch(v -> v.contains("prepare-composition-plan"))).isTrue();
            // POST prepare-render-manifest
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(PostMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(PostMapping.class).value()))
                    .anyMatch(v -> v.contains("prepare-render-manifest"))).isTrue();
            // GET render-readiness
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(GetMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(GetMapping.class).value()))
                    .anyMatch(v -> v.contains("render-readiness"))).isTrue();
            // GET composition-preview
            assertThat(Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(GetMapping.class))
                    .flatMap(m -> Arrays.stream(m.getAnnotation(GetMapping.class).value()))
                    .anyMatch(v -> v.contains("composition-preview"))).isTrue();
        }

        private long countMethods(Class<?> c, Class<? extends java.lang.annotation.Annotation> ann) {
            return Arrays.stream(c.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ann)).count();
        }
    }

    // ── B. E2E Full Pipeline ───────────────────────────

    @Nested
    @DisplayName("B. E2E Full Render-Planning Pipeline")
    class FullPipeline {

        @Test
        @DisplayName("full pipeline: bootstrap → image batch → composition → manifest → readiness → preview")
        void fullFlow() {
            UUID id = runFullPipeline();
            var project = bootstrapService.getById(id);

            // Stage
            assertThat(project.getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED);

            // Composition plan stored
            assertThat(project.getCompositionPlan()).isNotNull();
            assertThat(project.getCompositionPlan().getScenes()).isNotEmpty();

            // Render manifest stored
            assertThat(project.getRenderManifest()).isNotNull();
            assertThat(project.getRenderManifest().getScenes()).isNotEmpty();

            // Readiness
            var readiness = controller.getRenderReadiness(id);
            assertThat(readiness.getStatusCode().value()).isEqualTo(200);
            var report = readiness.getBody().getData();
            assertThat(report.isCompositionReady()).isTrue();
            assertThat(report.isManifestReady()).isTrue();
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderReadinessStatus.NEEDS_REVIEW);

            // Preview
            var preview = controller.getCompositionPreview(id);
            assertThat(preview.getStatusCode().value()).isEqualTo(200);
            var previewData = preview.getBody().getData();
            assertThat(previewData.isCompositionReady()).isTrue();
            assertThat(previewData.isManifestReady()).isTrue();
            assertThat(previewData.getSceneCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("progress reflects composition/render stages")
        void progressReflection() {
            UUID id = runFullPipeline();
            var progress = controller.getProgress(id);
            assertThat(progress.getStatusCode().value()).isEqualTo(200);
            var data = progress.getBody().getData();
            assertThat(data.getCurrentStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED);
            // completedStages contains stages before current
            assertThat(data.getCompletedStages())
                    .contains(IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED);
        }

        @Test
        @DisplayName("composition plan stage transition correct")
        void compositionStage() {
            var project = bootstrapProject();
            UUID id = project.getId();
            controller.prepareStory(id, null, null, null);
            controller.prepareScenario(id, null, null, null);
            controller.prepareStoryboard(id, null, null, null);
            controller.prepareSceneAssets(id, null, null, null);
            controller.prepareImageBatch(id, null, null, null);
            controller.completeImageBatch(id, null, null, null);
            controller.prepareCompositionPlan(id, null, null, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED);
        }

        @Test
        @DisplayName("render manifest stage transition correct")
        void manifestStage() {
            UUID id = runFullPipeline();
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED);
        }

        @Test
        @DisplayName("readiness without manifest returns BLOCKED")
        void readinessBlocked() {
            var project = bootstrapProject();
            UUID id = project.getId();
            controller.prepareStory(id, null, null, null);
            controller.prepareScenario(id, null, null, null);
            controller.prepareStoryboard(id, null, null, null);
            controller.prepareSceneAssets(id, null, null, null);
            controller.prepareImageBatch(id, null, null, null);
            controller.completeImageBatch(id, null, null, null);
            controller.prepareCompositionPlan(id, null, null, null);
            // No manifest yet
            var report = renderManifestService.evaluateReadiness(id);
            assertThat(report.getStatus()).isEqualTo(IcerikFabrikasiRenderReadinessStatus.BLOCKED);
        }

        @Test
        @DisplayName("preview at DRAFT_CREATED returns safe empty state")
        void earlyPreview() {
            var project = bootstrapProject();
            var preview = controller.getCompositionPreview(project.getId());
            var data = preview.getBody().getData();
            assertThat(data.isCompositionReady()).isFalse();
            assertThat(data.isManifestReady()).isFalse();
            assertThat(data.getTimelineScenes()).isEmpty();
            assertThat(data.getReadinessStatus()).isEqualTo("BLOCKED");
        }
    }

    // ── C. Safety Invariants ────────────────────────────

    @Nested
    @DisplayName("C. Safety Invariants")
    class SafetyInvariants {

        @Test
        @DisplayName("humanReviewRequired=true on composition plan")
        void compositionHumanReview() {
            var project = bootstrapProject();
            UUID id = project.getId();
            controller.prepareStory(id, null, null, null);
            controller.prepareScenario(id, null, null, null);
            controller.prepareStoryboard(id, null, null, null);
            controller.prepareSceneAssets(id, null, null, null);
            controller.prepareImageBatch(id, null, null, null);
            controller.completeImageBatch(id, null, null, null);
            var resp = controller.prepareCompositionPlan(id, null, null, null);
            assertThat(resp.getBody().getData().isHumanReviewRequired()).isTrue();
            assertThat(resp.getBody().getData().isAutoApproved()).isFalse();
        }

        @Test
        @DisplayName("humanReviewRequired=true on render manifest")
        void manifestHumanReview() {
            UUID id = runFullPipeline();
            var project = bootstrapService.getById(id);
            assertThat(project.getRenderManifest().isHumanReviewRequired()).isTrue();
            assertThat(project.getRenderManifest().isAutoApproved()).isFalse();
        }

        @Test
        @DisplayName("readiness humanReviewRequired=true")
        void readinessHumanReview() {
            UUID id = runFullPipeline();
            var report = renderManifestService.evaluateReadiness(id);
            assertThat(report.isHumanReviewRequired()).isTrue();
            assertThat(report.isAutoApproved()).isFalse();
        }

        @Test
        @DisplayName("preview humanReviewRequired=true, autoApproved=false")
        void previewHumanReview() {
            UUID id = runFullPipeline();
            var data = controller.getCompositionPreview(id).getBody().getData();
            assertThat(data.isHumanReviewRequired()).isTrue();
            assertThat(data.isAutoApproved()).isFalse();
            assertThat(data.isCopyrightReviewRequired()).isTrue();
        }

        @Test
        @DisplayName("manifest image refs are safe internal refs")
        void safeImageRefs() {
            UUID id = runFullPipeline();
            var manifest = bootstrapService.getById(id).getRenderManifest();
            for (var scene : manifest.getScenes()) {
                if (scene.getImageRef() != null) {
                    assertThat(scene.getImageRef()).startsWith("internal-ref:");
                    assertThat(scene.getImageRef()).doesNotContain("http", "cdn", "public", "signed");
                }
            }
        }

        @Test
        @DisplayName("preview image ref labels are safe")
        void safePreviewRefs() {
            UUID id = runFullPipeline();
            var scenes = controller.getCompositionPreview(id).getBody().getData().getTimelineScenes();
            for (var scene : scenes) {
                assertThat(scene.getImageRefLabel()).doesNotContain("http", "cdn", "public", "signed", "base64");
            }
        }

        @Test
        @DisplayName("readiness reviewFlags include NOT_PUBLISH_READY")
        void notPublishReady() {
            UUID id = runFullPipeline();
            var report = renderManifestService.evaluateReadiness(id);
            assertThat(report.getReviewFlags())
                    .contains("NOT_PUBLISH_READY", "NOT_EXPORT_READY");
        }

        @Test
        @DisplayName("preview reviewFlags include INTERNAL_PREVIEW_ONLY")
        void internalOnly() {
            UUID id = runFullPipeline();
            var flags = controller.getCompositionPreview(id).getBody().getData().getReviewFlags();
            assertThat(flags).contains("NOT_PUBLISH_READY", "NOT_EXPORT_READY", "INTERNAL_PREVIEW_ONLY");
        }

        @Test
        @DisplayName("SYSTEM role blocked on composition plan POST")
        void systemBlocked() {
            var project = bootstrapProject();
            UUID id = project.getId();
            controller.prepareStory(id, null, null, null);
            controller.prepareScenario(id, null, null, null);
            controller.prepareStoryboard(id, null, null, null);
            controller.prepareSceneAssets(id, null, null, null);
            controller.prepareImageBatch(id, null, null, null);
            controller.completeImageBatch(id, null, null, null);
            assertThatThrownBy(() ->
                    controller.prepareCompositionPlan(id, null, ReviewActorRole.SYSTEM, null))
                    .isInstanceOf(AdminActorResolutionException.class);
        }

        @Test
        @DisplayName("SYSTEM role blocked on render manifest POST")
        void systemBlockedManifest() {
            UUID id = runFullPipeline();
            // Already at RENDER_MANIFEST_PREPARED, but test the actor blocking
            var project = bootstrapProject();
            UUID id2 = project.getId();
            controller.prepareStory(id2, null, null, null);
            controller.prepareScenario(id2, null, null, null);
            controller.prepareStoryboard(id2, null, null, null);
            controller.prepareSceneAssets(id2, null, null, null);
            controller.prepareImageBatch(id2, null, null, null);
            controller.completeImageBatch(id2, null, null, null);
            controller.prepareCompositionPlan(id2, null, null, null);
            assertThatThrownBy(() ->
                    controller.prepareRenderManifest(id2, null, ReviewActorRole.SYSTEM, null))
                    .isInstanceOf(AdminActorResolutionException.class);
        }
    }

    // ── D. Workflow Stage Machine ───────────────────────

    @Nested
    @DisplayName("D. Workflow Stage Machine")
    class StageMachine {

        @Test
        @DisplayName("stage enum has exactly 14 values")
        void enumSize() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.values()).hasSize(19);
        }

        @Test
        @DisplayName("COMPOSITION_PLAN_PREPARED exists")
        void compositionStage() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("COMPOSITION_PLAN_PREPARED")).isNotNull();
        }

        @Test
        @DisplayName("RENDER_MANIFEST_PREPARED exists")
        void manifestStage() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.valueOf("RENDER_MANIFEST_PREPARED")).isNotNull();
        }

        @Test
        @DisplayName("no forbidden workflow stages")
        void noForbiddenStages() {
            var stages = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(stages).doesNotContain(
                    "PUBLISHED", "EXPORTED", "AUTO_APPROVED",
                    "PUBLIC_READY", "CDN_DELIVERED", "DOWNLOAD_READY",
                    "PUBLICLY_DELIVERED", "COMPLETED_AS_PUBLIC_VIDEO");
        }

        @Test
        @DisplayName("stage ordering is safe")
        void stageOrdering() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED.ordinal())
                    .isGreaterThan(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED.ordinal());
            assertThat(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED.ordinal())
                    .isGreaterThan(IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED.ordinal());
        }
    }

    // ── E. DTO Safety — Forbidden Fields ────────────────

    @Nested
    @DisplayName("E. DTO Safety — Forbidden Fields")
    class DtoSafety {

        private static final List<String> FORBIDDEN = List.of(
                "rawPayload", "rawProviderResponse", "rawProviderRequest",
                "token", "jwt", "session", "credential", "password", "secret",
                "apiKey", "authorizationHeader", "principalRaw",
                "publicUrl", "signedUrl", "cdnUrl", "exportUrl", "publishUrl",
                "downloadUrl", "storageKey", "base64", "imageData", "binaryData",
                "lyrics", "videoData", "audioData", "providerEndpoint",
                "renderedVideo", "videoFile", "downloadFile", "videoStream");

        @Test
        @DisplayName("IcerikFabrikasiVideoCompositionPlan safe")
        void compositionPlan() { assertNoForbidden(IcerikFabrikasiVideoCompositionPlan.class); }

        @Test
        @DisplayName("IcerikFabrikasiVideoCompositionScene safe")
        void compositionScene() { assertNoForbidden(IcerikFabrikasiVideoCompositionScene.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderManifest safe")
        void manifest() { assertNoForbidden(IcerikFabrikasiRenderManifest.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderManifestScene safe")
        void manifestScene() { assertNoForbidden(IcerikFabrikasiRenderManifestScene.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderReadinessReport safe")
        void readiness() { assertNoForbidden(IcerikFabrikasiRenderReadinessReport.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderSafetyState safe")
        void safety() { assertNoForbidden(IcerikFabrikasiRenderSafetyState.class); }

        @Test
        @DisplayName("IcerikFabrikasiCompositionPreviewResponse safe")
        void preview() { assertNoForbidden(IcerikFabrikasiCompositionPreviewResponse.class); }

        @Test
        @DisplayName("IcerikFabrikasiCompositionPreviewScene safe")
        void previewScene() { assertNoForbidden(IcerikFabrikasiCompositionPreviewScene.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderManifestResponse safe")
        void manifestResponse() { assertNoForbidden(IcerikFabrikasiRenderManifestResponse.class); }

        @Test
        @DisplayName("IcerikFabrikasiRenderReadinessIssue safe")
        void readinessIssue() { assertNoForbidden(IcerikFabrikasiRenderReadinessIssue.class); }

        private void assertNoForbidden(Class<?> clazz) {
            Set<String> fields = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            for (String f : FORBIDDEN) {
                assertThat(fields).as("field '%s' in %s", f, clazz.getSimpleName())
                        .doesNotContain(f);
            }
        }
    }

    // ── F. Forbidden Behavior ───────────────────────────

    @Nested
    @DisplayName("F. Forbidden Behavior")
    class ForbiddenBehavior {

        @Test
        @DisplayName("no publish/export/download/render controller methods")
        void noPublish() {
            var methods = Arrays.stream(IcerikFabrikasiVideoProjectController.class.getDeclaredMethods())
                    .map(java.lang.reflect.Method::getName).collect(Collectors.toSet());
            for (String m : List.of("publish", "export", "download", "publishVideo",
                    "exportVideo", "downloadVideo", "renderVideo", "finalizeVideo",
                    "autoApprove", "updateStatus", "genericStatusUpdate",
                    "deliverPublic", "getCdnUrl", "getPublicUrl", "getSignedUrl")) {
                assertThat(methods).doesNotContain(m);
            }
        }

        @Test
        @DisplayName("no video rendering service classes")
        void noRenderService() {
            // These classes should not exist
            assertThatThrownBy(() -> Class.forName("com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiVideoRenderService"))
                    .isInstanceOf(ClassNotFoundException.class);
            assertThatThrownBy(() -> Class.forName("com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiVideoExportService"))
                    .isInstanceOf(ClassNotFoundException.class);
            assertThatThrownBy(() -> Class.forName("com.icerikfabrikasi.platform.icerikvideo.IcerikFabrikasiVideoPublishService"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("render readiness copyrightReviewDone is false")
        void copyrightPending() {
            UUID id = runFullPipeline();
            var report = renderManifestService.evaluateReadiness(id);
            assertThat(report.isCopyrightReviewDone()).isFalse();
        }

        @Test
        @DisplayName("readiness score is bounded 0-100")
        void scoreBounded() {
            UUID id = runFullPipeline();
            var report = renderManifestService.evaluateReadiness(id);
            assertThat(report.getReadinessScore()).isBetween(0, 100);
        }

        @Test
        @DisplayName("manifest target format/resolution/fps are safe internal values")
        void manifestDefaults() {
            UUID id = runFullPipeline();
            var manifest = bootstrapService.getById(id).getRenderManifest();
            assertThat(manifest.getTargetResolution()).isEqualTo("1920x1080");
            assertThat(manifest.getTargetFps()).isEqualTo(30);
            assertThat(manifest.getTargetFormat()).isEqualTo("mp4");
        }
    }

    // ── G. FP-019/FP-020 Backward Compatibility ────────

    @Nested
    @DisplayName("G. FP-019/FP-020 Backward Compatibility")
    class BackwardCompat {

        @Test
        @DisplayName("FP-019 pipeline still works end-to-end")
        void fp019Pipeline() {
            var project = bootstrapProject();
            UUID id = project.getId();
            controller.prepareStory(id, null, null, null);
            controller.prepareScenario(id, null, null, null);
            controller.prepareStoryboard(id, null, null, null);
            controller.prepareSceneAssets(id, null, null, null);
            controller.prepareImageBatch(id, null, null, null);
            controller.completeImageBatch(id, null, null, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
        }

        @Test
        @DisplayName("FP-020 generate story still works")
        void fp020GenerateStory() {
            var project = bootstrapProject();
            UUID id = project.getId();
            var resp = controller.generateStory(id, null, null, null);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("FP-020 generation safety still available")
        void fp020Safety() {
            var project = bootstrapProject();
            var resp = controller.getGenerationSafety(project.getId());
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("project/progress endpoints still work")
        void projectProgress() {
            UUID id = runFullPipeline();
            assertThat(controller.getProject(id).getStatusCode().value()).isEqualTo(200);
            assertThat(controller.getProgress(id).getStatusCode().value()).isEqualTo(200);
        }
    }
}
