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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FP-019 Task 04: Scene Asset Plan + Image Batch Integration Tests.
 */
@DisplayName("FP-019 Task 04 — Scene Asset Plan + Image Batch")
class IcerikFabrikasiVideoAssetPlanningTest {

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

    private IcerikFabrikasiVideoProject createStoryboardedProject() {
        var req = new IcerikFabrikasiVideoBootstrapRequest();
        req.setIcerikFabrikasiName("Mihriban");
        req.setRegion("Erzurum");
        req.setSourceNotes("Aşık Sümmani'ye atfedilir");
        req.setMood("hüzünlü, özlem");
        req.setTargetDurationMinutes(4);
        req.setVisualStyle("suluboya");
        var project = bootstrapService.bootstrap(req, "admin");
        planningService.prepareStory(project.getId(), null);
        planningService.prepareScenario(project.getId(), null);
        planningService.prepareStoryboard(project.getId(), null);
        return bootstrapService.getById(project.getId());
    }

    // ── A. Scene Asset Plan ─────────────────────────────────

    @Nested
    @DisplayName("A. Scene Asset Plan Preparation")
    class SceneAssetPlan {

        @Test
        @DisplayName("prepare scene assets from STORYBOARD_PREPARED succeeds")
        void fromStoryboard() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            assertThat(plan).isNotNull();
            assertThat(plan.getItems()).isNotEmpty();
            assertThat(plan.getPlanId()).isNotNull();
        }

        @Test
        @DisplayName("scene asset items match storyboard scene count")
        void matchSceneCount() {
            var project = createStoryboardedProject();
            int sceneCount = project.getStoryboardPlan().getScenes().size();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            assertThat(plan.getItems()).hasSize(sceneCount);
        }

