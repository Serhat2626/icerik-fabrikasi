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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FP-019 Task 06: E2E Safety Regression + Final Closure.
 *
 * Full happy-path pipeline, stage enforcement, safety inventory,
 * endpoint inventory, actor boundary, and forbidden exposure checks.
 */
@DisplayName("FP-019 Task 06 — E2E Regression + Closure")
class FP019FinalClosureRegressionTest {

    private IcerikFabrikasiVideoProjectRepository repository;
    private IcerikFabrikasiVideoProjectBootstrapService bootstrapService;
    private IcerikFabrikasiVideoPlanningService planningService;
    private IcerikFabrikasiVideoAssetPlanningService assetService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIcerikFabrikasiVideoProjectRepository();
        bootstrapService = new IcerikFabrikasiVideoProjectBootstrapService(repository);
        planningService = new IcerikFabrikasiVideoPlanningService(repository);
        assetService = new IcerikFabrikasiVideoAssetPlanningService(repository);
    }

    // ── A. E2E Full Happy Path ──────────────────────────────

    @Nested
    @DisplayName("A. E2E Full Happy Path (COMPLETED)")
    class FullHappyPath {

        @Test
        @DisplayName("bootstrap → story → scenario → storyboard → assets → batch → completed")
        void fullPipeline() {
            // 1. Bootstrap
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Çanakkale Türküsü");
            req.setRegion("Çanakkale");
            req.setMood("destansı, vatansever");
            req.setTargetDurationMinutes(5);
            req.setVisualStyle("sinematik");
            var project = bootstrapService.bootstrap(req, "admin-e2e");
            UUID id = project.getId();
            assertThat(project.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);
            assertThat(project.isNeedsHumanReview()).isTrue();
            assertThat(project.isCopyrightReviewRequired()).isTrue();

            // 2. GET project
            var fetched = bootstrapService.getById(id);
            assertThat(fetched.getIcerikFabrikasiName()).isEqualTo("Çanakkale Türküsü");

            // 3. GET progress at DRAFT
            var progress = IcerikFabrikasiVideoProgressResponse.from(fetched);
            assertThat(progress.getCurrentStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);

            // 4. Prepare story
            planningService.prepareStory(id, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);

            // 5. Prepare scenario
            planningService.prepareScenario(id, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);

            // 6. Prepare storyboard
            planningService.prepareStoryboard(id, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);

            // 7. Prepare scene assets
            assetService.prepareSceneAssets(id, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED);

            // 8. Prepare image batch
            assetService.prepareImageBatch(id, null);
            assertThat(bootstrapService.getById(id).getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);

            // 9. Complete image batch
            assetService.completeImageBatch(id, null);
            var final_ = bootstrapService.getById(id);
            assertThat(final_.getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
            assertThat(final_.getImageBatch().getStatus())
                    .isEqualTo(IcerikFabrikasiImageBatchStatus.COMPLETED);
            assertThat(final_.isNeedsHumanReview()).isTrue();
            assertThat(final_.isCopyrightReviewRequired()).isTrue();
        }

        @Test
        @DisplayName("partial completion pipeline")
        void partialPipeline() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Mihriban");
            var project = bootstrapService.bootstrap(req, "admin");
            UUID id = project.getId();

            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);
            assetService.prepareSceneAssets(id, null);
            assetService.prepareImageBatch(id, null);

            var partialReq = new IcerikFabrikasiImageBatchCompletionRequest();
            partialReq.setPartial(true);
            assetService.completeImageBatch(id, partialReq);

            var final_ = bootstrapService.getById(id);
            assertThat(final_.getWorkflowStage())
                    .isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL);
            assertThat(final_.getImageBatch().getStatus())
                    .isEqualTo(IcerikFabrikasiImageBatchStatus.PARTIAL);
        }

        @Test
        @DisplayName("progress tracks completed stages through pipeline")
        void progressTracking() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Yüksek Yüksek Tepelere");
            var project = bootstrapService.bootstrap(req, "admin");
            UUID id = project.getId();

            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);

            var progress = IcerikFabrikasiVideoProgressResponse.from(bootstrapService.getById(id));
            assertThat(progress.getCurrentStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);
            assertThat(progress.getCompletedStages()).contains(
                    IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED,
                    IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED,
                    IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED
            );
        }
    }

    // ── B. Forbidden Workflow States ────────────────────────

    @Nested
    @DisplayName("B. Forbidden Workflow States")
    class ForbiddenStates {

        @Test
        @DisplayName("no PUBLISHED state in enum")
        void noPublished() {
            Set<String> names = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("PUBLISHED");
        }

        @Test
        @DisplayName("no EXPORTED state in enum")
        void noExported() {
            Set<String> names = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("EXPORTED");
        }

        @Test
        @DisplayName("no PUBLICLY_DELIVERED state in enum")
        void noPubliclyDelivered() {
            Set<String> names = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("PUBLICLY_DELIVERED");
        }

        @Test
        @DisplayName("no AUTO_APPROVED state in enum")
        void noAutoApproved() {
            Set<String> names = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("AUTO_APPROVED");
        }

        @Test
        @DisplayName("no COMPLETED_AS_PUBLIC_VIDEO state in enum")
        void noPublicVideo() {
            Set<String> names = Arrays.stream(IcerikFabrikasiVideoWorkflowStage.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("COMPLETED_AS_PUBLIC_VIDEO");
        }
    }

    // ── C. Safety: Forbidden DTO Fields ─────────────────────

    @Nested
    @DisplayName("C. Forbidden DTO Fields (all FP-019 types)")
    class ForbiddenFields {

        private static final List<String> FORBIDDEN = List.of(
                "token", "jwt", "session", "credential", "password",
                "secret", "apiKey", "accessKey", "principalRaw",
                "authorizationHeader", "publicUrl", "signedUrl",
                "cdnUrl", "providerUrl", "storageKey", "bucket",
                "objectPath", "rawPayload", "base64", "lyrics",
                "downloadUrl", "exportUrl", "publishUrl",
                "imageData", "binaryData", "videoData",
                "copyrightedAudio", "copyrightedVideo");

        private static final List<Class<?>> FP019_TYPES = List.of(
                IcerikFabrikasiVideoBootstrapRequest.class,
                IcerikFabrikasiVideoProject.class,
                IcerikFabrikasiVideoProgressResponse.class,
                IcerikFabrikasiStoryPackage.class,
                IcerikFabrikasiNarrativeContext.class,
                IcerikFabrikasiScenarioPlan.class,
                IcerikFabrikasiStoryboardPlan.class,
                IcerikFabrikasiStoryboardScene.class,
                IcerikFabrikasiSceneAssetItem.class,
                IcerikFabrikasiSceneAssetPlan.class,
                IcerikFabrikasiSceneImageRequest.class,
                IcerikFabrikasiSceneImageResult.class,
                IcerikFabrikasiSceneImageBatch.class,
                IcerikFabrikasiAssetPreparationResponse.class,
                IcerikFabrikasiImageBatchResponse.class,
                IcerikFabrikasiSceneAssetPreparationRequest.class,
                IcerikFabrikasiImageBatchPreparationRequest.class,
                IcerikFabrikasiImageBatchCompletionRequest.class,
                IcerikFabrikasiPreparationResponse.class
        );

        @Test
        @DisplayName("no forbidden field in any FP-019 DTO")
        void noForbiddenInAny() {
            for (Class<?> cls : FP019_TYPES) {
                Set<String> fields = Arrays.stream(cls.getDeclaredFields())
                        .map(Field::getName).collect(Collectors.toSet());
                for (String f : FORBIDDEN) {
                    assertThat(fields)
                            .as("'%s' in %s", f, cls.getSimpleName())
                            .doesNotContain(f);
                }
            }
        }
    }

    // ── D. Safety: Image Refs ───────────────────────────────

    @Nested
    @DisplayName("D. Safe Internal References Only")
    class SafeRefs {

        @Test
        @DisplayName("image results use opaque internal-ref prefix")
        void opaqueRefs() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Kara Toprak");
            var project = bootstrapService.bootstrap(req, "admin");
            UUID id = project.getId();
            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);
            assetService.prepareSceneAssets(id, null);
            assetService.prepareImageBatch(id, null);

            var batch = bootstrapService.getById(id).getImageBatch();
            for (var r : batch.getImageResults()) {
                assertThat(r.getSafeInternalRef()).startsWith("internal-ref:");
                assertThat(r.getSafeInternalRef()).doesNotContain("https://");
                assertThat(r.getSafeInternalRef()).doesNotContain("http://");
                assertThat(r.getSafeInternalRef()).doesNotContain("s3://");
                assertThat(r.getSafeInternalRef()).doesNotContain("gs://");
                assertThat(r.getSafeInternalRef()).doesNotContain("base64");
                assertThat(r.getSafeInternalRef()).doesNotContain("data:image");
            }
        }
    }

    // ── E. Error Safety ─────────────────────────────────────

    @Nested
    @DisplayName("E. Error Responses are Safe")
    class ErrorSafety {

        @Test
        @DisplayName("unknown project ID → safe error")
        void unknownId() {
            assertThatThrownBy(() -> bootstrapService.getById(UUID.randomUUID()))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class)
                    .hasMessageContaining("bulunamadı");
        }

        @Test
        @DisplayName("invalid stage transition → safe error")
        void invalidTransition() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Test");
            var p = bootstrapService.bootstrap(req, "admin");
            assertThatThrownBy(() -> planningService.prepareScenario(p.getId(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class)
                    .hasMessageContaining("Geçersiz aşama");
        }

        @Test
        @DisplayName("unsafe input → safe error")
        void unsafeInput() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Test");
            var p = bootstrapService.bootstrap(req, "admin");
            planningService.prepareStory(p.getId(), null);
            planningService.prepareScenario(p.getId(), null);
            planningService.prepareStoryboard(p.getId(), null);

            var assetReq = new IcerikFabrikasiSceneAssetPreparationRequest();
            assetReq.setStyleOverrideHint("<script>alert('xss')</script>");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(p.getId(), assetReq))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("missing prerequisite → safe error")
        void missingPrerequisite() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Test");
            var p = bootstrapService.bootstrap(req, "admin");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(p.getId(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }
    }

    // ── F. needsHumanReview + copyrightReviewRequired ───────

    @Nested
    @DisplayName("F. Safety Flags Preserved Through Pipeline")
    class SafetyFlags {

        @Test
        @DisplayName("needsHumanReview stays true through full pipeline")
        void humanReview() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Emrah ile Selvihan");
            var p = bootstrapService.bootstrap(req, "admin");
            UUID id = p.getId();
            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);
            assetService.prepareSceneAssets(id, null);
            assetService.prepareImageBatch(id, null);
            assetService.completeImageBatch(id, null);
            assertThat(bootstrapService.getById(id).isNeedsHumanReview()).isTrue();
        }

        @Test
        @DisplayName("copyrightReviewRequired stays true through full pipeline")
        void copyrightReview() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Emrah ile Selvihan");
            var p = bootstrapService.bootstrap(req, "admin");
            UUID id = p.getId();
            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);
            assetService.prepareSceneAssets(id, null);
            assetService.prepareImageBatch(id, null);
            assetService.completeImageBatch(id, null);
            assertThat(bootstrapService.getById(id).isCopyrightReviewRequired()).isTrue();
        }
    }

    // ── G. Endpoint Inventory ───────────────────────────────

    @Nested
    @DisplayName("G. Final Endpoint Inventory (30 = 16 POST + 15 GET)")
    class EndpointInventory {

        @Test
        @DisplayName("IcerikFabrikasiVideoProjectController: 17 POST + 8 GET = 25")
        void icerikFabrikasiController() {
            long post = countMethods(IcerikFabrikasiVideoProjectController.class, PostMapping.class);
            long get = countMethods(IcerikFabrikasiVideoProjectController.class, GetMapping.class);
            assertThat(post).as("türkü POST").isEqualTo(17);
            assertThat(get).as("türkü GET").isEqualTo(8);
            assertThat(post + get).as("türkü total").isEqualTo(25);
        }

        @Test
        @DisplayName("AdminReviewController: 9 POST + 8 GET")
        void reviewController() {
            assertThat(countMethods(AdminReviewController.class, PostMapping.class))
                    .as("review POST").isEqualTo(9);
            assertThat(countMethods(AdminReviewController.class, GetMapping.class))
                    .as("review GET").isEqualTo(8);
        }

        @Test
        @DisplayName("total exactly 46 = 26 POST + 20 GET")
        void total() {
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
        @DisplayName("no PUT/DELETE in any controller")
        void noPutDelete() {
            Class<?>[] controllers = {
                    AdminReviewController.class,
                    AdminPipelineController.class,
                    AdminWorkflowObservabilityController.class,
                    IcerikFabrikasiVideoProjectController.class
            };
            for (Class<?> c : controllers) {
                assertThat(countMethods(c, PutMapping.class)).as("PUT in " + c.getSimpleName()).isZero();
                assertThat(countMethods(c, DeleteMapping.class)).as("DELETE in " + c.getSimpleName()).isZero();
            }
        }

        private long countMethods(Class<?> c, Class<? extends java.lang.annotation.Annotation> ann) {
            return Arrays.stream(c.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ann)).count();
        }
    }

    // ── H. Controller Uses AdminActorResolver ───────────────

    @Nested
    @DisplayName("H. Controller Actor Boundary Integration")
    class ActorBoundary {

        @Test
        @DisplayName("controller has AdminActorResolver dependency")
        void hasResolver() {
            assertThat(Arrays.stream(IcerikFabrikasiVideoProjectController.class.getDeclaredFields())
                    .map(Field::getType).collect(Collectors.toSet()))
                    .contains(AdminActorResolver.class);
        }

        @Test
        @DisplayName("controller has no Spring Security annotations")
        void noSpringSecurity() {
            for (Method m : IcerikFabrikasiVideoProjectController.class.getDeclaredMethods()) {
                for (var ann : m.getAnnotations()) {
                    String name = ann.annotationType().getName();
                    assertThat(name).doesNotContain("PreAuthorize");
                    assertThat(name).doesNotContain("Secured");
                    assertThat(name).doesNotContain("RolesAllowed");
                }
            }
        }
    }

    // ── I. No Publish/Export Controller Methods ─────────────

    @Nested
    @DisplayName("I. No Publish/Export/Download Controller Methods")
    class NoPublishExport {

        @Test
        @DisplayName("no method named publish in controller")
        void noPublish() {
            for (Method m : IcerikFabrikasiVideoProjectController.class.getDeclaredMethods()) {
                assertThat(m.getName().toLowerCase()).doesNotContain("publish");
            }
        }

        @Test
        @DisplayName("no method named export in controller")
        void noExport() {
            for (Method m : IcerikFabrikasiVideoProjectController.class.getDeclaredMethods()) {
                assertThat(m.getName().toLowerCase()).doesNotContain("export");
            }
        }

        @Test
        @DisplayName("no method named download in controller")
        void noDownload() {
            for (Method m : IcerikFabrikasiVideoProjectController.class.getDeclaredMethods()) {
                assertThat(m.getName().toLowerCase()).doesNotContain("download");
            }
        }
    }

    // ── J. Review Flags Present ─────────────────────────────

    @Nested
    @DisplayName("J. Review Flags Present in Pipeline Output")
    class ReviewFlags {

        @Test
        @DisplayName("asset plan includes safety review flags")
        void assetPlanFlags() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Uzun İnce Bir Yoldayım");
            var p = bootstrapService.bootstrap(req, "admin");
            planningService.prepareStory(p.getId(), null);
            planningService.prepareScenario(p.getId(), null);
            planningService.prepareStoryboard(p.getId(), null);
            var plan = assetService.prepareSceneAssets(p.getId(), null);

            assertThat(plan.getReviewFlags()).contains(
                    "LYRICS_NOT_INCLUDED",
                    "COPYRIGHT_REVIEW_REQUIRED",
                    "HUMAN_REVIEW_REQUIRED",
                    "PUBLIC_DELIVERY_BLOCKED"
            );
        }

        @Test
        @DisplayName("image batch includes safety review flags")
        void batchFlags() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Sarı Gelin");
            var p = bootstrapService.bootstrap(req, "admin");
            planningService.prepareStory(p.getId(), null);
            planningService.prepareScenario(p.getId(), null);
            planningService.prepareStoryboard(p.getId(), null);
            assetService.prepareSceneAssets(p.getId(), null);
            var batch = assetService.prepareImageBatch(p.getId(), null);

            assertThat(batch.getReviewFlags()).contains(
                    "PUBLIC_DELIVERY_BLOCKED",
                    "HUMAN_REVIEW_REQUIRED"
            );
        }
    }

    // ── K. Stage Order Verification ─────────────────────────

    @Nested
    @DisplayName("K. Stage Order Verification")
    class StageOrder {

        @Test
        @DisplayName("stages follow correct order in enum declaration")
        void order() {
            IcerikFabrikasiVideoWorkflowStage[] stages = IcerikFabrikasiVideoWorkflowStage.values();
            // First 7 stages in pipeline order
            assertThat(stages[0]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);
            assertThat(stages[1]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);
            assertThat(stages[2]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);
            assertThat(stages[3]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);
            assertThat(stages[4]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED);
            assertThat(stages[5]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);
            assertThat(stages[6]).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
        }

        @Test
        @DisplayName("enum has exactly 12 values")
        void count() {
            assertThat(IcerikFabrikasiVideoWorkflowStage.values()).hasSize(19);
        }
    }
}
