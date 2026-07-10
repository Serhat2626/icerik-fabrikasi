package com.icerikfabrikasi.platform.shorts.publish;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * YouTube Data API v3 video yükleme gateway'i.
 *
 * <p>OAuth2 access token ile YouTube'a video yükler.
 * Resumable upload protokolünü destekler.</p>
 *
 * <p>Gerekli yapılandırma:</p>
 * <pre>
 * youtube:
 *   api-key: YOUR_API_KEY
 *   access-token: YOUR_OAUTH2_ACCESS_TOKEN
 * </pre>
 *
 * @see <a href="https://developers.google.com/youtube/v3/docs/videos/insert">YouTube API - Videos.insert</a>
 */
@Component
@ConditionalOnProperty(name = "youtube.access-token")
@Slf4j
public class YouTubeDataApiGateway implements YouTubeUploadGateway {

    private static final String UPLOAD_URL =
            "https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=snippet,status";

    private final String accessToken;
    private final RestClient restClient;

    public YouTubeDataApiGateway(@Value("${youtube.access-token}") String accessToken) {
        this.accessToken = accessToken;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }

    @Override
    public YouTubeUploadResult upload(YouTubeUploadRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("YouTube yükleme başlıyor: title='{}'", request.getTitle());

        try {
            // 1. Dosya kontrolü
            Path videoPath = Path.of(request.getVideoFilePath());
            if (!Files.exists(videoPath)) {
                return YouTubeUploadResult.failure("Video dosyası bulunamadı: " + videoPath);
            }

            // 2. Resumable upload session başlat
            String uploadSessionUrl = initiateUpload(request);

            // 3. Video dosyasını yükle
            YouTubeVideoResponse response = uploadVideoFile(uploadSessionUrl, videoPath);

            long duration = System.currentTimeMillis() - startTime;
            log.info("YouTube yükleme başarılı: videoId={}, title='{}', süre={}ms",
                    response.id, request.getTitle(), duration);

            return YouTubeUploadResult.success(response.id, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("YouTube yükleme hatası: title='{}', süre={}ms, hata={}",
                    request.getTitle(), duration, e.getMessage(), e);
            return YouTubeUploadResult.failure("YouTube API hatası: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return accessToken != null && !accessToken.isBlank();
    }

    @Override
    public String getProviderName() {
        return "youtube-data-api-v3";
    }

    /**
     * Resumable upload session başlatır.
     */
    private String initiateUpload(YouTubeUploadRequest request) {
        String tagsJson = request.getTags() != null
                ? String.join("\",\"", request.getTags())
                : "";

        // #Shorts tag'i ekle
        String titleWithShorts = request.isShort() && !request.getTitle().contains("#Shorts")
                ? request.getTitle() + " #Shorts"
                : request.getTitle();

        String metadata = String.format("""
                {
                  "snippet": {
                    "title": "%s",
                    "description": "%s",
                    "tags": ["%s"],
                    "categoryId": "%s",
                    "defaultLanguage": "%s"
                  },
                  "status": {
                    "privacyStatus": "%s",
                    "selfDeclaredMadeForKids": false
                  }
                }
                """,
                escapeJson(titleWithShorts),
                escapeJson(request.getDescription() != null ? request.getDescription() : ""),
                tagsJson,
                request.getCategoryId(),
                request.getDefaultLanguage(),
                request.getPrivacyStatus()
        );

        // Initiate resumable upload — YouTube döndürdüğü Location header'da upload URL'i verir
        String locationUrl = restClient.post()
                .uri(UPLOAD_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Upload-Content-Type", "video/mp4")
                .body(metadata)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation()
                .toString();

        log.debug("Upload session başlatıldı: url={}", locationUrl);
        return locationUrl;
    }

    /**
     * Video dosyasını upload session URL'ine yükler.
     */
    private YouTubeVideoResponse uploadVideoFile(String uploadUrl, Path videoPath) {
        File videoFile = videoPath.toFile();

        return restClient.put()
                .uri(uploadUrl)
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(videoFile.length()))
                .body(new FileSystemResource(videoFile))
                .retrieve()
                .body(YouTubeVideoResponse.class);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class YouTubeVideoResponse {
        String id;
        @JsonProperty("snippet")
        YouTubeSnippet snippet;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class YouTubeSnippet {
        String title;
        String channelId;
    }
}
