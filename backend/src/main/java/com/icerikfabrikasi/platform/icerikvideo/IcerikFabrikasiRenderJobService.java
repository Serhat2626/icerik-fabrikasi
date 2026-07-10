package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IcerikFabrikasiRenderJobService {

    private final IcerikFabrikasiVideoProjectRepository repository;
    private final IcerikFabrikasiRenderManifestService renderManifestService;

    public IcerikFabrikasiRenderJobService(IcerikFabrikasiVideoProjectRepository repository,
                                  IcerikFabrikasiRenderManifestService renderManifestService) {
        this.repository = repository;
        this.renderManifestService = renderManifestService;
    }

    public IcerikFabrikasiRenderJobResponse prepareRenderJob(UUID projectId) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (project.getWorkflowStage() != IcerikFabrikasiVideoWorkflowStage.RENDER_MANIFEST_PREPARED) {
            throw new IllegalStateException(
                    "Render job requires RENDER_MANIFEST_PREPARED, current: " + project.getWorkflowStage());
        }

        IcerikFabrikasiRenderManifest manifest = project.getRenderManifest();
        if (manifest == null) {
            throw new IllegalStateException("Render manifest is required");
        }

        IcerikFabrikasiRenderReadinessReport readiness = renderManifestService.evaluateReadiness(projectId);
        if (readiness.getStatus() == IcerikFabrikasiRenderReadinessStatus.BLOCKED) {
            throw new IllegalStateException("Render readiness is BLOCKED: " + readiness.getIssues());
        }

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("NOT_PUBLISH_READY");
        reviewFlags.add("NOT_EXPORT_READY");
        reviewFlags.add("INTERNAL_RENDER_ONLY");

        String manifestRef = "internal-manifest:" + manifest.getManifestId();
        IcerikFabrikasiRenderJob job = new IcerikFabrikasiRenderJob(UUID.randomUUID(), projectId, manifestRef, reviewFlags);

        project.setRenderJob(job);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED);
        repository.save(project);

        return new IcerikFabrikasiRenderJobResponse(
                projectId, job.getJobId(), job.getStatus(),
                project.getWorkflowStage(), manifestRef,
                0, reviewFlags, "Dahili render job hazirlandir — yayin/disa aktarim yok");
    }

    public IcerikFabrikasiRenderJobResponse getRenderJob(UUID projectId) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        IcerikFabrikasiRenderJob job = project.getRenderJob();
        if (job == null) {
            return new IcerikFabrikasiRenderJobResponse(
                    projectId, null, null,
                    project.getWorkflowStage(), null,
                    0, List.of("NO_RENDER_JOB"),
                    "Render job henuz olusturulmadi");
        }

        return new IcerikFabrikasiRenderJobResponse(
                projectId, job.getJobId(), job.getStatus(),
                project.getWorkflowStage(), job.getManifestRef(),
                job.getAttempts().size(), job.getReviewFlags(),
                "Render job durumu: " + job.getStatus());
    }
}
