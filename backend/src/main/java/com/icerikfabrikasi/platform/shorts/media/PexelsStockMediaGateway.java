package com.icerikfabrikasi.platform.shorts.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

/**
 * Pexels API stock fotoğraf sağlayıcısı.
 *
 * <p>Pexels ücretsiz API kullanarak anahtar kelimeye göre
 * yüksek kaliteli stock fotoğraflar arar.</p>
 *
 * <p>Kullanım: application.yml'de {@code pexels.api-key} tanımlayın.</p>
 *
 * @see <a href="https://www.pexels.com/api/documentation/">Pexels API Docs</a>
 */
@Component
@ConditionalOnProperty(name = "pexels.api-key")
@Slf4j
public class PexelsStockMediaGateway implements StockMediaGateway {

    private static final String BASE_URL = "https://api.pexels.com/v1";
    private final RestClient restClient;

    public PexelsStockMediaGateway(@Value("${pexels.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, apiKey)
                .build();
    }

    @Override
    public List<StockMediaItem> searchPhotos(String query, int count) {
        log.debug("Pexels fotoğraf araması: query='{}', count={}", query, count);
        try {
            PexelsSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("query", query)
                            .queryParam("per_page", Math.min(count, 15))
                            .queryParam("orientation", "portrait")
                            .build())
                    .retrieve()
                    .body(PexelsSearchResponse.class);

            if (response == null || response.photos == null) {
                return Collections.emptyList();
            }

            return response.photos.stream()
                    .map(this::toStockMediaItem)
                    .toList();

        } catch (Exception e) {
            log.warn("Pexels API hatası: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public StockMediaItem findPortraitPhoto(String query) {
        List<StockMediaItem> results = searchPhotos(query, 1);
        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public String getProviderName() {
        return "pexels";
    }

    private StockMediaItem toStockMediaItem(PexelsPhoto photo) {
        // Portrait için "portrait" boyutunu kullan (1080x1920'ye yakın)
        String url = photo.src != null ? photo.src.portrait : null;
        String thumbnail = photo.src != null ? photo.src.medium : null;

        return StockMediaItem.builder()
                .externalId(String.valueOf(photo.id))
                .url(url)
                .thumbnailUrl(thumbnail)
                .width(photo.width)
                .height(photo.height)
                .source("pexels")
                .photographer(photo.photographer)
                .mediaType(StockMediaItem.MediaType.PHOTO)
                .build();
    }

    // ── Pexels API response model'leri ──

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PexelsSearchResponse {
        List<PexelsPhoto> photos;
        @JsonProperty("total_results")
        int totalResults;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PexelsPhoto {
        int id;
        int width;
        int height;
        String photographer;
        PexelsPhotoSrc src;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PexelsPhotoSrc {
        String original;
        String large2x;
        String large;
        String medium;
        String portrait;    // 800x1200 — Shorts arka planı için ideal
        String landscape;
    }
}
