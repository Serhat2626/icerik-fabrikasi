package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptScene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ShortsScript → ShortsRenderPlan dönüştürücü.
 *
 * <p>Script ve kanal konfigürasyonundan render planı üretir.
 * Bu plan FFmpeg'e verilecek talimatları içerir.</p>
 */
@Component
@Slf4j
public class ShortsRenderPlanBuilder {

    /**
     * Script ve kanal bilgisinden render planı oluşturur.
     */
    public ShortsRenderPlan buildPlan(ShortsScript script, Channel channel) {
        log.info("Render planı oluşturuluyor: script={}, channel={}",
                script.getId(), channel.getName());

        List<ShortsRenderPlan.SceneRenderPlan> sceneRenderPlans = new ArrayList<>();

        // Hook sahnesi (ilk sahne: script.hook)
        sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                .orderIndex(0)
                .text(script.getHook())
                .durationSeconds(4)
                .isHook(true)
                .build());

        // İçerik sahneleri
        for (ShortsScriptScene scene : script.getScenes()) {
            sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                    .orderIndex(scene.getOrderIndex() + 1)
                    .text(scene.getText())
                    .backgroundImagePath(null) // Stock görsel sonra eklenecek
                    .durationSeconds(scene.getDurationSeconds())
                    .isHook(false)
                    .build());
        }

        // CTA sahnesi (son sahne)
        if (script.getCta() != null && !script.getCta().isBlank()) {
            sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                    .orderIndex(sceneRenderPlans.size())
                    .text(script.getCta())
                    .durationSeconds(4)
                    .isHook(false)
                    .build());
        }

        ShortsRenderPlan plan = ShortsRenderPlan.builder()
                .resolution("1080x1920")
                .fps(30)
                .outputFormat("mp4")
                .backgroundColor(channel.getBackgroundColor())
                .textColor(channel.getPrimaryTextColor())
                .accentColor(channel.getAccentColor())
                .fontFamily(channel.getFontFamily())
                .watermarkText(channel.getWatermarkText())
                .musicVolume(0.15)
                .scenes(sceneRenderPlans)
                .build();

        log.info("Render planı oluşturuldu: {} sahne, ~{} saniye",
                sceneRenderPlans.size(), plan.totalDurationSeconds());

        return plan;
    }
}
