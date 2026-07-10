package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptScene;
import com.icerikfabrikasi.platform.shorts.media.MusicLibraryService;
import com.icerikfabrikasi.platform.shorts.media.MusicLibraryService.MusicTrack;
import com.icerikfabrikasi.platform.shorts.media.SceneBackgroundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ShortsScript → ShortsRenderPlan dönüştürücü.
 *
 * <p>Script ve kanal konfigürasyonundan render planı üretir.
 * Her sahne için stock arka plan görseli arar ve müzik seçer.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShortsRenderPlanBuilder {

    private final SceneBackgroundService backgroundService;
    private final MusicLibraryService musicLibraryService;

    /**
     * Script ve kanal bilgisinden render planı oluşturur.
     */
    public ShortsRenderPlan buildPlan(ShortsScript script, Channel channel) {
        log.info("Render planı oluşturuluyor: script={}, channel={}",
                script.getId(), channel.getName());

        List<ShortsRenderPlan.SceneRenderPlan> sceneRenderPlans = new ArrayList<>();

        // Hook sahnesi (ilk sahne: script.hook)
        Path hookBg = backgroundService.resolveBackground("dramatic dark background finance");
        sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                .orderIndex(0)
                .text(script.getHook())
                .backgroundImagePath(hookBg != null ? hookBg.toString() : null)
                .durationSeconds(4)
                .isHook(true)
                .build());

        // İçerik sahneleri
        for (ShortsScriptScene scene : script.getScenes()) {
            Path bg = backgroundService.resolveBackground(scene.getVisualHint());
            sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                    .orderIndex(scene.getOrderIndex() + 1)
                    .text(scene.getText())
                    .backgroundImagePath(bg != null ? bg.toString() : null)
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

        // Müzik seçimi
        MusicTrack music = musicLibraryService.selectTrackForNiche(channel.getNiche());
        String musicPath = music != null ? music.getFileName() : null;

        ShortsRenderPlan plan = ShortsRenderPlan.builder()
                .resolution("1080x1920")
                .fps(30)
                .outputFormat("mp4")
                .backgroundColor(channel.getBackgroundColor())
                .textColor(channel.getPrimaryTextColor())
                .accentColor(channel.getAccentColor())
                .fontFamily(channel.getFontFamily())
                .watermarkText(channel.getWatermarkText())
                .backgroundMusicPath(musicPath)
                .musicVolume(0.15)
                .scenes(sceneRenderPlans)
                .build();

        log.info("Render planı oluşturuldu: {} sahne, ~{} saniye, müzik={}",
                sceneRenderPlans.size(), plan.totalDurationSeconds(),
                music != null ? music.getName() : "yok");

        return plan;
    }
}
