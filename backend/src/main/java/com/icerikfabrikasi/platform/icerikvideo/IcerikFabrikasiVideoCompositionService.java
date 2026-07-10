package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FP-021 Task 02: Composition plan service.
 *
 * <p>Assembles a video composition plan from existing storyboard + scene asset/image batch data.
 * No real video rendering. No lyrics. No export/publish.</p>
 */
@Service
public class IcerikFabrikasiVideoCompositionService {

    private final IcerikFabrikasiVideoProjectRepository repository;

    public IcerikFabrikasiVideoCompositionService(IcerikFabrikasiVideoProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Prepare a composition plan from the existing project data.
     * Requires IMAGE_BATCH_COMPLETED or IMAGE_BATCH_PARTIAL stage.
     */
    public IcerikFabrikasiVideoCompositionPlan prepareCompositionPlan(UUID projectId,
                                                             IcerikFabrikasiCompositionPlanRequest request) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));

        // Validate stage
        IcerikFabrikasiVideoWorkflowStage stage = project.getWorkflowStage();
        if (stage != IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED
                && stage != IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL) {
            throw new IllegalStateException(
                    "Composition plan requires IMAGE_BATCH_COMPLETED or IMAGE_BATCH_PARTIAL, " +
                    "current stage: " + stage);
        }

        // Require storyboard
        IcerikFabrikasiStoryboardPlan storyboard = project.getStoryboardPlan();
        if (storyboard == null || storyboard.getScenes().isEmpty()) {
            throw new IllegalStateException("Storyboard plan is required for composition");
        }

        // Build composition scenes from storyboard
        List<IcerikFabrikasiVideoCompositionScene> compositionScenes = storyboard.getScenes().stream()
                .map(scene -> buildCompositionScene(scene, project))
                .collect(Collectors.toList());

        // Build narration segments
        List<IcerikFabrikasiNarrationSegment> narrationSegments = storyboard.getScenes().stream()
                .map(this::buildNarrationSegment)
                .collect(Collectors.toList());

        // Build timing plan
        int targetDuration = project.getTargetDurationMinutes() != null
                ? project.getTargetDurationMinutes() * 60
                : storyboard.getTotalEstimatedDurationSeconds();

        Map<Integer, Double> sceneDurations = new LinkedHashMap<>();
        double totalDuration = 0;
        for (IcerikFabrikasiStoryboardScene scene : storyboard.getScenes()) {
            double d = scene.getEstimatedDurationSeconds();
            sceneDurations.put(scene.getSceneNo(), d);
            totalDuration += d;
        }
        boolean hasConflict = Math.abs(totalDuration - targetDuration) > targetDuration * 0.2;
        IcerikFabrikasiSceneTimingPlan timingPlan = new IcerikFabrikasiSceneTimingPlan(
                totalDuration, targetDuration, sceneDurations, hasConflict);

        // Build transition plan
        List<IcerikFabrikasiTransitionPlan.TransitionDirective> transitions = new ArrayList<>();
        List<IcerikFabrikasiStoryboardScene> scenes = storyboard.getScenes();
        for (int i = 0; i < scenes.size() - 1; i++) {
            transitions.add(new IcerikFabrikasiTransitionPlan.TransitionDirective(
                    scenes.get(i).getSceneNo(),
                    scenes.get(i + 1).getSceneNo(),
                    scenes.get(i).getTransitionHint() != null
                            ? scenes.get(i).getTransitionHint() : "CROSS_FADE",
                    500));
        }
        IcerikFabrikasiTransitionPlan transitionPlan = new IcerikFabrikasiTransitionPlan(transitions, "CROSS_FADE");

        // Review flags
        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("LYRICS_NOT_INCLUDED");
        reviewFlags.add("NO_RENDER_PRODUCED");
        if (stage == IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL) {
            reviewFlags.add("IMAGE_BATCH_PARTIAL");
        }
        if (hasConflict) {
            reviewFlags.add("TIMING_CONFLICT");
        }

        // Build composition plan
        IcerikFabrikasiVideoCompositionPlan plan = new IcerikFabrikasiVideoCompositionPlan(
                UUID.randomUUID(), projectId,
                (int) totalDuration, compositionScenes, narrationSegments,
                timingPlan, transitionPlan, reviewFlags);

        // Advance stage
        project.setCompositionPlan(plan);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED);
        repository.save(project);

        return plan;
    }

    private IcerikFabrikasiVideoCompositionScene buildCompositionScene(IcerikFabrikasiStoryboardScene scene,
                                                               IcerikFabrikasiVideoProject project) {
        // Derive safe image ref from image batch if available
        String imageRef = null;
        if (project.getImageBatch() != null
                && project.getImageBatch().getImageResults() != null) {
            imageRef = project.getImageBatch().getImageResults().stream()
                    .filter(r -> r.getSceneNo() == scene.getSceneNo())
                    .findFirst()
                    .map(r -> "internal-ref:" + r.getSafeInternalRef())
                    .orElse(null);
        }

        return new IcerikFabrikasiVideoCompositionScene(
                scene.getSceneNo(),
                scene.getSceneTitle(),
                scene.getVisualDescription(),
                scene.getNarrationSummary(),
                scene.getMood(),
                scene.getEstimatedDurationSeconds(),
                imageRef,
                scene.getTransitionHint() != null ? scene.getTransitionHint() : "CROSS_FADE",
                "CROSS_FADE",
                scene.getCameraDirection());
    }

    private IcerikFabrikasiNarrationSegment buildNarrationSegment(IcerikFabrikasiStoryboardScene scene) {
        return new IcerikFabrikasiNarrationSegment(
                scene.getSceneNo(),
                scene.getSceneNo(),
                scene.getNarrationSummary(),
                scene.getEstimatedDurationSeconds(),
                "documentary",
                "tr",
                false);
    }
}
