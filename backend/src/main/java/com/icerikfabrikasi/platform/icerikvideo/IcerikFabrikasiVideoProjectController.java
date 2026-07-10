package com.icerikfabrikasi.platform.icerikvideo;

import com.icerikfabrikasi.platform.common.api.ApiResponse;
import com.icerikfabrikasi.platform.review.AdminActorBoundaryPolicy;
import com.icerikfabrikasi.platform.review.AdminActorResolutionException;
import com.icerikfabrikasi.platform.review.AdminActorResolutionRequest;
import com.icerikfabrikasi.platform.review.AdminActorResolver;
import com.icerikfabrikasi.platform.review.ResolvedAdminActor;
import com.icerikfabrikasi.platform.review.ReviewActorRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * FP-019 Task 02: Admin controller for Türkü video project bootstrap.
 *
 * <p>Adds exactly 3 endpoints:
 * <ul>
 *   <li>POST /admin/icerikFabrikasi-video-projects/bootstrap — create project</li>
 *   <li>GET /admin/icerikFabrikasi-video-projects/{id} — get project</li>
 *   <li>GET /admin/icerikFabrikasi-video-projects/{id}/progress — get progress</li>
 * </ul>
 *
 * <p>POST routes through AdminActorResolver / AdminActorBoundaryPolicy.
 * actorRole is TEST_FALLBACK compatibility input only, NOT production auth.
 * No publish/export/public delivery. No AI generation. No unsafe fields.</p>
 */
@RestController
@RequestMapping("/api/admin/icerikFabrikasi-video-projects")
@Tag(name = "Türkü Video Projects", description = "Türkü video proje yönetimi (dahili/admin)")
public class IcerikFabrikasiVideoProjectController {

    private final IcerikFabrikasiVideoProjectBootstrapService bootstrapService;
    private final IcerikFabrikasiVideoPlanningService planningService;
    private final IcerikFabrikasiVideoAssetPlanningService assetPlanningService;
    private final IcerikFabrikasiProviderDraftPlanningService providerDraftService;
    private final IcerikFabrikasiProviderImageBatchService providerImageBatchService;
    private final IcerikFabrikasiVideoCompositionService compositionService;
    private final IcerikFabrikasiRenderManifestService renderManifestService;
    private final IcerikFabrikasiRenderJobService renderJobService;
    private final IcerikFabrikasiRenderExecutionService renderExecutionService;
    private final IcerikFabrikasiStoryboardInternalRenderChainHarness storyboardChainHarness;
    private final AdminActorResolver actorResolver;

    public IcerikFabrikasiVideoProjectController(IcerikFabrikasiVideoProjectBootstrapService bootstrapService,
                                        IcerikFabrikasiVideoPlanningService planningService,
                                        IcerikFabrikasiVideoAssetPlanningService assetPlanningService,
                                        IcerikFabrikasiProviderDraftPlanningService providerDraftService,
                                        IcerikFabrikasiProviderImageBatchService providerImageBatchService,
                                        IcerikFabrikasiVideoCompositionService compositionService,
                                        IcerikFabrikasiRenderManifestService renderManifestService,
                                        IcerikFabrikasiRenderJobService renderJobService,
                                        IcerikFabrikasiRenderExecutionService renderExecutionService,
                                        IcerikFabrikasiStoryboardInternalRenderChainHarness storyboardChainHarness,
                                        AdminActorResolver actorResolver) {
        this.bootstrapService = bootstrapService;
        this.planningService = planningService;
        this.assetPlanningService = assetPlanningService;
        this.providerDraftService = providerDraftService;
        this.providerImageBatchService = providerImageBatchService;
        this.compositionService = compositionService;
        this.renderManifestService = renderManifestService;
        this.renderJobService = renderJobService;
        this.renderExecutionService = renderExecutionService;
        this.storyboardChainHarness = storyboardChainHarness;
        this.actorResolver = actorResolver;
    }

    // ── POST /admin/icerikFabrikasi-video-projects/bootstrap ──

