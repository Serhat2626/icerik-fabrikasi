package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FP-020 Task 03: Provider-draft planning service.
 *
 * <p>Orchestrates provider-backed generation for story/scenario/storyboard.
 * Every call wraps with safety envelope, enforces policy, sanitizes output.
 * Uses IcerikFabrikasiGenerationProviderGateway (stub or real).</p>
 *
 * <p>Does NOT replace FP-019 prepare-* paths. generate-* is additive.</p>
 */
@Service
public class IcerikFabrikasiProviderDraftPlanningService {

    private final IcerikFabrikasiVideoProjectRepository repository;
    private final IcerikFabrikasiGenerationProviderGateway gateway;
    private final IcerikFabrikasiProviderSafetySanitizer sanitizer;
    private final IcerikFabrikasiProviderSafetyPolicy safetyPolicy;

    public IcerikFabrikasiProviderDraftPlanningService(
            IcerikFabrikasiVideoProjectRepository repository,
            IcerikFabrikasiGenerationProviderGateway gateway,
            IcerikFabrikasiProviderSafetySanitizer sanitizer,
            IcerikFabrikasiProviderSafetyPolicy safetyPolicy) {
        this.repository = repository;
        this.gateway = gateway;
        this.sanitizer = sanitizer;
        this.safetyPolicy = safetyPolicy;
    }

    // ── Generate Story ──────────────────────────────────────

    public IcerikFabrikasiProviderPlanningGenerationResponse generateStory(
            UUID projectId, IcerikFabrikasiProviderGenerationRequest request) {

        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);

        IcerikFabrikasiProviderMode mode = safetyPolicy.resolveProviderMode(project);

        // Check for non-generatable modes
        IcerikFabrikasiProviderPlanningGenerationResponse modeBlock = checkModeBlocking(
                projectId, mode, "STORY", project.getWorkflowStage());
        if (modeBlock != null) return modeBlock;

        // Create safety envelope
        IcerikFabrikasiGenerationSafetyEnvelope envelope = IcerikFabrikasiGenerationSafetyEnvelope.createDefault(
                projectId, "STORY", mode);
        safetyPolicy.enforcePreGenerationPolicy(envelope);

        // Sanitize input
        sanitizeRequest(request);

        // Build gateway request
        IcerikFabrikasiAiGenerationRequest aiRequest = IcerikFabrikasiAiGenerationRequest.builder()
                .projectId(projectId)
                .generationStage("STORY")
                .context(buildNarrativeContext(project))
                .promptTemplateId("icerikFabrikasi-story-v1")
                .safeParams(request != null && request.getSafeParams() != null
                        ? request.getSafeParams() : Map.of())
                .build();

        // Generate through gateway
        IcerikFabrikasiAiGenerationResult result = gateway.generateAi(aiRequest, envelope);

        // Sanitize and validate output
        sanitizer.sanitizeEnvelope(envelope);
        safetyPolicy.enforcePostGenerationPolicy(envelope);

