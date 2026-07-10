package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-020 Task 04: Provider-backed image batch execution service.
 *
 * <p>Runs image generation for each scene through IcerikFabrikasiGenerationProviderGateway.
 * Every item wrapped in safety envelope. Policy enforced pre/post.
 * Output sanitized. Opaque internal refs only.</p>
 *
 * <p>No base64, no public/signed/CDN URLs, no storage keys, no raw provider data.</p>
 */
@Service
public class IcerikFabrikasiProviderImageBatchService {

    private final IcerikFabrikasiVideoProjectRepository repository;
    private final IcerikFabrikasiGenerationProviderGateway gateway;
    private final IcerikFabrikasiProviderSafetySanitizer sanitizer;
    private final IcerikFabrikasiProviderSafetyPolicy safetyPolicy;

    public IcerikFabrikasiProviderImageBatchService(
            IcerikFabrikasiVideoProjectRepository repository,
            IcerikFabrikasiGenerationProviderGateway gateway,
            IcerikFabrikasiProviderSafetySanitizer sanitizer,
            IcerikFabrikasiProviderSafetyPolicy safetyPolicy) {
        this.repository = repository;
        this.gateway = gateway;
        this.sanitizer = sanitizer;
        this.safetyPolicy = safetyPolicy;
    }

    public IcerikFabrikasiImageBatchRunResponse runImageBatch(UUID projectId,
                                                     IcerikFabrikasiImageBatchRunRequest request) {

        IcerikFabrikasiVideoProject project = getProject(projectId);

        // Validate stage — must have image batch prepared
        if (project.getWorkflowStage() != IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED) {
            throw IcerikFabrikasiVideoProjectException.invalidStageTransition(
                    project.getWorkflowStage(),
                    IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PREPARED);
        }

        if (project.getImageBatch() == null || project.getImageBatch().getImageRequests().isEmpty()) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite(
                    "Görsel batch hazırlanmalıdır (prepare-image-batch)");
        }

        IcerikFabrikasiProviderMode mode = safetyPolicy.resolveProviderMode(project);

        // Check mode blocking
        IcerikFabrikasiImageBatchRunResponse modeBlock = checkModeBlocking(projectId, mode,
                project.getImageBatch());
        if (modeBlock != null) return modeBlock;

        // Sanitize request input
        sanitizeRequest(request);

        IcerikFabrikasiSceneImageBatch batch = project.getImageBatch();
        List<IcerikFabrikasiImageBatchRunSceneStatus> sceneStatuses = new ArrayList<>();
        List<IcerikFabrikasiSceneImageResult> imageResults = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        IcerikFabrikasiProviderTraceReference lastTrace = null;

