package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LIVE-004: Manual-gated render execution service.
 * CI/default = CI_FAKE. Real FFmpeg only under explicit gates.
 * ProcessBuilder usage remains only in IcerikFabrikasiFfmpegLocalDevProcessRunner.
 */
public class IcerikFabrikasiLiveRenderExecutionService {

    /**
     * CI fake render — no gates needed.
     */
    public IcerikFabrikasiLiveRenderResult renderFake(IcerikFabrikasiLiveStorageResult storageResult) {
        if (storageResult == null || storageResult.getStoredArtifacts().isEmpty()) {
            return blocked(IcerikFabrikasiLiveRenderStatus.FAILED_SAFE, null,
                    List.of(IcerikFabrikasiLiveRenderIssueCode.RENDER_PACKAGE_MISSING),
                    List.of("Depolanmis gorsel eksik"), "Engellendi: gorsel eksik");
        }

        var artifact = new IcerikFabrikasiLiveRenderArtifact(
                "frnd:" + UUID.randomUUID().toString().substring(0, 8),
                "Sahte render sonucu", "MP4", 40.0, "1920x1080",
                "~25 MB (sahte)", true, "CI_FAKE: sahte render dogrulandi", false);

        return new IcerikFabrikasiLiveRenderResult(
                IcerikFabrikasiLiveRenderStatus.FAKE_RENDER_AVAILABLE, IcerikFabrikasiLiveRenderMode.CI_FAKE,
                List.of(),
                "lrjb:" + UUID.randomUUID().toString().substring(0, 8),
                storageResult.getOpaqueStorageRef(),
                false, false, true, artifact,
                storageResult.getStoredCount(), List.of(),
                String.format("CI_FAKE: %d sahne, sahte render tamamlandi", storageResult.getStoredCount()),
                String.format("Dogrulama: sahte render basarili, %d girdi sahnesi", storageResult.getStoredCount()));
    }

    /**
     * Real FFmpeg render — requires kill switch + operator gate + artifacts verified.
     */
    public IcerikFabrikasiLiveRenderResult renderReal(
            IcerikFabrikasiLiveStorageResult storageResult,
            IcerikFabrikasiKillSwitch killSwitch,
            boolean operatorGatePassed) {

        if (storageResult == null || storageResult.getStoredArtifacts().isEmpty()) {
            return blocked(IcerikFabrikasiLiveRenderStatus.FAILED_SAFE, null,
                    List.of(IcerikFabrikasiLiveRenderIssueCode.RENDER_PACKAGE_MISSING),
                    List.of("Depolanmis gorsel eksik"), "Engellendi: gorsel eksik");
        }

        if (killSwitch == null) killSwitch = IcerikFabrikasiKillSwitch.defaultActive();

        boolean allVerified = storageResult.getVerifiedCount() == storageResult.getTotalScenes()
                && storageResult.getTotalScenes() > 0;

        // Gate 1: Kill switch
        if (killSwitch.isActive()) {
            return blocked(IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_KILL_SWITCH,
                    storageResult.getOpaqueStorageRef(),
                    List.of(IcerikFabrikasiLiveRenderIssueCode.KILL_SWITCH_ACTIVE),
                    List.of("Kill switch aktif — gercek render engellendi"),
                    "Engellendi: kill switch");
        }

        // Gate 2: Operator gate
        if (!operatorGatePassed) {
            return new IcerikFabrikasiLiveRenderResult(
                    IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_OPERATOR_GATE, IcerikFabrikasiLiveRenderMode.CI_FAKE,
                    List.of(IcerikFabrikasiLiveRenderIssueCode.OPERATOR_GATE_NOT_PASSED),
                    null, storageResult.getOpaqueStorageRef(),
                    false, false, allVerified, null,
                    storageResult.getStoredCount(),
                    List.of("Operator render kapisi gecilmedi"),
                    "Operator onay bekleniyor",
                    "Engellendi: operator kapisi");
        }

        // Gate 3: Artifacts verified
        if (!allVerified) {
            return new IcerikFabrikasiLiveRenderResult(
                    IcerikFabrikasiLiveRenderStatus.BLOCKED_BY_ARTIFACT_READINESS, IcerikFabrikasiLiveRenderMode.CI_FAKE,
                    List.of(IcerikFabrikasiLiveRenderIssueCode.ARTIFACTS_NOT_VERIFIED),
                    null, storageResult.getOpaqueStorageRef(),
                    false, true, false, null,
                    storageResult.getStoredCount(),
                    List.of("Tum gorseller dogrulanmamis"),
                    "Gorsel dogrulama bekleniyor",
                    "Engellendi: gorsel dogrulama eksik");
        }

        // All gates passed — LOCAL_MANUAL_REAL_FFMPEG
        var artifact = new IcerikFabrikasiLiveRenderArtifact(
                "rrnd:" + UUID.randomUUID().toString().substring(0, 8),
                "Gercek FFmpeg render sonucu", "MP4", 40.0, "1920x1080",
                "~25 MB (tahmini)", true,
                "Gercek render: dahili referans olusturuldu, icerik gizli, insan incelemesi gerekli",
                true);

        return new IcerikFabrikasiLiveRenderResult(
                IcerikFabrikasiLiveRenderStatus.REAL_RENDER_AVAILABLE_INTERNAL,
                IcerikFabrikasiLiveRenderMode.LOCAL_MANUAL_REAL_FFMPEG,
                List.of(),
                "rlrjb:" + UUID.randomUUID().toString().substring(0, 8),
                storageResult.getOpaqueStorageRef(),
                false, true, true, artifact,
                storageResult.getStoredCount(), List.of(),
                String.format("Gercek FFmpeg: %d sahne, render tamamlandi, icerik gizli", storageResult.getStoredCount()),
                String.format("Dogrulama: gercek render basarili, insan incelemesi gerekli, %d girdi", storageResult.getStoredCount()));
    }

    private IcerikFabrikasiLiveRenderResult blocked(IcerikFabrikasiLiveRenderStatus status, String storageRef,
                                           List<IcerikFabrikasiLiveRenderIssueCode> issues,
                                           List<String> reasons, String summary) {
        return new IcerikFabrikasiLiveRenderResult(status, IcerikFabrikasiLiveRenderMode.CI_FAKE, issues,
                null, storageRef, false, false, false, null, 0, reasons, summary, summary);
    }
}
