package com.icerikfabrikasi.platform.shorts.media;

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

/**
 * Sahne görsel ipucuna göre arka plan görseli sağlayan servis.
 *
 * <p>Öncelik sırası:
 * <ol>
 *   <li>AI görsel üretimi (fal.ai Flux Pro) — en kaliteli</li>
 *   <li>Stock fotoğraf (Pexels) — fallback</li>
 *   <li>Düz renk arka plan — son çare</li>
 * </ol></p>
 */
@Service
@Slf4j
public class SceneBackgroundService {

    private final StockMediaGateway stockMediaGateway;
    private final FalAiImageGateway falAiGateway;
    private final AiScenePromptBuilder promptBuilder;

    public SceneBackgroundService(
            StockMediaGateway stockMediaGateway,
            @org.springframework.lang.Nullable FalAiImageGateway falAiGateway,
            AiScenePromptBuilder promptBuilder) {
        this.stockMediaGateway = stockMediaGateway;
        this.falAiGateway = falAiGateway;
        this.promptBuilder = promptBuilder;
    }

    @Value("${shorts.media.cache-dir:${java.io.tmpdir}/shorts-media-cache}")
    private String cacheDir;

    /**
     * Sahne için arka plan görseli üretir/bulur.
     *
     * @param visualHint Görsel ipucu
     * @param sceneText Sahne metni (AI prompt için)
     * @param sceneIndex Sahne index
     * @param isHook Hook sahnesi mi
     * @return Görsel dosya yolu veya null
     */
    public Path resolveBackground(String visualHint, String sceneText, int sceneIndex, boolean isHook) {
        // 1. AI görsel üretimi dene
        Path aiImage = tryAiGeneration(sceneText, visualHint, sceneIndex, isHook);
        if (aiImage != null) return aiImage;

        // 2. Pexels stock fotoğraf fallback
        Path stockImage = tryStockPhoto(visualHint);
        if (stockImage != null) return stockImage;

        log.info("Görsel bulunamadı — düz renk arka plan kullanılacak: scene={}", sceneIndex);
        return null;
    }

    /**
     * Kanal bazlı stil preset'i ile sahne arka planı çözer.
     */
    public Path resolveBackground(String visualHint, String sceneText, int sceneIndex, boolean isHook, String imageStylePreset) {
        Path aiImage = tryAiGeneration(sceneText, visualHint, sceneIndex, isHook, imageStylePreset);
        if (aiImage != null) return aiImage;

        Path stockImage = tryStockPhoto(visualHint);
        if (stockImage != null) return stockImage;

        log.info("Görsel bulunamadı — düz renk arka plan kullanılacak: scene={}", sceneIndex);
        return null;
    }

    /**
     * Eski API uyumluluğu — sadece visualHint ile çağrılınca Pexels dener.
     */
    public Path resolveBackground(String visualHint) {
        return tryStockPhoto(visualHint);
    }

    private Path tryAiGeneration(String sceneText, String visualHint, int sceneIndex, boolean isHook, String imageStylePreset) {
        if (falAiGateway == null) return null;
        try {
            String prompt = (imageStylePreset != null && !imageStylePreset.isBlank())
                    ? promptBuilder.buildPrompt(sceneText, visualHint, isHook, imageStylePreset)
                    : promptBuilder.buildPrompt(sceneText, visualHint, isHook);
            return falAiGateway.generateImage(prompt, sceneIndex);
        } catch (Exception e) {
            log.warn("AI görsel üretimi başarısız (preset), Pexels'e geçiliyor: scene={}, hata={}",
                    sceneIndex, e.getMessage());
            return null;
        }
    }

    private Path tryAiGeneration(String sceneText, String visualHint, int sceneIndex, boolean isHook) {
        if (falAiGateway == null) return null;

        try {
            String prompt = promptBuilder.buildPrompt(sceneText, visualHint, isHook);
            return falAiGateway.generateImage(prompt, sceneIndex);
        } catch (Exception e) {
            log.warn("AI görsel üretimi başarısız, Pexels'e geçiliyor: scene={}, hata={}",
                    sceneIndex, e.getMessage());
            return null;
        }
    }

    private Path tryStockPhoto(String visualHint) {
        if (visualHint == null || visualHint.isBlank()) {
            return null;
        }

        try {
            StockMediaItem item = stockMediaGateway.findPortraitPhoto(visualHint);
            if (item == null || item.getUrl() == null) {
                return null;
            }

            Path dir = Path.of(cacheDir);
            Files.createDirectories(dir);
            Path cached = dir.resolve(item.getExternalId() + ".jpg");
            if (Files.exists(cached) && Files.size(cached) > 1000) {
                return cached;
            }

            return downloadImage(item.getUrl(), cached);
        } catch (Exception e) {
            log.warn("Stock fotoğraf indirme hatası: hint='{}', hata={}", visualHint, e.getMessage());
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
