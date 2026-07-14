package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptScene;
import com.icerikfabrikasi.platform.shorts.media.MusicLibraryService;
import com.icerikfabrikasi.platform.shorts.media.MusicLibraryService.MusicTrack;
import com.icerikfabrikasi.platform.shorts.media.SceneBackgroundService;
import com.icerikfabrikasi.platform.shorts.media.VoiceoverService;
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
 * Her sahne için stock arka plan görseli arar, seslendirme üretir ve müzik seçer.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShortsRenderPlanBuilder {

    private final SceneBackgroundService backgroundService;
    private final MusicLibraryService musicLibraryService;
    private final VoiceoverService voiceoverService;

    /**
     * Script ve kanal bilgisinden render planı oluşturur.
     */
    public ShortsRenderPlan buildPlan(ShortsScript script, Channel channel) {
        log.info("Render planı oluşturuluyor: script={}, channel={}",
                script.getId(), channel.getName());

        // 1. Sahne metinlerini topla (seslendirme için)
        List<String> allTexts = new ArrayList<>();
        allTexts.add(script.getHook());
        for (ShortsScriptScene scene : script.getScenes()) {
            allTexts.add(scene.getText());
        }
        if (script.getCta() != null && !script.getCta().isBlank()) {
            allTexts.add(script.getCta());
        }

        // 2. Seslendirme üret (ElevenLabs TTS)
        List<Path> voicePaths = voiceoverService.generateVoiceovers(allTexts);

        // 3. Sahne render planları oluştur
        List<ShortsRenderPlan.SceneRenderPlan> sceneRenderPlans = new ArrayList<>();
        int voiceIdx = 0;

        // Hook sahnesi
        Path hookBg = backgroundService.resolveBackground(
                "dramatic dark background finance", script.getHook(), 0, true);
        Path hookVoice = voiceIdx < voicePaths.size() ? voicePaths.get(voiceIdx++) : null;
        int hookDuration = calculateDuration(hookVoice, 4);

        sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                .orderIndex(0)
                .text(script.getHook())
                .backgroundImagePath(hookBg != null ? hookBg.toString() : null)
                .voiceoverPath(hookVoice != null ? hookVoice.toString() : null)
                .durationSeconds(hookDuration)
                .isHook(true)
                .build());

        // İçerik sahneleri — soru sahnelerinden sonra countdown enjekte et
        String lastBgPath = hookBg != null ? hookBg.toString() : null;

        for (ShortsScriptScene scene : script.getScenes()) {
            Path bg = backgroundService.resolveBackground(
                    scene.getVisualHint(), scene.getText(), sceneRenderPlans.size(), false);
            Path voice = voiceIdx < voicePaths.size() ? voicePaths.get(voiceIdx++) : null;
            int duration = calculateDuration(voice, scene.getDurationSeconds());

            String bgPath = bg != null ? bg.toString() : null;
            sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                    .orderIndex(sceneRenderPlans.size())
                    .text(scene.getText())
                    .backgroundImagePath(bgPath)
                    .voiceoverPath(voice != null ? voice.toString() : null)
                    .durationSeconds(duration)
                    .isHook(false)
                    .build());

            lastBgPath = bgPath != null ? bgPath : lastBgPath;

            // Soru sahnesi tespit et — cevaptan önce countdown ekle
            if (isQuestionScene(scene.getText())) {
                log.info("Soru sahnesi tespit edildi, countdown ekleniyor: '{}'",
                        scene.getText().substring(0, Math.min(40, scene.getText().length())));

                sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                        .orderIndex(sceneRenderPlans.size())
                        .text("3... 2... 1...")
                        .backgroundImagePath(lastBgPath)
                        .durationSeconds(4)
                        .isHook(false)
                        .isCountdown(true)
                        .build());
            }
        }

        // CTA sahnesi
        if (script.getCta() != null && !script.getCta().isBlank()) {
            Path ctaVoice = voiceIdx < voicePaths.size() ? voicePaths.get(voiceIdx++) : null;
            int ctaDuration = calculateDuration(ctaVoice, 4);

            sceneRenderPlans.add(ShortsRenderPlan.SceneRenderPlan.builder()
                    .orderIndex(sceneRenderPlans.size())
                    .text(script.getCta())
                    .voiceoverPath(ctaVoice != null ? ctaVoice.toString() : null)
                    .durationSeconds(ctaDuration)
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

        long voiceCount = sceneRenderPlans.stream()
                .filter(s -> s.getVoiceoverPath() != null)
                .count();

        log.info("Render planı oluşturuldu: {} sahne, ~{} saniye, seslendirme={}/{}, müzik={}",
                sceneRenderPlans.size(), plan.totalDurationSeconds(),
                voiceCount, sceneRenderPlans.size(),
                music != null ? music.getName() : "yok");

        return plan;
    }

    /**
     * Seslendirme varsa MP3 süresine göre sahne süresini hesaplar.
     * Seslendirme yoksa varsayılan süreyi kullanır.
     */
    private int calculateDuration(Path voicePath, int defaultDuration) {
        if (voicePath == null) return defaultDuration;
        try {
            // MP3 dosya boyutundan tahmini süre (128kbps → 16KB/saniye)
            long fileSize = java.nio.file.Files.size(voicePath);
            int estimatedSeconds = (int) Math.ceil(fileSize / 16000.0);
            // Minimum 3 saniye, seslendirme süresine 1 saniye ekle (nefes + geçiş)
            return Math.max(3, estimatedSeconds + 1);
        } catch (Exception e) {
            return defaultDuration;
        }
    }

    /**
     * Sahne metninin bir soru olup olmadığını tespit eder.
     * Soru cümleleri: "?", "guess", "what is", "do you know" vb.
     */
    private boolean isQuestionScene(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase().trim();
        return lower.contains("?")
                || lower.contains("guess")
                || lower.contains("can you name")
                || lower.contains("do you know")
                || lower.contains("what is")
                || lower.contains("what do you think");
    }
}
