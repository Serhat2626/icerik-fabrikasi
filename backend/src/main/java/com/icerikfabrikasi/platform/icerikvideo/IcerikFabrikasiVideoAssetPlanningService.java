package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 04: Asset planning service — scene asset plan + image batch.
 *
 * <p>Uses deterministic internal generation (stubs). No external image provider.
 * Provider-ready seams available for future integration.</p>
 *
 * <p>No base64, no public URLs, no storage keys, no raw provider data.</p>
 */
@Service
public class IcerikFabrikasiVideoAssetPlanningService {

    private final IcerikFabrikasiVideoProjectRepository repository;

    public IcerikFabrikasiVideoAssetPlanningService(IcerikFabrikasiVideoProjectRepository repository) {
        this.repository = repository;
    }

    // ── Scene Asset Plan ────────────────────────────────────

    public IcerikFabrikasiSceneAssetPlan prepareSceneAssets(UUID projectId,
                                                   IcerikFabrikasiSceneAssetPreparationRequest request) {
        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED);

        if (project.getStoryboardPlan() == null || project.getStoryboardPlan().getScenes().isEmpty()) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Storyboard sahneleri mevcut olmalıdır");
        }

        if (request != null) {
            validateOptionalHint(request.getStyleOverrideHint(), "styleOverrideHint", 200);
            validateOptionalHint(request.getAspectRatioHint(), "aspectRatioHint", 50);
        }

        String styleHint = (request != null && request.getStyleOverrideHint() != null)
                ? request.getStyleOverrideHint()
                : (project.getVisualStyle() != null ? project.getVisualStyle() : "Sinematik Anadolu");

        String aspectRatio = (request != null && request.getAspectRatioHint() != null)
                ? request.getAspectRatioHint() : "16:9";

        List<IcerikFabrikasiSceneAssetItem> items = new ArrayList<>();
        for (IcerikFabrikasiStoryboardScene scene : project.getStoryboardPlan().getScenes()) {
            items.add(new IcerikFabrikasiSceneAssetItem(
                    scene.getSceneNo(),
                    "Sahne " + scene.getSceneNo() + " görseli",
                    buildVisualPromptSummary(scene, styleHint),
                    "Gerçek kişi portresi kullanma, telif ihlali riski olan logo/marka kullanma",
                    styleHint,
                    scene.getMood(),
                    aspectRatio,
                    true, // needsHumanReview
                    true  // copyrightReviewRequired
            ));
        }

        List<String> reviewFlags = buildAssetReviewFlags(project);

        IcerikFabrikasiSceneAssetPlan plan = new IcerikFabrikasiSceneAssetPlan(
                UUID.randomUUID(), projectId, items, reviewFlags);

        project.setSceneAssetPlan(plan);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED);
        repository.save(project);

        return plan;
    }

    // ── Image Batch Preparation ─────────────────────────────

    public IcerikFabrikasiSceneImageBatch prepareImageBatch(UUID projectId,
                                                    IcerikFabrikasiImageBatchPreparationRequest request) {
        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);

        if (project.getSceneAssetPlan() == null || project.getSceneAssetPlan().getItems().isEmpty()) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Sahne varlık planı mevcut olmalıdır");
        }

        if (request != null) {
            validateOptionalHint(request.getProviderHint(), "providerHint", 100);
        }

        // Deterministic stub — create image requests from asset items
        List<IcerikFabrikasiSceneImageRequest> imageRequests = new ArrayList<>();
        for (IcerikFabrikasiSceneAssetItem item : project.getSceneAssetPlan().getItems()) {
            imageRequests.add(new IcerikFabrikasiSceneImageRequest(
                    item.getSceneNo(),
                    item.getVisualPromptSummary(),
                    item.getNegativePromptSafetyNotes(),
                    item.getStyleHint(),
                    item.getAspectRatioHint()
            ));
        }

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("LYRICS_NOT_INCLUDED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("PUBLIC_DELIVERY_BLOCKED");

        IcerikFabrikasiSceneImageBatch batch = new IcerikFabrikasiSceneImageBatch(
                UUID.randomUUID(), projectId,
                IcerikFabrikasiImageBatchStatus.PREPARED,
                imageRequests, reviewFlags);

        // Deterministic stub — generate safe placeholder results
        List<IcerikFabrikasiSceneImageResult> stubResults = new ArrayList<>();
        for (IcerikFabrikasiSceneImageRequest req : imageRequests) {
            stubResults.add(new IcerikFabrikasiSceneImageResult(
                    req.getSceneNo(),
                    "internal-ref:" + UUID.randomUUID().toString().substring(0, 8),
                    true,
                    null
            ));
        }
        batch.setImageResults(stubResults);

        project.setImageBatch(batch);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);
        repository.save(project);

        return batch;
    }

    // ── Image Batch Completion ───────────────────────────────

    public IcerikFabrikasiSceneImageBatch completeImageBatch(UUID projectId,
                                                     IcerikFabrikasiImageBatchCompletionRequest request) {
        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED,
                request != null && request.isPartial()
                        ? IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL
                        : IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);

        if (project.getImageBatch() == null) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Görsel üretim batch'i mevcut olmalıdır");
        }

        if (request != null) {
            validateOptionalHint(request.getSafeCompletionNote(), "safeCompletionNote", 500);
        }

        IcerikFabrikasiSceneImageBatch batch = project.getImageBatch();

        if (request != null && request.isPartial()) {
            batch.setStatus(IcerikFabrikasiImageBatchStatus.PARTIAL);
            project.advanceTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL);
        } else {
            batch.setStatus(IcerikFabrikasiImageBatchStatus.COMPLETED);
            project.advanceTo(IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED);
        }

        repository.save(project);
        return batch;
    }

    // ── Validation Helpers ──────────────────────────────────

    private IcerikFabrikasiVideoProject getProject(UUID id) {
        return repository.findById(id)
                .orElseThrow(IcerikFabrikasiVideoProjectException::projectNotFound);
    }

    private void validateStage(IcerikFabrikasiVideoProject project,
                                IcerikFabrikasiVideoWorkflowStage required,
                                IcerikFabrikasiVideoWorkflowStage target) {
        if (project.getWorkflowStage() != required) {
            throw IcerikFabrikasiVideoProjectException.invalidStageTransition(
                    project.getWorkflowStage(), target);
        }
    }

    private void validateOptionalHint(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) return;
        if (value.length() > maxLength) {
            throw IcerikFabrikasiVideoProjectException.fieldTooLong(fieldName, maxLength);
        }
        if (value.matches("(?i).*(javascript:|<script|on\\w+=|password|secret|token|bearer|authorization|eyJ[A-Za-z0-9]|data:image|base64,).*")) {
            throw IcerikFabrikasiVideoProjectException.unsafeInput(fieldName);
        }
    }

    private String buildVisualPromptSummary(IcerikFabrikasiStoryboardScene scene, String style) {
        return style + " tarzında, " + scene.getMood() + " atmosferinde " +
                scene.getVisualDescription() + " — Sahne " + scene.getSceneNo();
    }

    private List<String> buildAssetReviewFlags(IcerikFabrikasiVideoProject project) {
        List<String> flags = new ArrayList<>();
        flags.add("LYRICS_NOT_INCLUDED");
        flags.add("COPYRIGHT_REVIEW_REQUIRED");
        flags.add("HUMAN_REVIEW_REQUIRED");
        flags.add("PUBLIC_DELIVERY_BLOCKED");
        if (project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.UNKNOWN
                || project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.LOW) {
            flags.add("SOURCE_REVIEW_REQUIRED");
        }
        flags.add("CULTURAL_CONTEXT_REVIEW_REQUIRED");
        return flags;
    }
}
