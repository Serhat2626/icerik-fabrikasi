package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.channel.ChannelService;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptService;
import com.icerikfabrikasi.platform.shorts.ShortsScriptStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Shorts video üretim orkestratörü.
 *
 * <p>Onaylanmış bir script'i alır, render planı oluşturur,
 * FFmpeg ile video render eder ve sonucu kaydeder.</p>
 *
 * <p>Akış: Script (APPROVED) → RenderPlan → FFmpeg → Result → Script (RENDERED)</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShortsRenderOrchestrator {

    private final ShortsScriptService scriptService;
    private final ChannelService channelService;
    private final ShortsRenderPlanBuilder planBuilder;
    private final ShortsVideoRenderService renderService;

    /**
     * Onaylanmış bir script'i video olarak render eder.
     *
     * <p>NOT: @Transactional yok — render uzun sürdüğü için (AI görseller + TTS + FFmpeg)
     * DB bağlantısı timeout olur. Sonuç kaydetme kısa transaction ile yapılır.</p>
     *
     * @param scriptId Render edilecek script ID
     * @return Render sonucu
     * @throws IllegalStateException Script APPROVED durumunda değilse
     */
    public ShortsRenderResult renderScript(UUID scriptId) {
        ShortsScript script = scriptService.getScript(scriptId);

        // Guard: sadece onaylanmış script'ler render edilebilir
        if (script.getStatus() != ShortsScriptStatus.APPROVED) {
            throw new IllegalStateException(
                    String.format("Script render edilemez — mevcut durum: %s, beklenen: APPROVED",
                            script.getStatus()));
        }

        Channel channel = channelService.getChannel(script.getChannel().getId());

        log.info("Script render başlıyor: scriptId={}, title={}, channel={}",
                scriptId, script.getTitle(), channel.getName());

        // 1. Render planı oluştur (AI görseller + TTS — uzun sürer)
        ShortsRenderPlan plan = planBuilder.buildPlan(script, channel);

        // 2. FFmpeg ile render et
        ShortsRenderResult result = renderService.render(plan);

        // 3. Sonucu kaydet (kısa işlem — taze connection alır)
        if (result.isSuccess()) {
            scriptService.markRendered(scriptId, result.getOutputPath());

            log.info("Script render başarılı: scriptId={}, output={}, size={}KB",
                    scriptId, result.getOutputPath(), result.getFileSizeBytes() / 1024);
        } else {
            log.error("Script render başarısız: scriptId={}, hata={}",
                    scriptId, result.getErrorMessage());
        }

        return result;
    }
}
