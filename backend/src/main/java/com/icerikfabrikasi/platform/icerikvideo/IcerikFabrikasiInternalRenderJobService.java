package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-025 Task 03: Internal render job service.
 * Pure/stateless. No persistence, no FFmpeg, no filesystem, no provider.
 */
public class IcerikFabrikasiInternalRenderJobService {

    private final IcerikFabrikasiInternalRenderJobTransitionValidator transitionValidator;

    public IcerikFabrikasiInternalRenderJobService() {
        this.transitionValidator = new IcerikFabrikasiInternalRenderJobTransitionValidator();
    }

    public IcerikFabrikasiInternalRenderJobService(IcerikFabrikasiInternalRenderJobTransitionValidator validator) {
        this.transitionValidator = validator;
    }

    // ── Job Creation ──

    /**
     * Create a safe initial job summary in DRAFT state.
     */
    public IcerikFabrikasiInternalRenderJobSummary createDraft() {
        var ref = IcerikFabrikasiInternalRenderJobRef.create();
        return new IcerikFabrikasiInternalRenderJobSummary(
                ref.toOpaqueRef(),
                IcerikFabrikasiInternalRenderJobStatus.DRAFT,
                0,
                IcerikFabrikasiInternalRenderJobProgress.StageLabel.HAZIRLANIYOR.getLabel(),
                "trace:" + UUID.randomUUID());
    }

    // ── Explicit Action Dispatcher ──

    /**
     * Apply an explicit lifecycle action. Returns transition result + updated summary.
     * No generic setStatus/updateStatus/patchStatus.
     */
    public IcerikFabrikasiInternalRenderJobActionResult applyAction(
            IcerikFabrikasiInternalRenderJobSummary current,
            IcerikFabrikasiInternalRenderJobAction action) {

        if (current == null) {
            return IcerikFabrikasiInternalRenderJobActionResult.denied(
                    null, action,
                    IcerikFabrikasiInternalRenderJobReasonCode.JOB_REF_INVALID);
        }

        var transitionResult = transitionValidator.validate(current.getStatus(), action);

        if (!transitionResult.isAllowed()) {
            return IcerikFabrikasiInternalRenderJobActionResult.denied(
                    current, action, transitionResult.getReasonCode());
        }

        var progress = progressForState(transitionResult.getToState());
        var updated = new IcerikFabrikasiInternalRenderJobSummary(
                current.getJobRef(),
                transitionResult.getToState(),
                progress.getProgressPercent(),
                progress.getCurrentStage().getLabel(),
                current.getSafeTraceRef());

        return IcerikFabrikasiInternalRenderJobActionResult.allowed(updated, action);
    }

    // ── Readiness Snapshot ──

    /**
     * Produce readiness snapshot based on FP-024 safety chain.
     */
    public IcerikFabrikasiInternalRenderReadinessSnapshot readiness(
            boolean configValid, boolean workspaceSafe,
            boolean executionBoundaryAvailable, boolean artifactVerifiable) {

        List<IcerikFabrikasiInternalRenderReadinessIssue> issues = new ArrayList<>();

        if (!configValid) issues.add(IcerikFabrikasiInternalRenderReadinessIssue.CONFIG_INVALID);
        if (!workspaceSafe) issues.add(IcerikFabrikasiInternalRenderReadinessIssue.WORKSPACE_REF_INVALID);
        if (!executionBoundaryAvailable) issues.add(IcerikFabrikasiInternalRenderReadinessIssue.EXECUTION_BOUNDARY_DISABLED);
        if (!artifactVerifiable) issues.add(IcerikFabrikasiInternalRenderReadinessIssue.ARTIFACT_VERIFICATION_FAILED);

        // Always require human review
        issues.add(IcerikFabrikasiInternalRenderReadinessIssue.HUMAN_REVIEW_REQUIRED);
        // Always forbid public delivery
        issues.add(IcerikFabrikasiInternalRenderReadinessIssue.PUBLIC_DELIVERY_FORBIDDEN);
        // Always forbid auto-approval
        issues.add(IcerikFabrikasiInternalRenderReadinessIssue.AUTO_APPROVAL_FORBIDDEN);

        IcerikFabrikasiInternalRenderReadinessStatus status;
        if (!configValid) {
            status = IcerikFabrikasiInternalRenderReadinessStatus.BLOCKED_BY_CONFIG;
        } else if (!workspaceSafe) {
            status = IcerikFabrikasiInternalRenderReadinessStatus.BLOCKED_BY_WORKSPACE;
        } else if (!executionBoundaryAvailable) {
            status = IcerikFabrikasiInternalRenderReadinessStatus.BLOCKED_BY_EXECUTION_BOUNDARY;
        } else if (!artifactVerifiable) {
            status = IcerikFabrikasiInternalRenderReadinessStatus.BLOCKED_BY_ARTIFACT_VERIFICATION;
        } else {
            status = IcerikFabrikasiInternalRenderReadinessStatus.READY_INTERNAL_ONLY;
        }

        return new IcerikFabrikasiInternalRenderReadinessSnapshot(status, issues);
    }

    // ── FP-024 Safety Chain Snapshot ──

    /**
     * Produce FP-024 safety chain snapshot. Safe booleans and labels only.
     */
    public IcerikFabrikasiInternalRenderSafetyChainSnapshot safetyChain(
            boolean configValid, String configModeLabel,
            boolean workspaceSafe,
            boolean executionBoundarySafe, String executionModeLabel,
            boolean artifactVerified, String verificationStatusLabel) {

        return new IcerikFabrikasiInternalRenderSafetyChainSnapshot(
                configValid, workspaceSafe, executionBoundarySafe, artifactVerified,
                configModeLabel, executionModeLabel, verificationStatusLabel);
    }

    // ── Progress Snapshots ──

    /**
     * Produce safe progress snapshot for a given state.
     */
    public IcerikFabrikasiInternalRenderJobProgress progressForState(IcerikFabrikasiInternalRenderJobStatus state) {
        if (state == null) state = IcerikFabrikasiInternalRenderJobStatus.DRAFT;
        return switch (state) {
            case DRAFT -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 0, IcerikFabrikasiInternalRenderJobProgress.StageLabel.HAZIRLANIYOR);
            case READY_FOR_INTERNAL_RENDER -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 10, IcerikFabrikasiInternalRenderJobProgress.StageLabel.YAPILANDIRMA_DOGRULANIYOR);
            case QUEUED_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 25, IcerikFabrikasiInternalRenderJobProgress.StageLabel.CALISMA_ALANI_HAZIRLANIYOR);
            case RUNNING_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 60, IcerikFabrikasiInternalRenderJobProgress.StageLabel.DAHILI_RENDER_CALISIYOR);
            case SUCCEEDED_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 100, IcerikFabrikasiInternalRenderJobProgress.StageLabel.TAMAMLANDI);
            case FAILED_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 0, IcerikFabrikasiInternalRenderJobProgress.StageLabel.HAZIRLANIYOR);
            case CANCELLED_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 0, IcerikFabrikasiInternalRenderJobProgress.StageLabel.HAZIRLANIYOR);
            case EXPIRED_INTERNAL -> new IcerikFabrikasiInternalRenderJobProgress(
                    "", state, 0, IcerikFabrikasiInternalRenderJobProgress.StageLabel.HAZIRLANIYOR);
        };
    }
}