        // Update project state
        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);
        project.setStoryPackage(buildStubStoryPackage(project, result));
        repository.save(project);

        return buildResponse(projectId, project.getWorkflowStage(), mode,
                "STORY", result, envelope);
    }

    // ── Generate Scenario ───────────────────────────────────

    public IcerikFabrikasiProviderPlanningGenerationResponse generateScenario(
            UUID projectId, IcerikFabrikasiProviderGenerationRequest request) {

        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);

        if (project.getStoryPackage() == null) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Hikâye paketi hazırlanmalıdır");
        }

        IcerikFabrikasiProviderMode mode = safetyPolicy.resolveProviderMode(project);

        IcerikFabrikasiProviderPlanningGenerationResponse modeBlock = checkModeBlocking(
                projectId, mode, "SCENARIO", project.getWorkflowStage());
        if (modeBlock != null) return modeBlock;

        IcerikFabrikasiGenerationSafetyEnvelope envelope = IcerikFabrikasiGenerationSafetyEnvelope.createDefault(
                projectId, "SCENARIO", mode);
        safetyPolicy.enforcePreGenerationPolicy(envelope);

        sanitizeRequest(request);

        IcerikFabrikasiAiGenerationRequest aiRequest = IcerikFabrikasiAiGenerationRequest.builder()
                .projectId(projectId)
                .generationStage("SCENARIO")
                .context(buildNarrativeContext(project))
                .promptTemplateId("icerikFabrikasi-scenario-v1")
                .safeParams(request != null && request.getSafeParams() != null
                        ? request.getSafeParams() : Map.of())
                .build();

        IcerikFabrikasiAiGenerationResult result = gateway.generateAi(aiRequest, envelope);

        sanitizer.sanitizeEnvelope(envelope);
        safetyPolicy.enforcePostGenerationPolicy(envelope);

        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);
        project.setScenarioPlan(buildStubScenarioPlan(project, result));
        repository.save(project);

        return buildResponse(projectId, project.getWorkflowStage(), mode,
                "SCENARIO", result, envelope);
    }

    // ── Generate Storyboard ─────────────────────────────────

    public IcerikFabrikasiProviderPlanningGenerationResponse generateStoryboard(
            UUID projectId, IcerikFabrikasiProviderGenerationRequest request) {

        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);

        if (project.getScenarioPlan() == null) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Senaryo planı hazırlanmalıdır");
        }

        IcerikFabrikasiProviderMode mode = safetyPolicy.resolveProviderMode(project);

        IcerikFabrikasiProviderPlanningGenerationResponse modeBlock = checkModeBlocking(
                projectId, mode, "STORYBOARD", project.getWorkflowStage());
        if (modeBlock != null) return modeBlock;

        IcerikFabrikasiGenerationSafetyEnvelope envelope = IcerikFabrikasiGenerationSafetyEnvelope.createDefault(
                projectId, "STORYBOARD", mode);
        safetyPolicy.enforcePreGenerationPolicy(envelope);

        sanitizeRequest(request);

        IcerikFabrikasiAiGenerationRequest aiRequest = IcerikFabrikasiAiGenerationRequest.builder()
                .projectId(projectId)
                .generationStage("STORYBOARD")
                .context(buildNarrativeContext(project))
                .promptTemplateId("icerikFabrikasi-storyboard-v1")
                .safeParams(request != null && request.getSafeParams() != null
                        ? request.getSafeParams() : Map.of())
                .build();

        IcerikFabrikasiAiGenerationResult result = gateway.generateAi(aiRequest, envelope);

        sanitizer.sanitizeEnvelope(envelope);
        safetyPolicy.enforcePostGenerationPolicy(envelope);

        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);
        project.setStoryboardPlan(buildStubStoryboardPlan(project, result));
        repository.save(project);

        return buildResponse(projectId, project.getWorkflowStage(), mode,
                "STORYBOARD", result, envelope);
    }

    // ── Generate Planning Bundle ────────────────────────────

    public IcerikFabrikasiPlanningBundleGenerationResponse generatePlanningBundle(
            UUID projectId, IcerikFabrikasiProviderGenerationRequest request) {

        IcerikFabrikasiVideoProject project = getProject(projectId);
        validateStage(project, IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED);

        IcerikFabrikasiProviderMode mode = safetyPolicy.resolveProviderMode(project);

        // Check mode blocking for the full bundle
        if (mode == IcerikFabrikasiProviderMode.PROVIDER_DISABLED
                || mode == IcerikFabrikasiProviderMode.PROVIDER_FAILED_SAFE
                || mode == IcerikFabrikasiProviderMode.HUMAN_REVIEW_REQUIRED) {
            return IcerikFabrikasiPlanningBundleGenerationResponse.builder()
                    .projectId(projectId)
                    .currentStage(project.getWorkflowStage())
                    .providerMode(mode)
                    .safeOutputSummary("Sağlayıcı modu üretim için uygun değil: " + mode)
                    .safeFailureCode(mode == IcerikFabrikasiProviderMode.PROVIDER_DISABLED
                            ? "PROVIDER_DISABLED" : mode == IcerikFabrikasiProviderMode.PROVIDER_FAILED_SAFE
                            ? "PROVIDER_FAILED_SAFE" : "HUMAN_REVIEW_PENDING")
                    .humanReviewRequired(true)
                    .autoApproved(false)
                    .copyrightReviewRequired(true)
                    .storyGenerated(false)
                    .scenarioGenerated(false)
                    .storyboardGenerated(false)
                    .reviewFlags(List.of("HUMAN_REVIEW_REQUIRED", "COPYRIGHT_REVIEW_REQUIRED"))
                    .safeWarnings(List.of("Üretim engellendi: " + mode))
                    .build();
        }

        // Execute bundle in sequence: story → scenario → storyboard
        IcerikFabrikasiProviderPlanningGenerationResponse storyResult = generateStory(projectId, request);
        IcerikFabrikasiProviderPlanningGenerationResponse scenarioResult = generateScenario(projectId, request);
        IcerikFabrikasiProviderPlanningGenerationResponse storyboardResult = generateStoryboard(projectId, request);

        List<String> allFlags = new ArrayList<>();
        allFlags.addAll(storyResult.getReviewFlags());
        addMissing(allFlags, scenarioResult.getReviewFlags());
        addMissing(allFlags, storyboardResult.getReviewFlags());

        return IcerikFabrikasiPlanningBundleGenerationResponse.builder()
                .projectId(projectId)
                .currentStage(storyboardResult.getCurrentStage())
                .providerMode(mode)
                .safeOutputSummary("Planlama paketi tamamlandı: hikâye + senaryo + storyboard")
                .traceRef(storyboardResult.getTraceRef())
                .outputConfidence(IcerikFabrikasiSourceConfidence.LOW)
                .humanReviewRequired(true)
                .autoApproved(false)
                .copyrightReviewRequired(true)
                .storyGenerated(true)
                .scenarioGenerated(true)
                .storyboardGenerated(true)
                .reviewFlags(allFlags)
                .safeWarnings(storyResult.getSafeWarnings())
                .build();
    }

    // ── Private Helpers ─────────────────────────────────────

    private IcerikFabrikasiVideoProject getProject(UUID id) {
        return repository.findById(id)
                .orElseThrow(IcerikFabrikasiVideoProjectException::projectNotFound);
    }

    private void validateStage(IcerikFabrikasiVideoProject project, IcerikFabrikasiVideoWorkflowStage expected) {
        if (project.getWorkflowStage() != expected) {
            throw IcerikFabrikasiVideoProjectException.invalidStageTransition(
                    project.getWorkflowStage(), expected);
        }
    }

    private IcerikFabrikasiProviderPlanningGenerationResponse checkModeBlocking(
            UUID projectId, IcerikFabrikasiProviderMode mode, String stage,
            IcerikFabrikasiVideoWorkflowStage currentStage) {

        if (mode == IcerikFabrikasiProviderMode.PROVIDER_DISABLED) {
            return buildBlockedResponse(projectId, currentStage, mode, stage,
                    "Sağlayıcı devre dışı bırakıldı", "PROVIDER_DISABLED");
        }
        if (mode == IcerikFabrikasiProviderMode.PROVIDER_FAILED_SAFE) {
            return buildBlockedResponse(projectId, currentStage, mode, stage,
                    "Sağlayıcı güvenli başarısızlık modunda", "PROVIDER_FAILED_SAFE");
        }
        if (mode == IcerikFabrikasiProviderMode.HUMAN_REVIEW_REQUIRED) {
            return buildBlockedResponse(projectId, currentStage, mode, stage,
                    "İnsan incelemesi bekleniyor", "HUMAN_REVIEW_PENDING");
        }
        return null;
    }

    private IcerikFabrikasiProviderPlanningGenerationResponse buildBlockedResponse(
            UUID projectId, IcerikFabrikasiVideoWorkflowStage stage, IcerikFabrikasiProviderMode mode,
            String genStage, String summary, String failureCode) {

        return IcerikFabrikasiProviderPlanningGenerationResponse.builder()
                .projectId(projectId)
                .currentStage(stage)
                .providerMode(mode)
                .generationStage(genStage)
                .safeOutputSummary(summary)
                .safeFailureCode(failureCode)
                .humanReviewRequired(true)
                .autoApproved(false)
                .copyrightReviewRequired(true)
                .reviewFlags(List.of("HUMAN_REVIEW_REQUIRED", "COPYRIGHT_REVIEW_REQUIRED"))
                .safeWarnings(List.of(summary))
                .build();
    }

    private void sanitizeRequest(IcerikFabrikasiProviderGenerationRequest request) {
        if (request == null) return;
        if (request.getNarrativeAngleHint() != null) {
            if (sanitizer.containsPromptInjection(request.getNarrativeAngleHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Prompt injection tespit edildi", "PROMPT_INJECTION_DETECTED");
            }
            if (sanitizer.containsForbiddenPattern(request.getNarrativeAngleHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Güvenlik ihlali: yasaklı desen tespit edildi", "UNSAFE_INPUT");
            }
        }
        if (request.getAdditionalContextHint() != null) {
            if (sanitizer.containsPromptInjection(request.getAdditionalContextHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Prompt injection tespit edildi", "PROMPT_INJECTION_DETECTED");
            }
            if (sanitizer.containsForbiddenPattern(request.getAdditionalContextHint())) {
                throw new IcerikFabrikasiVideoProjectException(
                        "Güvenlik ihlali: yasaklı desen tespit edildi", "UNSAFE_INPUT");
            }
        }
    }

    private IcerikFabrikasiNarrativeContext buildNarrativeContext(IcerikFabrikasiVideoProject project) {
        return new IcerikFabrikasiNarrativeContext(
                project.getMood() != null ? project.getMood() : "Geleneksel",
                "Duygusal yolculuk",
                project.getNarrationStyle() != null ? project.getNarrationStyle() : "Klasik anlatı",
                "Genel izleyici",
                project.getVisualStyle() != null ? project.getVisualStyle() : "Sinematik"
        );
    }

    private IcerikFabrikasiProviderPlanningGenerationResponse buildResponse(
            UUID projectId, IcerikFabrikasiVideoWorkflowStage stage, IcerikFabrikasiProviderMode mode,
            String genStage, IcerikFabrikasiAiGenerationResult result,
            IcerikFabrikasiGenerationSafetyEnvelope envelope) {

        return IcerikFabrikasiProviderPlanningGenerationResponse.builder()
                .projectId(projectId)
                .currentStage(stage)
                .providerMode(mode)
                .generationStage(genStage)
                .safeOutputSummary(result.getSafeOutputSummary())
                .traceRef(result.getTraceRef())
                .outputConfidence(result.getOutputConfidence())
                .humanReviewRequired(true)
                .autoApproved(false)
                .copyrightReviewRequired(true)
                .reviewFlags(result.getReviewFlags())
                .safeWarnings(result.getSafeWarnings())
                .build();
    }

    private IcerikFabrikasiStoryPackage buildStubStoryPackage(IcerikFabrikasiVideoProject project,
                                                     IcerikFabrikasiAiGenerationResult result) {
        return IcerikFabrikasiStoryPackage.builder()
                .icerikFabrikasiName(project.getIcerikFabrikasiName())
                .regionContext(project.getRegion() != null ? project.getRegion() : "Anadolu")
                .originSummary("Sağlayıcı tarafından üretilen özet (inceleme gerekli)")
                .emotionalTheme(project.getMood() != null ? project.getMood() : "Geleneksel Anadolu duygusu")
                .culturalSignificance("Kültürel bağlam — insan incelemesi gerektirir")
                .narrativeAngle("Sağlayıcı perspektifi")
                .storySummary(result.getSafeOutputSummary())
                .sourceConfidence(result.getOutputConfidence())
                .needsHumanReview(true)
                .copyrightReviewRequired(true)
                .reviewFlags(result.getReviewFlags())
                .build();
    }

    private IcerikFabrikasiScenarioPlan buildStubScenarioPlan(IcerikFabrikasiVideoProject project,
                                                     IcerikFabrikasiAiGenerationResult result) {
        return IcerikFabrikasiScenarioPlan.builder()
                .title(project.getIcerikFabrikasiName() + " — Senaryo")
                .logline(result.getSafeOutputSummary())
                .narrationTone(project.getMood() != null ? project.getMood() : "Hüzünlü")
                .targetDurationHintSeconds(project.getTargetDurationMinutes() != null
                        ? project.getTargetDurationMinutes() * 60 : 480)
                .sceneCountHint(6)
                .scenarioBeats(List.of(
                        "Hook (inceleme gerekli)",
                        "Giriş (inceleme gerekli)",
                        "Ana hikâye (inceleme gerekli)",
                        "Duygusal zirve (inceleme gerekli)",
                        "Kapanış (inceleme gerekli)",
                        "CTA (inceleme gerekli)"))
                .reviewFlags(result.getReviewFlags())
                .build();
    }

    private IcerikFabrikasiStoryboardPlan buildStubStoryboardPlan(IcerikFabrikasiVideoProject project,
                                                         IcerikFabrikasiAiGenerationResult result) {
        List<IcerikFabrikasiStoryboardScene> scenes = List.of(
                new IcerikFabrikasiStoryboardScene(
                        1, "Sahne 1 (inceleme gerekli)",
                        "Sağlayıcı görsel 1",
                        "Sağlayıcı anlatım 1",
                        project.getMood() != null ? project.getMood() : "Hüzünlü",
                        30, "Geniş açı", "Fade in",
                        "Arka plan (telif incelemesi gerekli)")
        );

        return new IcerikFabrikasiStoryboardPlan(scenes, 30,
                result.getReviewFlags());
    }

    private void addMissing(List<String> target, List<String> source) {
        if (source == null) return;
        for (String s : source) {
            if (!target.contains(s)) target.add(s);
        }
    }
}
