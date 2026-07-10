package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-019 Task 03: Planning service for story/scenario/storyboard orchestration.
 *
 * <p>Uses deterministic internal generation (stubs) for Task 03.
 * No external AI provider integration yet. Provider-ready seams can be
 * added in future tasks when a safe adapter is established.</p>
 *
 * <p>Enforces controlled stage transitions:
 * DRAFT_CREATED → STORY_CONTEXT_PREPARED → SCENARIO_PREPARED → STORYBOARD_PREPARED</p>
 *
 * <p>No lyrics, no copyrighted media, no public URLs, no raw provider data.</p>
 */
@Service
public class IcerikFabrikasiVideoPlanningService {

    private final IcerikFabrikasiVideoProjectRepository repository;

    public IcerikFabrikasiVideoPlanningService(IcerikFabrikasiVideoProjectRepository repository) {
        this.repository = repository;
    }

    // ── Story Preparation ───────────────────────────────────

    public IcerikFabrikasiStoryPackage prepareStory(UUID projectId, IcerikFabrikasiStoryPreparationRequest request) {
        IcerikFabrikasiVideoProject project = getAndValidateProject(projectId);
        validateStageTransition(project, IcerikFabrikasiVideoWorkflowStage.DRAFT_CREATED,
                IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);

        if (request != null) {
            validateOptionalHint(request.getNarrativeAngleHint(), "narrativeAngleHint", 200);
            validateOptionalHint(request.getAdditionalContextHint(), "additionalContextHint", 500);
        }

        // Deterministic internal generation (stub)
        List<String> reviewFlags = buildStoryReviewFlags(project);

        IcerikFabrikasiStoryPackage storyPackage = IcerikFabrikasiStoryPackage.builder()
                .icerikFabrikasiName(project.getIcerikFabrikasiName())
                .regionContext(project.getRegion() != null ? project.getRegion() : "Anadolu")
                .originSummary(buildOriginSummary(project))
                .emotionalTheme(project.getMood() != null ? project.getMood() : "Geleneksel Anadolu duygusu")
                .culturalSignificance("Bu türkü, Anadolu halk geleneğinin önemli bir parçasıdır. " +
                        "Kültürel bağlamı ve anlamı detaylı inceleme gerektirir.")
                .narrativeAngle(request != null && request.getNarrativeAngleHint() != null
                        ? request.getNarrativeAngleHint()
                        : "Hikâye anlatıcısı perspektifi")
                .storySummary(buildStorySummary(project))
                .sourceConfidence(project.getSourceConfidence())
                .needsHumanReview(true) // always true for story packages
                .copyrightReviewRequired(true) // always true
                .reviewFlags(reviewFlags)
                .build();

        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED);
        project.setStoryPackage(storyPackage);
        repository.save(project);

