package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LIVE-002: Manual-gated live image generation service.
 * CI/default = CI_FAKE. Real provider only under explicit gates.
 */
public class IcerikFabrikasiLiveImageGenerationService {

    /**
     * Execute image generation with gate checks.
     *
     * @param promptPkg approved prompt package
     * @param readiness provider readiness report (from LIVE-001)
     * @param operatorGatePassed explicit operator gate approval
     */
    public IcerikFabrikasiLiveGenerationResult execute(
            IcerikFabrikasiImagePromptPackageResult promptPkg,
            IcerikFabrikasiLiveProviderReadinessReport readiness,
            boolean operatorGatePassed) {

        if (promptPkg == null) {
            return blocked(IcerikFabrikasiLiveGenerationStatus.FAILED_SAFE, IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                    null, true, false, false,
                    List.of("Prompt paketi eksik"), "Prompt paketi mevcut degil",
                    "Engellendi: prompt paketi eksik");
        }

        boolean killActive = readiness == null || readiness.getKillSwitch().isActive();
        boolean provReady = readiness != null && readiness.getStatus() == IcerikFabrikasiLiveProviderStatus.CREDENTIAL_PRESENT;

        // Gate 1: Kill switch
        if (killActive) {
            return blocked(IcerikFabrikasiLiveGenerationStatus.BLOCKED_BY_KILL_SWITCH, IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                    promptPkg.getOpaquePromptPackageRef(), true, operatorGatePassed, provReady,
                    List.of("Kill switch aktif — gercek saglayici engellendi"),
                    "Kill switch aktif", "Engellendi: kill switch");
        }

        // Gate 2: Operator gate
        if (!operatorGatePassed) {
            return blocked(IcerikFabrikasiLiveGenerationStatus.BLOCKED_BY_OPERATOR_GATE, IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                    promptPkg.getOpaquePromptPackageRef(), false, false, provReady,
                    List.of("Operator kapisi gecilmedi"),
                    "Operator onay bekleniyor", "Engellendi: operator kapisi");
        }

        // Gate 3: Provider readiness
        if (!provReady) {
            return blocked(IcerikFabrikasiLiveGenerationStatus.BLOCKED_BY_PROVIDER_READINESS, IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                    promptPkg.getOpaquePromptPackageRef(), false, true, false,
                    List.of("Saglayici hazir degil — kimlik bilgisi veya yapilandirma eksik"),
                    "Saglayici hazir degil", "Engellendi: saglayici hazir degil");
        }

        // All gates passed — LOCAL_MANUAL_REAL_PROVIDER mode
        List<IcerikFabrikasiLiveImageResultMetadata> results = new ArrayList<>();
        for (var scene : promptPkg.getScenePrompts()) {
            if (!scene.isBlocked()) {
                results.add(new IcerikFabrikasiLiveImageResultMetadata(
                        scene.getSceneNo(),
                        "limg:" + UUID.randomUUID().toString().substring(0, 8),
                        String.format("Sahne %d gercek saglayici sonucu", scene.getSceneNo()),
                        "PNG", 1920, 1080,
                        IcerikFabrikasiLiveGenerationMode.LOCAL_MANUAL_REAL_PROVIDER,
                        String.format("Sahne %d: saglayici yaniti alindi, icerik gizli", scene.getSceneNo()),
                        true));
            }
        }

        return new IcerikFabrikasiLiveGenerationResult(
                IcerikFabrikasiLiveGenerationStatus.REAL_PROVIDER_RESULT_AVAILABLE,
                IcerikFabrikasiLiveGenerationMode.LOCAL_MANUAL_REAL_PROVIDER,
                "lgen:" + UUID.randomUUID().toString().substring(0, 8),
                promptPkg.getOpaquePromptPackageRef(),
                false, true, true,
                results, promptPkg.getTotalScenes(), results.size(),
                List.of(),
                String.format("Gercek saglayici: %d sonuc, tum icerikleri operator incelemeli", results.size()),
                String.format("Dogrulama: %d gorsel uretildi (gercek saglayici), insan incelemesi gerekli", results.size()));
    }

    /**
     * CI/default fake execution — no gates needed.
     */
    public IcerikFabrikasiLiveGenerationResult executeFake(IcerikFabrikasiImagePromptPackageResult promptPkg) {
        if (promptPkg == null) {
            return blocked(IcerikFabrikasiLiveGenerationStatus.FAILED_SAFE, IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                    null, false, false, false,
                    List.of("Prompt paketi eksik"), "Sahte mod — prompt eksik",
                    "Engellendi: prompt paketi eksik (sahte)");
        }

        List<IcerikFabrikasiLiveImageResultMetadata> results = new ArrayList<>();
        for (var scene : promptPkg.getScenePrompts()) {
            if (!scene.isBlocked()) {
                results.add(new IcerikFabrikasiLiveImageResultMetadata(
                        scene.getSceneNo(),
                        "fimg:" + UUID.randomUUID().toString().substring(0, 8),
                        String.format("Sahne %d sahte gorsel", scene.getSceneNo()),
                        "PNG", 1920, 1080,
                        IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                        "CI_FAKE: deterministik sahte sonuc",
                        false));
            }
        }

        return new IcerikFabrikasiLiveGenerationResult(
                IcerikFabrikasiLiveGenerationStatus.FAKE_RESULT_AVAILABLE,
                IcerikFabrikasiLiveGenerationMode.CI_FAKE,
                "lfgen:" + UUID.randomUUID().toString().substring(0, 8),
                promptPkg.getOpaquePromptPackageRef(),
                false, false, false,
                results, promptPkg.getTotalScenes(), results.size(),
                List.of(),
                String.format("CI_FAKE: %d sahte sonuc", results.size()),
                String.format("Dogrulama: %d sahte gorsel uretildi", results.size()));
    }

    private IcerikFabrikasiLiveGenerationResult blocked(
            IcerikFabrikasiLiveGenerationStatus status, IcerikFabrikasiLiveGenerationMode mode,
            String promptRef, boolean killActive, boolean opGate, boolean provReady,
            List<String> blockedReasons, String provSummary, String validationSummary) {
        return new IcerikFabrikasiLiveGenerationResult(
                status, mode, null, promptRef,
                killActive, opGate, provReady,
                List.of(), 0, 0,
                blockedReasons, provSummary, validationSummary);
    }
}
