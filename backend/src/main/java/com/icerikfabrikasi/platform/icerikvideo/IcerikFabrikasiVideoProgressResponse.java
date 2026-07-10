package com.icerikfabrikasi.platform.icerikvideo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 02: Progress response DTO. Safe stage/progress data only.
 */
public final class IcerikFabrikasiVideoProgressResponse {
    private final UUID id;
    private final IcerikFabrikasiVideoWorkflowStage currentStage;
    private final List<IcerikFabrikasiVideoWorkflowStage> completedStages;
    private final String nextAvailableAction;
    private final String safeProgressSummary;

    public IcerikFabrikasiVideoProgressResponse(UUID id, IcerikFabrikasiVideoWorkflowStage currentStage,
                                       List<IcerikFabrikasiVideoWorkflowStage> completedStages,
                                       String nextAvailableAction, String safeProgressSummary) {
        this.id = id;
        this.currentStage = currentStage;
        this.completedStages = completedStages;
        this.nextAvailableAction = nextAvailableAction;
        this.safeProgressSummary = safeProgressSummary;
    }

    public UUID getId() { return id; }
    public IcerikFabrikasiVideoWorkflowStage getCurrentStage() { return currentStage; }
    public List<IcerikFabrikasiVideoWorkflowStage> getCompletedStages() { return completedStages; }
    public String getNextAvailableAction() { return nextAvailableAction; }
    public String getSafeProgressSummary() { return safeProgressSummary; }

    public static IcerikFabrikasiVideoProgressResponse from(IcerikFabrikasiVideoProject project) {
        IcerikFabrikasiVideoWorkflowStage stage = project.getWorkflowStage();
        List<IcerikFabrikasiVideoWorkflowStage> completed = computeCompleted(stage);
        String next = computeNextAction(stage);
        String summary = "Aşama: " + stage + " (" + completed.size() + " aşama tamamlandı)";
        return new IcerikFabrikasiVideoProgressResponse(project.getId(), stage, completed, next, summary);
    }

    private static List<IcerikFabrikasiVideoWorkflowStage> computeCompleted(IcerikFabrikasiVideoWorkflowStage current) {
        IcerikFabrikasiVideoWorkflowStage[] ordered = {
                IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED,
                IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.SCENE_ASSET_PLAN_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED,
                IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_RUNNING,
                IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_COMPLETED_INTERNAL,
                IcerikFabrikasiVideoWorkflowStage.INTERNAL_RENDER_ARTIFACT_READY,
                IcerikFabrikasiVideoWorkflowStage.INTERNAL_PREVIEW_READY
        };
        int idx = Arrays.asList(ordered).indexOf(current);
        if (idx <= 0) return List.of();
        return List.of(Arrays.copyOfRange(ordered, 0, idx));
    }

    private static String computeNextAction(IcerikFabrikasiVideoWorkflowStage stage) {
        return switch (stage) {
            case DRAFT_CREATED -> "prepare-story";
            case STORY_CONTEXT_PREPARED -> "prepare-scenario";
            case SCENARIO_PREPARED -> "prepare-storyboard";
            case STORYBOARD_PREPARED -> "prepare-scene-assets";
            case SCENE_ASSET_PLAN_PREPARED -> "prepare-image-batch";
            case IMAGE_BATCH_PREPARED, IMAGE_BATCH_PARTIAL -> "İlerleme bekleniyor";
            case IMAGE_BATCH_COMPLETED -> "prepare-composition-plan";
            case COMPOSITION_PLAN_PREPARED -> "prepare-render-manifest";
            case RENDER_MANIFEST_PREPARED -> "prepare-render-job";
            case RENDER_JOB_PREPARED -> "run-render-job";
            case RENDER_JOB_RUNNING -> "Render çalışıyor — bekleyin";
            case RENDER_JOB_COMPLETED_INTERNAL -> "Dahili render tamamlandı — inceleme gerekli";
            case RENDER_JOB_FAILED_SAFE -> "Render başarısız — güvenli kapanış";
            case INTERNAL_RENDER_ARTIFACT_READY -> "Dahili artifact hazır — inceleme gerekli";
            case INTERNAL_PREVIEW_READY -> "Tamamlandı — dahili önizleme hazır";
            case NEEDS_REVISION -> "Revizyon gerekli";
            case BLOCKED -> "Engellendi";
            case ARCHIVED -> "Arşivlendi";
        };
    }
}
