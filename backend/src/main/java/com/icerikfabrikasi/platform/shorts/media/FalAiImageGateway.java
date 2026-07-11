package com.icerikfabrikasi.platform.shorts.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * fal.ai Flux Schnell API ile AI görsel üretimi.
 *
 * <p>Her sahne için özel prompt oluşturarak yüksek kaliteli,
 * dikey (portrait 9:16) görseller üretir.</p>
 *
 * <p>Maliyet: ~$0.003/görsel (Flux Schnell)</p>
 *
 * @see <a href="https://fal.ai/models/fal-ai/flux/schnell">Flux Schnell</a>
 */
@Component
@ConditionalOnProperty(name = "fal.api-key", matchIfMissing = false)
@Slf4j
public class FalAiImageGateway {

    private static final String FAL_RUN_URL = "https://fal.run";

    private final RestClient restClient;
    private final String model;
    private final String cacheDir;

    public FalAiImageGateway(
            @Value("${fal.api-key}") String apiKey,
            @Value("${fal.model:fal-ai/flux/schnell}") String model,
            @Value("${shorts.media.cache-dir:${java.io.tmpdir}/shorts-media-cache}") String cacheDir) {

        this.restClient = RestClient.builder()
                .baseUrl(FAL_RUN_URL)
                .defaultHeader("Authorization", "Key " + apiKey)
                .build();
        this.model = model;
        this.cacheDir = cacheDir;

        log.info("fal.ai AI görsel üretimi aktif: model={}", model);
    }

    /**
     * Prompt'tan AI görsel üretir ve dosyaya kaydeder.
     *
     * @param prompt Görsel açıklaması (İngilizce)
     * @param sceneIndex Sahne numarası (dosya adı için)
     * @return Üretilen görsel dosya yolu
     */
    public Path generateImage(String prompt, int sceneIndex) throws IOException {
        log.info("AI görsel üretimi: scene={}, prompt='{}...'", sceneIndex,
                prompt.substring(0, Math.min(60, prompt.length())));

        try {
            // fal.ai API çağrısı
            FalResponse response = restClient.post()
                    .uri("/{model}", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "prompt", prompt,
                            "image_size", Map.of(
                                    "width", 1080,
                                    "height", 1920
                            ),
                            "num_inference_steps", 4,
                            "num_images", 1,
                            "enable_safety_checker", false
                    ))
                    .retrieve()
                    .body(FalResponse.class);

            if (response == null || response.images == null || response.images.isEmpty()) {
                throw new IOException("fal.ai boş yanıt döndü");
            }

            String imageUrl = response.images.get(0).url;
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new IOException("fal.ai görsel URL'si boş");
            }

            // İndir ve cache'le
            Path aiDir = Path.of(cacheDir, "ai-generated");
            Files.createDirectories(aiDir);
            Path outputFile = aiDir.resolve(
                    String.format("scene_%03d_%s.jpg", sceneIndex,
                            UUID.randomUUID().toString().substring(0, 8)));

            downloadImage(imageUrl, outputFile);

            log.info("AI görsel üretildi: scene={}, size={}KB, path={}",
                    sceneIndex, Files.size(outputFile) / 1024, outputFile);

            return outputFile;

        } catch (Exception e) {
            log.error("AI görsel üretim hatası: scene={}, hata={}", sceneIndex, e.getMessage());
            throw new IOException("fal.ai görsel üretim hatası: " + e.getMessage(), e);
        }
    }

    private void downloadImage(String url, Path target) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestProperty("User-Agent", "IcerikFabrikasi/1.0");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }
    }

    // ── fal.ai Response modelleri ──

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FalResponse {
        List<FalImage> images;
        @JsonProperty("timings")
        Map<String, Double> timings;
        @JsonProperty("has_nsfw_concepts")
        List<Boolean> hasNsfwConcepts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FalImage {
        String url;
        int width;
        int height;
        @JsonProperty("content_type")
        String contentType;
    }
}
