package com.icerikfabrikasi.platform.shorts.publish;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.channel.ChannelService;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptService;
import com.icerikfabrikasi.platform.shorts.ShortsScriptStatus;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Video yayınlama orkestratörü — render edilmiş videoları YouTube'a yükler.
 *
 * <p>Generic servis: Tüm kanallar ve platformlar için çalışır.</p>
 *
 * <p>Akış: RENDERED script → YouTube upload → PUBLISHED</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoPublishService {

    private final ShortsScriptService scriptService;
    private final ChannelService channelService;
    private final YouTubeUploadGateway youTubeUploadGateway;

    /**
     * Tek bir render edilmiş video'yu YouTube'a yükler.
     */
    public PublishResult publishScript(UUID scriptId) {
        ShortsScript script = scriptService.getScript(scriptId);

        if (script.getStatus() != ShortsScriptStatus.RENDERED) {
            return PublishResult.failure(scriptId, script.getTitle(),
                    "Script yayınlanamaz — mevcut durum: " + script.getStatus() + ", beklenen: RENDERED");
        }

        if (script.getRenderOutputPath() == null) {
            return PublishResult.failure(scriptId, script.getTitle(),
                    "Render çıktı dosyası bulunamadı");
        }

        Channel channel = channelService.getChannel(script.getChannel().getId());

        log.info("Video yayınlama başlıyor: scriptId={}, title='{}', channel={}",
                scriptId, script.getTitle(), channel.getName());

        // Hashtag'leri tag listesine çevir
        List<String> tags = script.getHashtags() != null
                ? Arrays.asList(script.getHashtags().replace("#", "").split(","))
                : List.of();

        // YouTube upload isteği oluştur
        YouTubeUploadRequest uploadRequest = YouTubeUploadRequest.builder()
                .videoFilePath(script.getRenderOutputPath())
                .title(script.getTitle())
                .description(buildDescription(script, channel))
                .tags(tags)
                .categoryId("22") // People & Blogs
                .privacyStatus("public")
                .isShort(true)
                .defaultLanguage(channel.getContentLanguage())
                .build();

        // Yükle
        YouTubeUploadResult uploadResult = youTubeUploadGateway.upload(uploadRequest);

        if (uploadResult.isSuccess()) {
            scriptService.markPublished(scriptId);

            log.info("Video yayınlandı: scriptId={}, youtubeVideoId={}, url={}",
                    scriptId, uploadResult.getVideoId(), uploadResult.getVideoUrl());

            return PublishResult.success(scriptId, script.getTitle(),
                    uploadResult.getVideoId(), uploadResult.getVideoUrl());
        } else {
            log.error("Video yayınlama başarısız: scriptId={}, hata={}",
                    scriptId, uploadResult.getErrorMessage());

            return PublishResult.failure(scriptId, script.getTitle(), uploadResult.getErrorMessage());
        }
    }

    /**
     * Kanalın tüm render edilmiş videolarını toplu yayınlar.
     */
    public BatchPublishReport publishAllRendered(UUID channelId) {
        List<ShortsScript> renderedScripts = scriptService.getScriptsByStatus(
                channelId, ShortsScriptStatus.RENDERED);

        log.info("Toplu yayınlama başlıyor: channelId={}, scriptCount={}",
                channelId, renderedScripts.size());

        List<PublishResult> results = renderedScripts.stream()
                .map(script -> publishScript(script.getId()))
                .toList();

        long successCount = results.stream().filter(PublishResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        log.info("Toplu yayınlama tamamlandı: channelId={}, toplam={}, başarılı={}, başarısız={}",
                channelId, results.size(), successCount, failureCount);

        return BatchPublishReport.builder()
                .channelId(channelId)
                .totalScripts(results.size())
                .successCount((int) successCount)
                .failureCount((int) failureCount)
                .results(results)
                .build();
    }

    private String buildDescription(ShortsScript script, Channel channel) {
        StringBuilder desc = new StringBuilder();
        desc.append(script.getHook()).append("\n\n");

        if (script.getHashtags() != null) {
            desc.append(script.getHashtags().replace(",", " ")).append("\n\n");
        }

        if (channel.getWatermarkText() != null) {
            desc.append("📱 Follow: ").append(channel.getWatermarkText()).append("\n");
        }

        desc.append("\n⚠️ This video is for educational purposes only. Not financial advice.");
        return desc.toString();
    }

    /**
     * Tek video yayınlama sonucu.
     */
    @Value
    @Builder
    public static class PublishResult {
        UUID scriptId;
        String title;
        boolean success;
        String youtubeVideoId;
        String youtubeUrl;
        String errorMessage;

        public static PublishResult success(UUID scriptId, String title, String videoId, String url) {
            return PublishResult.builder()
                    .scriptId(scriptId).title(title).success(true)
                    .youtubeVideoId(videoId).youtubeUrl(url).build();
        }

        public static PublishResult failure(UUID scriptId, String title, String error) {
            return PublishResult.builder()
                    .scriptId(scriptId).title(title).success(false)
                    .errorMessage(error).build();
        }
    }

    /**
     * Toplu yayınlama raporu.
     */
    @Value
    @Builder
    public static class BatchPublishReport {
        UUID channelId;
        int totalScripts;
        int successCount;
        int failureCount;
        List<PublishResult> results;
    }
}
