package com.icerikfabrikasi.platform.shorts.render;

import lombok.Builder;
import lombok.Value;

/**
 * Shorts video render sonucu.
 */
@Value
@Builder
public class ShortsRenderResult {

    /** Render başarılı mı */
    boolean success;

    /** Çıkış video dosya yolu */
    String outputPath;

    /** Video süresi (saniye) */
    int durationSeconds;

    /** Dosya boyutu (byte) */
    long fileSizeBytes;

    /** Çözünürlük */
    String resolution;

    /** Hata mesajı (başarısız ise) */
    String errorMessage;

    public static ShortsRenderResult success(String outputPath, int duration, long fileSize, String resolution) {
        return ShortsRenderResult.builder()
                .success(true)
                .outputPath(outputPath)
                .durationSeconds(duration)
                .fileSizeBytes(fileSize)
                .resolution(resolution)
                .build();
    }

    public static ShortsRenderResult failure(String errorMessage) {
        return ShortsRenderResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