        return storyPackage;
    }

    // ── Scenario Preparation ────────────────────────────────

    public IcerikFabrikasiScenarioPlan prepareScenario(UUID projectId, IcerikFabrikasiScenarioPreparationRequest request) {
        IcerikFabrikasiVideoProject project = getAndValidateProject(projectId);
        validateStageTransition(project, IcerikFabrikasiVideoWorkflowStage.STORY_CONTEXT_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);

        if (project.getStoryPackage() == null) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Hikâye paketi hazırlanmalıdır");
        }

        if (request != null) {
            validateOptionalHint(request.getNarrationToneHint(), "narrationToneHint", 200);
        }

        int sceneCount = (request != null && request.getSceneCountHint() != null)
                ? Math.max(3, Math.min(request.getSceneCountHint(), 12))
                : 6;

        String tone = (request != null && request.getNarrationToneHint() != null)
                ? request.getNarrationToneHint()
                : (project.getNarrationStyle() != null ? project.getNarrationStyle() : "Hikâyeci");

        int durationHint = project.getTargetDurationMinutes() != null
                ? project.getTargetDurationMinutes() * 60 : 240;

        List<String> beats = buildScenarioBeats(project, sceneCount);
        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("LYRICS_NOT_INCLUDED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        if (project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.UNKNOWN
                || project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.LOW) {
            reviewFlags.add("SOURCE_REVIEW_REQUIRED");
        }

        IcerikFabrikasiScenarioPlan plan = IcerikFabrikasiScenarioPlan.builder()
                .title(project.getIcerikFabrikasiName() + " — Video Senaryosu")
                .logline("\"" + project.getIcerikFabrikasiName() + "\" türküsünün hikâyesini anlatan " +
                         "kısa belgesel format video senaryosu.")
                .narrationTone(tone)
                .targetDurationHintSeconds(durationHint)
                .sceneCountHint(sceneCount)
                .scenarioBeats(beats)
                .reviewFlags(reviewFlags)
                .build();

        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED);
        project.setScenarioPlan(plan);
        repository.save(project);

        return plan;
    }

    // ── Storyboard Preparation ──────────────────────────────

    public IcerikFabrikasiStoryboardPlan prepareStoryboard(UUID projectId, IcerikFabrikasiStoryboardPreparationRequest request) {
        IcerikFabrikasiVideoProject project = getAndValidateProject(projectId);
        validateStageTransition(project, IcerikFabrikasiVideoWorkflowStage.SCENARIO_PREPARED,
                IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);

        if (project.getScenarioPlan() == null) {
            throw IcerikFabrikasiVideoProjectException.missingPrerequisite("Senaryo planı hazırlanmalıdır");
        }

        if (request != null) {
            validateOptionalHint(request.getVisualStyleHint(), "visualStyleHint", 200);
            validateOptionalHint(request.getCameraStyleHint(), "cameraStyleHint", 200);
        }

        String visualStyle = (request != null && request.getVisualStyleHint() != null)
                ? request.getVisualStyleHint()
                : (project.getVisualStyle() != null ? project.getVisualStyle() : "Sinematik Anadolu");

        String cameraStyle = (request != null && request.getCameraStyleHint() != null)
                ? request.getCameraStyleHint() : "Yavaş geçişli belgesel";

        IcerikFabrikasiScenarioPlan scenario = project.getScenarioPlan();
        int sceneCount = scenario.getSceneCountHint();
        int durationPerScene = scenario.getTargetDurationHintSeconds() != null
                ? scenario.getTargetDurationHintSeconds() / Math.max(1, sceneCount) : 40;

        List<IcerikFabrikasiStoryboardScene> scenes = buildStoryboardScenes(
                project, scenario, sceneCount, durationPerScene, visualStyle, cameraStyle);

        int totalDuration = scenes.stream().mapToInt(IcerikFabrikasiStoryboardScene::getEstimatedDurationSeconds).sum();

        List<String> reviewFlags = new ArrayList<>();
        reviewFlags.add("LYRICS_NOT_INCLUDED");
        reviewFlags.add("COPYRIGHT_REVIEW_REQUIRED");
        reviewFlags.add("CULTURAL_CONTEXT_REVIEW_REQUIRED");

        IcerikFabrikasiStoryboardPlan plan = new IcerikFabrikasiStoryboardPlan(scenes, totalDuration, reviewFlags);

        project.advanceTo(IcerikFabrikasiVideoWorkflowStage.STORYBOARD_PREPARED);
        project.setStoryboardPlan(plan);
        repository.save(project);

        return plan;
    }

    // ── Validation Helpers ──────────────────────────────────

    private IcerikFabrikasiVideoProject getAndValidateProject(UUID id) {
        return repository.findById(id)
                .orElseThrow(IcerikFabrikasiVideoProjectException::projectNotFound);
    }

    private void validateStageTransition(IcerikFabrikasiVideoProject project,
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
        // unsafe content check
        if (value.matches("(?i).*(javascript:|<script|on\\w+=|password|secret|token|bearer|authorization|eyJ[A-Za-z0-9]).*")) {
            throw IcerikFabrikasiVideoProjectException.unsafeInput(fieldName);
        }
    }

    // ── Deterministic Generation (Stubs) ────────────────────

    private String buildOriginSummary(IcerikFabrikasiVideoProject project) {
        String region = project.getRegion() != null ? project.getRegion() : "Anadolu";
        String notes = project.getSourceNotes() != null ? " " + project.getSourceNotes() : "";
        return "\"" + project.getIcerikFabrikasiName() + "\" türküsü, " + region +
                " yöresine ait geleneksel bir halk türküsüdür." + notes +
                " Kesin tarihsel bilgi için uzman incelemesi önerilir.";
    }

    private String buildStorySummary(IcerikFabrikasiVideoProject project) {
        return "\"" + project.getIcerikFabrikasiName() + "\" türküsünün hikâyesi, Anadolu kültürel geleneği " +
                "içinde önemli bir yere sahiptir. Bu özet, ön araştırma niteliğindedir ve " +
                "yayın öncesi uzman incelemesi gerektirir. Şu anda sözleri dahil edilmemiştir.";
    }

    private List<String> buildStoryReviewFlags(IcerikFabrikasiVideoProject project) {
        List<String> flags = new ArrayList<>();
        flags.add("LYRICS_NOT_INCLUDED");
        flags.add("COPYRIGHT_REVIEW_REQUIRED");
        if (project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.UNKNOWN
                || project.getSourceConfidence() == IcerikFabrikasiSourceConfidence.LOW) {
            flags.add("SOURCE_REVIEW_REQUIRED");
        }
        flags.add("CULTURAL_CONTEXT_REVIEW_REQUIRED");
        return flags;
    }

    private List<String> buildScenarioBeats(IcerikFabrikasiVideoProject project, int sceneCount) {
        String name = project.getIcerikFabrikasiName();
        List<String> beats = new ArrayList<>();
        beats.add("[Hook] \"" + name + "\" — Bu türkünün arkasında nasıl bir hikâye yatıyor?");
        beats.add("[Giriş] " + name + " türküsünün doğduğu coğrafya ve dönem.");
        beats.add("[Hikâye] Türkünün anlattığı olaylar ve karakterler.");
        beats.add("[Duygusal Zirve] Türkünün en derin duygusal mesajı.");
        beats.add("[Kapanış] Bu türkünün bugün bize ne anlattığı.");
        beats.add("[CTA] Yorumlarınızı paylaşın — bu türkünün sizin için anlamı nedir?");
        // sceneCount'a göre ek detaylar eklenebilir
        if (sceneCount > 6) {
            for (int i = 7; i <= sceneCount; i++) {
                beats.add("[Ek Sahne " + i + "] Kültürel/tarihi bağlam detayı.");
            }
        }
        return beats;
    }

    private List<IcerikFabrikasiStoryboardScene> buildStoryboardScenes(
            IcerikFabrikasiVideoProject project, IcerikFabrikasiScenarioPlan scenario,
            int sceneCount, int durationPerScene, String visualStyle, String cameraStyle) {
        List<String> beats = scenario.getScenarioBeats();
        List<IcerikFabrikasiStoryboardScene> scenes = new ArrayList<>();

        String[] moods = {"meraklı", "nostaljik", "hüzünlü", "derin", "huzurlu", "umutlu"};
        String[] cameras = {"Geniş açı manzara", "Yakın çekim detay", "Orta plan sahne",
                            "Yavaş pan hareket", "Sabit kare çekim", "Geçiş efekti"};
        String[] transitions = {"Yumuşak geçiş", "Çapraz geçiş", "Karartma", "Sert kesim",
                                "Yumuşak geçiş", "Son kare"};

        for (int i = 0; i < Math.min(sceneCount, beats.size()); i++) {
            scenes.add(new IcerikFabrikasiStoryboardScene(
                    i + 1,
                    "Sahne " + (i + 1),
                    visualStyle + " — " + beats.get(i).replaceAll("\\[.*?]\\s*", ""),
                    beats.get(i),
                    moods[i % moods.length],
                    durationPerScene,
                    cameras[i % cameras.length] + " (" + cameraStyle + ")",
                    transitions[i % transitions.length],
                    "Geleneksel Anadolu enstrümanları — telif kontrol gerekli"
            ));
        }
        return scenes;
    }
}
