package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RPE-007: Verifies fake artifacts and prepares render package candidate.
 * CI/default = fake/test-double. No real storage/render/FFmpeg.
 */
public class IcerikFabrikasiArtifactVerificationService {

    private static final List<String> RENDER_PREREQUISITES = List.of(
            "Tum sahne gorselleri dogrulanmis olmali",
            "Ses/muzik dosyalari hazir olmali",
            "Telif guvenlik kontrolu tamamlanmis olmali",
            "FFmpeg ortami dogrulanmis olmali",
            "Operator render onay kapisi gecilmis olmali");

    public IcerikFabrikasiArtifactVerificationReport verify(IcerikFabrikasiImageGenerationExecutionResult genResult) {
        if (genResult == null) {
            return new IcerikFabrikasiArtifactVerificationReport(
                    IcerikFabrikasiArtifactVerificationStatus.BLOCKED_BY_SAFETY,
                    null, null, List.of(), 0, 0,
                    "Engellendi: gorsel uretim sonucu eksik");
        }

        List<IcerikFabrikasiSceneArtifactReadiness> readiness = new ArrayList<>();
        for (var fake : genResult.getFakeResults()) {
            readiness.add(new IcerikFabrikasiSceneArtifactReadiness(
                    fake.getSceneNo(),
                    fake.getOpaqueArtifactRef(),
                    fake.getArtifactLabel(),
                    true,
                    "CI_FAKE: sahte dogrulama basarili"));
        }

        return new IcerikFabrikasiArtifactVerificationReport(
                IcerikFabrikasiArtifactVerificationStatus.FAKE_VERIFIED,
                "avrf:" + UUID.randomUUID().toString().substring(0, 8),
                genResult.getOpaqueGenerationRef(),
                readiness,
                genResult.getTotalScenes(),
                readiness.size(),
                String.format("Dogrulama: %d/%d sahne dogrulandi (sahte)", readiness.size(), genResult.getTotalScenes()));
    }

    public IcerikFabrikasiRenderPackageResult prepareRenderPackage(IcerikFabrikasiArtifactVerificationReport verification) {
        if (verification == null || verification.getStatus() != IcerikFabrikasiArtifactVerificationStatus.FAKE_VERIFIED) {
            return new IcerikFabrikasiRenderPackageResult(
                    IcerikFabrikasiRenderPackageStatus.BLOCKED_BY_SAFETY,
                    null, null,
                    "Render giris manifesti olusturulamadi",
                    RENDER_PREREQUISITES, false,
                    "Yerel/manuel render devre disi — dogrulama eksik",
                    List.of("Dogrulama tamamlanmadi"), "Engellendi: dogrulama eksik");
        }

        List<String> blocked = new ArrayList<>();
        blocked.add("FFmpeg ortami yapilandirilmadi");
        blocked.add("Operator render onay kapisi gecilmedi");

        return new IcerikFabrikasiRenderPackageResult(
                IcerikFabrikasiRenderPackageStatus.WAITING_FOR_MANUAL_GATE,
                "rpkg:" + UUID.randomUUID().toString().substring(0, 8),
                verification.getOpaqueVerificationRef(),
                String.format("%d sahne gorseli dogrulandi, render giris manifesti hazir (sahte)", verification.getVerifiedScenes()),
                RENDER_PREREQUISITES,
                false,
                "Yerel/manuel render devre disi — RPE-007: henuz etkinlestirilmedi",
                blocked,
                String.format("Dogrulama: render paketi taslagi, %d on kosul bekleniyor", blocked.size()));
    }
}
