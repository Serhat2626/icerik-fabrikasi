package com.icerikfabrikasi.platform.shorts.media;

import com.icerikfabrikasi.platform.channel.ChannelNiche;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Telifsiz müzik kütüphanesi servisi.
 *
 * <p>Kanal nişine göre uygun arka plan müzik önerir.
 * Müzikler lokal dosya sistemi veya CDN'den yüklenir.</p>
 *
 * <p>Not: Gerçek müzik dosyaları proje dışında yönetilir.
 * Bu servis sadece metadata ve dosya yolu eşlemesi sağlar.</p>
 */
@Service
@Slf4j
public class MusicLibraryService {

    private static final Random RANDOM = new Random();

    /** Nişe göre müzik metadata listesi */
    private static final Map<ChannelNiche, List<MusicTrack>> MUSIC_CATALOG = Map.of(
            ChannelNiche.MONEY_MINDSET, List.of(
                    MusicTrack.builder()
                            .id("mm-01")
                            .name("Motivational Corporate")
                            .mood("motivational, confident")
                            .bpm(120)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("motivational_corporate.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("mm-02")
                            .name("Dark Ambient Cinematic")
                            .mood("dramatic, serious")
                            .bpm(90)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("dark_ambient_cinematic.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("mm-03")
                            .name("Epic Motivational")
                            .mood("epic, rising, powerful")
                            .bpm(130)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("epic_motivational.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("mm-04")
                            .name("Lo-Fi Chill Background")
                            .mood("calm, thoughtful")
                            .bpm(85)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("lofi_chill_background.mp3")
                            .build()
            ),
            ChannelNiche.QUIZ_TRIVIA, List.of(
                    MusicTrack.builder()
                            .id("qt-01")
                            .name("Game Show Countdown")
                            .mood("exciting, ticking, urgent")
                            .bpm(140)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("game_show_countdown.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("qt-02")
                            .name("Fun Upbeat Quiz")
                            .mood("fun, playful, energetic")
                            .bpm(135)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("fun_upbeat_quiz.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("qt-03")
                            .name("Suspenseful Thinking")
                            .mood("suspense, thinking time")
                            .bpm(100)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("suspenseful_thinking.mp3")
                            .build()
            ),
            ChannelNiche.AI_TECH, List.of(
                    MusicTrack.builder()
                            .id("at-01")
                            .name("Futuristic Tech")
                            .mood("futuristic, digital, electronic")
                            .bpm(110)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("futuristic_tech.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("at-02")
                            .name("Cyber Innovation")
                            .mood("innovative, sleek, modern")
                            .bpm(125)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("cyber_innovation.mp3")
                            .build(),
                    MusicTrack.builder()
                            .id("at-03")
                            .name("Ambient Digital")
                            .mood("ambient, space, atmospheric")
                            .bpm(95)
                            .durationSeconds(60)
                            .licenseFree(true)
                            .fileName("ambient_digital.mp3")
                            .build()
            )
    );

    /**
     * Kanal nişine göre rastgele müzik seçer.
     */
    public MusicTrack selectTrackForNiche(ChannelNiche niche) {
        List<MusicTrack> tracks = MUSIC_CATALOG.get(niche);
        if (tracks == null || tracks.isEmpty()) {
            log.warn("Niş için müzik bulunamadı: {}", niche);
            return null;
        }

        MusicTrack selected = tracks.get(RANDOM.nextInt(tracks.size()));
        log.info("Müzik seçildi: niche={}, track={} ({})", niche, selected.getName(), selected.getMood());
        return selected;
    }

    /**
     * Tüm müzik kataloğunu listeler.
     */
    public Map<ChannelNiche, List<MusicTrack>> getCatalog() {
        return MUSIC_CATALOG;
    }

    /**
     * Belirli nişin müzik listesini döndürür.
     */
    public List<MusicTrack> getTracksForNiche(ChannelNiche niche) {
        return MUSIC_CATALOG.getOrDefault(niche, List.of());
    }

    /**
     * Müzik parçası metadata'sı.
     */
    @Value
    @Builder
    public static class MusicTrack {
        String id;
        String name;
        String mood;
        int bpm;
        int durationSeconds;
        boolean licenseFree;
        String fileName;
    }
}