    @PostMapping("/bootstrap")
    @Operation(summary = "Yeni türkü video projesi oluştur",
               description = "Türkü adı ile yeni bir dahili video projesi başlatır. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiVideoBootstrapResponse>> bootstrap(
            @RequestBody IcerikFabrikasiVideoBootstrapRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        // ── Actor resolution via FP-018 pattern ──
        ResolvedAdminActor actor = resolveBootstrapActor(actorRole, actorReference);

        // ── Bootstrap project ──
        IcerikFabrikasiVideoProject project = bootstrapService.bootstrap(request, actor.getActorReference());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(IcerikFabrikasiVideoBootstrapResponse.from(project),
                        "Türkü video projesi başarıyla oluşturuldu"));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id} ──

    @GetMapping("/{id}")
    @Operation(summary = "Türkü video projesi detayı",
               description = "Güvenli proje bilgilerini döndürür. Yayın/dışa aktarım verisi yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiVideoProjectResponse>> getProject(
            @PathVariable UUID id) {

        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(IcerikFabrikasiVideoProjectResponse.from(project)));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/progress ──

    @GetMapping("/{id}/progress")
    @Operation(summary = "Türkü video projesi ilerleme durumu",
               description = "Güvenli aşama ve ilerleme bilgisi döndürür.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiVideoProgressResponse>> getProgress(
            @PathVariable UUID id) {

        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(IcerikFabrikasiVideoProgressResponse.from(project)));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/generation-safety ──

    @GetMapping("/{id}/generation-safety")
    @Operation(summary = "Üretim güvenlik durumu",
               description = "Provider modu, üretim uygunluğu ve güvenlik bayraklarını döndürür.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiGenerationSafetyResponse>> getGenerationSafety(
            @PathVariable UUID id) {

        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);
        IcerikFabrikasiProviderMode mode = project.getProviderMode();
        IcerikFabrikasiVideoWorkflowStage stage = project.getWorkflowStage();

        boolean blocked = mode == IcerikFabrikasiProviderMode.PROVIDER_DISABLED
                || mode == IcerikFabrikasiProviderMode.PROVIDER_FAILED_SAFE
                || mode == IcerikFabrikasiProviderMode.HUMAN_REVIEW_REQUIRED;

        String blockedReason = blocked ? switch (mode) {
            case PROVIDER_DISABLED -> "Sağlayıcı devre dışı";
            case PROVIDER_FAILED_SAFE -> "Sağlayıcı güvenli hata modunda";
            case HUMAN_REVIEW_REQUIRED -> "İnsan incelemesi bekleniyor";
            default -> null;
        } : null;

        IcerikFabrikasiGenerationSafetyResponse resp = IcerikFabrikasiGenerationSafetyResponse.builder()
                .projectId(project.getId())
                .providerMode(mode)
                .currentStage(stage)
                .generationAvailable(!blocked)
                .blockedReasonSummary(blockedReason)
                .humanReviewRequired(true)
                .autoApproved(false)
                .copyrightReviewRequired(true)
                .lyricsIncluded(false)
                .canGenerateStory(!blocked && stage == IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED)
                .canGenerateScenario(!blocked && stage == IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED)
                .canGenerateStoryboard(!blocked && stage == IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED)
                .canGeneratePlanningBundle(!blocked && stage == IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED)
                .canRunImageBatch(!blocked && stage == IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED)
                .safeFailureCode(blocked ? mode.name() : null)
                .reviewFlags(java.util.List.of("HUMAN_REVIEW_REQUIRED",
                        "COPYRIGHT_REVIEW_REQUIRED", "LYRICS_NOT_INCLUDED"))
                .safeWarnings(blocked
                        ? java.util.List.of("Üretim engellendi: " + mode)
                        : java.util.List.of())
                .build();

        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-story ──

    @PostMapping("/{id}/prepare-story")
    @Operation(summary = "Hikâye paketi hazırla",
               description = "Türkü projesi için hikâye/kültürel bağlam paketi hazırlar.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiPreparationResponse>> prepareStory(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiStoryPreparationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiStoryPackage story = planningService.prepareStory(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiPreparationResponse(id, project.getWorkflowStage(),
                        "Hikâye paketi başarıyla hazırlandı",
                        story.getSourceConfidence(), story.isNeedsHumanReview(),
                        story.isCopyrightReviewRequired(), story.getReviewFlags()),
                "Hikâye paketi hazır"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-scenario ──

    @PostMapping("/{id}/prepare-scenario")
    @Operation(summary = "Senaryo planı hazırla",
               description = "Hikâye paketine dayalı senaryo planı hazırlar.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiPreparationResponse>> prepareScenario(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiScenarioPreparationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiScenarioPlan scenario = planningService.prepareScenario(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiPreparationResponse(id, project.getWorkflowStage(),
                        "Senaryo planı başarıyla hazırlandı",
                        project.getSourceConfidence(), project.isNeedsHumanReview(),
                        project.isCopyrightReviewRequired(), scenario.getReviewFlags()),
                "Senaryo planı hazır"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-storyboard ──

    @PostMapping("/{id}/prepare-storyboard")
    @Operation(summary = "Storyboard planı hazırla",
               description = "Senaryoya dayalı sahne bazlı storyboard planı hazırlar.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiPreparationResponse>> prepareStoryboard(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiStoryboardPreparationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiStoryboardPlan storyboard = planningService.prepareStoryboard(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiPreparationResponse(id, project.getWorkflowStage(),
                        "Storyboard planı başarıyla hazırlandı (" + storyboard.getScenes().size() + " sahne)",
                        project.getSourceConfidence(), project.isNeedsHumanReview(),
                        project.isCopyrightReviewRequired(), storyboard.getReviewFlags()),
                "Storyboard hazır"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-scene-assets ──

    @PostMapping("/{id}/prepare-scene-assets")
    @Operation(summary = "Sahne varlık planı hazırla",
               description = "Storyboard sahnelerine dayalı görsel varlık planı hazırlar.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiAssetPreparationResponse>> prepareSceneAssets(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiSceneAssetPreparationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiSceneAssetPlan plan = assetPlanningService.prepareSceneAssets(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiAssetPreparationResponse(id, project.getWorkflowStage(),
                        "Sahne varlık planı hazırlandı (" + plan.getItems().size() + " varlık)",
                        plan.getItems().size(), project.isNeedsHumanReview(),
                        project.isCopyrightReviewRequired(), plan.getReviewFlags()),
                "Varlık planı hazır"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-image-batch ──

    @PostMapping("/{id}/prepare-image-batch")
    @Operation(summary = "Görsel üretim batch'i hazırla",
               description = "Sahne varlıklarına dayalı görsel üretim batch'i hazırlar.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiImageBatchResponse>> prepareImageBatch(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiImageBatchPreparationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiSceneImageBatch batch = assetPlanningService.prepareImageBatch(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiImageBatchResponse(id, batch.getBatchId(),
                        batch.getStatus(), project.getWorkflowStage(),
                        "Görsel üretim batch'i hazırlandı",
                        batch.getImageRequests().size(), batch.getImageResults().size(),
                        project.isNeedsHumanReview(), project.isCopyrightReviewRequired(),
                        batch.getReviewFlags()),
                "Batch hazır"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/complete-image-batch ──

    @PostMapping("/{id}/complete-image-batch")
    @Operation(summary = "Görsel batch'ini tamamla/kısmi işaretle",
               description = "Görsel üretim batch'ini tamamlandı veya kısmi olarak işaretler.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiImageBatchResponse>> completeImageBatch(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiImageBatchCompletionRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiSceneImageBatch batch = assetPlanningService.completeImageBatch(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                new IcerikFabrikasiImageBatchResponse(id, batch.getBatchId(),
                        batch.getStatus(), project.getWorkflowStage(),
                        "Batch durumu güncellendi: " + batch.getStatus(),
                        batch.getImageRequests().size(), batch.getImageResults().size(),
                        project.isNeedsHumanReview(), project.isCopyrightReviewRequired(),
                        batch.getReviewFlags()),
                "Batch güncellendi"));
    }

    // ── FP-020 Task 03: Provider-Draft Generation Endpoints ──

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/generate-story ──

    @PostMapping("/{id}/generate-story")
    @Operation(summary = "Sağlayıcı ile hikâye üret",
               description = "Provider-draft yolu ile hikâye paketi üretir. Güvenlik zarfı ile sarılır.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiProviderPlanningGenerationResponse>> generateStory(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiProviderGenerationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiProviderPlanningGenerationResponse response = providerDraftService.generateStory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hikâye üretimi tamamlandı"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/generate-scenario ──

    @PostMapping("/{id}/generate-scenario")
    @Operation(summary = "Sağlayıcı ile senaryo üret",
               description = "Provider-draft yolu ile senaryo planı üretir. Güvenlik zarfı ile sarılır.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiProviderPlanningGenerationResponse>> generateScenario(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiProviderGenerationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiProviderPlanningGenerationResponse response = providerDraftService.generateScenario(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Senaryo üretimi tamamlandı"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/generate-storyboard ──

    @PostMapping("/{id}/generate-storyboard")
    @Operation(summary = "Sağlayıcı ile storyboard üret",
               description = "Provider-draft yolu ile storyboard planı üretir. Güvenlik zarfı ile sarılır.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiProviderPlanningGenerationResponse>> generateStoryboard(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiProviderGenerationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiProviderPlanningGenerationResponse response = providerDraftService.generateStoryboard(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Storyboard üretimi tamamlandı"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/generate-planning-bundle ──

    @PostMapping("/{id}/generate-planning-bundle")
    @Operation(summary = "Sağlayıcı ile planlama paketi üret",
               description = "Provider-draft yolu ile hikâye+senaryo+storyboard paketini sıralı üretir.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiPlanningBundleGenerationResponse>> generatePlanningBundle(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiProviderGenerationRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiPlanningBundleGenerationResponse response = providerDraftService.generatePlanningBundle(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Planlama paketi üretimi tamamlandı"));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/run-image-batch ──

    @PostMapping("/{id}/run-image-batch")
    @Operation(summary = "Sağlayıcı ile görsel batch çalıştır",
               description = "Provider-draft yolu ile sahne görsellerini batch olarak üretir. Güvenlik zarfı ile sarılır.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiImageBatchRunResponse>> runImageBatch(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiImageBatchRunRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiImageBatchRunResponse response = providerImageBatchService.runImageBatch(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Görsel batch çalıştırıldı"));
    }

    // ── FP-021 Task 02: Composition Plan Endpoint ──

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-composition-plan ──

    @PostMapping("/{id}/prepare-composition-plan")
    @Operation(summary = "Video kompozisyon planı hazırla",
               description = "Storyboard + sahne görselleri + anlatım planından dahili video kompozisyon planı oluşturur.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiCompositionPlanResponse>> prepareCompositionPlan(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiCompositionPlanRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiVideoCompositionPlan plan = compositionService.prepareCompositionPlan(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        IcerikFabrikasiCompositionPlanResponse response = new IcerikFabrikasiCompositionPlanResponse(
                project.getId(), plan.getCompositionId(),
                project.getWorkflowStage(),
                plan.getScenes().size(), plan.getTotalDurationSeconds(),
                plan.getTimingPlan().isHasTimingConflict(),
                plan.isHumanReviewRequired(), plan.isCopyrightReviewRequired(),
                plan.getReviewFlags(),
                "Kompozisyon planı hazırlandı: " + plan.getScenes().size() + " sahne");

        return ResponseEntity.ok(ApiResponse.success(response, "Kompozisyon planı hazırlandı"));
    }

    // ── FP-021 Task 03: Render Manifest + Readiness Endpoints ──

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-render-manifest ──

    @PostMapping("/{id}/prepare-render-manifest")
    @Operation(summary = "Render manifest hazırla",
               description = "Kompozisyon planından dahili render manifest oluşturur. Gerçek video üretimi yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderManifestResponse>> prepareRenderManifest(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiRenderManifestRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {

        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiRenderManifest manifest = renderManifestService.prepareRenderManifest(id, request);
        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);

        IcerikFabrikasiRenderManifestResponse response = new IcerikFabrikasiRenderManifestResponse(
                project.getId(), manifest.getManifestId(),
                project.getWorkflowStage(),
                manifest.getScenes().size(), manifest.getTotalDurationSeconds(),
                manifest.getTargetResolution(), manifest.getTargetFps(),
                manifest.getTargetFormat(),
                manifest.isHumanReviewRequired(), manifest.isCopyrightReviewRequired(),
                manifest.getReviewFlags(),
                "Render manifest hazırlandı: " + manifest.getScenes().size() + " sahne");

        return ResponseEntity.ok(ApiResponse.success(response, "Render manifest hazırlandı"));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/render-readiness ──

    @GetMapping("/{id}/render-readiness")
    @Operation(summary = "Render hazırlık durumu",
               description = "Dahili render hazırlık raporu döndürür. Yayın/dışa aktarım hazırlığı değil.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderReadinessReport>> getRenderReadiness(
            @PathVariable UUID id) {

        IcerikFabrikasiRenderReadinessReport report = renderManifestService.evaluateReadiness(id);
        return ResponseEntity.ok(ApiResponse.success(report, "Render hazırlık raporu"));
    }

    // ── FP-021 Task 04: Composition Preview ──

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/composition-preview ──

    @GetMapping("/{id}/composition-preview")
    @Operation(summary = "Kompozisyon önizleme",
               description = "Dahili kompozisyon timeline önizleme meta verisi. Yayın/dışa aktarım hazırlığı değil.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiCompositionPreviewResponse>> getCompositionPreview(
            @PathVariable UUID id) {

        IcerikFabrikasiVideoProject project = bootstrapService.getById(id);
        IcerikFabrikasiVideoCompositionPlan comp = project.getCompositionPlan();
        IcerikFabrikasiRenderManifest manifest = project.getRenderManifest();

        boolean compositionReady = comp != null;
        boolean manifestReady = manifest != null;

        java.util.List<IcerikFabrikasiCompositionPreviewScene> timelineScenes = new java.util.ArrayList<>();
        double totalDuration = 0;

        if (compositionReady) {
            for (var scene : comp.getScenes()) {
                timelineScenes.add(new IcerikFabrikasiCompositionPreviewScene(
                        scene.getSceneNo(), scene.getSceneTitle(),
                        scene.getNarrationText(),
                        scene.getImageRef() != null ? "internal-ref (mevcut)" : "referans yok",
                        scene.getDurationSeconds(), scene.getMood(),
                        scene.getTransitionIn(), scene.getTransitionOut(),
                        scene.getCameraDirection()));
                totalDuration += scene.getDurationSeconds();
            }
        }

        // Readiness summary
        String readinessStatus;
        java.util.List<String> readinessIssues = new java.util.ArrayList<>();
        if (!compositionReady) {
            readinessStatus = "BLOCKED";
            readinessIssues.add("Kompozisyon planı hazırlanmadı");
        } else if (!manifestReady) {
            readinessStatus = "BLOCKED";
            readinessIssues.add("Render manifest hazırlanmadı");
        } else {
            readinessStatus = "NEEDS_REVIEW";
            readinessIssues.add("Telif incelemesi bekliyor");
        }

        java.util.List<String> reviewFlags = java.util.List.of(
                "HUMAN_REVIEW_REQUIRED", "COPYRIGHT_REVIEW_REQUIRED",
                "NOT_PUBLISH_READY", "NOT_EXPORT_READY", "INTERNAL_PREVIEW_ONLY");

        IcerikFabrikasiCompositionPreviewResponse response = new IcerikFabrikasiCompositionPreviewResponse(
                project.getId(), project.getWorkflowStage(),
                compositionReady, manifestReady, timelineScenes,
                totalDuration, timelineScenes.size(), readinessStatus, readinessIssues,
                reviewFlags, "Dahili kompozisyon önizleme meta verisi");

        return ResponseEntity.ok(ApiResponse.success(response, "Kompozisyon önizleme"));
    }

    private ResolvedAdminActor resolveBootstrapActor(ReviewActorRole role, String reference) {
        return resolvePlanningActor(role, reference);
    }

    /**
     * Resolve admin actor for all türkü project planning actions.
     * SYSTEM is blocked. ADMIN/REVIEWER/CREATOR allowed via TEST_FALLBACK.
     */
    private ResolvedAdminActor resolvePlanningActor(ReviewActorRole role, String reference) {
        boolean systemAllowed = false;

        if (role == null) {
            role = ReviewActorRole.ADMIN;
        }

        AdminActorResolutionRequest resolutionRequest = AdminActorResolutionRequest.builder()
                .requestedActorRole(role)
                .requestedActorReference(reference)
                .fallbackAllowed(true)
                .systemInternalAllowed(systemAllowed)
                .build();

        ResolvedAdminActor actor = actorResolver.resolve(resolutionRequest);

        AdminActorBoundaryPolicy.enforce(
                AdminActorBoundaryPolicy.ActionCategory.TURKU_PROJECT_BOOTSTRAP, actor);

        return actor;
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/prepare-render-job ──

    @PostMapping("/{id}/prepare-render-job")
    @Operation(summary = "Dahili render job hazırla",
               description = "Render manifest'ten dahili render job oluşturur. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderJobResponse>> prepareRenderJob(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiRenderJobRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {
        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiRenderJobResponse response = renderJobService.prepareRenderJob(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/render-job ──

    @GetMapping("/{id}/render-job")
    @Operation(summary = "Render job durumu",
               description = "Dahili render job durumunu döndürür. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderJobResponse>> getRenderJob(
            @PathVariable UUID id) {
        IcerikFabrikasiRenderJobResponse response = renderJobService.getRenderJob(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/run-render-job ──

    @PostMapping("/{id}/run-render-job")
    @Operation(summary = "Dahili render job çalıştır",
               description = "Render job'u stub runner ile çalıştırır. Gerçek render yok. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderExecutionResponse>> runRenderJob(
            @PathVariable UUID id,
            @RequestBody(required = false) IcerikFabrikasiRenderExecutionRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {
        resolvePlanningActor(actorRole, actorReference);
        IcerikFabrikasiRenderExecutionResponse response = renderExecutionService.runRenderJob(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/render-execution-safety ──

    @GetMapping("/{id}/render-execution-safety")
    @Operation(summary = "Render çalıştırma güvenlik durumu",
               description = "Render çalıştırma güvenlik zarfını döndürür. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiRenderExecutionSafetyResponse>> getRenderExecutionSafety(
            @PathVariable UUID id) {
        IcerikFabrikasiRenderExecutionSafetyResponse response = renderExecutionService.getRenderExecutionSafety(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── POST /admin/icerikFabrikasi-video-projects/{id}/trigger-internal-dry-run ──

    @PostMapping("/{id}/trigger-internal-dry-run")
    @Operation(summary = "Dahili hikaye-video dry-run tetikle",
               description = "FP-047 storyboard render zincirini fake/test-double ile çalıştırır. Gerçek render yok. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiInternalDryRunTriggerResponse>> triggerInternalDryRun(
            @PathVariable UUID id,
            @RequestBody IcerikFabrikasiInternalDryRunTriggerRequest request,
            @RequestParam(required = false) ReviewActorRole actorRole,
            @RequestParam(required = false) String actorReference) {
        resolvePlanningActor(actorRole, actorReference);
        var fixture = new IcerikFabrikasiStoryboardInternalRenderChainFixture(
                request.getOpaqueStoryboardRef(),
                request.getOpaqueStoryRef(),
                request.getSceneInputs());
        var summary = storyboardChainHarness.execute(fixture);
        var response = new IcerikFabrikasiInternalDryRunTriggerResponse(summary);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── GET /admin/icerikFabrikasi-video-projects/{id}/internal-dry-run-status ──

    @GetMapping("/{id}/internal-dry-run-status")
    @Operation(summary = "Dahili dry-run durumu",
               description = "Dahili dry-run meta veri durumunu döndürür. Yayın/dışa aktarım yok.")
    public ResponseEntity<ApiResponse<IcerikFabrikasiInternalDryRunTriggerResponse>> getInternalDryRunStatus(
            @PathVariable UUID id) {
        // Fake fixture — returns metadata-only summary
        var fixture = IcerikFabrikasiStoryboardInternalRenderChainFixture.icerikFabrikasiFixture(3);
        var summary = storyboardChainHarness.execute(fixture);
        var response = new IcerikFabrikasiInternalDryRunTriggerResponse(summary);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Exception Handling ──

    @ExceptionHandler(IcerikFabrikasiVideoProjectException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectException(IcerikFabrikasiVideoProjectException ex) {
        HttpStatus status = switch (ex.getSafeErrorCode()) {
            case "PROJECT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "TURKU_NAME_REQUIRED", "TURKU_NAME_TOO_LONG",
                 "UNSAFE_INPUT", "FIELD_TOO_LONG", "INVALID_DURATION",
                 "INVALID_STAGE_TRANSITION", "MISSING_PREREQUISITE",
                 "PROMPT_INJECTION_DETECTED" -> HttpStatus.BAD_REQUEST;
            case "SANITIZATION_CRITICAL_VIOLATION", "SANITIZATION_SECRET_VIOLATION",
                 "POLICY_AUTO_APPROVED_VIOLATION", "POLICY_HUMAN_REVIEW_VIOLATION",
                 "POLICY_LYRICS_VIOLATION", "POLICY_COPYRIGHT_VIOLATION" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AdminActorResolutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleActorException(AdminActorResolutionException ex) {
        HttpStatus status = switch (ex.getSafeErrorCode()) {
            case "SYSTEM_NOT_ALLOWED", "BOUNDARY_POLICY_VIOLATION" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }
}