        @Test
        @DisplayName("each asset item has visual prompt summary")
        void hasPromptSummary() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            for (var item : plan.getItems()) {
                assertThat(item.getVisualPromptSummary()).isNotBlank();
                assertThat(item.getNegativePromptSafetyNotes()).isNotBlank();
            }
        }

        @Test
        @DisplayName("asset items preserve needsHumanReview=true")
        void humanReview() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            for (var item : plan.getItems()) {
                assertThat(item.isNeedsHumanReview()).isTrue();
                assertThat(item.isCopyrightReviewRequired()).isTrue();
            }
        }

        @Test
        @DisplayName("project advances to SCENE_ASSET_PLAN_PREPARED")
        void stageAdvanced() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var updated = bootstrapService.getById(project.getId());
            assertThat(updated.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED);
        }

        @Test
        @DisplayName("plan includes PUBLIC_DELIVERY_BLOCKED flag")
        void publicBlocked() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            assertThat(plan.getReviewFlags()).contains("PUBLIC_DELIVERY_BLOCKED");
        }

        @Test
        @DisplayName("plan includes HUMAN_REVIEW_REQUIRED flag")
        void humanFlag() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            assertThat(plan.getReviewFlags()).contains("HUMAN_REVIEW_REQUIRED");
        }
    }

    // ── B. Image Batch Preparation ──────────────────────────

    @Nested
    @DisplayName("B. Image Batch Preparation")
    class ImageBatch {

        @Test
        @DisplayName("prepare image batch after scene asset plan succeeds")
        void afterAssetPlan() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            assertThat(batch).isNotNull();
            assertThat(batch.getBatchId()).isNotNull();
            assertThat(batch.getStatus()).isEqualTo(IcerikFabrikasiImageBatchStatus.PREPARED);
        }

        @Test
        @DisplayName("image requests match scene asset count")
        void matchAssetCount() {
            var project = createStoryboardedProject();
            var plan = assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            assertThat(batch.getImageRequests()).hasSize(plan.getItems().size());
        }

        @Test
        @DisplayName("stub results generated with safe internal refs")
        void stubResults() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            assertThat(batch.getImageResults()).isNotEmpty();
            for (var result : batch.getImageResults()) {
                assertThat(result.getSafeInternalRef()).startsWith("internal-ref:");
                assertThat(result.isSuccess()).isTrue();
            }
        }

        @Test
        @DisplayName("project advances to IMAGE_BATCH_PREPARED")
        void stageAdvanced() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            assetService.prepareImageBatch(project.getId(), null);
            var updated = bootstrapService.getById(project.getId());
            assertThat(updated.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);
        }

        @Test
        @DisplayName("batch includes PUBLIC_DELIVERY_BLOCKED flag")
        void publicBlocked() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            assertThat(batch.getReviewFlags()).contains("PUBLIC_DELIVERY_BLOCKED");
        }
    }

    // ── C. Image Batch Completion ────────────────────────────

    @Nested
    @DisplayName("C. Image Batch Completion")
    class BatchCompletion {

        @Test
        @DisplayName("complete batch fully advances to IMAGE_BATCH_COMPLETED")
        void fullCompletion() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            assetService.prepareImageBatch(project.getId(), null);
            assetService.completeImageBatch(project.getId(), null);
            var updated = bootstrapService.getById(project.getId());
            assertThat(updated.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
            assertThat(updated.getImageBatch().getStatus()).isEqualTo(IcerikFabrikasiImageBatchStatus.COMPLETED);
        }

        @Test
        @DisplayName("partial completion advances to IMAGE_BATCH_PARTIAL")
        void partialCompletion() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            assetService.prepareImageBatch(project.getId(), null);
            var req = new IcerikFabrikasiImageBatchCompletionRequest();
            req.setPartial(true);
            assetService.completeImageBatch(project.getId(), req);
            var updated = bootstrapService.getById(project.getId());
            assertThat(updated.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL);
            assertThat(updated.getImageBatch().getStatus()).isEqualTo(IcerikFabrikasiImageBatchStatus.PARTIAL);
        }
    }

    // ── D. Stage Transition Enforcement ─────────────────────

    @Nested
    @DisplayName("D. Stage Transition Enforcement")
    class StageEnforcement {

        @Test
        @DisplayName("scene assets from DRAFT_CREATED rejected")
        void assetsFromDraft() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Test");
            var project = bootstrapService.bootstrap(req, "admin");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(project.getId(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class)
                    .hasMessageContaining("Geçersiz aşama");
        }

        @Test
        @DisplayName("image batch from STORYBOARD_PREPARED rejected")
        void batchFromStoryboard() {
            var project = createStoryboardedProject();
            assertThatThrownBy(() -> assetService.prepareImageBatch(project.getId(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("complete batch from SCENE_ASSET_PLAN_PREPARED rejected")
        void completionFromAssetPlan() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            assertThatThrownBy(() -> assetService.completeImageBatch(project.getId(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("unknown project ID safe error")
        void unknownProject() {
            assertThatThrownBy(() -> assetService.prepareSceneAssets(UUID.randomUUID(), null))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class)
                    .hasMessageContaining("bulunamadı");
        }
    }

    // ── E. Input Validation ─────────────────────────────────

    @Nested
    @DisplayName("E. Input Validation")
    class InputValidation {

        @Test
        @DisplayName("too long style hint rejected")
        void longStyle() {
            var project = createStoryboardedProject();
            var req = new IcerikFabrikasiSceneAssetPreparationRequest();
            req.setStyleOverrideHint("a".repeat(201));
            assertThatThrownBy(() -> assetService.prepareSceneAssets(project.getId(), req))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("script injection rejected")
        void scriptRejected() {
            var project = createStoryboardedProject();
            var req = new IcerikFabrikasiSceneAssetPreparationRequest();
            req.setStyleOverrideHint("<script>alert('xss')</script>");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(project.getId(), req))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("base64 image data rejected")
        void base64Rejected() {
            var project = createStoryboardedProject();
            var req = new IcerikFabrikasiSceneAssetPreparationRequest();
            req.setStyleOverrideHint("data:image/png;base64,iVBORw0KGgo");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(project.getId(), req))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }

        @Test
        @DisplayName("credential pattern rejected")
        void credentialRejected() {
            var project = createStoryboardedProject();
            var req = new IcerikFabrikasiSceneAssetPreparationRequest();
            req.setStyleOverrideHint("password=mysecret");
            assertThatThrownBy(() -> assetService.prepareSceneAssets(project.getId(), req))
                    .isInstanceOf(IcerikFabrikasiVideoProjectException.class);
        }
    }

    // ── F. No Forbidden Fields ──────────────────────────────

    @Nested
    @DisplayName("F. No Forbidden DTO Fields")
    class NoForbiddenFields {

        private static final List<String> FORBIDDEN = List.of(
                "token", "jwt", "session", "credential", "password",
                "secret", "apiKey", "accessKey", "principalRaw",
                "authorizationHeader", "publicUrl", "signedUrl",
                "cdnUrl", "providerUrl", "storageKey", "bucket",
                "objectPath", "rawPayload", "base64", "lyrics",
                "downloadUrl", "exportUrl", "imageData", "binaryData");

        @Test @DisplayName("IcerikFabrikasiSceneAssetItem safe")
        void assetItem() { checkFields(IcerikFabrikasiSceneAssetItem.class); }
        @Test @DisplayName("IcerikFabrikasiSceneAssetPlan safe")
        void assetPlan() { checkFields(IcerikFabrikasiSceneAssetPlan.class); }
        @Test @DisplayName("IcerikFabrikasiSceneImageRequest safe")
        void imageReq() { checkFields(IcerikFabrikasiSceneImageRequest.class); }
        @Test @DisplayName("IcerikFabrikasiSceneImageResult safe")
        void imageRes() { checkFields(IcerikFabrikasiSceneImageResult.class); }
        @Test @DisplayName("IcerikFabrikasiSceneImageBatch safe")
        void imageBatch() { checkFields(IcerikFabrikasiSceneImageBatch.class); }
        @Test @DisplayName("IcerikFabrikasiAssetPreparationResponse safe")
        void assetResp() { checkFields(IcerikFabrikasiAssetPreparationResponse.class); }
        @Test @DisplayName("IcerikFabrikasiImageBatchResponse safe")
        void batchResp() { checkFields(IcerikFabrikasiImageBatchResponse.class); }
        @Test @DisplayName("IcerikFabrikasiSceneAssetPreparationRequest safe")
        void assetReq() { checkFields(IcerikFabrikasiSceneAssetPreparationRequest.class); }
        @Test @DisplayName("IcerikFabrikasiImageBatchPreparationRequest safe")
        void batchPrepReq() { checkFields(IcerikFabrikasiImageBatchPreparationRequest.class); }
        @Test @DisplayName("IcerikFabrikasiImageBatchCompletionRequest safe")
        void completionReq() { checkFields(IcerikFabrikasiImageBatchCompletionRequest.class); }

        private void checkFields(Class<?> c) {
            Set<String> fields = Arrays.stream(c.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            for (String f : FORBIDDEN) {
                assertThat(fields).as("forbidden '%s' in %s", f, c.getSimpleName())
                        .doesNotContain(f);
            }
        }
    }

    // ── G. Safe Internal References ─────────────────────────

    @Nested
    @DisplayName("G. Safe Internal References")
    class SafeReferences {

        @Test
        @DisplayName("no base64 in internal refs")
        void noBase64() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            for (var r : batch.getImageResults()) {
                assertThat(r.getSafeInternalRef()).doesNotContain("base64");
                assertThat(r.getSafeInternalRef()).doesNotContain("data:image");
            }
        }

        @Test
        @DisplayName("no public URL in internal refs")
        void noPublicUrl() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            for (var r : batch.getImageResults()) {
                assertThat(r.getSafeInternalRef()).doesNotContain("https://");
                assertThat(r.getSafeInternalRef()).doesNotContain("http://");
            }
        }

        @Test
        @DisplayName("no storage key in internal refs")
        void noStorageKey() {
            var project = createStoryboardedProject();
            assetService.prepareSceneAssets(project.getId(), null);
            var batch = assetService.prepareImageBatch(project.getId(), null);
            for (var r : batch.getImageResults()) {
                assertThat(r.getSafeInternalRef()).doesNotContain("s3://");
                assertThat(r.getSafeInternalRef()).doesNotContain("gs://");
            }
        }
    }

    // ── H. Full Pipeline ────────────────────────────────────

    @Nested
    @DisplayName("H. Full Pipeline through Image Completion")
    class FullPipeline {

        @Test
        @DisplayName("DRAFT → ... → IMAGE_BATCH_COMPLETED full pipeline")
        void fullPipeline() {
            var req = new IcerikFabrikasiVideoBootstrapRequest();
            req.setIcerikFabrikasiName("Çanakkale");
            req.setRegion("Çanakkale");
            req.setMood("vatansever");
            req.setTargetDurationMinutes(5);
            var project = bootstrapService.bootstrap(req, "admin");
            UUID id = project.getId();

            planningService.prepareStory(id, null);
            planningService.prepareScenario(id, null);
            planningService.prepareStoryboard(id, null);
            assetService.prepareSceneAssets(id, null);
            assetService.prepareImageBatch(id, null);
            assetService.completeImageBatch(id, null);

            var updated = bootstrapService.getById(id);
            assertThat(updated.getWorkflowStage()).isEqualTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
            assertThat(updated.getSceneAssetPlan()).isNotNull();
            assertThat(updated.getImageBatch()).isNotNull();
            assertThat(updated.getImageBatch().getStatus()).isEqualTo(IcerikFabrikasiImageBatchStatus.COMPLETED);
        }
    }

    // ── I. Endpoint Inventory ───────────────────────────────

    @Nested
    @DisplayName("I. Endpoint Inventory (30 = 16 POST + 15 GET)")
    class EndpointInventory {

        @Test
        @DisplayName("IcerikFabrikasiVideoProjectController: 17 POST + 8 GET")
        void icerikFabrikasiController() {
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, PostMapping.class)).isEqualTo(17);
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, GetMapping.class)).isEqualTo(8);
        }

        @Test
        @DisplayName("total 46 = 26 POST + 20 GET")
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
            assertThat(post + get).as("total").isEqualTo(46);
        }

        @Test
        @DisplayName("no PUT/DELETE")
        void noPutDelete() {
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, PutMapping.class)).isZero();
            assertThat(countMethods(IcerikFabrikasiVideoProjectController.class, DeleteMapping.class)).isZero();
        }

        private long countMethods(Class<?> c, Class<? extends java.lang.annotation.Annotation> ann) {
            return Arrays.stream(c.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(ann)).count();
        }
    }

    // ── J. Controller Dependencies ──────────────────────────

    @Nested
    @DisplayName("J. Controller Dependencies")
    class ControllerDeps {

        @Test
        @DisplayName("controller has IcerikFabrikasiVideoAssetPlanningService")
        void hasAssetService() {
            assertThat(Arrays.stream(IcerikFabrikasiVideoProjectController.class.getDeclaredFields())
                    .map(Field::getType).collect(Collectors.toSet()))
                    .contains(IcerikFabrikasiVideoAssetPlanningService.class);
        }
    }

    // ── K. Batch Status Enum ────────────────────────────────

    @Nested
    @DisplayName("K. Batch Status Enum")
    class BatchStatusEnum {

        @Test
        @DisplayName("has exactly 5 values")
        void fiveValues() {
            assertThat(IcerikFabrikasiImageBatchStatus.values()).hasSize(5);
        }

        @Test
        @DisplayName("contains expected statuses")
        void expectedValues() {
            Set<String> names = Arrays.stream(IcerikFabrikasiImageBatchStatus.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).containsExactlyInAnyOrder(
                    "PREPARED", "PARTIAL", "COMPLETED", "BLOCKED", "FAILED");
        }

        @Test
        @DisplayName("no AUTO_APPROVED or PUBLISHED status")
        void noForbidden() {
            Set<String> names = Arrays.stream(IcerikFabrikasiImageBatchStatus.values())
                    .map(Enum::name).collect(Collectors.toSet());
            assertThat(names).doesNotContain("AUTO_APPROVED", "PUBLISHED", "EXPORTED");
        }
    }
}
