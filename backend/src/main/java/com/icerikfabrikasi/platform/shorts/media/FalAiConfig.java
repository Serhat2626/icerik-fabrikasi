package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * fal.ai AI görsel üretimi fallback konfigürasyonu.
 *
 * <p>API key yoksa null gateway döner — AI görsel atlanır, Pexels kullanılır.</p>
 */
@Configuration
@Slf4j
public class FalAiConfig {

    @Bean
    @ConditionalOnMissingBean(FalAiImageGateway.class)
    public FalAiImageGateway stubFalAiGateway() {
        log.warn("fal.ai devre dışı — API key yok. AI görsel için: fal.api-key=YOUR_KEY ekleyin");
        return null;
    }
}
