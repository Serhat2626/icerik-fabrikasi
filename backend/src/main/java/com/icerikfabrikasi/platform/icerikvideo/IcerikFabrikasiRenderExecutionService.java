package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-022 Task 03: Render execution service.
 *
 * <p>Executes render jobs through a gateway (stub by default).
 * No real video rendering. No binary file. No export/publish.</p>
 */
@Service
public class IcerikFabrikasiRenderExecutionService {

    private final IcerikFabrikasiVideoProjectRepository repository;
    private final IcerikFabrikasiRenderRunnerGateway renderRunnerGateway;

    @Autowired
    public IcerikFabrikasiRenderExecutionService(IcerikFabrikasiVideoProjectRepository repository) {
        this.repository = repository;
        this.renderRunnerGateway = new StubIcerikFabrikasiRenderRunnerGateway();
    }

    /** Test constructor — allows explicit gateway injection. */
    public IcerikFabrikasiRenderExecutionService(IcerikFabrikasiVideoProjectRepository repository,
                                        IcerikFabrikasiRenderRunnerGateway renderRunnerGateway) {
        this.repository = repository;
        this.renderRunnerGateway = renderRunnerGateway;
    }

    /**
     * Run render job through gateway. Requires RENDER_JOB_PREPARED.
     */
    public IcerikFabrikasiRenderExecutionResponse runRenderJob(UUID projectId) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (project.getWorkflowStage() != IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_PREPARED) {
            throw new IllegalStateException(
                    "Run render job requires RENDER_JOB_PREPARED, current: "
                    + project.getWorkflowStage());
        }

        IcerikFabrikasiRenderJob job = project.getRenderJob();
        if (job == null) {
            throw new IllegalStateException("Render job is required");
        }

        IcerikFabrikasiRenderManifest manifest = project.getRenderManifest();
        if (manifest == null) {
            throw new IllegalStateException("Render manifest is required");
        }

        // Build execution plan from manifest
        IcerikFabrikasiRenderExecutionPlan executionPlan = new IcerikFabrikasiRenderExecutionPlan(
                UUID.randomUUID(),
                manifest.getScenes().size(),
                manifest.getTotalDurationSeconds(),
                manifest.getTargetResolution(),
                manifest.getTargetFps(),
                manifest.getTargetFormat(),
                manifest.getTotalDurationSeconds() * 0.5,
                "deterministic-stub",
                "DETERMINISTIC_STUB");

