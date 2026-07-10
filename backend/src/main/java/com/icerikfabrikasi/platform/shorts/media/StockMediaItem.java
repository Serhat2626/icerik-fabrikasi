package com.icerikfabrikasi.platform.shorts.media;

import lombok.Builder;
import lombok.Value;

/**
 * Stock medya arama sonucu.
 */
@Value
@Builder
public class StockMediaItem {

    /** Benzersiz ID (provider tarafından atanan) */
    String externalId;

    /** Medya URL'i (orijinal çözünürlük) */
    String url;

    /** Küçük önizleme URL'i */
    String thumbnailUrl;

    /** Genişlik */
    int width;

    /** Yükseklik */
    int height;

    /** Kaynak — örn: "pexels", "pixabay", "ai-generated" */
    String source;

    /** Fotoğrafçı/oluşturucu adı (attribution) */
    String photographer;

    /** Medya tipi */
    MediaType mediaType;

    public enum MediaType {
        PHOTO, VIDEO
    }
}
