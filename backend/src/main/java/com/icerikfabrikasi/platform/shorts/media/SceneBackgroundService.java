package com.icerikfabrikasi.platform.shorts.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Sahne görsel ipucuna göre stock fotoğraf indiren servis.
 *
 * <p>visualHint → Pexels arama → fotoğraf indir → lokal dosya yolu döndür</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SceneBackgroundService {

    private final StockMediaGateway stockMediaGateway;

    @Value("${shorts.media.cache-dir:${java.io.tmpdir}/shorts-media-cache}")
    private String cacheDir;

    /**
     * Sahne için arka plan görseli bulur ve indirir.
     *
     * @param visualHint Görsel ipucu — örn: "luxury lifestyle overview"
     * @return İndirilen dosyanın yolu veya null (bulunamazsa)
     */
    public Path resolveBackground(String visualHint) {
        if (visualHint == null || visualHint.isBlank()) {
            log.debug("Görsel ipucu boş — düz renk arka plan kullanılacak");
            return null;
        }

        try {
            // 1. Stock fotoğraf ara
            StockMediaItem item = stockMediaGateway.findPortraitPhoto(visualHint);
            if (item == null || item.getUrl() == null) {
                log.info("Stock fotoğraf bulunamadı: hint='{}'", visualHint);
                return null;
            }

            // 2. İndir
            Path downloadPath = downloadImage(item.getUrl(), item.getExternalId());
            log.info("Arka plan görseli indirildi: hint='{}', source={}, path={}",
                    visualHint, item.getSource(), downloadPath);
            return downloadPath;

        } catch (Exception e) {
            log.warn("Arka plan görseli indirme hatası: hint='{}', hata={}", visualHint, e.getMessage());
            return null;
        }
    }

    private Path downloadImage(String url, String externalId) throws IOException {
        Path dir = Path.of(cacheDir);
        Files.createDirectories(dir);

        String filename = externalId + "_" + UUID.randomUUID() + ".jpg";
        Path target = dir.resolve(filename);

        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target;
    }
}