        // Advance to RUNNING
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_RUNNING);
        job.setStatus(IcerikFabrikasiRenderJobStatus.RUNNING);
        repository.save(project);

        // Execute through gateway
        IcerikFabrikasiRenderExecutionResult result;
        try {
            result = renderRunnerGateway.execute(executionPlan);
        } catch (Exception e) {
            // Failed safe
            return handleFailure(project, job, IcerikFabrikasiRenderFailureCode.RENDERER_INTERNAL_ERROR,
                    "Renderer error: " + e.getMessage());
        }

        if (!result.isSuccess()) {
            return handleFailure(project, job,
                    result.getFailureCode() != null ? result.getFailureCode()
                            : IcerikFabrikasiRenderFailureCode.RENDERER_INTERNAL_ERROR,
                    result.getFailureMessage());
        }

        // Success path
        IcerikFabrikasiRenderAttempt attempt = new IcerikFabrikasiRenderAttempt(
                UUID.randomUUID(), job.getAttempts().size() + 1,
                Instant.now().minusMillis(result.getDurationMs()), Instant.now(),
                IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL, null,
                result.getDurationMs(), "deterministic-stub");

        job.addAttempt(attempt);
        job.setStatus(IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL);
        if (result.getArtifact() != null) {
            job.setArtifact(result.getArtifact());
        }
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_COMPLETED_INTERNAL);
        repository.save(project);

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("NOT_PUBLISH_READY");
        reviewFlags.add("NOT_EXPORT_READY");
        reviewFlags.add("INTERNAL_RENDER_ONLY");
        reviewFlags.add("COMPLETED_INTERNAL");

        String artifactRef = result.getArtifact() != null
                ? result.getArtifact().getArtifactRef() : null;

        return new IcerikFabrikasiRenderExecutionResponse(
                projectId, job.getJobId(), attempt.getAttemptId(),
                IcerikFabrikasiRenderJobStatus.COMPLETED_INTERNAL,
                project.getWorkflowStage(),
                result.getSafeTraceRef(), artifactRef,
                reviewFlags,
                "Dahili render tamamlandı — yayın/dışa aktarım yok",
                null);
    }

    private IcerikFabrikasiRenderExecutionResponse handleFailure(IcerikFabrikasiVideoProject project,
                                                         IcerikFabrikasiRenderJob job,
                                                         IcerikFabrikasiRenderFailureCode failureCode,
                                                         String failureMessage) {
        IcerikFabrikasiRenderAttempt failedAttempt = new IcerikFabrikasiRenderAttempt(
                UUID.randomUUID(), job.getAttempts().size() + 1,
                Instant.now(), Instant.now(),
                IcerikFabrikasiRenderJobStatus.FAILED_SAFE, failureCode, 0, "deterministic-stub");

        job.addAttempt(failedAttempt);
        job.setStatus(IcerikFabrikasiRenderJobStatus.FAILED_SAFE);
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.RENDER_JOB_FAILED_SAFE);
        repository.save(project);

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("NOT_PUBLISH_READY");
        reviewFlags.add("NOT_EXPORT_READY");
        reviewFlags.add("RENDER_FAILED_SAFE");

        return new IcerikFabrikasiRenderExecutionResponse(
                project.getId(), job.getJobId(), failedAttempt.getAttemptId(),
                IcerikFabrikasiRenderJobStatus.FAILED_SAFE,
                project.getWorkflowStage(),
                null, null, reviewFlags,
                "Render başarısız (güvenli kapanış): " + failureMessage,
                failureCode);
    }

    /**
     * Get render execution safety status for a project.
     */
    public IcerikFabrikasiRenderExecutionSafetyResponse getRenderExecutionSafety(UUID projectId) {
        IcerikFabrikasiVideoProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        IcerikFabrikasiRenderJob job = project.getRenderJob();

        List<String> safetyFlags = new ArrayList<>();
        safetyFlags.add("NO_REAL_RENDER");
        safetyFlags.add("NO_BINARY_OUTPUT");
        safetyFlags.add("NO_PUBLIC_DELIVERY");
        safetyFlags.add("NO_EXPORT");

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("HUMAN_REVIEW_REQUIRED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("NOT_PUBLISH_READY");
        reviewFlags.add("NOT_EXPORT_READY");
        reviewFlags.add("INTERNAL_ONLY");

        List<IcerikFabrikasiRenderFailureCode> failureCodes = new ArrayList<>();
        List<String> blockedReasons = new ArrayList<>();

        if (job != null && job.getStatus() == IcerikFabrikasiRenderJobStatus.FAILED_SAFE) {
            job.getAttempts().stream()
                    .filter(a -> a.getFailureCode() != null)
                    .forEach(a -> failureCodes.add(a.getFailureCode()));
        }

        IcerikFabrikasiRenderExecutionSafetyEnvelope envelope = new IcerikFabrikasiRenderExecutionSafetyEnvelope(
                "DETERMINISTIC_STUB", safetyFlags, reviewFlags, failureCodes, blockedReasons);

        IcerikFabrikasiRenderJobStatus jobStatus = job != null ? job.getStatus() : null;
        UUID jobId = job != null ? job.getJobId() : null;

        return new IcerikFabrikasiRenderExecutionSafetyResponse(
                projectId, jobId, jobStatus, project.getWorkflowStage(),
                envelope, "Render güvenlik durumu: dahili, yayın/dışa aktarım yok");
    }
}
