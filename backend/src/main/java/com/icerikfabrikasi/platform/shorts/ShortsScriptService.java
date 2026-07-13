package com.icerikfabrikasi.platform.shorts;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.channel.ChannelService;
import com.icerikfabrikasi.platform.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Shorts video script yönetim servisi.
 *
 * <p>Script oluşturma, review ve lifecycle yönetimini sağlar.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShortsScriptService {

    private final ShortsScriptRepository scriptRepository;
    private final ChannelService channelService;

    /**
     * Yeni script oluşturur ve sahneleri ekler.
     */
    public ShortsScript createScript(UUID channelId, String topic, String title,
                                     String hook, String cta, String hashtags,
                                     List<SceneInput> sceneInputs) {
        Channel channel = channelService.getChannel(channelId);

        ShortsScript script = ShortsScript.builder()
                .channel(channel)
                .topic(topic)
                .title(title)
                .hook(hook)
                .cta(cta)
                .hashtags(hashtags)
                .status(ShortsScriptStatus.DRAFT)
                .targetDurationSeconds(channel.getTargetDurationSeconds())
                .build();

        for (int i = 0; i < sceneInputs.size(); i++) {
            SceneInput input = sceneInputs.get(i);
            script.addScene(input.text(), input.visualHint(), input.durationSeconds());
        }

        ShortsScript saved = scriptRepository.save(script);
        log.info("Shorts script oluşturuldu: id={}, title={}, channel={}",
                saved.getId(), saved.getTitle(), channel.getName());
        return saved;
    }

    /**
     * Kanal'ın tüm script'lerini listeler.
     */
    @Transactional(readOnly = true)
    public List<ShortsScript> getScriptsByChannel(UUID channelId) {
        return scriptRepository.findByChannelIdOrderByCreatedAtDesc(channelId);
    }

    /**
     * Belirli durumdaki script'leri listeler.
     */
    @Transactional(readOnly = true)
    public List<ShortsScript> getScriptsByStatus(UUID channelId, ShortsScriptStatus status) {
        return scriptRepository.findByChannelIdAndStatus(channelId, status);
    }

    /**
     * Script'i onaylar.
     */
    public ShortsScript approveScript(UUID scriptId) {
        ShortsScript script = getScriptOrThrow(scriptId);
        script.approve();
        log.info("Script onaylandı: id={}, title={}", scriptId, script.getTitle());
        return scriptRepository.save(script);
    }

    /**
     * Script'i revize ister.
     */
    public ShortsScript requestRevision(UUID scriptId, String reviewNotes) {
        ShortsScript script = getScriptOrThrow(scriptId);
        script.setStatus(ShortsScriptStatus.REVISION_REQUESTED);
        script.setReviewNotes(reviewNotes);
        log.info("Script revizyon istendi: id={}", scriptId);
        return scriptRepository.save(script);
    }

    /**
     * Script'i yayınlandı olarak işaretler.
     */
    public ShortsScript markPublished(UUID scriptId) {
        ShortsScript script = getScriptOrThrow(scriptId);
        script.markPublished();
        script.getChannel().incrementVideosPublished();
        log.info("Script yayınlandı: id={}, title={}", scriptId, script.getTitle());
        return scriptRepository.save(script);
    }

    /**
     * ID ile script getirir.
     */
    @Transactional(readOnly = true)
    public ShortsScript getScript(UUID scriptId) {
        return getScriptOrThrow(scriptId);
    }

    private ShortsScript getScriptOrThrow(UUID scriptId) {
        return scriptRepository.findById(scriptId)
                .orElseThrow(() -> new NotFoundException("Script", scriptId));
    }

    /**
     * Render tamamlandıktan sonra script'i RENDERED durumuna geçirir.
     * Taze DB bağlantısı ile çalışır — uzun render süresinden bağımsız.
     */
    public void markRendered(UUID scriptId, String outputPath) {
        ShortsScript script = getScript(scriptId);
        script.setStatus(ShortsScriptStatus.RENDERED);
        script.setRenderOutputPath(outputPath);
        script.getChannel().incrementVideosProduced();
        scriptRepository.save(script);
        log.info("Script durumu RENDERED olarak güncellendi: scriptId={}", scriptId);
    }

    /**
     * Sahne input record'u.
     */
    public record SceneInput(String text, String visualHint, int durationSeconds) {}
}