        // Run each scene through gateway
        for (IcerikFabrikasiSceneImageRequest sceneReq : batch.getImageRequests()) {

            IcerikFabrikasiGenerationSafetyEnvelope envelope = IcerikFabrikasiGenerationSafetyEnvelope.createDefault(
                    projectId, "IMAGE_GENERATION", mode);
            safetyPolicy.enforcePreGenerationPolicy(envelope);

            IcerikFabrikasiImageGenerationRequest gatewayReq = IcerikFabrikasiImageGenerationRequest.builder()
                    .projectId(projectId)
                    .sceneNo(sceneReq.getSceneNo())
                    .safePromptSummary(sceneReq.getSafePromptSummary())
                    .safeNegativeNotes(sceneReq.getSafeNegativeNotes())
                    .styleHint(sceneReq.getStyleHint())
                    .aspectRatioHint(sceneReq.getAspectRatioHint())
                    .build();

            IcerikFabrikasiImageGenerationResult result = gateway.generateImage(gatewayReq, envelope);

            sanitizer.sanitizeEnvelope(envelope);
            safetyPolicy.enforcePostGenerationPolicy(envelope);

            lastTrace = result.getTraceRef();

            IcerikFabrikasiImageBatchRunSceneStatus sceneStatus = IcerikFabrikasiImageBatchRunSceneStatus.builder()
                    .sceneNo(sceneReq.getSceneNo())
                    .success(result.isSuccess())
                    .safeInternalRef(result.getSafeInternalRef())
                    .safeErrorCode(result.getSafeErrorCode())
                    .needsHumanReview(result.isNeedsHumanReview())
                    .build();
            sceneStatuses.add(sceneStatus);

            imageResults.add(new IcerikFabrikasiSceneImageResult(
                    sceneReq.getSceneNo(),
                    result.getSafeInternalRef(),
                    result.isSuccess(),
                    result.getSafeErrorCode()));

            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        // Update batch and project state
        batch.setImageResults(imageResults);

        IcerikFabrikasiImageBatchStatus batchStatus;
        IcerikFabrikasiVideoWorkflowStage newStage;
        if (failureCount == 0) {
            batchStatus = IcerikFabrikasiImageBatchStatus.COMPLETED;
            newStage = IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_COMPLETED;
        } else if (successCount > 0) {
            batchStatus = IcerikFabrikasiImageBatchStatus.PARTIAL;
            newStage = IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL;
        } else {
            batchStatus = IcerikFabrikasiImageBatchStatus.FAILED;
            newStage = IcerikFabrikasiVideoWorkflowStage.IMAGE_BATCH_PARTIAL;
        }
        batch.setStatus(batchStatus);
        project.advanceTo(newStage);
        repository.save(project);

        List<String> flags = new ArrayList<>(List.of(
                "HUMAN_REVIEW_REQUIRED",
                "COPYRIGHT_REVIEW_REQUIRED",
                "LYRICS_NOT_INCLUDED"));
        if (failureCount > 0) flags.add("PARTIAL_FAILURE");

        return IcerikFabrikasiImageBatchRunResponse.builder()
                .projectId(projectId)
                .batchId(batch.getBatchId())
                .providerMode(mode)
                .batchStatus(batchStatus)
                .currentStage(newStage)
                .safeOutputSummary("Batch çalıştırıldı: " + successCount + " başarılı, "
                        + failureCount + " başarısız, toplam " + batch.getImageRequests().size())
                .traceRef(lastTrace)
                .requestCount(batch.getImageRequests().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .humanReviewRequired(true)
                .autoApproved(false)
                .copyrightReviewRequired(true)
                .sceneStatuses(sceneStatuses)
                .reviewFlags(flags)
                .safeWarnings(failureCount > 0
                        ? List.of("Bazı sahneler üretilemedi: " + failureCount + " başarısız")
                        : List.of())
                .build();
    }

    // ── Private Helpers ─────────────────────────────────────

    private IcerikFabrikasiVideoProject getProject(UUID id) {
        return repository.findById(id)
                .orElseThrow(IcerikFabrikasiVideoProjectException::projectNotFound);
    }

    private IcerikFabrikasiImageBatchRunResponse checkModeBlocking(
            UUID projectId, IcerikFabrikasiProviderMode mode, IcerikFabrikasiSceneImageBatch batch) {

        if (mode == IcerikFabrikasiProviderMode.PROVIDER_DISABLED
                || mode == IcerikFabrikasiProviderMode.PROVIDER_FAILED_SAFE
                || mode == IcerikFabrikasiProviderMode.HUMAN_REVIEW_REQUIRED) {

            String failureCode = switch (mode) {
                case PROVIDER_DISABLED -> "PROVIDER_DISABLED";
                case PROVIDER_FAILED_SAFE -> "PROVIDER_FAILED_SAFE";
                case HUMAN_REVIEW_REQUIRED -> "HUMAN_REVIEW_PENDING";
                default -> "UNKNOWN";
            };

            return IcerikFabrikasiImageBatchRunResponse.builder()
                    .projectId(projectId)
                    .batchId(batch.getBatchId())
                    .providerMode(mode)
                    .batchStatus(IcerikFabrikasiImageBatchStatus.BLOCKED)
                    .safeOutputSummary("Sağlayıcı modu üretim için uygun değil: " + mode)
                    .safeFailureCode(failureCode)
                    .requestCount(batch.getImageRequests().size())
                    .successCount(0)
                    .failureCount(0)
                    .humanReviewRequired(true)
                    .autoApproved(false)
                    .copyrightReviewRequired(true)
                    .sceneStatuses(List.of())
                    .reviewFlags(List.of("HUMAN_REVIEW_REQUIRED", "COPYRIGHT_REVIEW_REQUIRED"))
                    .safeWarnings(List.of("Batch engellendi: " + mode))
                    .build();
        }
        return null;
    }

    private void sanitizeRequest(IcerikFabrikasiImageBatchRunRequest request) {
        if (request == null) return;
        if (request.getStyleOverrideHint() != null) {
            if (sanitizer.containsPromptInjection(request.getStyleOverrideHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Prompt injection tespit edildi", "PROMPT_INJECTION_DETECTED");
            }
            if (sanitizer.containsForbiddenPattern(request.getStyleOverrideHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Güvenlik ihlali: yasaklı desen tespit edildi", "UNSAFE_INPUT");
            }
        }
    }
}
