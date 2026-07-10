package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * ElevenLabs TTS fallback konfigürasyonu.
 *
 * <p>API key yoksa stub servisler devreye girer — seslendirme atlanır.</p>
 */
@Configuration
@Slf4j
public class TtsConfig {

    @Bean
    @ConditionalOnMissingBean(ElevenLabsTtsGateway.class)
    public ElevenLabsTtsGateway stubTtsGateway() {
        log.warn("ElevenLabs TTS devre dışı — API key yok. " +
                "Seslendirme için: elevenlabs.api-key=YOUR_KEY ekleyin");
        // Stub: her zaman null döner (seslendirme yapılmaz)
        return new ElevenLabsTtsGateway("stub", "stub", "stub", System.getProperty("java.io.tmpdir")) {
            @Override
            public Path synthesize(String text, int sceneIndex) throws IOException {
                return null;
            }
            @Override
            public Path synthesize(String text, int sceneIndex, String voiceId, String model) throws IOException {
                return null;
            }
        };
    }
}
