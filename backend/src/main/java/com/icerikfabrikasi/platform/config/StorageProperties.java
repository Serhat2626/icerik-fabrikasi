package com.icerikfabrikasi.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Amazon S3 depolama konfigürasyonu.
 *
 * <p>Güvenlik: accessKey, secretKey, bucket gibi hassas bilgiler
 * toString(), API yanıtları veya log çıktılarında ASLA açığa çıkarılmaz.</p>
 */
@Configuration
@ConfigurationProperties(prefix = "app.storage.s3")
@Getter
@Setter
public class StorageProperties {

    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;

    /**
     * Güvenli toString — hassas bilgileri maskelenir.
     */
    @Override
    public String toString() {
        return "StorageProperties{" +
                "region='" + region + '\'' +
                ", bucket='[MASKED]'" +
                ", accessKey='[MASKED]'" +
                ", secretKey='[MASKED]'" +
                '}';
    }
}
