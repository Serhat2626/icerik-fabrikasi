package com.icerikfabrikasi.platform.shorts.publish;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * YouTube upload gateway bean konfigürasyonu.
 *
 * <p>YouTube credentials varsa gerçek API, yoksa stub kullanılır.</p>
 */
@Configuration
@Slf4j
public class YouTubeUploadConfig {

    @Bean
    @ConditionalOnMissingBean(YouTubeUploadGateway.class)
    public YouTubeUploadGateway stubYouTubeUploadGateway() {
        log.warn("StubYouTubeUploadGateway aktif — gerçek yükleme yapılmayacak. " +
                "YouTube credentials ekleyin: youtube.access-token=YOUR_TOKEN");
        return new StubYouTubeUploadGateway();
    }
}
