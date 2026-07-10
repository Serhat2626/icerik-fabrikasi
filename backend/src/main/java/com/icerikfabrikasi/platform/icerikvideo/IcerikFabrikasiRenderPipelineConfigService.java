package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RPE-003: Maps provider config to render pipeline configuration boundary.
 * Render execution disabled. Metadata only.
 */
public class IcerikFabrikasiRenderPipelineConfigService {

    private static final IcerikFabrikasiRenderEnvironmentBoundary DEFAULT_ENV =
            new IcerikFabrikasiRenderEnvironmentBoundary(
                    "Render ortam\u0131 (yerel/manuel)",
                    false,
                    "Ortam hen\u00fcz yap\u0131land\u0131r\u0131lmad\u0131");

    private static final List<IcerikFabrikasiFfmpegCapabilityMetadata> DEFAULT_FFMPEG = List.of(
            new IcerikFabrikasiFfmpegCapabilityMetadata("Video birle\u015ftirme", false, "RPE-003: hen\u00fcz etkinle\u015ftirilmedi"),
            new IcerikFabrikasiFfmpegCapabilityMetadata("Ses birle\u015ftirme", false, "RPE-003: hen\u00fcz etkinle\u015ftirilmedi"),
            new IcerikFabrikasiFfmpegCapabilityMetadata("Format d\u00f6n\u00fc\u015ft\u00fcrme", false, "RPE-003: hen\u00fcz etkinle\u015ftirilmedi"),
            new IcerikFabrikasiFfmpegCapabilityMetadata("\u00c7\u00f6z\u00fcn\u00fcrl\u00fck \u00f6l\u00e7ekleme", false, "RPE-003: hen\u00fcz etkinle\u015ftirilmedi"),
            new IcerikFabrikasiFfmpegCapabilityMetadata("Ge\u00e7i\u015f efektleri", false, "RPE-003: hen\u00fcz etkinle\u015ftirilmedi"));

    private static final IcerikFabrikasiOutputFormatPolicy DEFAULT_OUTPUT =
            new IcerikFabrikasiOutputFormatPolicy("MP4 (varsay\u0131lan)", 1920, 1080, 24, "Politika hen\u00fcz yap\u0131land\u0131r\u0131lmad\u0131");

    private static final List<String> SAFETY_PREREQUISITES = List.of(
            "G\u00f6rsel sa\u011flay\u0131c\u0131 yap\u0131land\u0131r\u0131lm\u0131\u015f olmal\u0131",
            "T\u00fcm sahne g\u00f6rselleri \u00fcretilmi\u015f olmal\u0131",
            "Telif kontrol\u00fc tamamlanm\u0131\u015f olmal\u0131",
            "Operat\u00f6r render ortam\u0131n\u0131 onaylam\u0131\u015f olmal\u0131",
            "FFmpeg ortam\u0131 do\u011frulanm\u0131\u015f olmal\u0131");

    public IcerikFabrikasiRenderPipelineConfigResult evaluate(IcerikFabrikasiProviderConfigResult providerConfig) {
        if (providerConfig == null) {
            return new IcerikFabrikasiRenderPipelineConfigResult(
                    IcerikFabrikasiRenderConfigStatus.BLOCKED_BY_SAFETY,
                    List.of(IcerikFabrikasiRenderConfigIssueCode.INTERNAL_ERROR),
                    null, DEFAULT_ENV, DEFAULT_FFMPEG, DEFAULT_OUTPUT,
                    SAFETY_PREREQUISITES, false,
                    "Sa\u011flay\u0131c\u0131 yap\u0131land\u0131rmas\u0131 eksik \u2014 render de\u011ferlendirmesi yap\u0131lam\u0131yor");
        }

        List<IcerikFabrikasiRenderConfigIssueCode> issues = new ArrayList<>();
        issues.add(IcerikFabrikasiRenderConfigIssueCode.RENDER_ENVIRONMENT_MISSING);
        issues.add(IcerikFabrikasiRenderConfigIssueCode.FFMPEG_NOT_AVAILABLE);
        issues.add(IcerikFabrikasiRenderConfigIssueCode.OPERATOR_REVIEW_PENDING);

        return new IcerikFabrikasiRenderPipelineConfigResult(
                IcerikFabrikasiRenderConfigStatus.NOT_CONFIGURED,
                issues,
                "rcfg:" + UUID.randomUUID().toString().substring(0, 8),
                DEFAULT_ENV, DEFAULT_FFMPEG, DEFAULT_OUTPUT,
                SAFETY_PREREQUISITES, false,
                String.format("Render y\u00fcr\u00fctme devre d\u0131\u015f\u0131 \u2014 %d \u00f6n ko\u015ful kar\u015f\u0131lanmad\u0131", issues.size()));
    }
}
