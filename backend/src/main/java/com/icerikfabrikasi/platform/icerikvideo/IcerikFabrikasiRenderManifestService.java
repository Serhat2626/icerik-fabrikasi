package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FP-021 Task 03: Render manifest + readiness service.
 *
 * <p>Prepares render manifest from composition plan and evaluates render readiness.
 * No actual video rendering. No export/publish. Internal readiness only.</p>
 */
@Service
public class IcerikFabrikasiRenderManifestService {

    private final IcerikFabrikasiVideoProjectRepository repository;

    public IcerikFabrikasiRenderManifestService(IcerikFabrikasiVideoProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Prepare render manifest from the composition plan.
     * Requires COMPOSITION_PLAN_PREPARED stage.
     */
    public IcerikFabrikasiRenderManifest prepareRenderManifest(UUID projectId,
                                                      IcerikFabrikasiRenderManifestRequest request) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));

        IcerikFabrikasiVideoWorkflowStage stage = project.getWorkflowStage();
        if (stage != IcerikFabrikasiVideoWorkflowStage.COMPOSITION_PLAN_PREPARED) {
            throw new IllegalStateException(
                    "Render manifest requires COMPOSITION_PLAN_PREPARED, current stage: " + stage);
        }

        IcerikFabrikasiVideoCompositionPlan composition = project.getCompositionPlan();
        if (composition == null || composition.getScenes().isEmpty()) {
            throw new IllegalStateException("Composition plan is required for render manifest");
        }

        // Build manifest scenes from composition scenes
        List<IcerikFabrikasiRenderManifestScene> manifestScenes = composition.getScenes().stream()
                .map(cs -> new IcerikFabrikasiRenderManifestScene(
                        cs.getSceneNo(),
                        cs.getSceneTitle(),
                        cs.getImageRef(),
                        "narration-ref:" + UUID.randomUUID(),
                        null, // music ref placeholder
                        cs.getDurationSeconds(),
                        cs.getTransitionIn(),
                        cs.getTransitionOut(),
                        cs.getCameraDirection()))
                .collect(Collectors.toList());

        // Review flags
        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("LYRICS_NOT_INCLUDED");
        reviewFlags.add("NO_RENDER_PRODUCED");
        reviewFlags.add("INTERNAL_READINESS_ONLY");

        IcerikFabrikasiRenderManifest manifest = new IcerikFabrikasiRenderManifest(
                UUID.randomUUID(), projectId, composition.getCompositionId(),
                manifestScenes, "1920x1080", 30, "mp4",
                composition.getTotalDurationSeconds(), reviewFlags);

        // Advance stage and store
        project.setRenderManifest(manifest);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED);
        repository.save(project);

        return manifest;
    }

    /**
     * Evaluate render readiness for a project.
     * Read-only operation — no state change.
     * Internal readiness only — NOT publish/export readiness.
     */
    public IcerikFabrikasiRenderReadinessReport evaluateReadiness(UUID projectId) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));

        boolean compositionReady = project.getCompositionPlan() != null;
        boolean manifestReady = project.getRenderManifest() != null;

        // Check scene images
        boolean allScenesHaveImages = false;
        boolean allNarrationPresent = false;
        boolean timingComplete = false;

        if (compositionReady) {
            IcerikFabrikasiVideoCompositionPlan comp = project.getCompositionPlan();
            allScenesHaveImages = comp.getScenes().stream()
                    .allMatch(s -> s.getImageRef() != null && !s.getImageRef().isEmpty());
            allNarrationPresent = comp.getNarrationSegments().stream()
                    .allMatch(n -> n.getNarrationText() != null && !n.getNarrationText().isEmpty());
            timingComplete = comp.getTimingPlan() != null
                    && !comp.getTimingPlan().isHasTimingConflict();
        }

        // Build issues
        List<IcerikFabrikasiRenderReadinessIssue> issues = new ArrayList<>();
        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("NOT_PUBLISH_READY");
        reviewFlags.add("NOT_EXPORT_READY");

        if (!compositionReady) {
            issues.add(new IcerikFabrikasiRenderReadinessIssue(
                    "MISSING_COMPOSITION", "Composition plan not prepared", "BLOCKING"));
        }
        if (!manifestReady) {
            issues.add(new IcerikFabrikasiRenderReadinessIssue(
                    "MISSING_MANIFEST", "Render manifest not prepared", "BLOCKING"));
        }
        if (compositionReady && !allScenesHaveImages) {
            issues.add(new IcerikFabrikasiRenderReadinessIssue(
                    "MISSING_IMAGES", "Not all scenes have image references", "BLOCKING"));
        }
        if (compositionReady && !allNarrationPresent) {
            issues.add(new IcerikFabrikasiRenderReadinessIssue(
                    "MISSING_NARRATION", "Not all narration segments have text", "WARNING"));
        }
        if (compositionReady && !timingComplete) {
            issues.add(new IcerikFabrikasiRenderReadinessIssue(
                    "TIMING_CONFLICT", "Timing plan has conflicts", "WARNING"));
        }
        // Copyright always uncleared
        issues.add(new IcerikFabrikasiRenderReadinessIssue(
                "COPYRIGHT_PENDING", "Copyright review not completed", "WARNING"));

        // Calculate readiness
        boolean hasBlocking = issues.stream()
                .anyMatch(i -> "BLOCKING".equals(i.getSeverity()));
        IcerikFabrikasiRenderReadinessStatus status;
        int score;
        if (hasBlocking) {
            status = IcerikFabrikasiRenderReadinessStatus.BLOCKED;
            score = calculateScore(compositionReady, manifestReady,
                    allScenesHaveImages, allNarrationPresent, timingComplete);
        } else {
            status = IcerikFabrikasiRenderReadinessStatus.NEEDS_REVIEW;
            score = calculateScore(compositionReady, manifestReady,
                    allScenesHaveImages, allNarrationPresent, timingComplete);
        }

        return new IcerikFabrikasiRenderReadinessReport(
                projectId, status, compositionReady, manifestReady,
                allScenesHaveImages, allNarrationPresent, timingComplete,
                false, // copyrightReviewDone always false
                score, issues, reviewFlags);
    }

    private int calculateScore(boolean comp, boolean manifest,
                                boolean images, boolean narration, boolean timing) {
        int score = 0;
        if (comp) score += 25;
        if (manifest) score += 25;
        if (images) score += 20;
        if (narration) score += 15;
        if (timing) score += 15;
        return score;
    }
}
