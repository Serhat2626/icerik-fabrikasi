package com.icerikfabrikasi.platform.shorts.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * ElevenLabs Text-to-Speech API gateway.
 *
 * <p>Script sahne metinlerini profesyonel seslendirmeye çevirir.
 * Her çağrı bir MP3 dosyası döndürür.</p>
 *
 * @see <a href="https://elevenlabs.io/docs/api-reference/text-to-speech">ElevenLabs API</a>
 */
@Component
@ConditionalOnProperty(name = "elevenlabs.api-key", matchIfMissing = false)
@Slf4j
public class ElevenLabsTtsGateway {

    private static final String BASE_URL = "https://api.elevenlabs.io/v1";

    private final RestClient restClient;
    private final String defaultVoiceId;
    private final String defaultModel;
    private final String cacheDir;

    public ElevenLabsTtsGateway(
            @Value("${elevenlabs.api-key}") String apiKey,
            @Value("${elevenlabs.voice-id:pNInz6obpgDQGcFmaJgB}") String voiceId,
            @Value("${elevenlabs.model:eleven_turbo_v2_5}") String model,
            @Value("${shorts.media.cache-dir:${java.io.tmpdir}/shorts-media-cache}") String cacheDir) {

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("xi-api-key", apiKey)
                .build();
        this.defaultVoiceId = voiceId;
        this.defaultModel = model;
        this.cacheDir = cacheDir;

        log.info("ElevenLabs TTS aktif: voice={}, model={}", voiceId, model);
    }

    /**
     * Metni seslendirip MP3 dosyası olarak kaydeder.
     *
     * @param text Seslendirilecek metin
     * @param sceneIndex Sahne numarası (dosya adı için)
     * @return MP3 dosya yolu
     */
    public Path synthesize(String text, int sceneIndex) throws IOException {
        return synthesize(text, sceneIndex, defaultVoiceId, defaultModel);
    }

    /**
     * Belirli ses ve model ile seslendirme yapar.
     */
    public Path synthesize(String text, int sceneIndex, String voiceId, String model) throws IOException {
        log.info("TTS istek: scene={}, text='{}...', voice={}", sceneIndex,
                text.substring(0, Math.min(40, text.length())), voiceId);

        try {
            // ElevenLabs API çağrısı — binary audio döner
            byte[] audioBytes = restClient.post()
                    .uri("/text-to-speech/{voiceId}", voiceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "text", text,
                            "model_id", model,
                            "voice_settings", Map.of(
                                    "stability", 0.5,
                                    "similarity_boost", 0.75,
                                    "style", 0.3
                            )
                    ))
                    .retrieve()
                    .body(byte[].class);

            if (audioBytes == null || audioBytes.length == 0) {
                throw new IOException("ElevenLabs boş yanıt döndü");
            }

            // MP3 dosyasına kaydet
            Path voiceDir = Path.of(cacheDir, "voiceover");
            Files.createDirectories(voiceDir);
            Path outputFile = voiceDir.resolve(
                    String.format("scene_%03d_%s.mp3", sceneIndex, UUID.randomUUID().toString().substring(0, 8)));
            Files.write(outputFile, audioBytes);

            log.info("TTS başarılı: scene={}, size={}KB, path={}",
                    sceneIndex, audioBytes.length / 1024, outputFile);

            return outputFile;

        } catch (Exception e) {
            log.error("TTS hatası: scene={}, hata={}", sceneIndex, e.getMessage());
            throw new IOException("ElevenLabs TTS hatası: " + e.getMessage(), e);
        }
    }

    /**
     * Kalan kredi miktarını sorgular.
     */
    public SubscriptionInfo getSubscriptionInfo() {
        try {
            return restClient.get()
                    .uri("/user/subscription")
                    .retrieve()
                    .body(SubscriptionInfo.class);
        } catch (Exception e) {
            log.warn("Subscription bilgisi alınamadı: {}", e.getMessage());
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionInfo {
        @JsonProperty("character_count")
        int characterCount;
        @JsonProperty("character_limit")
        int characterLimit;
        String tier;
    }
}
