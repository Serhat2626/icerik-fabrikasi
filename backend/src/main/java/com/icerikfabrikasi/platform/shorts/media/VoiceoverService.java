package com.icerikfabrikasi.platform.shorts.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Shorts video seslendirme servisi.
 *
 * <p>Script sahnelerinin text'lerini ElevenLabs TTS ile seslendirir.
 * Her sahne için ayrı MP3 dosyası üretir.</p>
 *
 * <p>ElevenLabs API key yoksa sessiz çalışır (seslendirme atlanır).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceoverService {

    private final ElevenLabsTtsGateway ttsGateway;

    /**
     * Sahne metinlerini seslendirerek MP3 dosya listesi döndürür.
     *
     * @param sceneTexts Sahne metinleri (sıralı)
     * @return Sahne başına MP3 dosya yolları (null olanlar sessiz sahne)
     */
    public List<Path> generateVoiceovers(List<String> sceneTexts) {
        log.info("Seslendirme başlıyor: {} sahne", sceneTexts.size());

        List<Path> voicePaths = new ArrayList<>();

        for (int i = 0; i < sceneTexts.size(); i++) {
            String text = sceneTexts.get(i);
            if (text == null || text.isBlank()) {
                voicePaths.add(null);
                continue;
            }

            try {
                Path mp3Path = ttsGateway.synthesize(text, i);
                voicePaths.add(mp3Path);
            } catch (IOException e) {
                log.warn("Sahne {} seslendirme başarısız, atlanıyor: {}", i, e.getMessage());
                voicePaths.add(null);
            }
        }

        long successCount = voicePaths.stream().filter(p -> p != null).count();
        log.info("Seslendirme tamamlandı: {}/{} sahne başarılı", successCount, sceneTexts.size());

        return voicePaths;
    }
}
