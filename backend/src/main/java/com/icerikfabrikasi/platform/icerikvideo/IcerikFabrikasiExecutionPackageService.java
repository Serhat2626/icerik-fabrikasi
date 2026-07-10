package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RPE-004: Aggregates work order + provider config + render config into execution package.
 * All execution disabled. Manual gates metadata only.
 */
public class IcerikFabrikasiExecutionPackageService {

    private static final List<IcerikFabrikasiManualGate> DEFAULT_GATES = List.of(
            new IcerikFabrikasiManualGate(1, "Gorsel saglayici yapilandirmasi", "Saglayici kimlik bilgilerini dogrulayin", false, "Saglayici yapilandirilmadi"),
            new IcerikFabrikasiManualGate(2, "Render ortami dogrulamasi", "FFmpeg ortamini dogrulayin", false, "Render ortami yapilandirilmadi"),
            new IcerikFabrikasiManualGate(3, "Telif guvenlik kontrolu", "Tum sahnelerin telif durumunu onaylayin", false, "Telif kontrolu bekleniyor"),
            new IcerikFabrikasiManualGate(4, "Son kalite kontrolu", "Storyboard ve sahne planini son kez inceleyin", false, "Kalite kontrolu bekleniyor"),
            new IcerikFabrikasiManualGate(5, "Operator yurutme onayi", "Yurutme paketini onayla ve basla", false, "Operator onayi bekleniyor"));

    private static final List<IcerikFabrikasiExecutionCapability> DISABLED_CAPABILITIES = List.of(
            new IcerikFabrikasiExecutionCapability("Gorsel uretim", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("AI saglayici", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("Video render", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("FFmpeg otomasyon", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("Depolama teslim", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("Herkese acik oynatici", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("Indirme/disa aktarma", false, "RPE-004: devre disi"),
            new IcerikFabrikasiExecutionCapability("Yayinlama", false, "RPE-004: devre disi"));

    private static final List<String> FUTURE_STEPS = List.of(
            "Gorsel saglayiciyi etkinlestirin ve yapilandirin (RPE-005+)",
            "Render hattini etkinlestirin (RPE-006+)",
            "FFmpeg otomasyonunu genisletin (RPE-007+)",
            "Depolama teslimini etkinlestirin (RPE-008+)",
            "Kontrollu teslim/yayini degerlendirin (RPE-009+)");

    public IcerikFabrikasiExecutionPackageResult assemble(
            IcerikFabrikasiWorkOrderPreviewResult workOrder,
            IcerikFabrikasiProviderConfigResult providerConfig,
            IcerikFabrikasiRenderPipelineConfigResult renderConfig) {

        if (workOrder == null || providerConfig == null || renderConfig == null) {
            List<IcerikFabrikasiExecutionPackageIssueCode> issues = new ArrayList<>();
            if (workOrder == null) issues.add(IcerikFabrikasiExecutionPackageIssueCode.WORK_ORDER_NOT_ELIGIBLE);
            if (providerConfig == null) issues.add(IcerikFabrikasiExecutionPackageIssueCode.IMAGE_PROVIDER_NOT_CONFIGURED);
            if (renderConfig == null) issues.add(IcerikFabrikasiExecutionPackageIssueCode.RENDER_PIPELINE_NOT_CONFIGURED);
            return new IcerikFabrikasiExecutionPackageResult(
                    IcerikFabrikasiExecutionPackageStatus.BLOCKED_BY_SAFETY, issues,
                    null, null, null,
                    "Saglayici bilgisi eksik", "Render bilgisi eksik",
                    DEFAULT_GATES, DISABLED_CAPABILITIES,
                    "Yurutme paketi olusturulamadi — on kosullar karsilanmadi",
                    List.of("On kosullari tamamlayin"), "Engellendi: eksik bilesen");
        }

        List<IcerikFabrikasiExecutionPackageIssueCode> issues = new ArrayList<>();
        issues.add(IcerikFabrikasiExecutionPackageIssueCode.MANUAL_GATE_NOT_PASSED);
        if (!providerConfig.isExecutionEnabled())
            issues.add(IcerikFabrikasiExecutionPackageIssueCode.IMAGE_PROVIDER_NOT_CONFIGURED);
        if (!renderConfig.isRenderExecutionEnabled())
            issues.add(IcerikFabrikasiExecutionPackageIssueCode.RENDER_PIPELINE_NOT_CONFIGURED);

        String title = workOrder.getSafeIcerikFabrikasiTitleLabel() != null ? workOrder.getSafeIcerikFabrikasiTitleLabel() : "Proje";

        return new IcerikFabrikasiExecutionPackageResult(
                IcerikFabrikasiExecutionPackageStatus.WAITING_FOR_MANUAL_GATE, issues,
                "epkg:" + UUID.randomUUID().toString().substring(0, 8),
                workOrder.getOpaqueWorkOrderRef(),
                title,
                String.format("Saglayici: %s", providerConfig.getStatus()),
                String.format("Render: %s", renderConfig.getStatus()),
                DEFAULT_GATES, DISABLED_CAPABILITIES,
                String.format("%s — Yurutme paketi hazir degil, manuel kapilar bekleniyor", title),
                FUTURE_STEPS,
                String.format("Dogrulama: %d sorun, tum yetenekler devre disi", issues.size()));
    }
}
