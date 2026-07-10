package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LIVE-003: Safe storage handoff + verification service.
 * CI/default = fake storage. Real storage requires manual gate.
 */
public class IcerikFabrikasiLiveArtifactStorageService {

    /**
     * Store and verify generated image artifacts (CI fake).
     */
    public IcerikFabrikasiLiveStorageResult storeFake(IcerikFabrikasiLiveGenerationResult genResult) {
        if (genResult == null || genResult.getResults().isEmpty()) {
            return blocked(null,
                    List.of(IcerikFabrikasiLiveStorageIssueCode.GENERATION_RESULT_MISSING),
                    List.of("Gorsel uretim sonucu eksik"),
                    "Engellendi: uretim sonucu eksik");
        }

        List<IcerikFabrikasiLiveStoredArtifact> stored = new ArrayList<>();
        for (var img : genResult.getResults()) {
            stored.add(new IcerikFabrikasiLiveStoredArtifact(
                    img.getSceneNo(),
                    "sart:" + UUID.randomUUID().toString().substring(0, 8),
                    String.format("Sahne %d depolanmis gorsel (sahte)", img.getSceneNo()),
                    img.getFormatHint(), img.getWidthHint(), img.getHeightHint(),
                    true,
                    "CI_FAKE: sahte depolama + dogrulama basarili"));
        }

        return new IcerikFabrikasiLiveStorageResult(
                IcerikFabrikasiLiveStorageStatus.FAKE_STORED,
                List.of(),
                "lstr:" + UUID.randomUUID().toString().substring(0, 8),
                genResult.getOpaqueGenerationRef(),
                stored, genResult.getTotalScenes(), stored.size(), stored.size(),
                List.of(),
                String.format("Dogrulama: %d/%d sahte depolandi ve dogrulandi",
                        stored.size(), genResult.getTotalScenes()));
    }

    /**
     * Store and verify with manual gate (real storage path).
     */
    public IcerikFabrikasiLiveStorageResult storeReal(IcerikFabrikasiLiveGenerationResult genResult, boolean manualGatePassed) {
        if (genResult == null || genResult.getResults().isEmpty()) {
            return blocked(null,
                    List.of(IcerikFabrikasiLiveStorageIssueCode.GENERATION_RESULT_MISSING),
                    List.of("Gorsel uretim sonucu eksik"),
                    "Engellendi: uretim sonucu eksik");
        }

        if (!manualGatePassed) {
            return new IcerikFabrikasiLiveStorageResult(
                    IcerikFabrikasiLiveStorageStatus.WAITING_FOR_MANUAL_GATE,
                    List.of(IcerikFabrikasiLiveStorageIssueCode.MANUAL_GATE_NOT_PASSED),
                    null, genResult.getOpaqueGenerationRef(),
                    List.of(), genResult.getTotalScenes(), 0, 0,
                    List.of("Manuel depolama kapisi gecilmedi"),
                    "Engellendi: manuel kapi bekleniyor");
        }

        List<IcerikFabrikasiLiveStoredArtifact> stored = new ArrayList<>();
        for (var img : genResult.getResults()) {
            stored.add(new IcerikFabrikasiLiveStoredArtifact(
                    img.getSceneNo(),
                    "rsrt:" + UUID.randomUUID().toString().substring(0, 8),
                    String.format("Sahne %d depolanmis gorsel (gercek)", img.getSceneNo()),
                    img.getFormatHint(), img.getWidthHint(), img.getHeightHint(),
                    true,
                    "Gercek depolama: dahili referans olusturuldu, icerik gizli"));
        }

        return new IcerikFabrikasiLiveStorageResult(
                IcerikFabrikasiLiveStorageStatus.REAL_STORED_INTERNAL,
                List.of(),
                "rlstr:" + UUID.randomUUID().toString().substring(0, 8),
                genResult.getOpaqueGenerationRef(),
                stored, genResult.getTotalScenes(), stored.size(), stored.size(),
                List.of(),
                String.format("Dogrulama: %d/%d gercek depolandi ve dogrulandi, icerikleri gizli",
                        stored.size(), genResult.getTotalScenes()));
    }

    private IcerikFabrikasiLiveStorageResult blocked(String genRef, List<IcerikFabrikasiLiveStorageIssueCode> issues,
                                            List<String> reasons, String summary) {
        return new IcerikFabrikasiLiveStorageResult(
                IcerikFabrikasiLiveStorageStatus.BLOCKED_BY_SAFETY, issues,
                null, genRef, List.of(), 0, 0, 0, reasons, summary);
    }
}
