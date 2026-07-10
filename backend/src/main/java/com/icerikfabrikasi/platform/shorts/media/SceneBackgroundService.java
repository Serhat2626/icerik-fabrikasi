package com.icerikfabrikasi.platform.shorts.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

            // 2. Daha önce indirilmişse cache'ten döndür
            Path dir = Path.of(cacheDir);
            Files.createDirectories(dir);
            Path cached = dir.resolve(item.getExternalId() + ".jpg");
            if (Files.exists(cached) && Files.size(cached) > 1000) {
                log.info("Arka plan cache'ten yüklendi: hint='{}', path={}", visualHint, cached);
                return cached;
            }

            // 3. İndir (header ile — Pexels 403 sorunu fix)
            Path downloadPath = downloadImage(item.getUrl(), cached);
            log.info("Arka plan görseli indirildi: hint='{}', source={}, path={}",
                    visualHint, item.getSource(), downloadPath);
            return downloadPath;

        } catch (Exception e) {
            log.warn("Arka plan görseli indirme hatası: hint='{}', hata={}", visualHint, e.getMessage());
            return null;
        }
    }

    private Path downloadImage(String url, Path target) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestProperty("User-Agent", "IcerikFabrikasi/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }

        return target;
    }
}
