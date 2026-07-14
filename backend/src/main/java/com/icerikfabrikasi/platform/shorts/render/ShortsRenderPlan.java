package com.icerikfabrikasi.platform.shorts.render;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Shorts video render planı.
 *
 * <p>Bir ShortsScript'ten üretilen, FFmpeg'e verilecek render talimatı.</p>
 */
@Value
@Builder
public class ShortsRenderPlan {

    /** Video çözünürlüğü — 9:16 dikey (1080x1920) */
    @Builder.Default
    String resolution = "1080x1920";

    /** FPS */
    @Builder.Default
    int fps = 30;

    /** Çıkış formatı */
    @Builder.Default
    String outputFormat = "mp4";

    /** Arka plan rengi (hex) */
    String backgroundColor;

    /** Metin rengi (hex) */
    String textColor;

    /** Vurgu rengi (hex) */
    String accentColor;

    /** Font dosya yolu veya font adı */
    String fontFamily;

    /** Hook font boyutu */
    @Builder.Default
    int hookFontSize = 72;

    /** Gövde font boyutu */
    @Builder.Default
    int bodyFontSize = 48;

    /** Watermark metni */
    String watermarkText;

    /** Arka plan müzik dosya yolu */
    String backgroundMusicPath;

    /** Müzik ses seviyesi (0.0 - 1.0) */
    @Builder.Default
    double musicVolume = 0.15;

    /** Sahneler — sıralı render talimatları */
    List<SceneRenderPlan> scenes;

    /** Toplam tahmini süre (saniye) */
    public int totalDurationSeconds() {
        return scenes.stream().mapToInt(SceneRenderPlan::getDurationSeconds).sum();
    }

    @Value
    @Builder
    public static class SceneRenderPlan {

        /** Sahne sıra numarası */
        int orderIndex;

        /** Ekranda gösterilecek metin */
        String text;

        /** Arka plan görsel dosya yolu (stock image veya AI generated) */
        String backgroundImagePath;

        /** Seslendirme MP3 dosya yolu (ElevenLabs TTS) */
        String voiceoverPath;

        /** Sahne süresi (saniye) */
        int durationSeconds;

        /** Bu sahne hook mu? (farklı stil uygulanır) */
        boolean isHook;

        /** Bu sahne countdown mu? (3... 2... 1... animasyonu) */
        @Builder.Default
        boolean isCountdown = false;
    }
}
