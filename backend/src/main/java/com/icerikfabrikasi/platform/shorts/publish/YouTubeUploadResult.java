package com.icerikfabrikasi.platform.shorts.publish;

import lombok.Builder;
import lombok.Value;

/**
 * YouTube video yükleme sonucu.
 */
@Value
@Builder
public class YouTubeUploadResult {

    /** Yükleme başarılı mı */
    boolean success;

    /** YouTube video ID — örn: "dQw4w9WgXcQ" */
    String videoId;

    /** YouTube video URL'i */
    String videoUrl;

    /** Hata mesajı (başarısız ise) */
    String errorMessage;

    /** Yükleme süresi (milisaniye) */
    long uploadDurationMs;

    public static YouTubeUploadResult success(String videoId, long durationMs) {
        return YouTubeUploadResult.builder()
                .success(true)
                .videoId(videoId)
                .videoUrl("https://youtube.com/shorts/" + videoId)
                .uploadDurationMs(durationMs)
                .build();
    }

    public static YouTubeUploadResult failure(String errorMessage) {
        return YouTubeUploadResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
