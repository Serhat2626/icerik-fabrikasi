package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stock medya gateway konfigürasyonu.
 *
 * <p>Pexels API key tanımlıysa PexelsStockMediaGateway kullanılır.
 * Yoksa StubStockMediaGateway fallback olarak devreye girer.</p>
 */
@Configuration
@Slf4j
public class StockMediaConfig {

    @Bean
    @ConditionalOnMissingBean(StockMediaGateway.class)
    public StockMediaGateway stubStockMediaGateway() {
        log.warn("StubStockMediaGateway aktif — stock fotoğraf bulunamayacak. " +
                "Pexels API key ekleyin: pexels.api-key=YOUR_KEY");
        return new StubStockMediaGateway();
    }
}
